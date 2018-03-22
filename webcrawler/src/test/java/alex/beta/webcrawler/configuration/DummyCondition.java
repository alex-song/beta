/**
 * @File: DummyCondition.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/21 下午9:25
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.xmlbeans.AbstractCondition;

/**
 * @version ${project.version}
 * @Description
 */
public class DummyCondition extends AbstractCondition {

    public String getConditionClass() {
        return null;
    }

    @Override
    public boolean evaluate(String url) {
        return true;
    }
}
