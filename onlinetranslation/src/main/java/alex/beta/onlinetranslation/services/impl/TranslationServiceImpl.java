/**
 * @File: TranslationServiceImpl.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018/2/19 下午3:08
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.models.TranslationModel;
import alex.beta.onlinetranslation.persistence.TranslationEntity;
import alex.beta.onlinetranslation.persistence.TranslationRepository;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import alex.beta.onlinetranslation.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Objects;

import static alex.beta.onlinetranslation.persistence.TranslationEntity.TEXT_MAXLENGTH;

/**
 * @version ${project.version}
 * @Description
 */

@Service("translationService")
public class TranslationServiceImpl implements TranslationService {

    protected TranslationRepository translationRepository;

    @Autowired
    public TranslationServiceImpl(TranslationRepository translationRepository) {
        this.translationRepository = translationRepository;
    }

    @Override
    @Transactional
    public TranslationModel submit(String fromLanguage, String toLanguage, String text) {
        Objects.requireNonNull(text);

        return new TranslationModel(translationRepository.saveAndFlush(
                new TranslationEntity(TranslationStatus.SUBMITTED,
                        fromLanguage == null ? "auto" : fromLanguage,
                        toLanguage,
                        text.length() > TEXT_MAXLENGTH ?
                                text.substring(0, TEXT_MAXLENGTH) : text))
        );
    }

    @Override
    public TranslationModel getTranslation(String uuid) {
        Objects.requireNonNull(uuid);

        TranslationEntity tmp = translationRepository.findOne(uuid);
        return tmp == null ? null : new TranslationModel(tmp);
    }
}