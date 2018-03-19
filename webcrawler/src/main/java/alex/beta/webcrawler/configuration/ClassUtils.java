/**
 * @File: ClassUtils.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/18 9:51
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.ICondition;
import alex.beta.webcrawler.configuration.api.IJoint;
import alex.beta.webcrawler.configuration.api.IShouldVisit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description
 * @version ${project.version}
 */
public class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    public static ICondition customizedCondition(String clazz) throws ConfigurationException {
        try {
            return (ICondition) Class.forName(clazz).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            logger.error("Cannot instantiate Condition of {}", clazz, ex);
            throw new ConfigurationException("Cannot instantiate Condition of " + clazz, ex);
        }
    }

    public static IJoint customizedJoint(String clazz) throws ConfigurationException {
        try {
            return (IJoint) Class.forName(clazz).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            logger.error("Cannot instantiate Joint of {}", clazz, ex);
            throw new ConfigurationException("Cannot instantiate Joint of " + clazz, ex);
        }
    }

    public static IShouldVisit customizedShouldVisit(String clazz) throws ConfigurationException {
        try {
            return (IShouldVisit) Class.forName(clazz).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            logger.error("Cannot instantiate ShouldVisit of {}", clazz, ex);
            throw new ConfigurationException("Cannot instantiate ShouldVisit of " + clazz, ex);
        }
    }
}
