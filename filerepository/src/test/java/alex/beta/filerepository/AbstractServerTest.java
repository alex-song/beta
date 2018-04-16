/**
 * @File: AbstractServerTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/9 下午10:25
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @version ${project.version}
 * @Description
 */

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@TestPropertySource(properties = "frs.config=classpath:frs-config-test.xml")
public abstract class AbstractServerTest {

    protected static final Logger logger = LoggerFactory.getLogger(FrsConfigResolverTest.class);

    public void await(int timeout, TimeUnit unit) throws InterruptedException {
        unit.sleep(timeout);
    }

    public void awaitOneMillisecond() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1);
    }
}
