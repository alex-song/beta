package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.IVisitor;
import alex.beta.webcrawler.configuration.api.PathSupport;
import alex.beta.webcrawler.configuration.xmlbeans.XmlConfiguration;
import edu.uci.ics.crawler4j.crawler.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Created by songlip on 2018/3/22.
 */
public class LoggerVisitor implements IVisitor {

    private static final Logger logger = LoggerFactory.getLogger(LoggerVisitor.class);

    @Override
    public String getVisitorClass() {
        return null;
    }

    @Override
    public void visit(Page page) {
        logger.info("Visiting {}", page.getWebURL().getURL());
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

    @Override
    public String getPath() {
        return XmlConfiguration.ROOT.getPath() + "/Visitor";
    }
}
