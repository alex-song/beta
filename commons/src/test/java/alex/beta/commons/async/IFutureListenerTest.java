/**
 * @File: IFutureListenerTest.java
 * @Project: commons
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018年1月29日 下午9:53:51
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
public class IFutureListenerTest extends AbstractFutureTest {
    private BaseFuture<String> future;
    private IFutureListener<String> listener;
    private String initialStr;

    @Before
    public void setUp() {
        this.future = new BaseFuture<>();

        this.listener = future -> initialStr += future.getNow();

        this.future.addListener(listener);

        this.initialStr = "a";
    }

    @After
    public void tearDown() {
        this.future.removeListener(listener);
        this.listener = null;
        this.future = null;
        this.initialStr = null;
    }

    @Test
    public void testOperationCompleted() throws Exception {
        new Thread(() -> {
            try {
                delayInMilliSeconds(150);
                future.setSuccess("b");
            } catch (InterruptedException e) {
                future.setFailure(e.getCause());
            }
        }).start();
        this.initialStr += "c";
        assertNull(future.getNow());
        assertEquals("ac", initialStr);
        this.future.await();
        delayInMilliSeconds(50);
        assertEquals("acb", initialStr);
    }
}
