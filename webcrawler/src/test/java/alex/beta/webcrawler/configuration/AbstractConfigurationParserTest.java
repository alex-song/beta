/**
 * @File: AbstractConfigurationParserTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/15 10:11
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @version ${project.version}
 * @Description
 */
public class AbstractConfigurationParserTest {
    @Before
    public void setUp() {
        //
    }

    @After
    public void tearDown() {
        //
    }

    @Test
    public void testParse() throws Exception {
        IConfiguration configuration = XmlConfigurationParser.parse("XmlConfigurationParserTest-1.xml");
        assertNotNull(configuration);

        IShouldVisit.InnerShouldVisit sv = configuration.getShouldVisit();
        assertNotNull(sv);

        assertNull(sv.getCondition());
        IJoint j = sv.getJoint();
        assertNotNull(j);
        assertTrue(j instanceof INot);

        List<? extends ICondition> c = j.getCondition();
        assertNotNull(c);
        assertEquals(1, c.size());
        assertTrue(c.get(0) instanceof IEndsWith);
    }

    @Test
    public void testEvaluator() throws Exception {
        IConfiguration configuration = XmlConfigurationParser.parse("XmlConfigurationParserTest-2.xml");
        assertNotNull(configuration);

        IShouldVisit sv = configuration.getShouldVisit();
        boolean result = sv.shouldVisit("Qw3rTyUiOp");
        assertTrue(result);
    }
}
