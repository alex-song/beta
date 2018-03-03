/**
 * @File:      IFutureListener.java
 * @Project:   commons
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @Date:      2018年1月28日 上午11:28:26
 * @author:    <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.commons.async;

/**
 * @Description
 * @version ${project.version}
 */
public interface IFutureListener<V> {
	/**
	 * 
	 * @param future
	 */
    void operationCompleted(IFuture<V> future);
}
