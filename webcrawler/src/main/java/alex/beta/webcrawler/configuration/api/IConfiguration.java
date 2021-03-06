/**
 * @File: IConfiguration.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/15 8:20
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
public interface IConfiguration extends PathSupport {
    List<String> getEntryPoints();

    int getDepth();

    int getNumberOfCrawlers();

    int getTimeout();

    boolean isShutOnEmpty();

    int getPolitenessDelayInMS();

    String getCrawlStorageFolder();

    IShouldVisit.InnerShouldVisit getShouldVisit();

    IVisitor.InnerVisitor getVisitor();
}
