/**
 * @File: MongoConfiguration.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/1 上午10:58
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.mongo.FileInfoCascadingMongoEventListener;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.security.Principal;

/**
 * @version ${project.version}
 * @Description
 */

@Configuration
@EnableAutoConfiguration
@EnableMongoAuditing
public class MongoConfig {

    @Bean
    public FileInfoCascadingMongoEventListener cascadingMongoEventListener() {
        return new FileInfoCascadingMongoEventListener();
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            // 没有认证信息
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
                return null;
            }

            // 获取认证主体对象
            final Object principal = authentication.getPrincipal();

            if (principal instanceof Principal) {
                // 返回name，如果认证主体是Principal子类
                return ((Principal) principal).getName();
            } else if (principal instanceof UserDetails) {
                // 返回username，如果认证主体是UserDetails子类
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                // 返回principal，如果认证主体是String类
                return (String) principal;
            }

            // 猜名字 :-)
            final PrincipalNameFieldCallback callback = new PrincipalNameFieldCallback(principal);

            ReflectionUtils.doWithFields(principal.getClass(), callback, field -> {
                ReflectionUtils.makeAccessible(field);
                if (field.getType() == String.class) {
                    final String fieldName = field.getName();
                    switch (fieldName) {
                        case "username":
                        case "userName":
                        case "loginname":
                        case "loginName":
                        case "upn":
                        case "UPN":
                        case "name":
                            return true;
                        default:
                            return false;
                    }
                } else {
                    return false;
                }
            });
            return callback.value;
        };
    }

    private static class PrincipalNameFieldCallback implements ReflectionUtils.FieldCallback {
        private String value;
        private Object source;

        PrincipalNameFieldCallback(Object source) {
            this.source = source;
        }

        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
            // 接受最上层的
            if (value == null) {
                value = (String) field.get(source);
            }
        }
    }
}
