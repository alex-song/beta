/**
 * <p>
 * File Name: HousekeepingRepositoryImpl.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/22 上午9:42
 * </p>
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
 * @author alexsong
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
