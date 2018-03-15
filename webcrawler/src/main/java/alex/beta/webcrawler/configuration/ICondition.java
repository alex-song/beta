/**
 * @File: ICondition.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/14 22:30
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

/**
 * @version ${project.version}
 * @Description
 */
public interface ICondition {
    boolean isValue();

    void setValue(Boolean value);

    String getConditionClass();

    void setConditionClass(String conditionClass);
}
