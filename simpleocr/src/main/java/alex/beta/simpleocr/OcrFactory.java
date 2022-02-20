package alex.beta.simpleocr;

import alex.beta.simpleocr.baidu.BaiduOcr;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class OcrFactory {
    public static final String PROXY_HOST = "PROXY_HOST";
    public static final String PROXY_PORT = "PROXY_PORT";
    private static final String CONFIG_FILE = "simpleocr.config";

    private static Properties configuration;

    private OcrFactory() {
        //Empty construct of this factory class
    }

    public static synchronized Ocr newInstance(Provider provider) throws OcrException {
        if (configuration == null) {
            configuration = new Properties();
            try (InputStream in = new BufferedInputStream(new FileInputStream(CONFIG_FILE))) {
                configuration.load(in);
            } catch (IOException ex) {
                throw new OcrException(OcrException.CONFIG_FILE_READ_EXCEPTION, ex);
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
