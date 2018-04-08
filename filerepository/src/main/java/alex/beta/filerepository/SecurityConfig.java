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
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_FRS_ADMIN = "FRS_ADMIN";
    public static final String ROLE_FRS_OPERATOR = "FRS_OPERATOR";
    public static final String ROLE_FRS_GUEST = "FRS_GUEST";


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
            roleHierarchy.setHierarchy(
                    ROLE_PREFIX + ROLE_FRS_ADMIN
                            + " > " + ROLE_PREFIX + ROLE_FRS_OPERATOR
                            + " > " + ROLE_PREFIX + ROLE_FRS_GUEST);
            return roleHierarchy;
        }

        //TODO how to integrate with consume authentication/authorization?
        //TODO UT
        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth, FileRepositoryUserConfig userConfig) throws Exception {
            InMemoryUserDetailsManager um = new InMemoryUserDetailsManager();

            if (userConfig != null) {
                if (StringUtils.isEmpty(userConfig.getAdminUsername())) {
                    throw new InvalidConfigurationException("Admin user is missing in the configuration");
                } else {
                    um.createUser(User.withUsername(userConfig.getAdminUsername())
                            .password(StringUtils.isEmpty(userConfig.getAdminPassword()) ? "" : userConfig.getAdminPassword())
                            .roles(ROLE_FRS_ADMIN).build());
                }

                if (StringUtils.isEmpty(userConfig.getOperatorUsername())) {
                    throw new InvalidConfigurationException("Operator user is missing in the configuration");
                } else {
                    um.createUser(User.withUsername(userConfig.getOperatorUsername())
                            .password(StringUtils.isEmpty(userConfig.getOperatorPassword()) ? "" : userConfig.getOperatorPassword())
                            .roles(ROLE_FRS_OPERATOR).build());
                }

                if (!StringUtils.isEmpty(userConfig.getGuestUsername())) {
                    um.createUser(User.withUsername(userConfig.getGuestUsername())
                            .password(StringUtils.isEmpty(userConfig.getGuestPassword()) ? "" : userConfig.getGuestPassword())
                            .roles(ROLE_FRS_GUEST).build());
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
        @Value("${filerepository.users.admin.username}")
        String adminUsername;

        @Value("${filerepository.users.admin.password}")
        String adminPassword;

        @Value("${filerepository.users.guest.username}")
        String guestUsername;

        @Value("${filerepository.users.guest.password}")
        String guestPassword;

        @Value("${filerepository.users.operator.username}")
        String operatorUsername;

        @Value("${filerepository.users.operator.password}")
        String operatorPassword;
    }
}
