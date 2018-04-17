package alex.beta.filerepository.services.impl;

import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.persistence.entity.Quota;
import alex.beta.filerepository.persistence.repository.FileInfoCustomizedRepository;
import alex.beta.filerepository.persistence.repository.QuotaRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

/**
 * Created by songlip on 2018/4/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class QuotaServiceImplTest {
    private static final String APPID = "FileRepositoryServiceImplTest";

    @Mock
    private QuotaRepository quotaRepository;

    @Mock
    private FileInfoCustomizedRepository fileInfoCustomizedRepository;

    @InjectMocks
    private QuotaServiceImpl quotaService;

    @Before
    public void setUp() {
        // Use RunWith annotation or initMocks in setUp
        // MockitoAnnotations.initMocks(this);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testGetUsedQuota() throws Exception {
        doReturn(null).when(quotaRepository).findOneByAppidIgnoreCase("abc");
        assertEquals(Long.MIN_VALUE, quotaService.getUsedQuota("abc"));

        Quota quota = Quota.builder().id("1").usedQuota(10).appid(APPID).build();
        doReturn(quota).when(quotaRepository).findOneByAppidIgnoreCase(APPID);
        assertEquals(10, quotaService.getUsedQuota(APPID));
    }

    @Test
    public void testGetMaxQuota() throws Exception {
        doReturn(null).when(quotaRepository).findOneByAppidIgnoreCase("abc");
        assertEquals(Long.MIN_VALUE, quotaService.getMaxQuota("abc"));

        Quota quota = Quota.builder().id("2").maxQuota(100).appid(APPID).build();
        doReturn(quota).when(quotaRepository).findOneByAppidIgnoreCase(APPID);
        assertEquals(100, quotaService.getMaxQuota(APPID));
    }

    @Test
    public void testUseQuota() throws Exception {
        Quota currentQuota = Quota.builder().id("3").usedQuota(10).maxQuota(100).appid(APPID).build();
        doReturn(currentQuota).when(quotaRepository).findOneOrCreateOneByAppidIgnoreCase(APPID);

        Quota updatedQuota = Quota.builder().id("3").usedQuota(60).maxQuota(100).appid(APPID).build();
        doReturn(updatedQuota).when(quotaRepository).findAndIncreaseUsedQuotaByAppidIgnoreCase(APPID, 50);

        quotaService.useQuota(APPID, 50);
    }

    @Test(expected = QuotaExceededException.class)
    public void testUseQuotaThrowsQuotaExceededException() throws Exception {
        Quota currentQuota = Quota.builder().id("4").usedQuota(10).maxQuota(100).appid(APPID).build();
        doReturn(currentQuota).when(quotaRepository).findOneOrCreateOneByAppidIgnoreCase(APPID);

        quotaService.useQuota(APPID, 91);
    }

    @Ignore
    @Test
    public void testRecalculateQuota() throws Exception {
        String appid1 = "a" + System.currentTimeMillis();
        String appid2 = "b" + System.currentTimeMillis();
        String appid3 = "c" + System.currentTimeMillis();

        Map<String, Long> returnedUsedQuotas = new HashMap<>(2);
        returnedUsedQuotas.put(appid1, 10L);
        returnedUsedQuotas.put(appid2, 20L);

        doReturn(returnedUsedQuotas).when(quotaRepository).aggregateUsedQuotaByAppidIgnoreCase(appid1, appid2, appid3);
        doAnswer(new DummyAnswer(appid1, appid2, appid3)).when(quotaRepository).findAndModifyUsedQuotaByAppidIgnoreCase(anyString(), anyLong());

        quotaService.recalculateQuota(appid1, appid2, appid3);
    }

    static class DummyAnswer implements Answer<Quota> {

        private String[] appids;

        DummyAnswer(String... appids) {
            this.appids = appids;
        }

        @Override
        public Quota answer(InvocationOnMock invocation) throws Throwable {
            System.out.println(invocation);
            Object[] args = invocation.getArguments();
            String appid = (String)args[0];
            long usedQuota = (long)args[1];
            if ((appids[0].equalsIgnoreCase(appid) && usedQuota == 10L) || (appids[1].equalsIgnoreCase(appid) && usedQuota == 20L) || (appids[2].equalsIgnoreCase(appid) && usedQuota == 0L)){
                return isA(Quota.class);
            } else {
                throw new RuntimeException();
            }
        }
    }
}
