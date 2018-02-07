/**
 * <p>
 * File Name: AbstractFutureTest.java
 * </p>
 * <p>
 * Project:   async
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018年1月28日 下午12:02:04
 * </p>
 */

package alex.beta.commons.async;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author alexsong
 */
public class BaseFutureTest {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    initialStr1 += "b";
                    future.setSuccess(initialStr1);
                } catch (InterruptedException e) {
                    future.setFailure(e.getCause());
                }
            }
        }).start();
        this.initialStr1 += "c";
        assertNull(future.getNow());
        assertEquals("ac", initialStr1);
        this.future.await();
        assertEquals("acb", initialStr1);
    }
}
