package alex.beta.webcrawler.configuration.api;

import edu.uci.ics.crawler4j.crawler.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by songlip on 2018/3/20.
 */
public interface IVisitor {
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
    }
}
