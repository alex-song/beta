package alex.beta.simpleocr.baidu;

import alex.beta.simpleocr.Ocr;
import alex.beta.simpleocr.OcrException;
import com.baidu.aip.ocr.AipOcr;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import static alex.beta.simpleocr.OcrFactory.PROXY_HOST;
import static alex.beta.simpleocr.OcrFactory.PROXY_PORT;

public class BaiduOcr implements Ocr {

    public static final String BAIDU_APP_ID = "BAIDU_APP_ID";
    public static final String BAIDU_API_KEY = "BAIDU_API_KEY";
    public static final String BAIDU_SECRET_KEY = "BAIDU_SECRET_KEY";
    private static final Logger logger = LoggerFactory.getLogger(BaiduOcr.class);
    private static final String ERROR_MSG = "error_msg";

    private AipOcr client;

    public BaiduOcr(@NonNull Properties configuration) {
        this.client = new AipOcr(configuration.getProperty(BAIDU_APP_ID),
                configuration.getProperty(BAIDU_API_KEY),
                configuration.getProperty(BAIDU_SECRET_KEY));

        if (!Strings.isNullOrEmpty(configuration.getProperty(PROXY_HOST))) {
            int proxyPort = 80;
            if (!Strings.isNullOrEmpty(configuration.getProperty(PROXY_PORT))) {
                proxyPort = Integer.parseInt(configuration.getProperty(PROXY_PORT));
            }
            this.client.setHttpProxy(configuration.getProperty(PROXY_HOST), proxyPort);
            this.client.setConnectionTimeoutInMillis(1000 * 60); //1 min
            this.client.setSocketTimeoutInMillis(1000 * 60 * 3); //3 min
        }
    }

    @Override
    public List<String> analyse(byte[] image) throws OcrException {
        HashMap<String, String> options = new HashMap<>();
        //options.put("detect_language", "true");
        options.put("detect_direction", "true");
        options.put("language_type", "auto_detect");
        JSONObject result = client.basicAccurateGeneral(image, options);
        if (logger.isDebugEnabled()) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            logger.debug(gson.toJson(result));
        }
        List<String> analyseResult = new ArrayList<>();
        String resultStr = result.toString();
        if (resultStr.contains(ERROR_MSG)) {
            throw new OcrException(OcrException.SERVER_ERROR_EXCEPTION).setServerErrorMsg(resultStr);
        } else {
            JSONArray jsonArray = result.getJSONArray("words_result");
            for (Object aResult : jsonArray) {
                String tmp = aResult.toString();
                //analyseResult.add(tmp.substring(10, tmp.lastIndexOf("\",\"location")));
                analyseResult.add(tmp.substring(10, tmp.lastIndexOf('"')));
            }
        }
        return analyseResult;
    }

    @Override
    public List<String> analyse(File file) throws OcrException {
        try {
            return analyse(Files.toByteArray(file));
        } catch (IOException ex) {
            throw new OcrException(OcrException.IMAGE_FILE_READ_EXCEPTION, ex);
        }
    }
}
