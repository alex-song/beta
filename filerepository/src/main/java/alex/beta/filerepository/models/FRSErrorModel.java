/**
 * @File: FRSErrorModel.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/6 下午9:33
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @version ${project.version}
 * @Description
 */

@Data
@AllArgsConstructor
public class FRSErrorModel {
    @JsonProperty("errorCode")
    @ApiModelProperty
    @NotNull
    private String errorCode;

    @JsonProperty("message")
    @ApiModelProperty
    private String message;

    public FRSErrorModel() {
        //default construction
    }

    public FRSErrorModel(String errorCode) {
        this.errorCode = errorCode;
    }
}
