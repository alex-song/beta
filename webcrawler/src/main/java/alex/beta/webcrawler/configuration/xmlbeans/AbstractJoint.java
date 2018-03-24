/**
 * @File: AbstractJoint.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/17 9:17
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.xmlbeans;

import alex.beta.webcrawler.configuration.ConfigurationException;
import alex.beta.webcrawler.configuration.api.*;
import org.apache.commons.lang.StringUtils;

import javax.xml.bind.Unmarshaller;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractJoint implements IJoint {

    private PathSupport parent;

    @Override
    public boolean evaluate(String url) throws ConfigurationException {
        return !StringUtils.isEmpty(url) && JointEvaluator.getInstance().evaluate(this, url);
    }

    @Override
    public String getPath() {
        if (this instanceof IAnd) {
            return this.parent.getPath() + "/And";
        } else if (this instanceof IOr) {
            return this.parent.getPath() + "/Or";
        } else if (this instanceof INot) {
            return this.parent.getPath() + "/Not";
        } else {
            return this.parent.getPath() + "/UnknownJoint";
        }
    }

    @SuppressWarnings("squid:S1172")
    public void afterUnmarshal(Unmarshaller u, Object parent) {
        if (parent instanceof PathSupport) {
            this.parent = (PathSupport) parent;
        }
    }
}
