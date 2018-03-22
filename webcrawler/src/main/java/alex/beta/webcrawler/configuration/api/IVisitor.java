/**
 * @File: IVisitor.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/20 9:13
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import edu.uci.ics.crawler4j.crawler.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * @version ${project.version}
 * @Description
 */
public interface IVisitor extends XPathNode {
    String getVisitorClass();

    void visit(Page page);

    class LoggerVisitor implements IVisitor {

        private static final Logger logger = LoggerFactory.getLogger(LoggerVisitor.class);

        public String getVisitorClass() {
            return null;
        }

        public void visit(Page page) {
            logger.info("Visisting {}", page.getWebURL().getURL());
            logger.info("Response code: {}", page.getStatusCode());
            if (logger.isDebugEnabled()) {
                try {
                    logger.debug("Response data: {}{}", System.lineSeparator(), new String(page.getContentData(),
                            (page.getContentCharset() == null ? "UTF-8" : page.getContentCharset())));
                } catch (UnsupportedEncodingException ex) {
                    logger.error("Unsupported page charset {}", page.getContentCharset(), ex);
                }
            }
        }

        public XPathNode getParent() {
            return XPathNode.ROOT;
        }

        public String getPath() {
            return XPathNode.ROOT.getPath() + "/Visitor";
        }
    }
}
