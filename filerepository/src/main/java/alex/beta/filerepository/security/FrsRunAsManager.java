/**
 * @File: FrsRunAsManager.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/14 下午10:39
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ReflectiveMethodInvocation;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.intercept.RunAsManagerImpl;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @version ${project.version}
 * @Description
 */
public class FrsRunAsManager extends RunAsManagerImpl {

    private Logger logger = LoggerFactory.getLogger(FrsRunAsManager.class);

    @Override
    public Authentication buildRunAs(Authentication authentication, Object object, Collection<ConfigAttribute> attributes) {
        RunWithRole runWithRole = null;
        if (!(object instanceof ReflectiveMethodInvocation) || (runWithRole = getRunWithRoleAnnotation(object)) == null) {
            return super.buildRunAs(authentication, object, attributes);
        }
        String[] roles = runWithRole.value();
        if (roles == null || roles.length == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No role is defined, use current authorities");
            }
            return super.buildRunAs(authentication, object, attributes);
        }
        if (logger.isInfoEnabled()) {
            logger.info("RunAs current authorities : {}", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));
        }

        Set<GrantedAuthority> newAuthorities = new HashSet<>();
        if (runWithRole.append()) {
            newAuthorities.addAll(authentication.getAuthorities());
        }

        for (String role : roles) {
            GrantedAuthority runAsAuthority = new SimpleGrantedAuthority(role);
            newAuthorities.add(runAsAuthority);
        }
        if (logger.isInfoEnabled()) {
            logger.info("RunAs new authorities : {}", Arrays.stream(runWithRole.value()).collect(Collectors.joining(", ")));
        }

        return new RunAsUserToken(getKey(), authentication.getPrincipal(), authentication.getCredentials(),
                newAuthorities, authentication.getClass());
    }

    private RunWithRole getRunWithRoleAnnotation(Object object) {
        Method method = ((ReflectiveMethodInvocation) object).getMethod();
        ReflectionUtils.makeAccessible(method);
        if (!method.isAnnotationPresent(RunWithRole.class)) {
            return null;
        } else {
            return method.getAnnotation(RunWithRole.class);
        }
    }
}
