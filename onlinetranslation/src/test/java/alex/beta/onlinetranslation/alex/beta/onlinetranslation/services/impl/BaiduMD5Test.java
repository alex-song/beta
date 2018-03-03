/**
 * @File:      BaiduMD5Test.java
 * @Project:   beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @Date:      2018/2/23 下午5:04
 * @author:    <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.services.impl.BaiduMD5;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @Description
 * @version ${project.version}
 */
public class BaiduMD5Test {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testMd5() {
        assertEquals("bf9998764e0f679130d99f79aa0a5849", BaiduMD5.md5("Online Translation"));
        assertNull(BaiduMD5.md5(null));
    }
}
