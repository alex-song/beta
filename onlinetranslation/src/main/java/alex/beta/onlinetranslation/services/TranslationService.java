/**
 * @File: TranslationService.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/19 下午2:57
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.services;

import alex.beta.onlinetranslation.models.TranslationModel;

/**
 * @Description
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
