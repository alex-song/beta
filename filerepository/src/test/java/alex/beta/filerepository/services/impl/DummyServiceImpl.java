/**
 * @File: DummyServiceImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/19 下午8:38
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services.impl;

import alex.beta.filerepository.services.DummyService;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import static alex.beta.filerepository.SecurityConfig.*;

/**
 * @version ${project.version}
 * @Description
 */
@Profile("test")
@Service("dummyService")
public class DummyServiceImpl implements DummyService {

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public String requireAdminRole(String arg) {
        return "admin" + arg;
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public String requireOperatorRole(String arg) {
        return "operator" + arg;
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public String requireGuestRole(String arg) {
        return "guest" + arg;
    }
}
