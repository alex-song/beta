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
     * Get translation result according to given uuid
     *
     * @param uuid
     * @return
     */
    TranslationModel getTranslation(String uuid);
}
