/**
 * @File: IRegexMatches.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/16 下午10:39
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

/**
 * @version ${project.version}
 * @Description
 */
public interface IRegexMatches extends ICondition {
    String getRegex();

    boolean isToLowerCase();
}
