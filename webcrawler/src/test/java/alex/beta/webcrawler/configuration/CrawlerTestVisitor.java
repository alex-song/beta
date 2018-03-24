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

/**
 * @version ${project.version}
 * @Description
 */
public class CrawlerTestVisitor implements IVisitor {

    public void visit(Page page) {
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>" + page.getWebURL().getURL());
    }
}
