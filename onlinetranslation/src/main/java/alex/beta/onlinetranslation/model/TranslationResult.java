/**
 * <p>
 * File Name: TranslationResult.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/17 下午10:03
 * </p>
 */
package alex.beta.onlinetranslation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class TranslationResult {

    @JsonProperty("uuid")
    @ApiModelProperty
    @NotNull
    private String uuid;

    @JsonProperty("status")
    @ApiModelProperty
    private TranslationStatus status;

    @JsonProperty("message")
    @ApiModelProperty
    private String message;

    @JsonProperty("text")
    @ApiModelProperty
    private String text;

    @JsonProperty("translatedText")
    @ApiModelProperty
    private String translatedText;

    @JsonProperty("fromLanguage")
    @ApiModelProperty
    private String fromLanguage;

    @JsonProperty("toLanguage")
    @ApiModelProperty
    private String toLanguage;

    @JsonProperty("submittedOn")
    @ApiModelProperty
    private Date submittedOn;

    public TranslationResult(String uuid, TranslationStatus status, String text, String toLanguage) {
        this.uuid = uuid;
        this.status = status;
        this.text = text;
        this.toLanguage = toLanguage;
        submittedOn = Calendar.getInstance().getTime();
    }

    public TranslationResult(String uuid, TranslationStatus status, String message, String text, String translatedText, String fromLanguage, String toLanguage) {
        this(uuid, status, text, toLanguage);
        this.message = message;
        this.translatedText = translatedText;
        this.fromLanguage = fromLanguage;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public TranslationStatus getStatus() {
        return status;
    }

    public void setStatus(TranslationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getFromLanguage() {
        return fromLanguage;
    }

    public void setFromLanguage(String fromLanguage) {
        this.fromLanguage = fromLanguage;
    }

    public String getToLanguage() {
        return toLanguage;
    }

    public void setToLanguage(String toLanguage) {
        this.toLanguage = toLanguage;
    }

    public Date getSubmittedOn() {
        return submittedOn;
    }

    public void setSubmittedOn(Date submittedOn) {
        this.submittedOn = submittedOn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\"TranslationResult\" : {");
        if (uuid != null)
            sb.append("\"uuid\" : \"").append(this.uuid).append("\", ");
        if (status != null)
            sb.append("\"status\" : \"").append(this.status).append("\", ");
        if (message != null)
            sb.append("\"message\" : \"").append(this.message).append("\", ");
        if (text != null)
            sb.append("\"text\" : \"").append(this.text).append("\", ");
        if (fromLanguage != null)
            sb.append("\"fromLanguage\" : \"").append(this.fromLanguage).append("\", ");
        if (translatedText != null)
            sb.append("\"translatedText\" : \"").append(this.translatedText).append("\", ");
        if (toLanguage != null)
            sb.append("\"toLanguage\" : \"").append(this.toLanguage).append("\", ");
        if (submittedOn != null)
            sb.append("\"submittedOn\" : \"").append(this.submittedOn).append("\", ");
        sb.append("}");
        return sb.toString();
    }
}
