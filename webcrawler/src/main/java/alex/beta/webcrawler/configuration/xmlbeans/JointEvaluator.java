/**
 * @File: JointEvaluator.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/18 9:46
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.xmlbeans;

import alex.beta.webcrawler.configuration.ClassUtils;
import alex.beta.webcrawler.configuration.ConfigurationException;
import alex.beta.webcrawler.configuration.api.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
public class JointEvaluator {
    private static final Logger logger = LoggerFactory.getLogger(JointEvaluator.class);

    private static JointEvaluator ourInstance = new JointEvaluator();

    private JointEvaluator() {
    }

    public static JointEvaluator getInstance() {
        return ourInstance;
    }

    private static void startLog(String url, XPathNode node) {
        if (logger.isDebugEnabled()) {
            logger.debug("Evaluating \'{}\' at {}", url, node.getPath());
        }
    }

    private static void endLog(String url, XPathNode node, boolean value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Evaluated \'{}\' at \'{}\', and result is {}.", url, node.getPath(), value);
        }
    }

    private static void invalidJointLog(String url, XPathNode node) {
        if (logger.isWarnEnabled()) {
            logger.warn("Evaluated \'{}\' at \'{}\', and result is false. Because there is no condition or joint inside.", url, node.getPath());
        }
    }

    public boolean evaluate(IJoint joint, String url) throws ConfigurationException {
        boolean value;
        if (StringUtils.isEmpty(joint.getJointClass())) {
            if (joint instanceof IAnd) {
                value = evaluateAnd((IAnd) joint, url);
            } else if (joint instanceof IOr) {
                value = evaluateOr((IOr) joint, url);
            } else if (joint instanceof INot) {
                value = evaluateNot((INot) joint, url);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Unsupported Joint \'{}\' at {}", joint.getClass().getSimpleName(), joint.getPath());
                }
                throw new ConfigurationException("Unsupported Joint \'"
                        + joint.getClass().getSimpleName() + "\' at " + joint.getPath());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating \'{}\' using customized Joint class \'{}\' at {}", url, joint.getJointClass(), joint.getPath());
            }
            value = ClassUtils.customizedJoint(joint.getJointClass()).evaluate(url);
            endLog(url, joint, value);
        }
        return value;
    }

    private boolean evaluateAnd(IAnd and, String url) throws ConfigurationException {
        startLog(url, and);

        List<? extends ICondition> conditions = and.getCondition();
        List<? extends IJoint> joints = and.getJoint();
        if ((conditions == null || conditions.isEmpty()) && (joints == null || joints.isEmpty())) {
            invalidJointLog(url, and);
            return false;
        } else {
            if (conditions != null && !conditions.isEmpty()) {
                for (ICondition c : conditions) {
                    if (!c.evaluate(url)) {
                        endLog(url, and, false);
                        return false;
                    }
                }
            }
            if (joints != null && !joints.isEmpty()) {
                for (IJoint j : joints) {
                    if (!j.evaluate(url)) {
                        endLog(url, and, false);
                        return false;
                    }
                }
            }
            endLog(url, and, true);
            return true;
        }
    }

    private boolean evaluateOr(IOr or, String url) throws ConfigurationException {
        startLog(url, or);

        List<? extends ICondition> conditions = or.getCondition();
        List<? extends IJoint> joints = or.getJoint();
        if ((conditions == null || conditions.isEmpty()) && (joints == null || joints.isEmpty())) {
            invalidJointLog(url, or);
            return false;
        } else {
            if (conditions != null && !conditions.isEmpty()) {
                for (ICondition c : conditions) {
                    if (c.evaluate(url)) {
                        endLog(url, or, true);
                        return true;
                    }
                }
            }
            if (joints != null && !joints.isEmpty()) {
                for (IJoint j : joints) {
                    if (j.evaluate(url)) {
                        endLog(url, or, true);
                        return true;
                    }
                }
            }
            endLog(url, or, false);
            return false;
        }
    }

    private boolean evaluateNot(INot not, String url) throws ConfigurationException {
        startLog(url, not);

        List<? extends ICondition> conditions = not.getCondition();
        List<? extends IJoint> joints = not.getJoint();
        boolean value;
        if ((conditions == null || conditions.isEmpty()) && (joints == null || joints.isEmpty())) {
            invalidJointLog(url, not);
            return false;
        } else if ((conditions != null && !conditions.isEmpty()) && (joints != null && !joints.isEmpty())) {
            if (logger.isErrorEnabled()) {
                logger.error("Only one Condition or Joint can be defined in {}.", not.getPath());
            }
            throw new ConfigurationException("Only one Condition or Joint can be defined in " + not.getPath() + ".");
        } else if (conditions != null && !conditions.isEmpty()) {
            if (conditions.size() > 1) {
                if (logger.isErrorEnabled()) {
                    logger.error("Only one Condition can be defined in {}.", not.getPath());
                }
                throw new ConfigurationException("Only one Condition can be defined in " + not.getPath() + ".");
            } else {
                value = !conditions.get(0).evaluate(url);
            }
        } else {
            if (joints == null) {
                invalidJointLog(url, not);
                return false;
            } else {
                if (joints.size() > 1) {
                    if (logger.isErrorEnabled()) {
                        logger.error("Only one Joint can be defined in {}.", not.getPath());
                    }
                    throw new ConfigurationException("Only one Joint can be defined in " + not.getPath() + ".");
                } else {
                    value = !joints.get(0).evaluate(url);
                }
            }
        }
        endLog(url, not, value);
        return value;
    }
}
