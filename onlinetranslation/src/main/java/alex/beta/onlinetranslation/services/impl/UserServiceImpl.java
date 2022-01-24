/**
 * @File: UserServiceImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/6 下午9:33
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.persistence.UserEntity;
import alex.beta.onlinetranslation.persistence.UserRepository;
import alex.beta.onlinetranslation.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @version ${project.version}
 * @Description
 */
@Service("userService")
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static List<GrantedAuthority> roles2Authorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.<GrantedAuthority>emptyList();
        }

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String role : roles) {
            if (role != null && !role.trim().isEmpty()) {
                grantedAuthorities.add(new SimpleGrantedAuthority(role.trim().startsWith("ROLE_") ? role.trim() : ("ROLE_" + role.trim())));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        if (logger.isDebugEnabled()) {
            logger.debug("Load user by path \'{}\'.", username);
        }
        Objects.requireNonNull(username);
        UserEntity userEntity = userRepository.findTopByNameIgnoreCaseOrderByIdAsc(username);
        if (userEntity != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("User found, id: {}", userEntity.getId());
            }
            return new User(userEntity.getName(), userEntity.getPassword(), roles2Authorities(userEntity.getRoles()));
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("User \'{}\' does not exist.", username);
            }
            throw new UsernameNotFoundException("User \'" + username + "\' does not exist.");
        }
    }
}
