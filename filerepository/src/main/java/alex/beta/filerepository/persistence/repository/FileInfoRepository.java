/**
 * @File: FileInfoRepository.java
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
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.persistence.entity.FileInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @Description
 * @version ${project.version}
 */
public interface FileInfoRepository extends MongoRepository<FileInfo, Long> {
}
