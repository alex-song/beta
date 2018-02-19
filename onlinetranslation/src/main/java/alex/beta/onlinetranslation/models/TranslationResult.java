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
package alex.beta.onlinetranslation.models;

import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class TranslationResult {

    public static final TranslationResult NOTHING_TO_TRANSLATE = new TranslationResult("NOTHING_TO_TRANSLATE", TranslationStatus.READY);
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
    @JsonProperty("createdOn")
    @ApiModelProperty
    private Date createdOn;
    @JsonProperty("lastUpdatedOn")
    @ApiModelProperty
    private Date lastUpdatedOn;

    public TranslationResult(Translation translation) {
        this.uuid = translation.getUuid();
        this.status = translation.getStatus();
        this.text = translation.getText();
        this.toLanguage = translation.getToLanguage();
        this.createdOn = translation.getCreatedOn();
        this.fromLanguage = translation.getFromLanguage();
        this.translatedText = translation.getTranslatedText();
        this.message = translation.getMessage();
        this.lastUpdatedOn = translation.getLastUpdatedOn();
    }

    private TranslationResult(String uuid, TranslationStatus status) {
        this.uuid = uuid;
        this.status = status;
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

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
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
        if (createdOn != null)
            sb.append("\"createdOn\" : \"").append(this.createdOn).append("\", ");
        if (lastUpdatedOn != null)
            sb.append("\"lastUpdatedOn\" : \"").append(this.lastUpdatedOn).append("\"");
        sb.append("}");
        return sb.toString();
    }
}