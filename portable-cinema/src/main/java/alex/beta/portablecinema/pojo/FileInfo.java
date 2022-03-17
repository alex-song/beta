package alex.beta.portablecinema.pojo;

import alex.beta.portablecinema.PortableCinemaConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.time.Duration;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@NoArgsConstructor
public class FileInfo {

    public static final String HOUR = "小时";
    public static final String MINUTE = "分钟";
    public static final String SECOND = "秒";

    /**
     * One time ID
     * Generated when aggregating file info into database
     */
    private String otid;
    /**
     * This is relevant path to the root, and it's calculated on the fly
     */
    private String path;

    @Expose
    private String name;

    @Expose
    private String cover1;

    @Expose
    private String cover2;

    @Expose
    private Date lastModifiedOn;

    @Expose
    private Resolution resolution;

    @Expose
    private Set<String> tags;

    @Expose
    private long size; // in bytes

    @Expose
    private long duration; // duration in seconds

    @Expose
    private boolean manualOverride; // keep all information as it is during scan

    @Expose
    private boolean decodeError; // in case Xuggle cannot decode the video file

    //@formatter:off
    public static String randomOtid() {
        return UUID.randomUUID().toString();
    }

    public static long toSeconds(long hours, long mins, long seconds) {
        return hours * 3600 + mins * 60 + seconds;
    }

    public static long parseDurationText(String formattedDuration) {
        if (formattedDuration == null || StringUtils.isBlank(formattedDuration.trim())) {
            return 0;
        } else {
            String text = formattedDuration.trim();
            if (!text.endsWith(HOUR) && !text.endsWith(MINUTE) && !text.endsWith(SECOND)) {
                text += SECOND;
            }
            text = text.replaceAll("\\s+", "")
                    .replaceFirst("(\\d+d)", "P$1T")
                    .replace("小时", "H")
                    .replace("分钟", "M")
                    .replace("秒", "S");
            text = text.charAt(0) != 'P' ? "PT" + text : text;
            return Duration.parse(text).getSeconds();
        }
    }

    private static String getString(long t) {
        return t > 0 ? String.valueOf(t) : "0";
    }

    public long getDurationHoursPart() {
        return duration / 3600;
    }

    public int getDurationMinsPart() {
        return (int) (duration % 3600) / 60;
    }

    public int getDurationSecondsPart() {
        return (int) duration % 60;
    }

    public String getFormattedDuration() {
        if (duration < 60) {
            return getDurationSecondsPart() + SECOND;
        } else if (duration < 3600) {
            return getString(getDurationMinsPart()) + MINUTE + getString((getDurationSecondsPart())) + SECOND;
        } else {
            return getString(getDurationHoursPart()) + HOUR + getString(getDurationMinsPart()) + MINUTE + getString((getDurationSecondsPart())) + SECOND;
        }
    }

    public String toPrettyString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
        return gson.toJson(this);
    }

    public boolean hasCover() {
        return isNotBlank(cover1) || isNotBlank(cover2);
    }
    //@formatter:on

    public Set<String> appendTags(Collection<String> newTags) {
        if (this.tags == null) {
            this.tags = new LinkedHashSet<>();
        }
        tags.addAll(newTags);
        return this.tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return Objects.equals(name, fileInfo.name) && size == fileInfo.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size);
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "otid='" + otid + '\'' +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", cover1='" + (cover1 == null ? "N/A" : cover1) + '\'' +
                ", cover2='" + (cover2 == null ? "N/A" : cover2) + '\'' +
                ", duration='" + getFormattedDuration() + '\'' +
                ", tags=[" + StringUtils.join(tags, ", ") + "]" +
                ", lastModifiedOn='" + (lastModifiedOn == null ? "N/A" : DateFormatUtils.format(lastModifiedOn, PortableCinemaConfig.DATE_FORMATTER)) + '\'' +
                ", resolution='" + (resolution == null ? "N/A" : resolution.toString()) + '\'' +
                ", size=" + size +
                ", manualOverride=" + manualOverride +
                ", decodeError=" + decodeError +
                '}';
    }

    @Setter
    @Getter
    @NoArgsConstructor
    public static class Resolution {

        @Expose
        private int width;

        @Expose
        private int height;

        public Resolution(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public boolean isHD() {
            return width >= 1080 && height >= 720;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Resolution that = (Resolution) o;
            return width == that.width &&
                    height == that.height;
        }

        @Override
        public int hashCode() {
            return Objects.hash(width, height);
        }

        @Override
        public String toString() {
            return String.format("%s * %s", width, height);
        }
    }
}
