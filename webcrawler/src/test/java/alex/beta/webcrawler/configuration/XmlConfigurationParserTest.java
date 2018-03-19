/**
 * @File: XmlConfigurationParserTest.java
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

import alex.beta.webcrawler.configuration.api.IConfiguration;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @version ${project.version}
 * @Description
 */
public class XmlConfigurationParserTest {
    @Before
    public void setUp() {
        //
    }

    @After
    public void tearDown() {
        //
    }

    @Ignore
    @Test
    public void testConfiguration() throws Exception {
        try {
            IConfiguration configuration = XmlConfigurationParser.parse("XmlConfigurationParserTest-1.xml");
            CrawlController controller = XmlConfigurationParser.getCrawlController(configuration);
            controller.start(XmlConfigurationParser.getWebCrawlerFactory(configuration), configuration.getDepth());

            /**
             * 启动爬虫，爬虫从此刻开始执行爬虫任务，根据以上配置
             */
            controller.start(XmlConfigurationParser.getWebCrawlerFactory(configuration), configuration.getNumberOfCrawlers());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
