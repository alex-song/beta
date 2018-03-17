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
package alex.beta.webcrawler.configuration.api;

import alex.beta.webcrawler.configuration.ConfigurationException;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractJoint implements IJoint {

    public abstract String getJointClass();

    @Override
    public boolean evaluate(String url) throws ConfigurationException {
        //TODO this
        return true;
    }
}
