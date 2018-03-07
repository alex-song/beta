/**
 * @File: TranslationEntity.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/18 下午9:07
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

import com.google.gson.Gson;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @Description
 * @version ${project.version}
 */
@Entity
@Table(name = "Translation",
        indexes = {@Index(columnList = "lastUpdatedOn"), @Index(columnList = "status, lastUpdatedOn")})
public class TranslationEntity {
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

    @Column(name = "fromLanguage", length = 8)
    private String fromLanguage;

    @Column(name = "toLanguage", length = 8)
    private String toLanguage;

    @Column(name = "createdOn")
    private Date createdOn;

    @Column(name = "lastUpdatedOn")
    private Date lastUpdatedOn;

    @ElementCollection(targetClass = TranslationLineEntity.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "TranslationLine", joinColumns = {@JoinColumn(name = "translation_uuid")},
            indexes = {@Index(columnList = "translation_uuid")})
    private List<TranslationLineEntity> translationLines;

    public TranslationEntity() {
        //default constructor
    }

    public TranslationEntity(String uuid) {
        this.uuid = uuid;
    }

    public TranslationEntity(TranslationStatus status, String fromLanguage, String toLanguage, String text) {
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

    public List<TranslationLineEntity> getTranslationLines() {
        return translationLines;
    }

    public void setTranslationLines(List<TranslationLineEntity> translationLines) {
        this.translationLines = translationLines;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
