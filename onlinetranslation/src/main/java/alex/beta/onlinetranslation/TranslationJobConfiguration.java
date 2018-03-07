/**
 * @File: TranslationJobConfiguration.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018/2/19 下午4:32
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
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
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

/**
 * @Description
 * @version ${project.version}
 */

@EnableAsync
@EnableScheduling
@Component
@EnableAutoConfiguration
public class TranslationJobConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(TranslationJobConfiguration.class);

    @Value("${TranslationJobConfiguration.numOfThreads:2}")
    private int numOfThreads;

    @Bean
    public Executor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

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