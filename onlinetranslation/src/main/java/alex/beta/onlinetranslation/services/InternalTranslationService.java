/**
 * <p>
 * File Name: InternalTranslationService.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/27 下午10:38
 * </p>
 */
package alex.beta.onlinetranslation.services;

import alex.beta.onlinetranslation.persistence.TranslationEntity;

import java.util.List;

/**
 * This service is for internal use only, won't expose to controller or 3rd party.
 *
 * @author alexsong
 * @version ${project.version}
 */
public interface InternalTranslationService extends TranslationService {
    /**
     * Update the translation request, such as the status, last updated timestamp, etc
     * <p>
     * For application internal use only
     *
     * @param request Set current datetime as the lastUpdatedOn, if the value is null in the input request
     * @return
     */
    TranslationEntity updateTranslationRequest(TranslationEntity request);

    /**
     * Find top 3 requests to translate, whose timestamp(lastUpdatedOn) before current time
     *
     * @return
     */
    List<TranslationEntity> findRequestsToTranslate();

    /**
     * Perform translation asynchronized. Overwrite the lastUpdatedOn automatically.
     * This method returns immediately after sending the request, and client needs to call {@link TranslationService#getTranslation(String)} to get the result late.
     *
     * @param request
     */
    void asyncPerformTranslation(TranslationEntity request);

    /**
     * Perform translation and return the result. Overwrite the lastUpdatedOn automatically.
     *
     * @param request
     * @return
     */
    TranslationEntity performTranslation(TranslationEntity request);

    /**
     * Remove translation request, which is older than 24 hours
     */
    void performHousekeeping();
}
