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

import alex.beta.onlinetranslation.persistence.Translation;

import javax.transaction.Transactional;

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
     * @return Persisted request
     */
    @Transactional
    Translation submit(String fromLanguage, String toLanguage, String text);

    /**
     * Update the translation request, such as the status, last updated timestamp, etc
     *
     * @param request
     * @param flush
     * @return
     */
    @Transactional
    Translation updateTranslationRequest(Translation request, boolean flush);

    /**
     * Get translation request according to given uuid
     *
     * @param uuid
     * @return
     */
    Translation getTranslation(String uuid);

    /**
     * Find and translate unproceeded 5 requests
     * Execute once every 1 second
     */
    void executeTranslationJob();

    /**
     * Perform translation using Baidu fanyi API
     *
     * @param request
     */
    @Transactional
    void performTranslation(Translation request);
}
