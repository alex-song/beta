/**
 * @File: QuotaRepositoryImplTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/17 下午9:28
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.AbstractServerTest;
import alex.beta.filerepository.config.xmlbeans.IFrsConfig;
import alex.beta.filerepository.persistence.entity.Quota;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.regex.Pattern;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * @Description
 * @version ${project.version}
 */
public class QuotaRepositoryImplTest extends AbstractServerTest {

    @Mock
    private MongoOperations mongoOperations;

    @Spy
    @Autowired
    private IFrsConfig frsConfig;

    @InjectMocks
    private QuotaRepositoryImpl quotaRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
    }


    @Test
    public void testFindOneOrCreateOneByAppidIgnoreCase1() throws Exception {
        String appid = "abc";
        doReturn(null).when(mongoOperations).findOne(
                new Query(Criteria.where(QuotaRepositoryImpl.APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))), Quota.class);

        Quota q = quotaRepository.findOneOrCreateOneByAppidIgnoreCase(appid);
        verify(mongoOperations, times(1)).insert(Matchers.any(Quota.class), eq(QuotaRepositoryImpl.QUOTA_COLLECTION_NAME));
        assertNull(q);
    }

    @Test
    public void testFindOneOrCreateOneByAppidIgnoreCase2() throws Exception {
        String appid = "def";
        Quota existingQ = Quota.builder().appid(appid).id("1").maxQuota(500).usedQuota(100).build();

        doReturn(existingQ).when(mongoOperations).findOne(
                new Query(Criteria.where(QuotaRepositoryImpl.APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))), Quota.class);

        Quota q = quotaRepository.findOneOrCreateOneByAppidIgnoreCase(appid);
        verify(mongoOperations, never()).insert(Matchers.any(Quota.class), eq(QuotaRepositoryImpl.QUOTA_COLLECTION_NAME));
        assertEquals(existingQ, q);
    }

    @Test
    public void testFindOneOrCreateOne1() throws Exception {
        String appid = "ghi";
        Quota quota = Quota.builder().appid(appid).maxQuota(1234).build();
        doReturn(null).when(mongoOperations).findOne(
                new Query(Criteria.where(QuotaRepositoryImpl.APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))), Quota.class);

        Quota q = quotaRepository.findOneOrCreateOne(quota);
        verify(mongoOperations, times(1)).insert(Matchers.any(Quota.class), eq(QuotaRepositoryImpl.QUOTA_COLLECTION_NAME));
        assertNull(q);
    }

    @Test
    public void testFindOneOrCreateOne2() throws Exception {
        String appid = "jkl";
        Quota quota = Quota.builder().appid(appid).maxQuota(1234).build();
        Quota existingQ = Quota.builder().appid(appid).id("2").maxQuota(1234).usedQuota(100).build();

        doReturn(existingQ).when(mongoOperations).findOne(
                new Query(Criteria.where(QuotaRepositoryImpl.APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))), Quota.class);

        Quota q = quotaRepository.findOneOrCreateOne(quota);
        verify(mongoOperations, never()).insert(Matchers.any(Quota.class), eq(QuotaRepositoryImpl.QUOTA_COLLECTION_NAME));
        assertEquals(existingQ, q);
    }

    //TODO maxQuota is configured, but gets overwritten
    //TODO getDefaultMaxQuota
}
