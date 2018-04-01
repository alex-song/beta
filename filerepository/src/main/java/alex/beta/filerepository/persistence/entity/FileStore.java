/**
 * @File: FileStore.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/1 上午10:01
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @version ${project.version}
 * @Description
 */

@Data
@AllArgsConstructor
@Builder
@Document(collection = "FileStore")
public class FileStore {
    @Id
    @Field("id")
    private String id;

    @Field("md5")
    private String md5;

    @Field("content")
    private byte[] content;

    /**
     * 反向链接，指向对应的FileInfo。为清理数据，及联删除提供便利。
     */
    @Field("infoId")
    private String infoId;
}
