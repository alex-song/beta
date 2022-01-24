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

import java.util.*;

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

    public FileInfo(String otid, String path, String name, String cover1, String cover2, Date lastModifiedOn, int width, int height, long size, long duration, Set<String> tags) {
        this.otid = otid;
        this.path = path;
        this.name = name;
        this.cover1 = cover1;
        this.cover2 = cover2;
        this.lastModifiedOn = lastModifiedOn;
        this.resolution = new Resolution(width, height);
        this.tags = tags;
        this.size = size;
        this.duration = duration;
    }

    //@formatter:off
    public static String randomOtid() {
        return UUID.randomUUID().toString();
    }

    public static long toSeconds(long hours, long mins, long seconds) {
        return hours * 3600 + mins * 60 + seconds;
    }

    public static long parseDurationText(String formattedDuration) {
        int hourIndex = formattedDuration.indexOf(HOUR);
        int minIndex = formattedDuration.indexOf(MINUTE);
        int secondIndex = formattedDuration.indexOf(SECOND);

        String hourValueStr = (hourIndex <= 0 ? "0" : formattedDuration.substring(0, hourIndex));
        String minValueStr = (hourIndex <= 0
                ? (minIndex <= 0 ? "0" : formattedDuration.substring(0, minIndex))
                : (minIndex <= 0 ? "0" : formattedDuration.substring(hourIndex + HOUR.length(), minIndex)));
        String secondValueStr = (minIndex <= 0
                ? (hourIndex <= 0
                    ? (secondIndex <= 0 ? formattedDuration : formattedDuration.substring(0, secondIndex))
                    : (secondIndex <= 0 ? "0" : (formattedDuration.substring(hourIndex + HOUR.length(), secondIndex))))
                : (secondIndex <= 0
                    ? "0"
                    : (formattedDuration.substring(minIndex + MINUTE.length(), secondIndex))));

        return toSeconds(Long.parseLong(hourValueStr), Long.parseLong(minValueStr), Long.parseLong(secondValueStr));
    }

    private String getString(long t) {
        return t > 0 ? String.valueOf(t) : "0";
    }

    public long getDurationHours() {
        return duration / 3600;
    }

    public long getDurationMins() {
        return (duration % 3600) / 60;
    }

    public long getDurationSeconds() {
        return duration % 60;
    }

    public String getFormattedDuration() {
        if (duration < 60) {
            return getDurationSeconds() + SECOND;
        } else if ((duration >= 60) && (duration < 3600)) {
            return getString(getDurationMins()) + MINUTE + getString((getDurationSeconds())) + SECOND;
        } else {
            return getString(getDurationHours()) + HOUR + getString(getDurationMins()) + MINUTE + getString((getDurationSeconds())) + SECOND;
        }
    }

    public String toPrettyString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat(PortableCinemaConfig.DATE_FORMATTER).create();
        return gson.toJson(this);
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
