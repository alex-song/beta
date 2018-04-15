/**
 * @File: FileInfoCustomizedRepositoryTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/15 下午3:05
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.AbstractServerTest;
import alex.beta.filerepository.persistence.entity.FileInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Description
 * @version ${project.version}
 */

@Ignore
public class FileInfoCustomizedRepositoryTest extends AbstractServerTest {

    @Autowired
    private FileInfoCustomizedRepository fileInfoCustomizedRepository;

    @Autowired
    private FileInfoRepository fileInfoRepository;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFindByAppidAndNameContainsIgnoreCaseStartsWith() {
        String appid = String.valueOf(System.currentTimeMillis());
        FileInfo fi1 = FileInfo.builder().appid(appid).description("fi1").name("QwErTyUi").build();
        FileInfo fi2 = FileInfo.builder().appid(appid).description("fi2").name("QwEr").build();
        FileInfo fi3 = FileInfo.builder().appid(appid).description("fi3").name("qWeR").build();

        fi1 = fileInfoRepository.save(fi1);
        fi2 = fileInfoRepository.save(fi2);
        fi3 = fileInfoRepository.save(fi3);

        assertNotNull(fi1.getId());
        assertNotNull(fi2.getId());
        assertNotNull(fi3.getId());

        List<FileInfo> fis = fileInfoCustomizedRepository.findByAppidAndNameContainsIgnoreCase(appid, "qwe", 0, 10000);

        assertEquals(3, fis.size());
    }

    @Test
    public void testFindByAppidAndNameContainsIgnoreCaseEndsWith() {
        String appid = String.valueOf(System.currentTimeMillis());
        FileInfo fi1 = FileInfo.builder().appid(appid).description("fi1").name("QwErTyUi").build();
        FileInfo fi2 = FileInfo.builder().appid(appid).description("fi2").name("tYUI").build();
        FileInfo fi3 = FileInfo.builder().appid(appid).description("fi3").name("tyui").build();

        fi1 = fileInfoRepository.save(fi1);
        fi2 = fileInfoRepository.save(fi2);
        fi3 = fileInfoRepository.save(fi3);

        assertNotNull(fi1.getId());
        assertNotNull(fi2.getId());
        assertNotNull(fi3.getId());

        List<FileInfo> fis = fileInfoCustomizedRepository.findByAppidAndNameContainsIgnoreCase(appid, "ui", 0, 10000);

        assertEquals(3, fis.size());
    }

    @Test
    public void testFindByAppidAndNameContainsIgnoreCaseContains() {
        String appid = String.valueOf(System.currentTimeMillis());
        FileInfo fi1 = FileInfo.builder().appid(appid).description("fi1").name("QwErTyUi").build();
        FileInfo fi2 = FileInfo.builder().appid(appid).description("fi2").name("Erty").build();
        FileInfo fi3 = FileInfo.builder().appid(appid).description("fi3").name("ERTY").build();

        fi1 = fileInfoRepository.save(fi1);
        fi2 = fileInfoRepository.save(fi2);
        fi3 = fileInfoRepository.save(fi3);

        assertNotNull(fi1.getId());
        assertNotNull(fi2.getId());
        assertNotNull(fi3.getId());

        List<FileInfo> fis = fileInfoCustomizedRepository.findByAppidAndNameContainsIgnoreCase(appid, "ty", 0, 10000);

        assertEquals(3, fis.size());
    }
}
