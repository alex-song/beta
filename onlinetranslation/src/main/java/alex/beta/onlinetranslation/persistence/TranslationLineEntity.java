/**
 * @File: TranslationLineEntity.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/24 上午10:42
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * @Description
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
