/**
 * @File: CrawlerTestVisitor.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/24 上午9:24
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.IVisitor;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version ${project.version}
 * @Description
 */
public class CrawlerTestVisitor implements IVisitor {
    private static final Logger logger = LoggerFactory.getLogger(CrawlerTestVisitor.class);

    public void visit(Page page) {
        String url = page.getWebURL().getURL();
        int firstColon = url.indexOf(":");
        int secondColon = url.indexOf(":", firstColon + 1);
        int slash = url.indexOf("/", secondColon + 1);
        int port = Integer.parseInt(url.substring(secondColon + 1, slash));
        if (logger.isDebugEnabled()) {
            logger.debug("CrawlerTestVisitor on port {}", port);
        }

        if (url.endsWith("/1.html")) {
            if (!((HtmlParseData) page.getParseData()).getText().contains("Hello World - 1")) {
                AssertionErrorBus.getInstance().put(port,
                        new AssertionError("Content of 1.html doesn't match expected text."
                                + System.lineSeparator()
                                + ((HtmlParseData) page.getParseData()).getHtml()));
            }
        } else if (url.endsWith("/11.html")) {
            if (!((HtmlParseData) page.getParseData()).getText().contains("Hello World - 11")) {
                AssertionErrorBus.getInstance().put(port,
                        new AssertionError("Content of 1.html doesn't match expected text."
                                + System.lineSeparator()
                                + ((HtmlParseData) page.getParseData()).getHtml()));
            }
        } else {
            AssertionErrorBus.getInstance().put(port, new AssertionError("Unexpected URL visiting, " + url));
        }
    }
}
