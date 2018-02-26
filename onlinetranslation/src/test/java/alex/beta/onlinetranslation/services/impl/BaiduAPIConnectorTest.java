/**
 * <p>
 * File Name: BaiduAPIConnectorTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/26 上午8:19
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.AbstractOnlineTranslationServerTest;
import alex.beta.onlinetranslation.persistence.TranslationEntity;
import alex.beta.onlinetranslation.persistence.TranslationLineEntity;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author alexsong
 * @version ${project.version}
 */

@EnableAutoConfiguration
public class BaiduAPIConnectorTest extends AbstractOnlineTranslationServerTest {
    private static final String goodStr = "{\"from\":\"en\",\"to\":\"zh\",\"trans_result\":[{\"src\":\"hello\",\"dst\":\"\\u4f60\\u597d\"}]}";

    private static final String[] unauthStrs = new String[]{
            "{\"error_code\":\"52003\",\"error_msg\":\"UNAUTHORIZED USER\"}",
            "{\"error_code\":\"54001\",\"error_msg\":\"INVALID SIGN\"}",
            "{\"error_code\":\"58000\",\"error_msg\":\"INVALID IP\"}"
    };

    private static final String[] retryStrs = new String[]{
            "{\"error_code\":\"52002\",\"error_msg\":\"INTERNAL ERROR\"}",
            "{\"error_code\":\"52001\",\"error_msg\":\"TIMEOUT\"}",
            "{\"error_code\":\"54003\",\"error_msg\":\"TOO FREQUENT\"}",
            "{\"error_code\":\"54005\",\"error_msg\":\"LONG QUERY TOO FREQUENT\"}",
    };

    private static final String oversizeStr = "{\"from\":\"en\",\"to\":\"zh\",\"trans_result\":[{\"src\":\"hello\",\"dst\":\"%s\"}]}";

    private static final String[] emptyStrs = new String[]{
            "{\"from\":\"en\",\"to\":\"zh\",\"trans_result\":[]}",
            "{\"from\":\"en\",\"to\":\"zh\"}"
    };

    @Autowired
    private BaiduAPIConnector apiConnector;

    @Before
    public void setUp() {
        apiConnector = spy(apiConnector);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testTranslateGood() throws Exception {
        //spy callBaiduAPI method
        doReturn(goodStr).when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));

        //call the translate method to parse the mock response from Baidu API
        TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-1"));

        //verify the response
        verify(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
        assertEquals("uuid-1", output.getUuid());
        assertEquals(TranslationStatus.READY, output.getStatus());
        assertNull(output.getToLanguage());
        assertNull(output.getCreatedOn());
        assertNull(output.getLastUpdatedOn());
        assertNull(output.getMessage());
        assertEquals("en", output.getFromLanguage());
        assertEquals(1, output.getTranslationLines().size());
        TranslationLineEntity tle = output.getTranslationLines().get(0);
        assertEquals("hello", tle.getSrc());
        assertEquals("你好", tle.getDst());
    }

    @Test
    public void testTranslateUnauthorized() throws Exception {
        int i = 2;
        //spy callBaiduAPI method
        for (String rspStr : unauthStrs) {
            doReturn(rspStr).when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
            //call the translate method to parse the mock response from Baidu API
            TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-" + i));

            //verify the response
            assertEquals("uuid-" + i++, output.getUuid());
            assertEquals(TranslationStatus.NOT_AUTHORIZED, output.getStatus());
            assertNull(output.getToLanguage());
            assertNull(output.getCreatedOn());
            assertNull(output.getLastUpdatedOn());
            assertNull(output.getFromLanguage());
            assertNull(output.getTranslationLines());
            switch (rspStr) {
                case "{\"error_code\":\"52003\",\"error_msg\":\"UNAUTHORIZED USER\"}":
                    assertEquals("52003 - UNAUTHORIZED USER", output.getMessage());
                    break;
                case "{\"error_code\":\"54001\",\"error_msg\":\"INVALID SIGN\"}":
                    assertEquals("54001 - INVALID SIGN", output.getMessage());
                    break;
                case "{\"error_code\":\"58000\",\"error_msg\":\"INVALID IP\"}":
                    assertEquals("58000 - INVALID IP", output.getMessage());
                    break;
                default:
                    Assert.fail();
            }
        }
        verify(apiConnector, times(3)).callBaiduAPI(Matchers.any(TranslationEntity.class));
    }

    @Test
    public void testTranslateRetry() throws Exception {
        int i = 5;
        //spy callBaiduAPI method
        for (String rspStr : retryStrs) {
            doReturn(rspStr).when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
            //call the translate method to parse the mock response from Baidu API
            TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-" + i));

            //verify the response
            assertEquals("uuid-" + i++, output.getUuid());
            assertEquals(TranslationStatus.SUBMITTED, output.getStatus());
            assertNull(output.getToLanguage());
            assertNull(output.getCreatedOn());
            assertNotNull(output.getLastUpdatedOn());
            assertTrue(output.getLastUpdatedOn().getTime() > System.currentTimeMillis());
            assertNull(output.getFromLanguage());
            assertNull(output.getTranslationLines());
        }
        verify(apiConnector, times(4)).callBaiduAPI(Matchers.any(TranslationEntity.class));
    }

    @Test
    public void testTranslateTruncated() throws Exception {
        StringBuilder sb = new StringBuilder("q");
        for (int j = 0; j < TranslationEntity.TRANSLATED_TEXT_MAXLENGTH; j++) {
            sb.append("q");
        }

        //spy callBaiduAPI method
        doReturn(String.format(oversizeStr, sb.toString())).when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));

        //call the translate method to parse the mock response from Baidu API
        TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-9"));

        //verify the response
        verify(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
        assertEquals("uuid-9", output.getUuid());
        assertEquals(TranslationStatus.WARNING, output.getStatus());
        assertNull(output.getToLanguage());
        assertNull(output.getCreatedOn());
        assertNull(output.getLastUpdatedOn());
        assertEquals("en", output.getFromLanguage());
        assertEquals(1, output.getTranslationLines().size());
        TranslationLineEntity tle = output.getTranslationLines().get(0);
        assertEquals("hello", tle.getSrc());
        assertEquals(TranslationEntity.TRANSLATED_TEXT_MAXLENGTH, tle.getDst().length());
    }

    @Test
    public void testTranslateEmptyResult() throws Exception {
        int i = 10;
        //spy callBaiduAPI method
        for (String rspStr : emptyStrs) {
            doReturn(rspStr).when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
            //call the translate method to parse the mock response from Baidu API
            TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-" + i));

            //verify the response
            assertEquals("uuid-" + i++, output.getUuid());
            assertEquals(TranslationStatus.WARNING, output.getStatus());
            assertNull(output.getToLanguage());
            assertNull(output.getCreatedOn());
            assertNull(output.getLastUpdatedOn());
            assertEquals("en", output.getFromLanguage());
            assertNull(output.getTranslationLines());
        }
        verify(apiConnector, times(2)).callBaiduAPI(Matchers.any(TranslationEntity.class));
    }

    @Test
    public void testTranslateNull() throws Exception {
        doReturn(null).when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
        //call the translate method to parse the mock response from Baidu API
        TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-12"));

        //verify the response
        assertEquals("uuid-12", output.getUuid());
        assertEquals(TranslationStatus.ERROR, output.getStatus());
        assertNull(output.getToLanguage());
        assertNull(output.getCreatedOn());
        assertNull(output.getLastUpdatedOn());
        assertNull(output.getTranslationLines());

        verify(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
    }

    @Test
    public void testTranslateInvalidResponse() throws Exception {
        doReturn("abcdefg").when(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
        //call the translate method to parse the mock response from Baidu API
        TranslationEntity output = apiConnector.translate(new TranslationEntity("uuid-13"));

        //verify the response
        assertEquals("uuid-13", output.getUuid());
        assertEquals(TranslationStatus.ERROR, output.getStatus());
        assertNull(output.getToLanguage());
        assertNull(output.getCreatedOn());
        assertNull(output.getLastUpdatedOn());
        assertNull(output.getTranslationLines());

        verify(apiConnector).callBaiduAPI(Matchers.any(TranslationEntity.class));
    }
}
