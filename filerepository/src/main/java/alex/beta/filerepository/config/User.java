package alex.beta.filerepository.config;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by songlip on 2018/4/8.
 */
public abstract class User implements UserDetails {
    @Getter
    protected String password;

    @Getter
    protected String username;

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

    public static class AdminUser extends User {
        public AdminUser(String username, String password) {
            super.username = username;
            super.password = password;
        }

        public Collection<GrantedAuthority> getAuthorities() {
            GrantedAuthority admin = (GrantedAuthority) () -> "FRS_ADMIN";
            return Arrays.asList(new GrantedAuthority[]{admin});
        }
    }

    public static class OperatorUser extends User {
        public OperatorUser(String username, String password) {
            super.username = username;
            super.password = password;
        }

        public Collection<GrantedAuthority> getAuthorities() {
            GrantedAuthority admin = (GrantedAuthority) () -> "FRS_OPERATOR";
            return Arrays.asList(new GrantedAuthority[]{admin});
        }
    }

    public static class GuestUser extends User {
        public GuestUser(String username, String password) {
            super.username = username;
            super.password = password;
        }

        public Collection<GrantedAuthority> getAuthorities() {
            GrantedAuthority admin = (GrantedAuthority) () -> "FRS_GUEST";
            return Arrays.asList(new GrantedAuthority[]{admin});
        }
    }
}
