/**
 * @File: MethodSecurityConfig.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/15 上午10:27
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.security.FrsRunAsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.intercept.RunAsManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * @version ${project.version}
 * @Description
 */

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(MethodSecurityConfig.class);

    @Override
    protected RunAsManager runAsManager() {
        if (logger.isInfoEnabled()) {
            logger.info("RunAsManager FrsRunAsManager");
        }

        FrsRunAsManager runAsManager = new FrsRunAsManager();
        runAsManager.setKey(SecurityConfig.RUN_AS_KEY);
        return runAsManager;
    }
}
