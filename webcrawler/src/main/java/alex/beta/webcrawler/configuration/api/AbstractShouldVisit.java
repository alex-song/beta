/**
 * @File: AbstractShouldVisit.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/17 9:00
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import alex.beta.webcrawler.configuration.ClassUtils;
import alex.beta.webcrawler.configuration.ConfigurationException;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractShouldVisit implements IShouldVisit {

    private static final Logger logger = LoggerFactory.getLogger(AbstractShouldVisit.class);

    public abstract String getShouldVisitClass();

    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) throws ConfigurationException {
        return shouldVisit(url.getURL());
    }

    @Override
    public boolean shouldVisit(String url) throws ConfigurationException {
        if (logger.isDebugEnabled()) logger.debug("shouldVisit {}", url);
        if (StringUtils.isEmpty(url)) {
            return false;
        } else if (StringUtils.isEmpty(this.getShouldVisitClass())) {
            boolean flag;
            if (this.getCondition() != null) {
                flag = this.getCondition().evaluate(url);
                if (logger.isDebugEnabled()) {
                    logger.debug("Condition evaluation of {} is {}", url, flag);
                }
                return flag;
            } else if (this.getJoint() != null) {
                flag = this.getJoint().evaluate(url);
                if (logger.isDebugEnabled()) {
                    logger.debug("Joint evaluation of {} is {}", url, flag);
                }
                return flag;
            } else {
                throw new ConfigurationException("Illegal configuration of ShouldVisit node. Must provide one of shouldVisitClass, Condition or Joint.");
            }
        } else {
            IShouldVisit customizedShouldVisit = ClassUtils.customizedShouldVisit(this.getShouldVisitClass());
            return customizedShouldVisit.shouldVisit(url);
        }
    }

    @Override
    public XPathNode getParent() {
        return XPathNode.ROOT;
    }

    @Override
    public String getPath() {
        return XPathNode.ROOT.getPath() + "/ShouldVisit";
    }
}
