/**
 * <p>
 * File Name: BaiduTranslation.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午11:53
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class BaiduTranslation {
    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("trans_result")
    private List<BaiduTranslationResult> transResult;

    public static BaiduTranslation fromString(String jsonString) throws IOException {
        return new ObjectMapper().readValue(jsonString, BaiduTranslation.class);
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<BaiduTranslationResult> getTransResult() {
        return transResult;
    }

    public void setTransResult(List<BaiduTranslationResult> transResult) {
        this.transResult = transResult;
    }

    public String joinAllDstsWithLineSeparator() {
        if (this.transResult != null && !this.transResult.isEmpty()) {
            return transResult.stream().map(BaiduTranslationResult::<String>getDst).collect(Collectors.joining(System.lineSeparator()));
        } else {
            return null;
        }
    }
}
