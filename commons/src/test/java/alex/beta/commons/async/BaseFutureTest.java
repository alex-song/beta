/**
 * @File: AbstractFutureTest.java
 * @Project: commons
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018年1月28日 下午12:02:04
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.commons.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version ${project.version}
 * @Description
 */
public class BaseFutureTest extends AbstractFutureTest {
    private BaseFuture<String> future;
    private String initialStr1;

    @Before
    public void setUp() {
        this.future = new BaseFuture<>();
        this.initialStr1 = "";
    }

    @After
    public void tearDown() {
        this.future = null;
        this.initialStr1 = null;
    }

    @Test
    public void testGetNow() throws Exception {
        this.initialStr1 += "a";
        new Thread(() -> {
            try {
                delayInMilliSeconds(200);
                initialStr1 += "b";
                future.setSuccess(initialStr1);
            } catch (InterruptedException e) {
                future.setFailure(e.getCause());
            }
        }).start();
        this.initialStr1 += "c";
        assertNull(future.getNow());
        assertEquals("ac", initialStr1);
        this.future.await();
        assertEquals("acb", initialStr1);
    }
}
