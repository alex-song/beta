/**
 * <p>
 * File Name: TranslationServiceImpl.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午3:08
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationRepository;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import alex.beta.onlinetranslation.services.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Service
public class TranslationServiceImpl implements TranslationService {

    @Autowired
    private TranslationRepository repository;

    public Translation submit(String fromLanguage, String toLanguage, String text) {
        String sl = fromLanguage == null ? "auto" : fromLanguage;
        return repository.saveAndFlush(new Translation(TranslationStatus.SUBMITTED, sl, toLanguage, text));
    }

    public Translation getOldestUnprocessedRequest() {
        return repository.findFirstByStatusOrderByLastUpdatedOnAsc(TranslationStatus.SUBMITTED);
    }

    public Translation updateTranslationRequest(Translation request) {
        Objects.requireNonNull(request);

        Translation persistedT = repository.findOne(request.getUuid());
        if (persistedT == null) {
            return null;
        }

        if (request.getFromLanguage() != null) {
            persistedT.setFromLanguage(request.getFromLanguage());
        }
        if (request.getToLanguage() != null) {
            persistedT.setToLanguage(request.getToLanguage());
        }
        if (request.getMessage() != null) {
            persistedT.setMessage(request.getMessage());
        }
        if (request.getStatus() != null) {
            persistedT.setStatus(request.getStatus());
        }
        if (request.getText() != null) {
            persistedT.setText(request.getText());
        }
        if (request.getTranslatedText() != null) {
            persistedT.setTranslatedText(request.getTranslatedText());
        }
        persistedT.setLastUpdatedOn(new Date());

        return repository.saveAndFlush(persistedT);
    }

    public Translation getTranslation(String uuid) {
        Objects.requireNonNull(uuid);

        return repository.findOne(uuid);
    }
}
