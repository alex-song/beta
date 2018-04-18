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

import alex.beta.filerepository.ContentValidationException;
import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.models.FileInfoModel;
import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.entity.FileStore;
import alex.beta.filerepository.persistence.repository.FileInfoCustomizedRepository;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.QuotaService;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @version ${project.version}
 * @Description
 */
@RunWith(MockitoJUnitRunner.class)
public class FileRepositoryServiceImplTest {
    private static final String APPID = "FileRepositoryServiceImplTest";

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
        final String name = APPID + System.currentTimeMillis();
        LocalDateTime dt = LocalDateTime.now();

        Resource res = new PathMatchingResourcePatternResolver().getResource("classpath:FileRepositoryServiceImplTest_10.txt");
        byte[] content = IOUtils.toByteArray(res.getInputStream());

        FileStore savedFileStore = FileStore.builder().content(content).infoId("1").id("a").build();
        FileInfo savedFileInfo = FileInfo.builder().appid(APPID).name(name).id("1").createDate(dt).lastModifiedDate(dt).fileStore(savedFileStore).size(10).build();
        doReturn(savedFileInfo).when(fileInfoRepository).save(Matchers.argThat(new FileInfoMatcher(APPID, name)));

        FileInfoModel fim = fileRepositoryService.add(APPID, name, null, null, null, null, content);
        assertEquals("1", fim.getId());
        assertEquals(10, fim.getSize());
    }

    @Test(expected = ContentValidationException.class)
    public void testAddThrowsContentValidationException() throws Exception {
        final String name = APPID + System.currentTimeMillis();
        LocalDateTime dt = LocalDateTime.now();

        Resource res = new PathMatchingResourcePatternResolver().getResource("classpath:FileRepositoryServiceImplTest_10.txt");
        byte[] content = IOUtils.toByteArray(res.getInputStream());

        fileRepositoryService.add(APPID, name, null, null, null, "1234", content);
    }

    @Test(expected = QuotaExceededException.class)
    public void testAddThrowsQuotaExceededException() throws Exception {
        final String name = APPID + System.currentTimeMillis();
        LocalDateTime dt = LocalDateTime.now();

        Resource res = new PathMatchingResourcePatternResolver().getResource("classpath:FileRepositoryServiceImplTest_10.txt");
        byte[] content = IOUtils.toByteArray(res.getInputStream());

        FileStore savedFileStore = FileStore.builder().content(content).infoId("2").id("a").build();
        FileInfo savedFileInfo = FileInfo.builder().appid(APPID).name(name).id("2").createDate(dt).lastModifiedDate(dt).fileStore(savedFileStore).size(10).build();
        doReturn(savedFileInfo).when(fileInfoRepository).save(Matchers.argThat(new FileInfoMatcher(APPID, name)));

        doThrow(new QuotaExceededException(APPID, 10, 100, 100)).when(quotaService).useQuota(APPID, 10);

        fileRepositoryService.add(APPID, name, null, null, null, null, content);
    }

    @Test
    public void testDeleteExpiredFiles() throws Exception {
        final String name = APPID + System.currentTimeMillis();
        LocalDateTime dt = LocalDateTime.now();

        FileInfo fi3 = FileInfo.builder().size(10).id("3").name(name + "3").appid(APPID).build();
        FileInfo fi4 = FileInfo.builder().size(100).id("4").name(name + "4").appid(APPID).build();
        FileInfo fi5 = FileInfo.builder().size(1000).id("5").name(name + "5").appid(APPID).build();

        doReturn(Arrays.asList(fi3, fi4, fi5)).when(fileInfoCustomizedRepository).findAllAndRemoveByAppidIgnoreCaseAndExpiredDateLessThan(APPID, dt);
        fileRepositoryService.deleteExpiredFiles(APPID, dt);
        verify(quotaService, times(1)).releaseQuota(APPID, 1110);
    }

    @Test
    public void testDelete() throws Exception {
        final String name = APPID + System.currentTimeMillis();
        FileInfo deletedFile = FileInfo.builder().id(name).name(name).appid(APPID).size(10).build();
        doReturn(deletedFile).when(fileInfoRepository).findOne(name);
        doNothing().when(fileInfoRepository).delete(name);
        fileRepositoryService.delete(name);
        verify(quotaService, times(1)).releaseQuota(APPID, 10);
    }

    static class FileInfoMatcher extends ArgumentMatcher<FileInfo> {
        private String appid;

        private String name;

        FileInfoMatcher(String appid, String name) {
            this.appid = appid;
            this.name = name;
        }

        @Override
        public boolean matches(Object obj) {
            if (obj != null && obj instanceof FileInfo) {
                FileInfo info = (FileInfo) obj;
                return appid.equals(info.getAppid()) && name.equals(info.getName());
            } else {
                return false;
            }
        }
    }
}
