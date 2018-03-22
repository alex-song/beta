/**
 * @File: AbstractCondition.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/17 9:13
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import alex.beta.webcrawler.configuration.ConfigurationException;
import alex.beta.webcrawler.configuration.xmlbeans.ConditionEvaluator;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.Unmarshaller;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractCondition implements ICondition {

    private XPathNode parent;

    @Override
    public boolean evaluate(String url) throws ConfigurationException {
        return !StringUtils.isEmpty(url) && ConditionEvaluator.getInstance().evaluate(this, url);
    }

    @Override
    public XPathNode getParent() {
        return this.parent;
    }

    @Override
    public String getPath() {
        if (this instanceof IContains) {
            return this.parent.getPath() + "/Contains";
        } else if (this instanceof IEndsWith) {
            return this.parent.getPath() + "/EndsWith";
        } else if (this instanceof IEquals) {
            return this.parent.getPath() + "/Equals";
        } else if (this instanceof IInTheListOf) {
            return this.parent.getPath() + "/InTheListOf";
        } else if (this instanceof IRegexMatches) {
            return this.parent.getPath() + "/RegexMatches";
        } else if (this instanceof IStartsWith) {
            return this.parent.getPath() + "/StartsWith";
        } else {
            return this.parent.getPath() + "/UnknownCondition";
        }
    }

    @SuppressWarnings("squid:S1172")
    public void afterUnmarshal(Unmarshaller u, Object parent) {
        if (parent instanceof XPathNode) {
            this.parent = (XPathNode) parent;
        }
    }
}
