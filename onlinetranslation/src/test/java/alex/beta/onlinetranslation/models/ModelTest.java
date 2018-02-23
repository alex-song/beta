/**
 * <p>
 * File Name: ModelTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/23 下午4:49
 * </p>
 */
package alex.beta.onlinetranslation.models;

import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class ModelTest {
    private TranslationError error;
    private TranslationResult result;

    @Before
    public void setUp() {
        error = new TranslationError("error code", "error message");
        Translation t = new Translation(TranslationStatus.SUBMITTED, "zh", "en", "你好");
        Date tmp = new Date(1519376391114L);
        t.setCreatedOn(tmp);
        t.setLastUpdatedOn(tmp);
        result = new TranslationResult(t);
    }

    @After
    public void tearDown() {
        error = null;
        result = null;
    }

    @Test
    public void testToString() {
        assertEquals("{\"errorCode\":\"error code\",\"message\":\"error message\"}", error.toString());
        assertEquals("{\"uuid\":null,\"status\":\"SUBMITTED\",\"message\":null,\"text\":\"你好\",\"translatedText\":null,\"fromLanguage\":\"zh\",\"toLanguage\":\"en\",\"createdOn\":1519376391114,\"lastUpdatedOn\":1519376391114}", result.toString());
    }
}
