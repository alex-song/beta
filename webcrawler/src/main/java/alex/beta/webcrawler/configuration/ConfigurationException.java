/**
 * @File: ConfigurationException.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/15 16:25
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

/**
 * @version ${project.version}
 * @Description
 */
public class ConfigurationException extends Exception {
    public ConfigurationException() {
        super();
    }

    public ConfigurationException(String msg) {
        super(msg);
    }

    public ConfigurationException(Throwable cause) {
        super(cause);
    }

    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
