/**
 * @File: SessionFactory.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/28 22:14
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.jcr;

import org.apache.commons.collections.map.ReferenceMap;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.SessionImpl;
import org.apache.jackrabbit.core.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.util.Map;

/**
 * @version ${project.version}
 * @Description
 */

@Component
@EnableAutoConfiguration
public class SessionFactory implements SessionListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionFactory.class);

    private RepositoryImpl frsRepository;

    private final Map<Session, Session> sessions;

    @Autowired
    public SessionFactory(RepositoryImpl frsRepository) {
        this.frsRepository = frsRepository;
        this.sessions = new ReferenceMap(2, 2);
    }

    public synchronized Session login(@Nonnull String userID, @Nonnull char[] password, @Nonnull String workspaceName) throws RepositoryException {
        SessionImpl session;
        try {
            logger.debug("Opening a new session for {}", userID);
            session = (SessionImpl) this.frsRepository.login(new SimpleCredentials(userID, password), workspaceName);
            this.sessions.put(session, session);
            session.addListener(this);
            logger.info("Session opened for {}", userID);
        } finally {
            if (this.sessions.isEmpty()) {
                logger.warn("Login failed for {}", userID);
            }
        }
        return session;
    }

    @Override
    public void loggingOut(SessionImpl session) {
        //do nothing
    }

    @Override
    public synchronized void loggedOut(SessionImpl session) {
        assert this.sessions.containsKey(session);

        this.sessions.remove(session);
        logger.info("Session closed");
    }

    @PreDestroy
    public synchronized void destroy() throws Exception {
        Session[] copy = sessions.keySet().toArray(new Session[0]);
        for (Session session : copy) {
            session.logout();
        }

        if (this.frsRepository != null) {
            this.frsRepository.shutdown();
            logger.info("frsRepository shut down");
        }
        frsRepository = null;
    }

    public Repository getRepository() {
        return this.frsRepository;
    }
}
