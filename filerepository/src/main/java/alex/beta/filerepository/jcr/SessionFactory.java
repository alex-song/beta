/**
 * @File: SessionFactory.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/28 下午3:36
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.jcr;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * @version ${project.version}
 * @Description
 */
public interface SessionFactory {
    /**
     * Returns a JCR Session using the credentials and workspace on this JcrSessionFactory.
     * The session factory doesn't allow specification of a different workspace name because:
     * <p>
     * " Each Session object is associated one-to-one with a Workspace object. The Workspace
     * object represents a `view` of an actual repository workspace entity as seen through
     * the authorization settings of its associated Session." (quote from javax.jcr.Session javadoc).
     * </p>
     *
     * @return the JCR session.
     * @throws RepositoryException
     */
    public Session getSession() throws RepositoryException;

    /**
     * Returns a specific SessionHolder for the given Session. The holder provider is used
     * internally by the framework in components such as transaction managers to provide
     * implementation specific information such as transactional support (if it is available).
     *
     * @return specific sessionHolder.
     */
    public SessionHolder getSessionHolder(Session session);
}
