/**
 * @File: IShouldVisit.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/17 9:42
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import alex.beta.webcrawler.configuration.ConfigurationException;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;

/**
 * @version ${project.version}
 * @Description
 */
public interface IShouldVisit extends PathSupport {
    ICondition getCondition();

    IJoint getJoint();

    boolean shouldVisit(String url) throws ConfigurationException;

    boolean shouldVisit(Page referringPage, WebURL url) throws ConfigurationException;
}
