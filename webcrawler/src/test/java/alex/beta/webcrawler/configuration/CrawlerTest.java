/**
 * @File: CrawlerTest.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/23 20:48
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.commons.util.SocketUtils;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * @version ${project.version}
 * @Description
 */
public class CrawlerTest {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerTest.class);

    private static final String[] resources = new String[]{
            "1.html", "11.html", "12.html", "1.js", "1.css",
            "2.html"};

    private final int serverPort = SocketUtils.findAvailableTcpPort(49152, 65535);

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(WireMockConfiguration.options().port(serverPort));

    @Before
    public void setUp() {
        logger.info("Mock server is listening on port {}.", serverPort);

        stubFor(get(urlEqualTo("/robots.txt")).willReturn(
                aResponse().withStatus(404)));

        for (String res : resources) {
            try {
                stubFor(get(urlEqualTo("/" + res)).willReturn(
                        aResponse().withStatus(200).withLogNormalRandomDelay(90, 0.1)
                                .withBody(Resources.toByteArray(Resources.getResource(res)))));
            } catch (IOException | IllegalArgumentException ex) {
                logger.error("Failed to load resource {}", res, ex);
            }
        }
    }

    @After
    public void tearDown() {
        removeAllMappings();
        AssertionErrorBus.getInstance().remove(serverPort);
    }

    @Test
    public void testParse() throws Exception {
        WebCrawlerBuilder builder = WebCrawlerBuilder.newInstance("CrawlerTest-1.xml");
        builder.buildController().addEntryPoints("http://localhost:" + serverPort + "/1.html")
                .buildCrawlerFactory().start(true);
        AssertionError error = AssertionErrorBus.getInstance().get(serverPort);
        if (error != null) {
            throw error;
        }
    }
}
