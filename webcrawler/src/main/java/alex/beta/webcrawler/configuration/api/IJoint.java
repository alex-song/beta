/**
 * @File: IJoint.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/14 23:02
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
public interface IJoint {
    boolean isValue();

    List<? extends ICondition> getConditions();

    List<? extends IJoint> getJoints();
}
