/**
 * @File: FrsConfigResolverTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/9 下午10:09
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.config.xmlbeans.AbstractApp;
import alex.beta.filerepository.config.xmlbeans.AbstractUser;
import alex.beta.filerepository.config.xmlbeans.IFrsConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @version ${project.version}
 * @Description
 */
public class FrsConfigResolverTest {

    private IFrsConfig frsConfig;

    @Before
    public void setUp() {
        FrsConfigResolver resolver = new FrsConfigResolver();
        resolver.setDefaultConfig("classpath:frs-config-test.xml");
        frsConfig = resolver.getFrsConfig();
    }

    @After
    public void tearDown() {
        frsConfig = null;
    }

    @Test
    public void testGetFrsConfig() throws Exception {
        assertNotNull(frsConfig);

        assertEquals(1, frsConfig.getAdmin().size());
        assertEquals(0, frsConfig.getGuest().size());
        assertEquals(0, frsConfig.getOperator().size());
        assertEquals(2, frsConfig.getApp().size());

        AbstractUser admin = frsConfig.getAdmin().get(0);
        assertEquals("test", admin.getUsername());
        assertEquals("test", admin.getPassword());

        AbstractApp app = frsConfig.getApp().get(0);
        assertEquals("default", app.getAppid());
        assertEquals(50 * 1024L * 1024L, app.getMaxQuotaValue());
    }
}
