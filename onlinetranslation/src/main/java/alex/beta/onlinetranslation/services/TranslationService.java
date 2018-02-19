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
     * Get the oldest unprocessed translation request
     *
     * @return null, if all translation requests are proceeded or being proceeded
     */
    Translation getOldestUnprocessedRequest();

    /**
     * Update the translation request, such as the status, last updated timestamp, etc
     *
     * @param request
     * @return
     */
    @Transactional
    Translation updateTranslationRequest(Translation request);

    /**
     * Get translation request according to given uuid
     *
     * @param uuid
     * @return
     */
    Translation getTranslation(String uuid);
}
