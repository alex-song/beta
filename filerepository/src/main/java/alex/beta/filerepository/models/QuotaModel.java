/**
 * @File: QuotaModel.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/7 下午9:33
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.models;

import alex.beta.filerepository.persistence.entity.Quota;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @version ${project.version}
 * @Description
 */

@Data
@AllArgsConstructor
public class QuotaModel {

    @JsonProperty("id")
    @ApiModelProperty
    private String id;

    @JsonProperty("appid")
    @ApiModelProperty
    private String appid;

    @JsonProperty("maxQuota")
    @ApiModelProperty
    private long maxQuota;

    @JsonProperty("usedQuota")
    @ApiModelProperty
    private long usedQuota;

    public QuotaModel(Quota quota) {
        this.setId(quota.getId());
        this.setAppid(quota.getAppid());
        this.setUsedQuota(quota.getUsedQuota());
        this.setMaxQuota(quota.getMaxQuota());
    }
}
