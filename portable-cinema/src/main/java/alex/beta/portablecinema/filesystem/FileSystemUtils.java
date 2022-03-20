package alex.beta.portablecinema.filesystem;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.video.Player;
import com.google.common.io.Files;
import lombok.NonNull;
import org.apache.commons.text.similarity.JaroWinklerSimilarity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.*;

/**
 * Utility class for file system operations
 * <p>
 * TODO Unit tests
 */
public final class FileSystemUtils {
    private FileSystemUtils() {
        // Do nothing
    }

    /**
     * Check if the given folder is an 'image' folder
     *
     * @param config
     * @param folder
     * @return true, if given folder only contains image files, that is allowed in the configuration
     */
    public static boolean isImageFolder(@NonNull final PortableCinemaConfig config, @NonNull File folder) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("[" + folder.getName() + "] is not folder");
        }
        File[] files = folder.listFiles();
        if (files != null)
            for (File file : files) {
                if (!doSkip(config, file) && !isImageFile(config, file) && !file.getName().equalsIgnoreCase(config.getDbFileName())) {
                    return false;
                }
            }
        return true;
    }

    /**
     * Check if the given file should be skipped in processing
     *
     * @param config
     * @param file
     * @return true, if the file name starts with configured text
     * @see PortableCinemaConfig#getSkipNameStartsWith()
     */
    public static boolean doSkip(@NonNull PortableCinemaConfig config, @NonNull File file) {
        String[] tokens = isBlank(config.getSkipNameStartsWith()) ? new String[]{} : split(config.getSkipNameStartsWith(), "\\,");
        for (String token : tokens) {
            if (isNotBlank(token) && file.getName().startsWith(token.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the given file is image, according to file extension and file size.
     * <p>
     * Always false, if the image file extension is not configured in config file
     * '*' means accept all
     *
     * @param config
     * @param file
     * @return
     * @see PortableCinemaConfig#getImageFileExtensions()
     * @see PortableCinemaConfig#getImageFileSizeThreshold()
     */
    public static boolean isImageFile(@NonNull final PortableCinemaConfig config, @NonNull File file) {
        if (isBlank(config.getImageFileExtensions()) || !file.isFile()) {
            return false;
        } else if (config.getImageFileExtensions().trim().equals("*")) {
            return true;
        }
        String[] imageExts = split(config.getImageFileExtensions(), "\\,");
        for (String imageExt : imageExts) {
            if (Files.getFileExtension(file.getPath()).equalsIgnoreCase(normalizeExt(imageExt))
                    && file.length() >= config.getImageFileSizeThreshold()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check the given file is video, according to file extension and file size.
     * <p>
     * Always false, if the video file extension is not configured in config file
     * '*' means accept all
     *
     * @param config
     * @param file
     * @return
     * @see PortableCinemaConfig#getVideoFileExtensions() ()
     * @see PortableCinemaConfig#getVideoFileSizeThreshold()
     */
    public static boolean isVideoFile(@NonNull final PortableCinemaConfig config, @NonNull File file) {
        if (isBlank(config.getVideoFileExtensions()) || !file.isFile()) {
            return false;
        } else if (config.getVideoFileExtensions().trim().equals("*")) {
            return true;
        }
        String[] videoExts = split(config.getVideoFileExtensions(), "\\,");
        for (String videoExt : videoExts) {
            if (Files.getFileExtension(file.getPath()).equalsIgnoreCase(normalizeExt(videoExt))
                    && file.length() >= config.getVideoFileSizeThreshold()) {
                return true;
            }
        }
        return false;
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
    public static List<String> getCoverOfVideo(final PortableCinemaConfig config, @NonNull File videoFile, @NonNull File currentFolder) {
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
        }

        //Rule 3
        File[] subFolders = currentFolder.listFiles(file -> file.isDirectory() && !doSkip(config, file));
        if (subFolders != null && subFolders.length == 1 && isImageFolder(config, subFolders[0])) {
            coverImageFiles = sortBySimilarity(subFolders[0].listFiles(file -> isImageFile(config, file)
                    && !doSkip(config, file) && !isScreenshot4Others(config, videoFile, currentFolder, file)), videoFile.getName());
            if (coverImageFiles.size() >= 2) {
                coversPath.add(subFolders[0].getName() + File.separator + coverImageFiles.get(0).getName());
                coversPath.add(subFolders[0].getName() + File.separator + coverImageFiles.get(1).getName());
                return coversPath;
            } else if (coverImageFiles.size() == 1) {
                coversPath.add(subFolders[0].getName() + File.separator + coverImageFiles.get(0).getName());
            }
        }
        return coversPath;
    }

    /**
     * Check if the given image file is a screenshot of other video, according to the naming convention defined in Player
     *
     * @param config
     * @param videoFile
     * @param currentFolder
     * @param imageToTest
     * @return
     * @see Player
     */
    public static boolean isScreenshot4Others(final PortableCinemaConfig config, @NonNull File videoFile, @NonNull File currentFolder, @NonNull File imageToTest) {
        if (!isImageFile(config, imageToTest) ||
                !PortableCinemaConfig.ScreenshotResolution.isSupported(Files.getFileExtension(imageToTest.getName()))) {
            return false;
        }
        String baseName = Files.getNameWithoutExtension(imageToTest.getName());
        if (!Player.SCREENSHOT_NAME_PATTERN.matcher(baseName).find()) {
            return false;
        } else {
            String vName = baseName.substring(0, baseName.length() - 7);
            File[] fVideos = currentFolder.listFiles(file -> isVideoFile(config, file)
                    && !file.getName().equalsIgnoreCase(videoFile.getName())
                    && Files.getNameWithoutExtension(file.getName()).equalsIgnoreCase(vName));
            return fVideos != null && fVideos.length > 0;
        }
    }

    /**
     * Get all images in the given folder, excludes the ones that match the condition
     *
     * @param config
     * @param folder
     * @param excludes file to exclude condition
     * @return list of file name
     */
    public static List<String> getAllImages(final PortableCinemaConfig config, @NonNull File folder, String... excludes) {
        if (!folder.isDirectory()) {
            throw new IllegalArgumentException("[" + folder.getName() + "] is not folder");
        }
        List<String> images = new ArrayList<>();
        File[] files = folder.listFiles(file -> isImageFile(config, file) && !doSkip(config, file));
        if (files != null) {
            for (File f : files) {
                String fName = f.getName();
                if (excludes == null || excludes.length == 0) {
                    images.add(fName);
                } else {
                    boolean flag = true;
                    for (String exclude : excludes) {
                        if (isNotBlank(exclude) && fName.equalsIgnoreCase(exclude)) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) images.add(fName);
                }
            }
        }
        return images;
    }

    /**
     * Test similarity of given files, using JaroWinklerSimilarity, and sorted by the result
     * Case sensitive
     *
     * @param files
     * @param fileName
     * @return
     */
    private static List<File> sortBySimilarity(File[] files, String fileName) {
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

    private static String normalizeExt(String inputExt) {
        if (isBlank(inputExt)) {
            return "";
        } else {
            String tmp = inputExt.trim();
            while (tmp.startsWith(".")) {
                tmp = tmp.substring(1);
            }
            return tmp;
        }
    }
}
