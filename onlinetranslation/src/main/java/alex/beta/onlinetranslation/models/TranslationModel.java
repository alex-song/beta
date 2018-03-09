/**
 * @File: TranslationModel.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/17 下午10:03
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.models;

import alex.beta.onlinetranslation.persistence.TranslationEntity;
import alex.beta.onlinetranslation.persistence.TranslationLineEntity;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.ApiModelProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
public class TranslationModel {
    public static final TranslationModel NOTHING_TO_TRANSLATE = new TranslationModel("NOTHING_TO_TRANSLATE", TranslationStatus.READY);
    private static final Logger logger = LoggerFactory.getLogger(TranslationModel.class);
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
    @JsonProperty("inputText")
    @ApiModelProperty
    private String text;
    @JsonProperty("translations")
    @ApiModelProperty
    private List<TranslationLineModel> translationLines;
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

    public TranslationModel(TranslationEntity translationEntity) {
        this.uuid = translationEntity.getUuid();
        this.status = translationEntity.getStatus();
        this.text = translationEntity.getText();
        this.toLanguage = translationEntity.getToLanguage();
        this.createdOn = translationEntity.getCreatedOn();
        this.fromLanguage = translationEntity.getFromLanguage();
        this.message = translationEntity.getMessage();
        this.lastUpdatedOn = translationEntity.getLastUpdatedOn();
        if (translationEntity.getTranslationLines() != null && !translationEntity.getTranslationLines().isEmpty()) {
            List<TranslationLineModel> lines = new ArrayList<>(translationEntity.getTranslationLines().size());
            for (TranslationLineEntity l : translationEntity.getTranslationLines()) {
                lines.add(new TranslationLineModel(l.getSrc(), l.getDst()));
            }
            this.translationLines = lines;
        }
    }

    private TranslationModel(String uuid, TranslationStatus status) {
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

    public List<TranslationLineModel> getTranslationLines() {
        return translationLines;
    }

    public void setTranslationLines(List<TranslationLineModel> translationLines) {
        this.translationLines = translationLines;
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