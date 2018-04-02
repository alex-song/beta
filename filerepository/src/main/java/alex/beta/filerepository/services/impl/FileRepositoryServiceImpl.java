/**
 * @File: FileRepositoryServiceImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/2 下午10:38
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services.impl;

import alex.beta.filerepository.models.FileModel;
import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.entity.FileStore;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.FileRepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Nonnull;

/**
 * @Description
 * @version ${project.version}
 */

@Service("fileRepositoryService")
public class FileRepositoryServiceImpl implements FileRepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryServiceImpl.class);

    private FileInfoRepository fileInfoRepository;

    @Autowired
    public FileRepositoryServiceImpl(FileInfoRepository fileInfoRepository) {
        this.fileInfoRepository = fileInfoRepository;
    }

    @Override
    @Transactional
    public FileModel add(@Nonnull String name, String description, String contentType, boolean temporary, byte[] content) {
        FileInfo fileInfo = FileInfo.builder()
                .name(name)
                .description(description)
                .temporary(temporary)
                .contentType(contentType)
                .size(content == null ? 0 : content.length)
                .build();

        if (content != null) {
            FileStore fileStore = FileStore.builder()
                    .content(content)
                    .md5(DigestUtils.md5DigestAsHex(content))
                    .build();

            fileInfo.setFileStore(fileStore);
        }

        return new FileModel(fileInfoRepository.save(fileInfo));
    }
}
