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
package alex.beta.onlinetranslation.services;

import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import alex.beta.onlinetranslation.services.impl.ConnectionManagerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Component
public class TranslationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TranslationScheduler.class);

    private TranslationService translationService;

    private ConnectionManagerHolder connectionManagerHolder;

    @Autowired
    public TranslationScheduler(TranslationService translationService, ConnectionManagerHolder connectionManagerHolder) {
        this.translationService = translationService;
        this.connectionManagerHolder = connectionManagerHolder;
    }

    /**
     * Find and translate un-proceeded 5 (or less) requests
     * Execute once every 2 seconds
     */
    @Async("translationJobExecutor")
    @Scheduled(fixedRate = 2000, initialDelay = 30000) // every 2 second, with initial delay 30 seconds
    public void executeTranslationJob() {
        if (connectionManagerHolder == null || connectionManagerHolder.getConnectionManager() == null) {
            return;
        }
        try {
            List<Translation> requests = translationService.findRequestsToTranslate();
            if (requests != null && !requests.isEmpty()) {
                for (Translation request : requests) {
                    performTranslation(request);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to find first 5 unproceeded requests", ex);
        }
    }

    private void performTranslation(Translation request) {
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
