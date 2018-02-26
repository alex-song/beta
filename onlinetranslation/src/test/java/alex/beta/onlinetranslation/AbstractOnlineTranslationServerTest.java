/**
 * <p>
 * File Name: AbstractOnlineTranslationServerTest.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/26 上午8:16
 * </p>
 */
package alex.beta.onlinetranslation;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author alexsong
 * @version ${project.version}
 */

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public abstract class AbstractOnlineTranslationServerTest {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractOnlineTranslationServerTest.class);
}
