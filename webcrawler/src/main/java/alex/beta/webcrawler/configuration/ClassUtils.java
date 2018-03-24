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

import alex.beta.webcrawler.configuration.api.IShouldVisit;
import alex.beta.webcrawler.configuration.api.IVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version ${project.version}
 * @Description
 */
public class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    private ClassUtils() {
        //hide default public constructor
    }

    public static IShouldVisit customizedShouldVisit(String clazz) throws ConfigurationException {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Instantiate {}", clazz);
            }
            return (IShouldVisit) Class.forName(clazz).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            logger.error("Cannot instantiate ShouldVisit of {}", clazz, ex);
            throw new ConfigurationException("Cannot instantiate ShouldVisit of " + clazz, ex);
        }
    }

    public static IVisitor customizedVisitor(String clazz) throws ConfigurationException {
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Instantiate {}", clazz);
            }
            return (IVisitor) Class.forName(clazz).newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            logger.error("Cannot instantiate Visitor of {}", clazz, ex);
            throw new ConfigurationException("Cannot instantiate Visitor of " + clazz, ex);
        }
    }
}
