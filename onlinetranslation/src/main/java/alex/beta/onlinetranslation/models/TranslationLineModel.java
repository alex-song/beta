/**
 * @File: TranslationLineModel.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/24 上午11:13
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

/**
 * @version ${project.version}
 * @Description
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
