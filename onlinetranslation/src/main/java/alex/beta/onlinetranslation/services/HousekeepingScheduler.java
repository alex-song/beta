/**
 * <p>
 * File Name: HousekeepingScheduler.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/24 下午9:53
 * </p>
 */
package alex.beta.onlinetranslation.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Component
@ConditionalOnProperty(value = "TranslationJobConfiguration.enableHousekeepingJob", havingValue = "true")
public class HousekeepingScheduler {
    private static final Logger logger = LoggerFactory.getLogger(HousekeepingScheduler.class);

    private InternalTranslationService translationService;

    @Value("${TranslationJobConfiguration.enableHousekeepingJob:true}")
    private boolean enableHousekeepingJob;

    @Autowired
    public HousekeepingScheduler(InternalTranslationService translationService) {
        this.translationService = translationService;
        if (logger.isWarnEnabled()) {
            logger.warn("Housekeeping job is enabled.");
        }
    }

    /**
     * Trigger housekeeping job, every 12 hours, with initial delay 25 seconds
     */
    @Async("housekeepingJobExecutor")
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000L, initialDelay = 25000L)
    public void executeHousekeepingJob() {
        if (!enableHousekeepingJob) {
            return;
        }
        translationService.performHousekeeping();
    }
}
