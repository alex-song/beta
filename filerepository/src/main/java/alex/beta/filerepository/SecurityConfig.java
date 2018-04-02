/**
 * @File: SecurityConfig.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/2 16:00
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.util.StringUtils;

/**
 * @version ${project.version}
 * @Description
 */

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Profile({"dev", "docker"})
    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER - 1)
    static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity.authorizeRequests().anyRequest().authenticated()
                    .and().httpBasic();
            httpSecurity.authorizeRequests().antMatchers("/").permitAll();
            httpSecurity.headers().frameOptions().disable();
            httpSecurity.csrf().disable();
        }

        @Bean
        public RoleHierarchy roleHierarchy() {
            RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
            roleHierarchy.setHierarchy("ROLE_FRS_ADMIN > ROLE_FRS_OPERATOR > ROLE_FRS_GUEST");
            return roleHierarchy;
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth, FileRepositoryUserConfig userConfig) throws Exception {
            InMemoryUserDetailsManager um = new InMemoryUserDetailsManager();

            if (userConfig != null) {
                if (StringUtils.isEmpty(userConfig.getAdminUsername())) {
                    throw new InvalidConfigurationException("Admin user is missing in the configuration");
                } else {
                    um.createUser(User.withUsername(userConfig.getAdminUsername())
                            .password(StringUtils.isEmpty(userConfig.getAdminPassword()) ? "" : userConfig.getAdminPassword())
                            .roles("FRS_ADMIN").build());
                }

                if (StringUtils.isEmpty(userConfig.getOperatorUsername())) {
                    throw new InvalidConfigurationException("Operator user is missing in the configuration");
                } else {
                    um.createUser(User.withUsername(userConfig.getOperatorUsername())
                            .password(StringUtils.isEmpty(userConfig.getOperatorPassword()) ? "" : userConfig.getOperatorPassword())
                            .roles("FRS_OPERATOR").build());
                }

                if (!StringUtils.isEmpty(userConfig.getGuestUsername())) {
                    um.createUser(User.withUsername(userConfig.getGuestUsername())
                            .password(StringUtils.isEmpty(userConfig.getGuestPassword()) ? "" : userConfig.getGuestPassword())
                            .roles("FRS_GUEST").build());
                }
            } else {
                throw new InvalidConfigurationException("FileRepositoryUserConfig is missing");
            }

            auth.userDetailsService(um);
        }

        @Bean
        public FileRepositoryUserConfig getUserConfig() {
            return new FileRepositoryUserConfig();
        }
    }

    @Data
    @EnableAutoConfiguration
    static class FileRepositoryUserConfig {
        @Value("${filerepository.admin.username}")
        String adminUsername;

        @Value("${filerepository.admin.password}")
        String adminPassword;

        @Value("${filerepository.guest.username}")
        String guestUsername;

        @Value("${filerepository.guest.password}")
        String guestPassword;

        @Value("${filerepository.operator.username}")
        String operatorUsername;

        @Value("${filerepository.operator.password}")
        String operatorPassword;
    }
}
