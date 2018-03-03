/**
 * @File:      HousekeepingRepositoryImpl.java
 * @Project:   onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @Date:      2018/2/22 上午9:42
 * @author:    <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.Date;

/**
 * @Description
 * @version ${project.version}
 */
@Repository
public class HousekeepingRepositoryImpl implements HousekeepingRepository {
    private static final Logger logger = LoggerFactory.getLogger(HousekeepingRepositoryImpl.class);

    private EntityManager entityManager;

    @Autowired
    public HousekeepingRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public int removeExpiredTranslationRequests(long timestamp) {
        if (logger.isDebugEnabled()) {
            logger.debug("Before {}.", timestamp);
        }
        Date date = new Date(timestamp);
        String deleteTranslationLinesSql = "DELETE FROM Translation_Line WHERE translation_uuid IN (SELECT t.uuid FROM Translation t WHERE t.last_Updated_On < :lastUpdatedOn)";
        int deleteCount = entityManager.createNativeQuery(deleteTranslationLinesSql).
                setParameter("lastUpdatedOn", date).executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("Delete {} translation lines that is older than {}", deleteCount, date);
        }

        String deleteRequestsSql = "DELETE FROM Translation WHERE last_Updated_On < :lastUpdatedOn";
        deleteCount = entityManager.createNativeQuery(deleteRequestsSql).
                setParameter("lastUpdatedOn", date).executeUpdate();
        if (logger.isDebugEnabled()) {
            logger.debug("Delete {} requests that is older than {}", deleteCount, date);
        }
        return deleteCount;
    }
}
