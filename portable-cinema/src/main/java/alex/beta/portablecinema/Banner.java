package alex.beta.portablecinema;

import com.google.common.io.Resources;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;

/**
 * Operator in banner:
 * <p>
 * #DATETIME{2022-01-27 16:40:00} - Convert the datetime to local format
 * #{portable-cinema.conf} - Display config file path
 * #{portable-cinema.conf.text} - Display config file content
 */
public final class Banner {
    private static final Logger logger = LoggerFactory.getLogger(Banner.class);

    private static final String PREFIX_1 = "#";

    private static final String PREFIX_2 = "{";

    private static final String SUFFIX = "}";

    private static final String DATETIME = "DATETIME";

    private static Banner instance;

    private Pattern pattern;

    private String template;

    private Banner() {
        StringBuilder patternString = new StringBuilder();
        patternString.append("\\").append(PREFIX_1).append("[\\w.-/]*").append("\\").append(PREFIX_2);
        patternString.append("(.+?)");
        patternString.append("\\").append(SUFFIX);
        pattern = Pattern.compile(patternString.toString());

        try {
            template = Resources.asCharSource(Resources.getResource("banner.txt"), StandardCharsets.UTF_8).read();

            Matcher m = pattern.matcher(template);
            int offset = 0;
            while (m.find()) {
                MatchResult result = m.toMatchResult();
                for (int i = 0; i < result.groupCount(); i++) {
                    String matchedText = result.group(i);
                    int matchedTextLength = matchedText.length();
                    int prefix2pos = matchedText.indexOf(PREFIX_2, 1);
                    String operator = matchedText.substring(1, prefix2pos);
                    String extractedText = matchedText.substring(prefix2pos + 1, matchedText.length() - 1);
                    if (isBlank(operator)) {
                        operator = extractedText;
                    }
                    //Handle each operator
                    if (DATETIME.equalsIgnoreCase(operator)) {
                        String replacement = toLocalDateTime(extractedText);
                        template = template.substring(0, result.start(i) + offset) + replacement + template.substring(result.end(i) + offset);
                        offset += (replacement.length() - matchedTextLength);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Cannot read banner.txt", ex);
            template = "Portable Cinema" + System.lineSeparator();
        }
    }

    static synchronized Banner getInstance() {
        if (instance == null) {
            instance = new Banner();
        }
        return instance;
    }

    private String toLocalDateTime(String timestampString) {
        if (isBlank(timestampString) || timestampString.length() < (DATETIME.length() + 3)) {
            return timestampString;
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Matched datetime text is {}", timestampString);
        }
        try {
            Date buildTimestamp;
            if (isNumeric(timestampString)) {
                buildTimestamp = DateUtils.parseDate(timestampString, "yyyyMMddHHmmss");
            } else if (timestampString.indexOf('T') > 1 && timestampString.endsWith("Z")) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                buildTimestamp = simpleDateFormat.parse(timestampString);
            } else {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                buildTimestamp = simpleDateFormat.parse(timestampString);
            }
            return DateFormatUtils.format(buildTimestamp, "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        } catch (ParseException ex) {
            logger.info("Cannot parse {}", timestampString, ex);
            return timestampString;
        }
    }

    String read(String confPath, PortableCinemaConfig config) {
        //Don't change banner template, create a copy of it
        String bannerText = template;

        Matcher m = pattern.matcher(bannerText);
        int offset = 0;
        while (m.find()) {
            MatchResult result = m.toMatchResult();
            for (int i = 0; i < result.groupCount(); i++) {
                String matchedText = result.group(i);
                int matchedTextLength = matchedText.length();
                int prefix2pos = matchedText.indexOf(PREFIX_2, 1);
                String operator = matchedText.substring(1, prefix2pos);
                String extractedText = matchedText.substring(prefix2pos + 1, matchedText.length() - 1);
                if (isBlank(operator)) {
                    operator = extractedText;
                }
                //Handle each operator with parameters
                if (PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME.equalsIgnoreCase(operator)) {
                    bannerText = bannerText.substring(0, result.start(i) + offset) + confPath + bannerText.substring(result.end(i) + offset);
                    offset += (confPath.length() - matchedTextLength);
                } else if ((PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME + ".text").equalsIgnoreCase(operator)) {
                    String replacement = (config == null ? "" : config.toString());
                    bannerText = bannerText.substring(0, result.start(i) + offset) + replacement + bannerText.substring(result.end(i) + offset);
                    offset += (replacement.length() - matchedTextLength);
                }
            }
        }

        return bannerText;
    }
}
