/**
 * <p>
 * File Name: TranslationLineEntity.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/24 上午10:42
 * </p>
 */
package alex.beta.onlinetranslation.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Embeddable
public class TranslationLineEntity {

    @Column(name = "src", length = TranslationEntity.TEXT_MAXLENGTH)
    private String src;

    @Column(name = "dst", length = TranslationEntity.TRANSLATED_TEXT_MAXLENGTH)
    private String dst;

    public TranslationLineEntity() {
        //default constructor
    }

    public TranslationLineEntity(String src, String dst) {
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
