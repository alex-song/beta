/**
 * <p>
 * File Name: IFutureListener.java
 * </p>
 * <p>
 * Project:   commons
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
	 */
    void operationCompleted(IFuture<V> future);
}
