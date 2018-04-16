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

import alex.beta.filerepository.models.FileInfoModel;
import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.repository.FileInfoCustomizedRepository;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.QuotaService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;

import static org.mockito.Mockito.doReturn;
import static org.junit.Assert.*;

/**
 * @Description
 * @version ${project.version}
 */
@RunWith(MockitoJUnitRunner.class)
public class FileRepositoryServiceImplTest {

    @Mock
    private FileInfoRepository fileInfoRepository;

    @Mock
    private FileInfoCustomizedRepository fileInfoCustomizedRepository;

    @Mock
    private QuotaService quotaService;

    @InjectMocks
    private FileRepositoryServiceImpl fileRepositoryService;

    @Before
    public void setUp() {
        // Use RunWith annotation or initMocks in setUp
        // MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAdd() throws Exception {
        String name = "test" + System.currentTimeMillis();
        LocalDateTime dt = LocalDateTime.now();

        FileInfo savedFileInfo = FileInfo.builder().appid("test").name(name).id("1").createDate(dt).lastModifiedDate(dt).build();
        doReturn(savedFileInfo).when(fileInfoRepository).save(Matchers.any(FileInfo.class));

        FileInfoModel fim = fileRepositoryService.add("test", name, null, null, null, null, null);
        assertEquals("1", fim.getId());
    }
}
