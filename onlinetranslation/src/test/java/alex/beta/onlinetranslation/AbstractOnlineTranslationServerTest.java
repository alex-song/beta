/**
 * @File: AbstractOnlineTranslationServerTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/26 上午8:16
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @version ${project.version}
 */

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public abstract class AbstractOnlineTranslationServerTest {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractOnlineTranslationServerTest.class);

    public void await(int timeout, TimeUnit unit) throws InterruptedException {
        unit.sleep(timeout);
    }

    public void awaitOneMillisecond() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(1);
    }
}
