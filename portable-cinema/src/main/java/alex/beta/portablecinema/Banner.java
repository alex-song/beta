package alex.beta.portablecinema;

import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

final class Banner {
    private static final Logger logger = LoggerFactory.getLogger(Banner.class);

    private Banner() {

    }

    public static String getBanner(String confPath, PortableCinemaConfig config) {
        try {
            String banner = Resources.asCharSource(Resources.getResource("banner.txt"), StandardCharsets.UTF_8).read();
            return banner.replace("@" + PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME + "@", confPath)
                    .replace("@" + PortableCinemaConfig.CONFIGURATION_PROPERTY_NAME + ".content@", config.toString());
        } catch (Exception ex) {
            logger.error("Cannot read banner.txt", ex);
            return "Portable Cinema" + System.lineSeparator();
        }
    }
}
