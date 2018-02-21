/**
 * <p>
 * File Name: BaiduTranslationResult.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午11:54
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class BaiduTranslationResult {
    @JsonProperty("src")
    private String src;

    @JsonProperty("dst")
    private String dst;

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
