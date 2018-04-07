/**
 * @File: FileStoreModel.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/5 下午9:22
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.models;

import alex.beta.filerepository.persistence.entity.FileStore;
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
public class FileStoreModel {
    @JsonProperty("id")
    @ApiModelProperty
    private String id;

    @JsonProperty("content")
    @ApiModelProperty
    private byte[] content;

    public FileStoreModel(FileStore fileStore) {
        this.setId(fileStore.getInfoId());
        this.setContent(fileStore.getContent());
    }
}
