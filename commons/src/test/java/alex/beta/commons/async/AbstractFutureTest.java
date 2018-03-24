/**
 * @File: AbstractFutureTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/24 下午3:26
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.commons.async;

import java.util.concurrent.TimeUnit;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractFutureTest {
    protected void delay(TimeUnit timeunit, long delay) throws InterruptedException {
        timeunit.sleep(delay);
    }

    protected void delayInMilliSeconds(long delay) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(delay);
    }
}
