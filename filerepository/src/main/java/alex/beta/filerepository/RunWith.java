package alex.beta.filerepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static alex.beta.filerepository.SecurityConfig.ROLE_FRS_ADMIN;
import static alex.beta.filerepository.SecurityConfig.ROLE_PREFIX;

/**
 * Created by songlip on 2018/4/18.
 */
@Deprecated
public class RunWith {
    private static final Logger logger = LoggerFactory.getLogger(RunWith.class);

    @FunctionalInterface
    public interface RunWithMethod {
        default Object run() throws Exception {
            return runWithException();
        }

        Object runWithException() throws Exception;
    }

    public static Object systemUser(final RunWithMethod func, final String... roles) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("RunWith.systemUser, additional roles: {}",
                    roles == null || roles.length == 0 ? "" : Arrays.stream(roles).collect(Collectors.joining(", ")));
        }
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> ROLE_PREFIX + ROLE_FRS_ADMIN);
        if (roles != null) {
            Arrays.stream(roles).forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        }
        final AnonymousAuthenticationToken token = new AnonymousAuthenticationToken("system", "system", authorities);
        final Authentication originalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(token);
        try {
            return func.run();
        } finally {
            SecurityContextHolder.getContext().setAuthentication(originalAuthentication);
        }
    }
}
