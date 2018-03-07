/**
 * @File: TranslationScheduler.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/23 下午4:18
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.services;

import alex.beta.onlinetranslation.persistence.TranslationEntity;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import alex.beta.onlinetranslation.services.impl.ConnectionManagerHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @Description
 * @version ${project.version}
 */
@Component
@ConditionalOnProperty(value = "TranslationJobConfiguration.enableTranslationJob", havingValue = "true")
public class TranslationScheduler {
    private static final Logger logger = LoggerFactory.getLogger(TranslationScheduler.class);

    private InternalTranslationService translationService;

    private ConnectionManagerHolder connectionManagerHolder;

    @Value("${TranslationJobConfiguration.enableTranslationJob:true}")
    private boolean enableTranslationJob;

    @Autowired
    public TranslationScheduler(InternalTranslationService translationService, ConnectionManagerHolder connectionManagerHolder) {
        this.translationService = translationService;
        this.connectionManagerHolder = connectionManagerHolder;
        if (logger.isWarnEnabled()) {
            logger.warn("Translation job is enabled.");
        }
    }

    /**
     * Find and translate un-proceeded 3 (or less) requests
     * Execute once every 2 seconds
     */
    @Async
    @Transactional
    @Scheduled(fixedRate = 2000, initialDelay = 30000) // every 2 second, with initial delay 30 seconds
    public void executeTranslationJob() {
        if (!enableTranslationJob) {
            return;
        }
        if (connectionManagerHolder == null || connectionManagerHolder.getConnectionManager() == null) {
            return;
        }
        try {
            List<TranslationEntity> requests = translationService.findRequestsToTranslate();
            if (requests != null && !requests.isEmpty()) {
                for (TranslationEntity request : requests) {
                    performTranslation(request);
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to find un-proceeded request.", ex);
        }
    }

    private void performTranslation(TranslationEntity request) {
        try {
            request.setStatus(TranslationStatus.PROCESSING);
            request.setLastUpdatedOn(null);
            request = translationService.updateTranslationRequest(request);

            translationService.asyncPerformTranslation(request);
        } catch (Exception ex) {//this should not happen
            logger.error("Unexpected error when initiating async job for request {}.", request.getUuid(), ex);
            //continue processing next request
        }
    }
}
