/**
 * @File: RunWithTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/19 下午8:44
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.services.DummyService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.Assert.assertEquals;

/**
 * @version ${project.version}
 * @Description
 */
public class RunWithTest extends AbstractServerTest {

    @Autowired
    private DummyService dummyService;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAdminRole() throws Exception {
        Authentication orgAuth = SecurityContextHolder.getContext().getAuthentication();
        assertEquals("adminabc", RunWith.adminRole(() -> dummyService.requireAdminRole("abc")));
        assertEquals("operatorabc", RunWith.operatorRole(() -> dummyService.requireOperatorRole("abc")));
        assertEquals("guestabc", RunWith.guestRole(() -> dummyService.requireGuestRole("abc")));
        assertEquals(orgAuth, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testRoleOverride() throws Exception {
        assertEquals("operatorabc", RunWith.adminRole(() -> dummyService.requireOperatorRole("abc")));
        assertEquals("guestabc", RunWith.adminRole(() -> dummyService.requireGuestRole("abc")));
        assertEquals("guestabc", RunWith.operatorRole(() -> dummyService.requireGuestRole("abc")));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPermissionDenied1() throws Exception {
        RunWith.operatorRole(() -> dummyService.requireAdminRole("abc"));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPermissionDenied2() throws Exception {
        RunWith.guestRole(() -> dummyService.requireAdminRole("abc"));
    }

    @Test(expected = AccessDeniedException.class)
    public void testPermissionDenied3() throws Exception {
        RunWith.guestRole(() -> dummyService.requireOperatorRole("abc"));
    }
}
