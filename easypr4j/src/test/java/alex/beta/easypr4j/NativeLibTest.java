/**
 * <p>
 * File Name: NativeLibTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/14 下午11:10
 * </p>
 */
package alex.beta.easypr4j;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/**
 * @author alexsong
 * @version ${project.version}
 */
public class NativeLibTest {
    private static final String OPENCV_LIB = "/usr/local/Cellar/opencv/3.4.0_1/share/OpenCV/java/libopencv_java340.dylib";

    @Before
    public void setUp() {
        //
    }

    @After
    public void tearDown() {

    }

    @Test
    public void isNativeLibAvailable() {
        File libFile = new File(OPENCV_LIB);
        assertNotNull(libFile);
        assertTrue(OPENCV_LIB + " is not found", libFile.canRead());
    }
}
