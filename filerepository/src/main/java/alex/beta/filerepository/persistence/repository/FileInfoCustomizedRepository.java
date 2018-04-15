/**
 * @File: FileInfoCustomizedRepository.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/6 上午9:32
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.persistence.entity.FileInfo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @version ${project.version}
 * @Description
 */
public interface FileInfoCustomizedRepository {

    /**
     * @return in lower case
     */
    Set<String> findAllAppid();

    /**
     * Delete all file info by appid
     *
     * @param appid
     * @return deleted FileInfo
     */
    List<FileInfo> deleteByAppid(String appid);

    /**
     * @param appid
     * @param name
     * @param skip
     * @param limit
     * @return
     */
    List<FileInfo> findByAppidAndNameIgnoreCase(String appid, String name, int skip, int limit);

    /**
     * @param appid
     * @param dateTime
     * @return
     */
    List<FileInfo> findAllAndRemoveByAppidIgnoreCaseAndExpiredDateLessThan(String appid, LocalDateTime dateTime);
}
