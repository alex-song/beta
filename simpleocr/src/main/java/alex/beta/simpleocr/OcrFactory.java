package alex.beta.simpleocr;

import alex.beta.simpleocr.baidu.BaiduOcr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OcrFactory {
    public static final String PROXY_HOST = "PROXY_HOST";
    public static final String PROXY_PORT = "PROXY_PORT";
    private static final Logger logger = LoggerFactory.getLogger(OcrFactory.class);
    private static final String CONFIG_FILE = "simpleocr.config";
    private static Properties configuration;

    private OcrFactory() {
        //Empty construct of this factory class
    }

    public static synchronized Ocr newInstance(Provider provider) {
        if (configuration == null) {
            configuration = new Properties();
            try (InputStream in = new BufferedInputStream(new FileInputStream(CONFIG_FILE))) {
                configuration.load(in);
            } catch (IOException ex) {
                logger.error("Failed to read {}", CONFIG_FILE, ex);
            }
        }
        switch (provider) {
            case BAIDU:
                return new BaiduOcr(configuration);
            case AMAZON:
            case ASPOSE:
            default:
                throw new UnsupportedOperationException();
        }
    }

    public enum Provider {
        BAIDU, AMAZON, ASPOSE
    }
}
