/**
 * <p>
 * File Name: BaiduTranslationError.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/22 下午2:33
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class BaiduTranslationError {
    public static final String TIMEOUT = "52001";
    public static final String INTERNAL_ERROR = "52002";
    public static final String UNAUTHORIZED_USER = "52003";

    public static final String INSUFFICIENT_PARAMETER = "54000";
    public static final String INVALID_SIGN = "54001";
    public static final String TOO_FREQUENT = "54003";
    public static final String INSUFFICIENT_BALANCE = "54004";
    public static final String LONG_QUERY_TOO_FREQUENT = "54005";

    public static final String INVALID_IP = "58000";
    public static final String INVALID_TO_LANGUAGE = "58001";

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_msg")
    private String errorMessage;

    public static BaiduTranslationError fromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, BaiduTranslationError.class);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
