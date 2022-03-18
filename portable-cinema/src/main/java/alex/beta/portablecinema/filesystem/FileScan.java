package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.FolderVisitorFactory;
import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.PortableCinemaConfig.ScreenshotResolution;
import alex.beta.portablecinema.pojo.FileDB;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.portablecinema.tag.TagService;
import alex.beta.portablecinema.video.Player;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    /**
     * 规则：
     * 1. 同目录中，名字叫cover*的图片文件是封面。back*的图片文件是封底。
     * 2. 同目录中，除了当前影片外，匹配度最高的两张图片就是封面。
     * 3. 只有一个子目录，且子目录中全部是图片，那么子目录中匹配度最高的两张图片是封面。
     *
     * @param config
     * @param videoFile
     * @param currentFolder
     * @return
     */
    private List<String> getCoverOfVideo(final PortableCinemaConfig config, @NonNull File videoFile, @NonNull File currentFolder) {
        List<String> coversPath = new ArrayList<>();
        //Rule 1
        File[] covers = currentFolder.listFiles(file -> isImageFile(config, file) && !doSkip(config, file) &&
                !isScreenshot4Others(config, videoFile, currentFolder, file) &&
                (file.getName().toLowerCase().startsWith("cover") || file.getName().toLowerCase().endsWith("cover") ||
                        file.getName().toLowerCase().startsWith("back") || file.getName().toLowerCase().endsWith("back") ||
                        file.getName().toLowerCase().contains("封面") ||
                        file.getName().toLowerCase().contains("封底") ||
                        file.getName().toLowerCase().contains("连拍") ||
                        file.getName().toLowerCase().contains("thumbnail")));
        if (covers != null) {
            if (covers.length >= 2) {
                coversPath.add(covers[0].getName());
                coversPath.add(covers[1].getName());
                return coversPath;
            } else if (covers.length == 1) {
                coversPath.add(covers[0].getName());
//                return coversPath;
            }
        }

        //Rule 2
        List<File> coverImageFiles = sortBySimilarity(currentFolder.listFiles(file -> isImageFile(config, file)
                && !doSkip(config, file) && !isScreenshot4Others(config, videoFile, currentFolder, file)), videoFile.getName());
        if (coverImageFiles.size() >= 2) {
            coversPath.add(coverImageFiles.get(0).getName());
            coversPath.add(coverImageFiles.get(1).getName());
            return coversPath;
        } else if (coverImageFiles.size() == 1) {
            coversPath.add(coverImageFiles.get(0).getName());
//            return coversPath;
        }

        //Rule 3
        File[] subFolders = currentFolder.listFiles(file -> file.isDirectory() && !doSkip(config, file));
        if (subFolders != null && subFolders.length == 1 && containsOnlyImages(config, subFolders[0])) {
            coverImageFiles = sortBySimilarity(subFolders[0].listFiles(file -> isImageFile(config, file)
                    && !doSkip(config, file) && !isScreenshot4Others(config, videoFile, currentFolder, file)), videoFile.getName());
            if (coverImageFiles.size() >= 2) {
                coversPath.add(subFolders[0].getName() + File.separator + coverImageFiles.get(0).getName());
                coversPath.add(subFolders[0].getName() + File.separator + coverImageFiles.get(1).getName());
                return coversPath;
            } else if (coverImageFiles.size() == 1) {
                coversPath.add(subFolders[0].getName() + File.separator + coverImageFiles.get(0).getName());
//                return coversPath;
            }
        }
        return coversPath;
    }

    private boolean isScreenshot4Others(final PortableCinemaConfig config, @NonNull File videoFile, @NonNull File currentFolder, @NonNull File fileToTest) {
        if (!isImageFile(config, fileToTest) ||
                !ScreenshotResolution.isSupported(com.google.common.io.Files.getFileExtension(fileToTest.getName()))) {
            return false;
        }
        String baseName = com.google.common.io.Files.getNameWithoutExtension(fileToTest.getName());
        if (!Player.SCREENSHOT_NAME_PATTERN.matcher(baseName).find()) {
            return false;
        } else {
            String vName = baseName.substring(0, baseName.length() - 7);
            File[] fVideos = currentFolder.listFiles(file -> isVideoFile(config, file)
                    && !file.getName().equalsIgnoreCase(videoFile.getName())
                    && com.google.common.io.Files.getNameWithoutExtension(file.getName()).equalsIgnoreCase(vName));
            return fVideos != null && fVideos.length > 0;
        }
    }

    /**
     * Test similarity of given files, using JaroWinklerSimilarity, and sorted by the result
     * Case sensitive
     *
     * @param files
     * @param fileName
     * @return
     */
    private List<File> sortBySimilarity(File[] files, String fileName) {
        JaroWinklerSimilarity jwSimilarity = new JaroWinklerSimilarity();
        List<File> tmp = new ArrayList<>();
        if (files != null)
            Collections.addAll(tmp, files);
        tmp.sort((o1, o2) -> {
            double similarity = jwSimilarity.apply(o2.getName(), fileName) - jwSimilarity.apply(o1.getName(), fileName);
            if (similarity > 0) {
                return 1;
            } else if (similarity < 0) {
                return -1;
            } else {
                return 0;
            }
        });
        return tmp;
    }

    /**
     * @param config
     * @param folder
     * @return
     */
    private boolean containsOnlyImages(final PortableCinemaConfig config, @NonNull File folder) {
        File[] files = folder.listFiles();
        if (files != null)
            for (File file : files) {
                if (!doSkip(config, file) && !isImageFile(config, file) && !file.getName().equalsIgnoreCase(config.getDbFileName())) {
                    return false;
                }
            }
        return true;
    }
}
