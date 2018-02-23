/**
 * <p>
 * File Name: Translation.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/18 下午9:07
 * </p>
 */
package alex.beta.onlinetranslation.persistence;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Entity
@Table(name = "Translation",
        indexes = {@Index(columnList = "status"),
                @Index(columnList = "lastUpdatedOn")})
public class Translation implements Serializable {
    public static final int TEXT_MAXLENGTH = 2048;
    public static final int TRANSLATED_TEXT_MAXLENGTH = 8192;

    @Id
    @Column(name = "uuid", length = 64)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TranslationStatus status;

    @Column(name = "message")
    private String message;

    @Column(name = "text", length = TEXT_MAXLENGTH)
    private String text;

    @Column(name = "translatedText", length = TRANSLATED_TEXT_MAXLENGTH)
    private String translatedText;

    @Column(name = "fromLanguage", length = 8)
    private String fromLanguage;

    @Column(name = "toLanguage", length = 8)
    private String toLanguage;

    @Column(name = "createdOn")
    private Date createdOn;

    @Column(name = "lastUpdatedOn")
    private Date lastUpdatedOn;

    public Translation() {
        //default constructor
    }

    public Translation(TranslationStatus status, String fromLanguage, String toLanguage, String text) {
        this.status = status;
        this.fromLanguage = fromLanguage;
        this.toLanguage = toLanguage;
        this.text = text;
        long timetamp = System.currentTimeMillis();
        this.createdOn = new Date(timetamp);
        this.lastUpdatedOn = new Date(timetamp);
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

    public Date getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(Date lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        if (this.getUuid() != null)
            sb.append("\"uuid\" : \"").append(this.getUuid()).append("\", ");
        if (this.getStatus() != null)
            sb.append("\"status\" : \"").append(this.getStatus()).append("\", ");
        if (this.getMessage() != null)
            sb.append("\"message\" : \"").append(this.getMessage()).append("\", ");
        if (this.getText() != null)
            sb.append("\"text\" : \"").append(this.getText()).append("\", ");
        if (this.getFromLanguage() != null)
            sb.append("\"fromLanguage\" : \"").append(this.getFromLanguage()).append("\", ");
        if (this.getTranslatedText() != null)
            sb.append("\"translatedText\" : \"").append(this.getTranslatedText()).append("\", ");
        if (this.getToLanguage() != null)
            sb.append("\"toLanguage\" : \"").append(this.getToLanguage()).append("\", ");
        if (this.getCreatedOn() != null)
            sb.append("\"createdOn\" : \"").append(this.getCreatedOn()).append("\", ");
        if (this.getLastUpdatedOn() != null)
            sb.append("\"lastUpdatedOn\" : \"").append(this.getLastUpdatedOn()).append("\"");
        sb.append("}");
        return sb.toString();
    }
}
