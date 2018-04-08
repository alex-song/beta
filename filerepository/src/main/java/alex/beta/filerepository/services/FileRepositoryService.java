/**
 * @File: FileRepositoryService.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/2 下午10:37
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services;

import alex.beta.filerepository.ContentValidationException;
import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.models.FileInfoModel;
import alex.beta.filerepository.models.FileStoreModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @version ${project.version}
 * @Description
 */
public interface FileRepositoryService {

    /**
     * @param appid
     * @param name
     * @param description
     * @param contentType
     * @param expiredDate
     * @param md5
     * @param content
     * @return
     * @throws ContentValidationException
     * @throws QuotaExceededException
     */
    FileInfoModel add(String appid, String name, String description, String contentType,
                      LocalDateTime expiredDate, String md5, byte[] content)
            throws ContentValidationException, QuotaExceededException;

    /**
     * @param appid
     * @param name
     * @param skip
     * @param size
     * @return
     */
    List<FileInfoModel> find(String appid, String name, int skip, int size);

    /**
     * Default page size is 50
     *
     * @param appid
     * @param name
     * @param pageNum
     * @return
     */
    List<FileInfoModel> page(String appid, String name, int pageNum);

    /**
     * @return
     */
    Set<String> findAllAppid();

    /**
     * @param fileInfoId
     * @return
     */
    FileInfoModel get(String fileInfoId);

    /**
     * Delete file according to given file info id, and release the quota
     *
     * @param fileInfoId
     * @return null, if the file is not found
     */
    FileInfoModel delete(String fileInfoId);

    /**
     * Delete all files of appid, and release the quota
     *
     * @param appid
     */
    void deleteAppid(String appid);

    /**
     * @param fileInfoId
     * @return
     */
    FileStoreModel getFile(String fileInfoId);

    /**
     * @param fileInfoId
     * @param description
     * @param expiredDate
     * @return
     */
    FileInfoModel update(String fileInfoId, String description, LocalDateTime expiredDate);
}
