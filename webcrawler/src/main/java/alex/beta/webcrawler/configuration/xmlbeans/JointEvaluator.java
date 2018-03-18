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

    public boolean evaluate(IJoint joint, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(joint.getJointClass())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating {} using Joint {}", url, joint.getClass().getSimpleName());
            }
            if (joint instanceof IAnd) {
                return evaluateAnd((IAnd) joint, url);
            } else if (joint instanceof IOr) {
                return evaluateOr((IOr) joint, url);
            } else if (joint instanceof INot) {
                return evaluateNot((INot) joint, url);
            } else {
                throw new ConfigurationException("Unsupported Joint " + joint.getClass().getSimpleName());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating {} using customized Joint {}", url, joint.getJointClass());
            }
            return ClassUtils.customizedJoint(joint.getJointClass()).evaluate(url);
        }
    }

    private boolean evaluateAnd(IAnd and, String url) throws ConfigurationException {
        List<? extends ICondition> conditions = and.getConditions();
        List<? extends IJoint> joints = and.getJoints();
        if ((conditions == null || conditions.isEmpty()) && (joints == null || joints.isEmpty())) {
            return false;
        } else {
            boolean flag = true;
            if (conditions != null && !conditions.isEmpty()) {
                for (ICondition c : conditions) {
                    flag = flag && c.evaluate(url);
                    if (!flag) {
                        return false;
                    }
                }
            }
            if (joints != null && !joints.isEmpty()) {
                for (IJoint j : joints) {
                    flag = flag && j.evaluate(url);
                    if (!flag) {
                        return false;
                    }
                }
            }
            return flag;
        }
    }

    private boolean evaluateOr(IOr or, String url) throws ConfigurationException {
        List<? extends ICondition> conditions = or.getConditions();
        List<? extends IJoint> joints = or.getJoints();
        if ((conditions == null || conditions.isEmpty()) && (joints == null || joints.isEmpty())) {
            return false;
        } else {
            boolean flag = false;
            if (conditions != null && !conditions.isEmpty()) {
                for (ICondition c : conditions) {
                    flag = flag || c.evaluate(url);
                    if (flag) {
                        return true;
                    }
                }
            }
            if (joints != null && !joints.isEmpty()) {
                for (IJoint j : joints) {
                    flag = flag || j.evaluate(url);
                    if (flag) {
                        return true;
                    }
                }
            }
            return flag;
        }
    }

    private boolean evaluateNot(INot not, String url) throws ConfigurationException {
        List<? extends ICondition> conditions = not.getConditions();
        List<? extends IJoint> joints = not.getJoints();
        if ((conditions == null || conditions.isEmpty()) && (joints == null || joints.isEmpty())) {
            return false;
        } else if ((conditions != null && !conditions.isEmpty()) && (joints != null && !joints.isEmpty())) {
            throw new ConfigurationException("Only one Condition or Joint can be defined in Not.");
        } else if (conditions != null && !conditions.isEmpty()) {
            if (conditions.size() > 1) {
                throw new ConfigurationException("Only one Condition can be defined in Not.");
            } else {
                return !conditions.get(0).evaluate(url);
            }
        } else {
            if (joints.size() > 1) {
                throw new ConfigurationException("Only one Joint can be defined in Not.");
            } else {
                return !joints.get(0).evaluate(url);
            }
        }
    }
}
