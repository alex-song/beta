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
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class TranslationError implements Serializable {
    @JsonProperty("errorCode")
    @ApiModelProperty
    @NotNull
    private String errorCode;

    @JsonProperty("message")
    @ApiModelProperty
    private String message;

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
}
