/**
 * @File:      IFuture.java
 * @Project:   commons
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @Date:      2018年1月28日 上午11:31:21
 * @author:    <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.commons.async;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @version ${project.version}
 */
public interface IFuture<V> extends Future<V> {
	/**
	 * 是否成功
	 * 
	 * @return
	 */
    boolean isSuccess();

	/**
	 * 是否可以取消
	 * 
	 * @return
	 */
    boolean isCancellable();

	/**
	 * 立即返回结果(不管Future是否处于完成状态)
	 * 
	 * @return
	 */
    V getNow();

	/**
	 * 若执行失败时的原因
	 * 
	 * @return
	 */
    Throwable cause();

	/**
	 * 等待future的完成
	 * 
	 * @return
	 * @throws InterruptedException
	 */
    IFuture<V> await() throws InterruptedException;

	boolean await(long timeoutMillis) throws InterruptedException;

	boolean await(long timeout, TimeUnit timeunit) throws InterruptedException;

	/**
	 * 等待future的完成，不响应中断
	 * 
	 * @return
	 */
    IFuture<V> awaitUninterruptibly();

	/**
	 * 
	 * @param timeoutMillis
	 * @return true, 如果在超时前完成
	 */
    boolean awaitUninterruptibly(long timeoutMillis);

	boolean awaitUninterruptibly(long timeout, TimeUnit timeunit);

	/**
	 * 当future完成时，会通知这些加进来的监听器
	 * 
	 * @param l
	 * @return
	 */
    IFuture<V> addListener(IFutureListener<V> l);

	IFuture<V> removeListener(IFutureListener<V> l);
}
