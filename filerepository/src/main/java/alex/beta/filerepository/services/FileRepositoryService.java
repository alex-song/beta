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

import alex.beta.filerepository.models.FileModel;

import javax.annotation.Nonnull;

/**
 * @Description
 * @version ${project.version}
 */
public interface FileRepositoryService {

    FileModel add(@Nonnull String name, String description, String contentType, boolean temporary, byte[] content);
}
