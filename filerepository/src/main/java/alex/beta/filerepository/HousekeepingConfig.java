/**
 * @File: HousekeepingConfig.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/14 下午9:29
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextScheduledExecutorService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.Executors;

import static alex.beta.filerepository.SecurityConfig.ROLE_FRS_ADMIN;
import static alex.beta.filerepository.SecurityConfig.ROLE_PREFIX;

/**
 * @version ${project.version}
 * @Description
 */

@EnableAsync
@EnableScheduling
@Component
@ConditionalOnProperty(value = "frs.housekeeping.enabled", havingValue = "true")
public class HousekeepingConfig implements SchedulingConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(HousekeepingConfig.class);

    @Value("${frs.housekeeping.numOfThreads:2}")
    private int numOfThreads;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        if (logger.isInfoEnabled()) {
            logger.info("Initiate housekeeping executor for file repository service, poolSize : {}", numOfThreads);
        }
        taskRegistrar.setScheduler(
                new DelegatingSecurityContextScheduledExecutorService(
                        Executors.newScheduledThreadPool(numOfThreads),
                        createSchedulerSecurityContext()));
    }

    private SecurityContext createSchedulerSecurityContext() {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(
                "housekeeping",
                "",
                Arrays.asList((GrantedAuthority) () -> ROLE_PREFIX + ROLE_FRS_ADMIN)
        ));
        return context;
    }
}
