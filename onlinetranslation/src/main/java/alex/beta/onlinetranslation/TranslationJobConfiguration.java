/**
 * <p>
 * File Name: TranslationJobConfiguration.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午4:32
 * </p>
 */
package alex.beta.onlinetranslation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

/**
 * @author alexsong
 * @version ${project.version}
 */

@Component
@EnableAutoConfiguration
public class TranslationJobConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TranslationJobConfiguration.class);

    @Value("${TranslationJobConfiguration.numOfThreads:2}")
    private int numOfThreads;

    @Bean(name = "translationJobExecutor")
    @ConditionalOnProperty(value = "TranslationJobConfiguration.enableTranslationJob", havingValue = "true")
    public AsyncTaskExecutor translationJobExecutor() {
        if (logger.isInfoEnabled()) {
            logger.info("Initiate ThreadPoolTaskExecutor for translation, poolSize : {}, queueCapacity : {}",
                    numOfThreads, numOfThreads * 20);
        }

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(numOfThreads);
        executor.setMaxPoolSize(numOfThreads);
        executor.setQueueCapacity(numOfThreads * 20);
        executor.setThreadNamePrefix("TranslationJob-");
        executor.initialize();
        return new SimpleAsyncTaskExecutor(executor);
    }

    @Bean(name = "housekeepingJobExecutor")
    @ConditionalOnProperty(value = "TranslationJobConfiguration.enableHousekeepingJob", havingValue = "true")
    public AsyncTaskExecutor housekeepingJobExecutor() {
        if (logger.isInfoEnabled()) {
            logger.info("Initiate ThreadPoolTaskExecutor for housekeeping");
        }

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(5);
        executor.setThreadNamePrefix("Housekeeping-");
        executor.initialize();
        return new SimpleAsyncTaskExecutor(executor);
    }
}