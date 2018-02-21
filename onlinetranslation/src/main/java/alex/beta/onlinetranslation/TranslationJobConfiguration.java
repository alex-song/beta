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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
    @Value("${TranslationJobConfiguration.poolSize:2}")
    private int poolSize;

    @Bean(name = "translationJobExecutor")
    public AsyncTaskExecutor translationJobExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(poolSize * 10);
        executor.setThreadNamePrefix("TranslationJob-");
        executor.initialize();
        return new SimpleAsyncTaskExecutor(executor);
    }
}
