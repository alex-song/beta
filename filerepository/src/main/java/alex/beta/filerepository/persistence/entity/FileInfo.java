/**
 * @File: FileInfo.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/31 22:00
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.entity;

import alex.beta.filerepository.mongo.Cascade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * @version ${project.version}
 * @Description
 */

@Data
@AllArgsConstructor
@Builder
@Document(collection = "FileInfo")
public class FileInfo {
    @Id
    @Field("id")
    private String id;

    @Field("appid")
    @NonNull
    private String appid;

    @Field("name")
    @NonNull
    private String name;

    @Field("description")
    private String description;

    @Field("size")
    private int size;

    @Field("contentType")
    private String contentType;

    @Field("expiredDate")
    private LocalDateTime expiredDate;

    @DBRef(lazy = true)
    @Cascade(delete = true)
    private FileStore fileStore;

    @CreatedDate
    @Field("createDate")
    private LocalDateTime createDate;

    @CreatedBy
    @Field("createdBy")
    private String createdBy;

    @LastModifiedBy
    @Field("lastModifiedBy")
    private String lastModifiedBy;

    @LastModifiedDate
    @Field("lastModifiedDate")
    private LocalDateTime lastModifiedDate;
}
