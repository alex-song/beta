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

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractCondition implements ICondition {

    @Override
    public boolean evaluate(String url) throws ConfigurationException {
        if (StringUtils.isEmpty(url)) {
            return false;
        } else {
            return ConditionEvaluator.getInstance().evaluate(this, url);
        }
    }
}
