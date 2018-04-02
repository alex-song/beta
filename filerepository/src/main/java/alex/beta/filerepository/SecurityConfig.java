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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

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

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            //TODO externalize the integration user
            InMemoryUserDetailsManager um = new InMemoryUserDetailsManager();
            um.createUser(User.withUsername("admin").password("admin").roles("ALL").build());
            auth.userDetailsService(um);
        }
    }
}
