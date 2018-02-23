/**
 * <p>
 * File Name: TranslationScheduler.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/23 下午4:18
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationRepository;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import alex.beta.onlinetranslation.services.TranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Component
public class TranslationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TranslationScheduler.class);

    private TranslationService translationService;

    private TranslationRepository translationRepository;

    @Autowired
    public TranslationScheduler(TranslationService translationService,
                                TranslationRepository translationRepository) {
        this.translationService = translationService;
        this.translationRepository = translationRepository;
    }

    /**
     * Find and translate un-proceeded 5 (or less) requests
     * Execute once every 2 seconds
     */
    @Async("translationJobExecutor")
    @Scheduled(fixedRate = 2000, initialDelay = 30000) // every 2 second, with initial delay 30 seconds
    public void executeTranslationJob() {
        if (translationService.lazyInitializeConnections()) {
            return;
        }
        try {
            Date filterDate = new Date();
            if (logger.isDebugEnabled()) {
                logger.debug("Translation job starts with filter {}.", filterDate);
            }
            List<Translation> requests = translationRepository.findFirst5ByStatusAndLastUpdatedOnLessThanOrderByLastUpdatedOnAsc(TranslationStatus.SUBMITTED, filterDate);
            if (requests == null || requests.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No pending request");
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Find {} translation request(s)", requests.size());
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Translation request(s):\n{}", requests.stream().map(Translation::<String>getUuid).collect(Collectors.joining(System.lineSeparator())));
                }
                for (Translation request : requests) {
                    try {
                        request.setStatus(TranslationStatus.PROCESSING);
                        request = translationService.updateTranslationRequest(request, 0);

                        translationService.performTranslation(request);
                    } catch (Exception ex) {//this should not happen
                        logger.error("Unexpected error when initiating async job for request {}", request.getUuid(), ex);
                        //continue processing next request
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to find first 5 unproceeded requests", ex);
        }
    }
}
