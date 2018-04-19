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
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * @version ${project.version}
 * @Description
 */

@SuppressWarnings("squid:S1118")
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfig {

    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_FRS_ADMIN = "FRS_ADMIN";
    public static final String ROLE_FRS_OPERATOR = "FRS_OPERATOR";
    public static final String ROLE_FRS_GUEST = "FRS_GUEST";

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
            httpSecurity.headers().frameOptions().disable();
            httpSecurity.authorizeRequests()
                    .antMatchers("/").permitAll()
                    .antMatchers("/api-spec/**", "/v2/api-docs", "/swagger-spec.json", "/console/**", "/favicon.ico").permitAll()// for swagger and h2 console
                    .and()
                    .authorizeRequests()
                    .antMatchers("/health", "/info", "/auditevents", "/manage", "/metrics").permitAll() // for spring health
                    .anyRequest().authenticated()
                    .and().httpBasic();
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers("/info", "/health");
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth, IFrsConfig frsConfig) throws Exception {
            InMemoryUserDetailsManager um = new InMemoryUserDetailsManager();

            if (frsConfig.getAdmin() != null && !frsConfig.getAdmin().isEmpty()) {
                frsConfig.getAdmin().forEach(um::createUser);
            }

            if (frsConfig.getOperator() != null && !frsConfig.getOperator().isEmpty()) {
                frsConfig.getOperator().forEach(um::createUser);
            }

            if (frsConfig.getGuest() != null && !frsConfig.getGuest().isEmpty()) {
                frsConfig.getGuest().forEach(um::createUser);
            }

            auth.userDetailsService(um);
        }
    }
}
