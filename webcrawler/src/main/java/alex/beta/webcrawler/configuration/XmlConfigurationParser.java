/**
 * @File: XmlConfigurationParser.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/15 9:21
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.IConfiguration;
import com.google.common.io.Resources;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Objects;

/**
 * @version ${project.version}
 * @Description
 */
public class XmlConfigurationParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlConfigurationParser.class);

    private static final String XML_CONFIGURATION_BEAN = "alex.beta.webcrawler.configuration.xmlbeans.Configuration";

    private XmlConfigurationParser() {
        //Hide default public constructor
    }

    public static IConfiguration parse(String filePath) throws ConfigurationException {
        Objects.requireNonNull(filePath);
        try {
            Class<? extends IConfiguration> rootElementClass = Class.forName(XML_CONFIGURATION_BEAN).asSubclass(IConfiguration.class);
            JAXBContext jaxbContext = JAXBContext.newInstance(rootElementClass);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(Resources.getResource(filePath).openStream());
            return jaxbUnmarshaller.unmarshal(reader, rootElementClass).getValue();
        } catch (IOException | XMLStreamException e1) {
            logger.error("Failed to read configuration XML file, {}.", filePath, e1);
            throw new ConfigurationException(filePath, e1);
        } catch (ClassNotFoundException e2) {
            logger.error("{} is not found.", XML_CONFIGURATION_BEAN, e2);
            throw new InternalError(XML_CONFIGURATION_BEAN, e2);
        } catch (JAXBException e3) {
            logger.error("Cannot bind XML {} with Configuration bean.", filePath, e3);
            throw new ConfigurationException(filePath, e3);
        }
    }

    public static CrawlController.WebCrawlerFactory getWebCrawlerFactory(IConfiguration configuration) {
        return new XmlWebCrawlerFactory(configuration);
    }

    public static CrawlController getCrawlController(IConfiguration configuration) throws Exception {
        CrawlConfig config = new CrawlConfig(); // 定义爬虫配置
        // 定义爬虫数据存储位置
        if (configuration.getCrawlStorageFolder() != null && !configuration.getCrawlStorageFolder().trim().isEmpty()) {
            config.setCrawlStorageFolder(configuration.getCrawlStorageFolder()); // 设置爬虫文件存储位置
        } else {
            config.setCrawlStorageFolder(System.getProperty("java.io.tmpdir"));
        }
        // 设置爬虫深度
        config.setMaxDepthOfCrawling(configuration.getDepth());

        // 实例化爬虫控制器
        PageFetcher pageFetcher = new PageFetcher(config); // 实例化页面获取器
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig(); // 实例化爬虫机器人配置 比如可以设置 user-agent

        // 实例化爬虫机器人对目标服务器的配置，每个网站都有一个robots.txt文件 规定了该网站哪些页面可以爬，哪些页面禁止爬，该类是对robots.txt规范的实现
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        // 实例化爬虫控制器
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // 配置爬虫种子页面，就是规定的从哪里开始爬，可以配置多个种子页面
        for (String seed : configuration.getEntryPoints()) {
            controller.addSeed(seed);
        }
        return controller;
    }

    static class XmlWebCrawlerFactory implements CrawlController.WebCrawlerFactory {

        private IConfiguration configuration;

        private XmlWebCrawlerFactory(IConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public WebCrawler newInstance() throws Exception {

            return new WebCrawler() {
                @Override
                public boolean shouldVisit(Page referringPage, WebURL url) {
                    if (configuration.getShouldVisit() == null) {
                        return false;
                    }
                    try {
                        return configuration.getShouldVisit().shouldVisit(referringPage, url);
                    } catch (ConfigurationException ex) {
                        logger.error("Cannot evaluate shouldVisit method on {}.", url, ex);
                        return false;
                    }
                }

                @Override
                public void visit(Page page) {
                    if (configuration.getVisitor() != null) {
                        try {
                            ClassUtils.customizedVisitor(configuration.getVisitor().getVisitorClass()).visit(page);
                        } catch (ConfigurationException ex) {
                            logger.error("Failed to visit page {}.", page.getWebURL().getURL(), ex);
                        }
                    }
                }
            };
        }
    }
}
