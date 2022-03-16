package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.database.DatabaseException;
import alex.beta.portablecinema.pojo.FileDB;
import alex.beta.portablecinema.pojo.FolderInfo;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static alex.beta.portablecinema.FolderVisitorFactory.Action;
import static alex.beta.portablecinema.FolderVisitorFactory.FolderVisitor;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.split;

public abstract class AbstractFolderVisitor implements FolderVisitor {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractFolderVisitor.class);

    //time out setting (in mins)
    private static final int TIMEOUT_MINS = 15;

    protected Action actionType;

    private VisitorMessageCallback messageCallback;

    @Override
    public VisitorMessageCallback getMessageCallback() {
        return messageCallback;
    }

    @Override
    public AbstractFolderVisitor messageCallback(VisitorMessageCallback messageCallback) {
        this.messageCallback = messageCallback;
        return this;
    }

    /**
     * @param config
     * @param currentFolder
     * @return
     */
    @Override
    public FolderInfo execute(@NonNull final PortableCinemaConfig config, @NonNull File currentFolder) {
        return execute(config, currentFolder, 0);
    }

    /**
     * @param config
     * @param currentFolder
     * @param level
     * @return
     */
    @SuppressWarnings("squid:S3776")
    public FolderInfo execute(@NonNull final PortableCinemaConfig config, @NonNull File currentFolder, int level) {
        final long startPoint = Calendar.getInstance().getTime().getTime();
        final long lastModifiedTime = currentFolder.lastModified();

        String currentFolderPath = null;
        FileDB db = null;
        FolderInfo folderInfo = new FolderInfo();

        try {
            currentFolderPath = currentFolder.getCanonicalPath();
            logger.debug("Visit folder [{}]", currentFolderPath);
            folderInfo.setPath(currentFolderPath);
            folderInfo.setLevel(level);
            File dbFile = new File(currentFolder, config.getDbFileName());
            db = dbFile.exists() ? new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create().fromJson(new FileReader(dbFile), FileDB.class) : new FileDB();
        } catch (IOException e) {
            logger.error("Error in accessing [{}] or reading the database file [{}] in it", currentFolderPath, config.getDbFileName(), e);
            return null;
        }

        //Step 1 - validate/update file info in database
        File[] files = currentFolder.listFiles(file -> file.isFile() && !file.isHidden()
                && !file.getName().equalsIgnoreCase(config.getDbFileName())
                && !file.getName().equalsIgnoreCase(config.getGlossaryFileName())
                && !doSkip(config, file));
        try {
            beforeAllFiles(config, db, currentFolder, files);
        } catch (IOException | DatabaseException e) {
            logger.error("Error before processing files in folder [{}]", currentFolderPath, e);
            return null;
        }

        //Step 2 - handle each file
        if (files != null && files.length > 0) {
            for (File file : files) {
                String fName = null;
                try {
                    fName = file.getName();
                    doFile(config, db, currentFolder, file);
                    if (isVideoFile(config, file)) {
                        folderInfo.setNumOfVideos(folderInfo.getNumOfVideos() + 1);
                    }
                } catch (IOException | DatabaseException e) {
                    logger.warn("Error in processing file [{}]", fName, e);
                } catch (Exception e) {
                    logger.error("Unexpected exception when processing file [{}]", fName, e);
                }
            }
        }

        //Step 3 - Persist file info database
        try {
            afterAllFiles(config, db, currentFolder, files);
        } catch (IOException | DatabaseException e) {
            logger.error("Error after processing files in folder [{}]", currentFolderPath, e);
            return null;
        }

        //Step 4 - Go through sub folders
        File[] folders = currentFolder.listFiles(file -> file.isDirectory() && !file.isHidden() && !doSkip(config, file));
        try {
            beforeAllSubFolders(config, db, currentFolder, folders);
        } catch (IOException | DatabaseException e) {
            logger.error("Error before processing sub-folders in folder [{}]", currentFolderPath, e);
            return folderInfo;
        }

        //Step5 - handle each folder recursively
        if (folders != null && folders.length > 0) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(Math.max(Runtime.getRuntime().availableProcessors() - 1, 4));
            List<FolderInfo> fis = Collections.synchronizedList(new ArrayList<>());
            CountDownLatch doneSignal = new CountDownLatch(folders.length);
            for (File folder : folders) {
                executor.execute(() -> {
                    String folderPath = null;
                    try {
                        folderPath = folder.getCanonicalPath();
                        AbstractFolderVisitor subVisitor = newFolderVisitor(actionType);
                        if (subVisitor != null) {
                            fis.add(subVisitor.messageCallback(this.getMessageCallback()).execute(config, folder, level + 1));
                        } else {
                            logger.warn("Unexpected action [{}]", actionType);
                        }
                    } catch (IOException ex) {
                        logger.error("Failed to visit folder [{}] with action [{}]", folderPath, actionType, ex);
                    } finally {
                        doneSignal.countDown();
                    }
                });
            }
            try {
                if (!doneSignal.await(TIMEOUT_MINS, TimeUnit.MINUTES)) {
                    logger.warn("Timeout after {} mins, failed to process sub folders of [{}] ", TIMEOUT_MINS, currentFolderPath);
                }
            } catch (InterruptedException ex) {
                logger.error("Process sub folders of [{}] is interrupted", currentFolderPath, ex);
                Thread.currentThread().interrupt();
            } finally {
                executor.shutdown();
            }
            folderInfo.setSubFolders(fis);
        }

        //Step 6 - Post handler
        try {
            afterAllSubFolders(config, db, currentFolder, folders);
        } catch (IOException | DatabaseException e) {
            logger.error("Error after processing sub folders in folder [{}]", currentFolderPath, e);
            return folderInfo;
        }

        //reset folder's last modified timestamp, if any update is made by folder visitor
        if (currentFolder.lastModified() >= startPoint - 999 && !currentFolderPath.equalsIgnoreCase(config.getRootFolderPath()) && currentFolder.setLastModified(lastModifiedTime)) {
            logger.debug("Reset [{}] last modified time from [{}] to [{}]", currentFolderPath, new Date(currentFolder.lastModified()), new Date(lastModifiedTime));
        }

        return folderInfo;
    }

    protected boolean doSkip(@NonNull PortableCinemaConfig config, @NonNull File file) {
        String[] tokens = isBlank(config.getSkipNameStartsWith()) ? new String[]{} : split(config.getSkipNameStartsWith(), "\\,");
        for (String token : tokens) {
            if (file.getName().toLowerCase().startsWith(token.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check file extension and file size, according to configuration
     *
     * @param config
     * @param file
     * @return
     */
    protected boolean isVideoFile(@NonNull final PortableCinemaConfig config, @NonNull File file) {
        if (isBlank(config.getVideoFileExtensions()) || !file.isFile()) {
            return false;
        } else if (config.getVideoFileExtensions().trim().equals("*")) {
            return true;
        }
        String[] videoExts = split(config.getVideoFileExtensions(), "\\,");
        for (String videoExt : videoExts) {
            if (file.getName().toLowerCase().endsWith(videoExt.toLowerCase().trim())
                    && file.length() >= config.getVideoFileSizeThreshold()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check file extension and file size, according to configuration
     *
     * @param config
     * @param file
     * @return
     */
    protected boolean isImageFile(@NonNull final PortableCinemaConfig config, @NonNull File file) {
        if (isBlank(config.getImageFileExtensions()) || !file.isFile()) {
            return false;
        } else if (config.getImageFileExtensions().trim().equals("*")) {
            return true;
        }
        String[] imageExts = split(config.getImageFileExtensions(), "\\,");
        for (String imageExt : imageExts) {
            if (file.getName().toLowerCase().endsWith(imageExt.toLowerCase().trim())
                    && file.length() >= config.getImageFileSizeThreshold()) {
                return true;
            }
        }
        return false;
    }

    private static AbstractFolderVisitor newFolderVisitor(@NonNull Action action) {
        switch (action) {
            case SCAN:
                return new FileScan();
            case AGGREGATE:
                return new FileAggregate();
            case RESET_FOLDER:
                return new FolderReset();
            case RESET_ALL:
                return new DeleteDBInFolder();
            default:
                return null;
        }
    }

    protected abstract void beforeAllFiles(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] files) throws IOException, DatabaseException;

    protected abstract void doFile(final PortableCinemaConfig config, FileDB db, File currentFolder, File file) throws IOException, DatabaseException;

    protected abstract void afterAllFiles(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] files) throws IOException, DatabaseException;

    protected abstract void beforeAllSubFolders(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] subFolders) throws IOException, DatabaseException;

    protected abstract void afterAllSubFolders(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] subFolders) throws IOException, DatabaseException;
}
