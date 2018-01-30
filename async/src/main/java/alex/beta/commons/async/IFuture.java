/**
 * <p>
 * File Name: IFuture.java
 * </p>
 * <p>
 * Project:   async
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018年1月28日 上午11:31:21
 * </p>
 */

package alex.beta.commons.async;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author alexsong
 * @version
 */
public interface IFuture<V> extends Future<V> {
	/**
	 * 是否成功
	 * 
	 * @return
	 */
	public boolean isSuccess();

	/**
	 * 是否可以取消
	 * 
	 * @return
	 */
	public boolean isCancellable();

	/**
	 * 立即返回结果(不管Future是否处于完成状态)
	 * 
	 * @return
	 */
	public V getNow();

	/**
	 * 若执行失败时的原因
	 * 
	 * @return
	 */
	public Throwable cause();

	/**
	 * 等待future的完成
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public IFuture<V> await() throws InterruptedException;

	public boolean await(long timeoutMillis) throws InterruptedException;

	public boolean await(long timeout, TimeUnit timeunit) throws InterruptedException;

	/**
	 * 等待future的完成，不响应中断
	 * 
	 * @return
	 */
	public IFuture<V> awaitUninterruptibly();

	/**
	 * 
	 * @param timeoutMillis
	 * @return true, 如果在超时前完成
	 */
	public boolean awaitUninterruptibly(long timeoutMillis);

	public boolean awaitUninterruptibly(long timeout, TimeUnit timeunit);

	/**
	 * 当future完成时，会通知这些加进来的监听器
	 * 
	 * @param l
	 * @return
	 */
	public IFuture<V> addListener(IFutureListener<V> l);

	public IFuture<V> removeListener(IFutureListener<V> l);
}
