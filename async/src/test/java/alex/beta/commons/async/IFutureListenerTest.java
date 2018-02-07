/**
 * <p>
 * File Name: IFutureListenerTest.java
 * </p>
 * <p>
 * Project:   async
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018年1月29日 下午9:53:51
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
public class IFutureListenerTest {
    private BaseFuture<String> future;
    private IFutureListener<String> listener;
    private String initialStr;

    @Before
    public void setUp() {
        this.future = new BaseFuture<>();

        this.listener = new IFutureListener<String>() {
            @Override
            public void operationCompleted(IFuture<String> future) {
                initialStr += future.getNow();
            }
        };

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(150);
                    future.setSuccess("b");
                } catch (InterruptedException e) {
                    future.setFailure(e.getCause());
                }
            }
        }).start();
        this.initialStr += "c";
        assertNull(future.getNow());
        assertEquals("ac", initialStr);
        this.future.await();
        Thread.sleep(50);
        assertEquals("acb", initialStr);
    }
}
