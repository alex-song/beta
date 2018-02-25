/**
 * <p>
 * File Name: TranslationService.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午2:57
 * </p>
 */
package alex.beta.onlinetranslation.services;

import alex.beta.onlinetranslation.models.TranslationModel;
import alex.beta.onlinetranslation.persistence.TranslationEntity;

import java.util.List;

/**
 * @author alexsong
 * @version ${project.version}
 */
public interface TranslationService {
    /**
     * Create a new transaltion request
     *
     * @param fromLanguage
     * @param toLanguage
     * @param text
     * @return Persisted translation request
     */
    TranslationModel submit(String fromLanguage, String toLanguage, String text);

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
     * Get translation result according to given uuid
     *
     * @param uuid
     * @return
     */
    TranslationModel getTranslation(String uuid);

    /**
     * Find top 3 requests to translate, whose timestamp(lastUpdatedOn) before current time
     *
     * @return
     */
    List<TranslationEntity> findRequestsToTranslate();

    /**
     * Perform translation. Overwrite the lastUpdatedOn automatically.
     *
     * @param request
     */
    void performTranslation(TranslationEntity request);

    /**
     * Remove translation request, which is older than 24 hours
     */
    void performHousekeeping();
}
