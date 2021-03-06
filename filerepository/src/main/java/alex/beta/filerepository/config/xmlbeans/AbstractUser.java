/**
 * @File: AbstractUser.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/8 21:57
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.config.xmlbeans;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

import static alex.beta.filerepository.SecurityConfig.*;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractUser implements UserDetails {

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public abstract static class AdminUser extends User {
        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return Arrays.asList((GrantedAuthority) () -> ROLE_PREFIX + ROLE_FRS_ADMIN);
        }
    }

    public abstract static class OperatorUser extends User {
        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return Arrays.asList((GrantedAuthority) () -> ROLE_PREFIX + ROLE_FRS_OPERATOR);
        }
    }

    public abstract static class GuestUser extends User {
        @Override
        public Collection<GrantedAuthority> getAuthorities() {
            return Arrays.asList((GrantedAuthority) () -> ROLE_PREFIX + ROLE_FRS_GUEST);
        }
    }
}
