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

import junit.framework.TestCase;

/**
 * @author  alexsong
 * @version 
 */
public class IFutureListenerTest extends TestCase {
	private BaseFuture<String> future;
	private IFutureListener<String> listener;
	private String initialStr;
	
	@Override
	@Before
	protected void setUp() {
		this.future = new BaseFuture<String>();
		
		this.listener = new IFutureListener<String>() {
			@Override
			public void operationCompleted(IFuture<String> future) throws Exception {
				initialStr += future.getNow();
			}
		};
		
		this.future.addListener(listener);
		
		this.initialStr = "a";
	}

	@Override
	@After
	protected void tearDown() {
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
					Thread.sleep(1000);
					initialStr += "b";
					future.setSuccess(initialStr);
				} catch (InterruptedException e) {
					future.setFailure(e.getCause());
				}
			}
		}).start();
		this.initialStr += "c";
		assertNull(future.getNow());
		assertEquals("ac", initialStr);
		this.future.await();
		assertEquals("acb", initialStr);
	}
}
