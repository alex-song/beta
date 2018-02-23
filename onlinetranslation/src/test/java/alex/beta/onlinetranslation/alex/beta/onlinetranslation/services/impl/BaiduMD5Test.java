/**
 * <p>
 * File Name: BaiduMD5Test.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/23 下午5:04
 * </p>
 */
package alex.beta.onlinetranslation.alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.services.impl.BaiduMD5;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author alexsong
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
