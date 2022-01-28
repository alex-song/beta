package alex.beta.portablecinema;

import com.google.common.io.Resources;
import lombok.NonNull;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * #{2022-01-27T16:40:00Z} - Convert the datetime to local format
 * #{config.path} - Display config file path
 */
public final class Banner {
    private static final Logger logger = LoggerFactory.getLogger(Banner.class);

    private static final String PREFIX_1 = "#";

    private static final String PREFIX_2 = "{";

    private static final String SUFFIX = "}";

    private static final Pattern normalizedTimestampPattern = Pattern.compile("\\" + PREFIX_1 + "\\" + PREFIX_2 + "\\d{14}" + "\\" + SUFFIX);

    private static final Pattern utcTimestampPattern = Pattern.compile("\\" + PREFIX_1 + "\\" + PREFIX_2 + "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z" + "\\" + SUFFIX);

    private static Banner instance;

    private String template;

    private Banner() {
        try {
            template = Resources.asCharSource(Resources.getResource("banner.txt"), StandardCharsets.UTF_8).read();
        } catch (Exception ex) {
            logger.error("Cannot read banner.txt", ex);
            template = "Portable Cinema" + System.lineSeparator();
        }
    }

    static synchronized Banner getInstance() {
        if (instance == null) {
            instance = new Banner();
            instance.template = instance.convertTimestamp(instance.template);
        }
        return instance;
    }

    String convertTimestamp(String text) {
        return convertUTCTimestamp(convertNormalizedTimestamp(text));
    }

    String read4CLI(String confPath, PortableCinemaConfig config) {
        return read(ConsoleColors.BLACK_BOLD + confPath + ConsoleColors.RESET, config);
    }

    String read4GUI(String confPath, PortableCinemaConfig config) {
        return read("<b>" + confPath + "</b>", config);
    }

    private String convertUTCTimestamp(String text) {
        Matcher m = utcTimestampPattern.matcher(text);
        int offset = 0;
        while (m.find()) {
            String matchedText = m.group(0);
            int matchedTextLength = matchedText.length();
            String extractedText = matchedText.substring(2, matchedText.length() - 1);
            //Handle each operator
            String replacement = toLocalDateTime(extractedText, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("UTC"));
            text = text.substring(0, m.start(0) + offset) + replacement + text.substring(m.end(0) + offset);
            offset += (replacement.length() - matchedTextLength);
        }
        return text;
    }

    private String convertNormalizedTimestamp(String text) {
        Matcher m = normalizedTimestampPattern.matcher(text);
        int offset = 0;
        while (m.find()) {
            String matchedText = m.group(0);
            int matchedTextLength = matchedText.length();
            String extractedText = matchedText.substring(2, matchedText.length() - 1);
            //Handle each operator
            String replacement = toLocalDateTime(extractedText, "yyyyMMddHHmmss", null);
            text = text.substring(0, m.start(0) + offset) + replacement + text.substring(m.end(0) + offset);
            offset += (replacement.length() - matchedTextLength);
        }
        return text;
    }

    private String toLocalDateTime(@NonNull String timestampString, @NonNull String format, TimeZone timeZone) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
            if (timeZone != null) {
                simpleDateFormat.setTimeZone(timeZone);
            }
            Date buildTimestamp = simpleDateFormat.parse(timestampString);
            return DateFormatUtils.format(buildTimestamp, "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        } catch (ParseException ex) {
            logger.info("Cannot parse {} with format {}", timestampString, format, ex);
            return timestampString;
        }
    }

    private String read(String confPath, PortableCinemaConfig config) {
        //Don't change banner template, create a copy of it
        String bannerText = template;
        //Replace #{config.path}
        return bannerText.replaceAll("\\" + PREFIX_1 + "\\" + PREFIX_2 + "config.path" + "\\" + SUFFIX, confPath);
    }
}
