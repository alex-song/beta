/**
 * @File: FileModel.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/2 下午10:42
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.models;

import alex.beta.filerepository.persistence.entity.FileInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @version ${project.version}
 * @Description
 */

@Data
@AllArgsConstructor
public class FileModel {

    @JsonProperty("id")
    @ApiModelProperty
    private String id;

    @JsonProperty("name")
    @ApiModelProperty
    private String name;

    @JsonProperty("description")
    @ApiModelProperty
    private String description;

    @JsonProperty("size")
    @ApiModelProperty
    private int size;

    @JsonProperty("contentType")
    @ApiModelProperty
    private String contentType;

    @JsonProperty("temporary")
    @ApiModelProperty
    private boolean temporary;

    @JsonProperty("md5")
    @ApiModelProperty
    private String md5;

    @JsonProperty("content")
    @ApiModelProperty
    private byte[] content;

    @JsonProperty("createDate")
    @ApiModelProperty
    private LocalDateTime createDate;

    @JsonProperty("createdBy")
    @ApiModelProperty
    private String createdBy;

    @JsonProperty("lastModifiedBy")
    @ApiModelProperty
    private String lastModifiedBy;

    @JsonProperty("lastModifiedDate")
    @ApiModelProperty
    private LocalDateTime lastModifiedDate;

    public FileModel(FileInfo fileInfo) {
        //TODO
    }
}
