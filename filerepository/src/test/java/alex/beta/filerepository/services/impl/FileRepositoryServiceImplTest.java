/**
 * @File: FileRepositoryServiceImplTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/15 下午10:59
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services.impl;

import alex.beta.filerepository.persistence.repository.FileInfoCustomizedRepository;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.QuotaService;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.doReturn;

/**
 * @Description
 * @version ${project.version}
 */
public class FileRepositoryServiceImplTest {

    @MockBean
    private FileInfoRepository fileInfoRepository;

    @MockBean
    private FileInfoCustomizedRepository fileInfoCustomizedRepository;

    @MockBean
    private QuotaService quotaService;

    @Test
    public void testAdd() {
        //TODO
        //doReturn(output).when(apiConnector).translate(Matchers.any(TranslationEntity.class));
    }
}
