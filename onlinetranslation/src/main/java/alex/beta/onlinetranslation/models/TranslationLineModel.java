/**
 * <p>
 * File Name: TranslationLineModel.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/24 上午11:13
 * </p>
 */
package alex.beta.onlinetranslation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class TranslationLineModel {
    @JsonProperty("src")
    @ApiModelProperty
    private String src;

    @JsonProperty("dst")
    @ApiModelProperty
    private String dst;

    public TranslationLineModel() {
        //default constructor
    }

    public TranslationLineModel(String src, String dst) {
        this.src = src;
        this.dst = dst;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }
}
