/**
 * <p>
 * File Name: BaiduTranslationTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/23 上午11:18
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class BaiduTranslationTest {
    private BaiduTranslation bt;

    @Before
    public void setUp() {
        bt = new BaiduTranslation();

        ArrayList<BaiduTranslationResult> btrl = new ArrayList<>();

        BaiduTranslationResult btr1 = new BaiduTranslationResult();
        btr1.setDst("a");
        BaiduTranslationResult btr2 = new BaiduTranslationResult();
        btr2.setDst("b");
        BaiduTranslationResult btr3 = new BaiduTranslationResult();
        btr3.setDst("c");

        btrl.add(btr1);
        btrl.add(btr2);
        btrl.add(btr3);

        bt.setTransResult(btrl);
    }

    @After
    public void tearDown() {
        bt = null;
    }

    @Test
    public void testJoinAllDstsWithLineSeparator() {
        assertEquals("a\nb\nc", bt.joinAllDstsWithLineSeparator());
    }
}
