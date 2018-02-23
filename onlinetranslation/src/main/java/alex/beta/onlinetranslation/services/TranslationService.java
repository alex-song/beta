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

import alex.beta.onlinetranslation.models.TranslationResult;
import alex.beta.onlinetranslation.persistence.Translation;

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
    TranslationResult submit(String fromLanguage, String toLanguage, String text);

    /**
     * Update the translation request, such as the status, last updated timestamp, etc
     * <p>
     * For application internal use only
     *
     * @param request
     * @param delay   lastUpdatedOn = current timestamp + delay (in ms)
     * @return
     */
    Translation updateTranslationRequest(Translation request, long delay);

    /**
     * Get translation result according to given uuid
     *
     * @param uuid
     * @return
     */
    TranslationResult getTranslation(String uuid);

    /**
     * Find top 5 requests to translate, whose timestamp(lastUpdatedOn) before current time
     *
     * @return
     */
    List<Translation> findRequestsToTranslate();

    /**
     * Perform translation using Baidu fanyi API
     *
     * @param request
     */
    void performTranslation(Translation request);

    /**
     * Remove translation request, which is older than 24 hours
     * Execute once every 12 hours
     */
    void performHousekeeping();
}
