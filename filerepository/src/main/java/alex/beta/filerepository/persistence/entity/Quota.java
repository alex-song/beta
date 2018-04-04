/**
 * @File: Quota.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午1:00
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @version ${project.version}
 * @Description
 */

@Data
@AllArgsConstructor
@Builder
@Document(collection = "Quota")
public class Quota {
    @Id
    @Field("id")
    private String id;

    @Field("appid")
    @Indexed(unique = true)
    @NonNull
    private String appid;

    @Field("maxQuota")
    private long maxQuota;

    @Field("usedQuota")
    private long usedQuota;
}
