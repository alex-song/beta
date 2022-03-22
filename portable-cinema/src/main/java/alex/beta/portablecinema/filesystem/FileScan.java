package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.FolderVisitorFactory;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileDB;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.tag.TagService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static alex.beta.portablecinema.filesystem.FileSystemUtils.getCoverOfVideo;
import static alex.beta.portablecinema.filesystem.FileSystemUtils.isVideoFile;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class FileScan extends AbstractFolderVisitor {

    public FileScan() {
        this.actionType = FolderVisitorFactory.Action.SCAN;
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param file
     * @throws IOException
     */
    @Override
    public void doFile(final PortableCinemaConfig config, @NonNull FileDB db, File currentFolder, @NonNull File file) throws IOException {
        if (isVideoFile(config, file)) {
            FileInfo fileInfo = db.findByName(file.getName());
            if (fileInfo == null) {
                fileInfo = new FileInfo();
            } else if (fileInfo.isManualOverride()) {
                return;
            }
            fileInfo.setPath(currentFolder.getCanonicalPath());
            fileInfo.setName(file.getName());
            if (isBlank(fileInfo.getOtid()))
                fileInfo.setOtid(FileInfo.randomOtid());
            fileInfo.setSize(file.length());
            fileInfo.appendTags(TagService.getInstance(config).detectTags(file, currentFolder));
            List<String> covers = getCoverOfVideo(config, file, currentFolder);
            fileInfo.setCover1(covers.isEmpty() ? null : covers.get(0));
            fileInfo.setCover2(covers.size() < 2 ? null : covers.get(1));
            fileInfo.setLastModifiedOn(new Date(file.lastModified()));
            try (Player player = Player.getInstance(config, fileInfo).read()) {
                player.getDuration(true);
                player.getResolution(true);
                player.isDecodable();
            }
            db.addFileInfos(fileInfo);
        }
    }

    /**
     * Clean up non-exists file records from database, according to file path
     *
     * @param config
     * @param db
     * @param currentFolder
     * @param files
     */
    @Override
    public void beforeAllFiles(final PortableCinemaConfig config, @NonNull FileDB db, File currentFolder, File[] files) {
        if (files == null || files.length == 0) {
            db.getFileInfos().clear();
        } else if (!db.getFileInfos().isEmpty()) {
            List<FileInfo> toRemove = new ArrayList<>();
            for (FileInfo fileInfo : db.getFileInfos()) {
                boolean found = false;
                for (File file : files) {
                    if (fileInfo.getName().equalsIgnoreCase(file.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    toRemove.add(fileInfo);
                }
            }
            db.getFileInfos().removeAll(toRemove);
        }
    }

    /**
     * Update database file according to scan result
     *
     * @param config
     * @param db
     * @param currentFolder
     * @param files
     * @throws IOException
     */
    @Override
    @SuppressWarnings("squid:S106")
    public void afterAllFiles(@NonNull final PortableCinemaConfig config, FileDB db, @NonNull File currentFolder, File[] files) throws IOException {
        if (db != null && db.getFileInfos() != null && !db.getFileInfos().isEmpty()) {
            FileWriter writer = new FileWriter(new File(currentFolder, config.getDbFileName()));
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
            gson.toJson(db, writer);
            writer.flush();
        } else {
            try {
                File tmp = new File(currentFolder, config.getDbFileName());
                Files.delete(tmp.toPath());
                logger.info("Deleted redundant DB file [{}]", tmp.getCanonicalPath());
            } catch (NoSuchFileException ex) {
                //ignore
            } catch (Exception ex) {
                logger.warn("Failed to delete redundant DB file in [{}]", currentFolder.getCanonicalPath(), ex);
            }
        }
        if (this.getMessageCallback() != null) {
            this.getMessageCallback().output("已索引 " + currentFolder.getCanonicalPath());
        }
        logger.info("Finish scanning [{}], and updating database", currentFolder.getCanonicalPath());
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param subFolders
     */
    @Override
    public void beforeAllSubFolders(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] subFolders) {
        // Nothing to do
    }

    /**
     * @param config
     * @param db
     * @param currentFolder
     * @param subFolders
     */
    @Override
    public void afterAllSubFolders(final PortableCinemaConfig config, FileDB db, File currentFolder, File[] subFolders) {
        // Nothing to do
    }
}
