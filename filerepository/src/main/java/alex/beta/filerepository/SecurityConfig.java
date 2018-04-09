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

import alex.beta.filerepository.config.xmlbeans.IFrsConfig;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @version ${project.version}
 * @Description
 */

@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_FRS_ADMIN = "FRS_ADMIN";
    public static final String ROLE_FRS_OPERATOR = "FRS_OPERATOR";
    public static final String ROLE_FRS_GUEST = "FRS_GUEST";


    @Profile({"dev", "docker", "test"})
    @Configuration
    @Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
    static class ApplicationSecurity extends WebSecurityConfigurerAdapter {

        @Bean
        public RoleHierarchy roleHierarchy() {
            RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
            roleHierarchy.setHierarchy(
                    ROLE_PREFIX + ROLE_FRS_ADMIN
                            + " > " + ROLE_PREFIX + ROLE_FRS_OPERATOR
                            + " " + ROLE_PREFIX + ROLE_FRS_OPERATOR
                            + " > " + ROLE_PREFIX + ROLE_FRS_GUEST);
            return roleHierarchy;
        }

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {
            httpSecurity.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and().csrf().disable();
            httpSecurity.authorizeRequests().anyRequest().authenticated()
                    .and().httpBasic();
            httpSecurity.authorizeRequests().antMatchers("/").permitAll();
            httpSecurity.headers().frameOptions().disable();
            httpSecurity.csrf().disable();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth, IFrsConfig frsConfig) throws Exception {
            InMemoryUserDetailsManager um = new InMemoryUserDetailsManager();

            if (frsConfig.getGuest() != null && !frsConfig.getGuest().isEmpty()) {
                frsConfig.getGuest().forEach(guest -> um.createUser(guest));
            }

            if (frsConfig.getOperator() != null && !frsConfig.getOperator().isEmpty()) {
                frsConfig.getOperator().forEach(operator -> um.createUser(operator));
            }

            if (frsConfig.getAdmin() != null && !frsConfig.getAdmin().isEmpty()) {
                frsConfig.getAdmin().forEach(admin -> um.createUser(admin));
            }
            auth.userDetailsService(um);
        }
    }
}
