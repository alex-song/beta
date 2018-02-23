/**
 * <p>
 * File Name: TranslationError.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午2:01
 * </p>
 */
package alex.beta.onlinetranslation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class TranslationError implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(TranslationError.class);

    @JsonProperty("errorCode")
    @ApiModelProperty
    @NotNull
    private String errorCode;

    @JsonProperty("message")
    @ApiModelProperty
    private String message;

    public TranslationError(String errorCode) {
        this.errorCode = errorCode;
    }

    public TranslationError(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error("Failed to write this object as JSON string", e);
            return super.toString();
        }
    }
}
