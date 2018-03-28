/**
 * @File: SessionHolder.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/28 下午3:39
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.jcr;

import org.springframework.transaction.support.ResourceHolderSupport;

import javax.jcr.Session;

/**
 * @version ${project.version}
 * @Description
 */
public class SessionHolder extends ResourceHolderSupport {

    private Session session;

    public SessionHolder(Session session) {
        setSession(session);
    }

    protected void setSession(Session session) {
        this.session = session;
    }

    public Session getSession() {
        return session;
    }

    /**
     * @see org.springframework.transaction.support.ResourceHolderSupport#clear()
     */
    public void clear() {
        super.clear();
        session = null;
    }
}