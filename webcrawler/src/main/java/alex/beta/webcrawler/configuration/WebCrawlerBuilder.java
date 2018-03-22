/**
 * @File: WebCrawlerBuilder.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/22 23:08
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.IConfiguration;
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

/**
 * @version ${project.version}
 * @Description
 */
public class WebCrawlerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(WebCrawlerBuilder.class);

    private IConfiguration configuration;

    private CrawlController controller;

    private CrawlController.WebCrawlerFactory factory;

    private WebCrawlerBuilder() {
        //hide default public constructor
    }

    public static WebCrawlerBuilder newInstance(String xmlConfig) throws ConfigurationException {
        WebCrawlerBuilder builder = new WebCrawlerBuilder();
        builder.configuration = XmlConfigurationParser.parse(xmlConfig);
        return builder;
    }

    public WebCrawlerBuilder buildController() throws Exception {
        CrawlConfig config = new CrawlConfig(); // 定义爬虫配置
        // 定义爬虫数据存储位置
        if (configuration.getCrawlStorageFolder() != null && !configuration.getCrawlStorageFolder().trim().isEmpty()) {
            config.setCrawlStorageFolder(configuration.getCrawlStorageFolder()); // 设置爬虫文件存储位置
        } else {
            config.setCrawlStorageFolder(System.getProperty("java.io.tmpdir"));
        }
        config.setShutdownOnEmptyQueue(configuration.isShutOnEmpty());
        config.setPolitenessDelay(configuration.getPolitenessDelayInMS());
        // 设置爬虫深度
        config.setMaxDepthOfCrawling(configuration.getDepth());

        // 实例化爬虫控制器
        PageFetcher pageFetcher = new PageFetcher(config); // 实例化页面获取器
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig(); // 实例化爬虫机器人配置 比如可以设置 user-agent

        // 实例化爬虫机器人对目标服务器的配置，每个网站都有一个robots.txt文件 规定了该网站哪些页面可以爬，哪些页面禁止爬，该类是对robots.txt规范的实现
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        // 实例化爬虫控制器
        this.controller = new CrawlController(config, pageFetcher, robotstxtServer);

        // 配置爬虫种子页面，就是规定的从哪里开始爬，可以配置多个种子页面
        for (String seed : configuration.getEntryPoints()) {
            this.controller.addSeed(seed);
        }

        return this;
    }

    public WebCrawlerBuilder buildCrawlerFactory() {
        factory = new XmlWebCrawlerFactory(configuration);
        return this;
    }

    public void start(boolean blocking) {
        controller.startNonBlocking(factory, configuration.getNumberOfCrawlers());
        //TODO
    }

    public CrawlController getController() {
        return controller;
    }

    public void setController(CrawlController controller) {
        this.controller = controller;
    }

    public CrawlController.WebCrawlerFactory getFactory() {
        return factory;
    }

    public void setFactory(CrawlController.WebCrawlerFactory factory) {
        this.factory = factory;
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
