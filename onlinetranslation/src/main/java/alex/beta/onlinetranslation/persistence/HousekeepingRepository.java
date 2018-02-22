/**
 * <p>
 * File Name: HousekeepingRepository.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/22 上午9:28
 * </p>
 */
package alex.beta.onlinetranslation.persistence;

/**
 * @author alexsong
 * @version ${project.version}
 */
public interface HousekeepingRepository {
    /**
     * Delete translation requests, whose lastUpdatedOn and createdOn is before this timestamp
     *
     * @param timestamp
     * @return number of requests that are deleted
     */
    int removeExpiredTranslationRequests(long timestamp);
}
