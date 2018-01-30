/**
 * <p>
 * File Name: IFutureListener.java
 * </p>
 * <p>
 * Project:   async
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018年1月28日 上午11:28:26
 * </p>
 */

package alex.beta.commons.async;

/**
 * @author alexsong
 * @version
 */
public interface IFutureListener<V> {
	/**
	 * 
	 * @param future
	 * @throws Exception
	 */
	public void operationCompleted(IFuture<V> future) throws Exception;
}
