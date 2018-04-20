/**
 * @File: RunWith.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/18 下午1:38
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static alex.beta.filerepository.SecurityConfig.*;

/**
 * @version ${project.version}
 * @Description
 */

public class RunWith {
    private static final Logger logger = LoggerFactory.getLogger(RunWith.class);

    private RunWith() {
        //hide fault constructor
    }

    /**
     * Execute the method with ROLE_FRS_OPERATOR role
     *
     * @param func
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T operatorRole(final RunWithMethod<T> func) throws Exception {
        return role(func, ROLE_PREFIX + ROLE_FRS_OPERATOR);
    }

    /**
     * Execute the method with ROLE_FRS_GUEST role
     *
     * @param func
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T guestRole(final RunWithMethod<T> func) throws Exception {
        return role(func, ROLE_PREFIX + ROLE_FRS_GUEST);
    }

    /**
     * Execute the method with ROLE_FRS_ADMIN role
     *
     * @param func
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T adminRole(final RunWithMethod<T> func) throws Exception {
        return role(func, ROLE_PREFIX + ROLE_FRS_ADMIN);
    }

    /**
     * Execute the method with given roles
     *
     * @param func
     * @param roles
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T role(final RunWithMethod<T> func, final String... roles) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("RunWith roles: {}",
                    roles == null || roles.length == 0 ? "" : Arrays.stream(roles).collect(Collectors.joining(", ")));
        }
        final Authentication originalAuthentication = SecurityContextHolder.getContext() == null ? null : SecurityContextHolder.getContext().getAuthentication();

        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        if (roles != null) {
            Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        final AnonymousAuthenticationToken token = new AnonymousAuthenticationToken("_system", "_system", authorities);
        boolean noContext = false;
        if (SecurityContextHolder.getContext() == null) {
            noContext = true;
            SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
        }
        SecurityContextHolder.getContext().setAuthentication(token);
        try {
            return func.run();
        } finally {
            if (noContext) {
                SecurityContextHolder.clearContext();
            } else {
                if (SecurityContextHolder.getContext() != null) {
                    SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
                } else {
                    SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
                    SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
                }
            }
        }
    }

    @FunctionalInterface
    public interface RunWithMethod<T> {
        default T run() throws Exception {
            return runWithException();
        }

        T runWithException() throws Exception;
    }
}
