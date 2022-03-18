package alex.beta.portablecinema;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class PortableCinemaConfig {
    public static final String CONFIGURATION_PROPERTY_NAME = "portable-cinema.conf";
    public static final String DEFAULT_CONFIGURATION_FILE_NAME = "portable_cinema_conf.json";
    public static final String DEFAULT_ROOT_FOLDER_NAME = ".";
    public static final String DEFAULT_IN_FOLDER_DB_FILE_NAME = "portable_cinema_db.json";
    public static final String DATE_FORMATTER = "yyyy-MM-dd hh:mm:ss";

    public static final String VERSION = "1";

    private String rootFolderPath;
    private String dbFileName;
    private long imageFileSizeThreshold;
    private long videoFileSizeThreshold;
    private String imageFileExtensions;
    private String videoFileExtensions;
    private String skipNameStartsWith;
    private String glossaryFileName;
    private BaiduOCR baiduOCR;
    private boolean enablePerformanceLog;
    private boolean enableQueryResultHTML;
    private String theme = "default";
    private ScreenshotResolution screenshotResolution = ScreenshotResolution.LOW;

    public static PortableCinemaConfig getDefault() {
        PortableCinemaConfig config = new PortableCinemaConfig();
        config.rootFolderPath = DEFAULT_ROOT_FOLDER_NAME;
        config.dbFileName = DEFAULT_IN_FOLDER_DB_FILE_NAME;
        config.imageFileSizeThreshold = 20 * 1024L; //20KB
        config.videoFileSizeThreshold = 100 * 1024 * 1024L; //100MB
        config.imageFileExtensions = ".jpg, .jpeg, .png, .bmp, .gif";
        config.videoFileExtensions = ".avi, .mpg, .mpeg, .mpe, .mp4, .mkv, .mts, .mov, .m4v, .qt, .rmvb, .rm, .vob, .wmv";
        config.skipNameStartsWith = "., _";
        config.glossaryFileName = "Glossary.xml";
        config.enablePerformanceLog = false;
        config.enableQueryResultHTML = false;
        config.theme = "default";
        config.screenshotResolution = ScreenshotResolution.LOW;
        return config;
    }

    public PortableCinemaConfig rootFolderPath(String rootFolderPath) {
        this.setRootFolderPath(rootFolderPath);
        return this;
    }

    @Override
    public String toString() {
        return "PortableCinemaConfig {" +
                "rootFolderPath='" + rootFolderPath + '\'' +
                ", dbFileName='" + dbFileName + '\'' +
                ", imageFileSizeThreshold=" + imageFileSizeThreshold +
                ", videoFileSizeThreshold=" + videoFileSizeThreshold +
                ", imageFileExtensions='" + imageFileExtensions + '\'' +
                ", videoFileExtensions='" + videoFileExtensions + '\'' +
                ", skipNameStartsWith='" + skipNameStartsWith + '\'' +
                ", glossaryFileName='" + glossaryFileName + '\'' +
                ", theme='" + theme + '\'' +
                ", screenshotResolution='" + screenshotResolution + '\'' +
                ", enablePerformanceLog=" + enablePerformanceLog +
                ", enableQueryResultHTML=" + enableQueryResultHTML +
                '}';
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class BaiduOCR {
        private String proxyHost;
        private int proxyPort;
        private String appId;
        private String apiKey;
        private String secretKey;
    }

    public enum ScreenshotResolution {
        HIGH ("png", "png"),
        LOW ("jpg", "jpg");

        private final String formatName;
        private final String surrfix;
        ScreenshotResolution(String formatName, String surrfix) {
            this.formatName = formatName;
            this.surrfix = surrfix;
        }

        public String getFormatName() {
            return this.formatName;
        }

        public String getSuffix() {
            return this.surrfix;
        }
    }
}
