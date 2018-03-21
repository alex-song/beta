/**
 * @File: ConditionEvaluator.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/17 10:16
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.xmlbeans;

import alex.beta.webcrawler.configuration.ClassUtils;
import alex.beta.webcrawler.configuration.ConfigurationException;
import alex.beta.webcrawler.configuration.api.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version ${project.version}
 * @Description
 */
public class ConditionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ConditionEvaluator.class);

    private static ConditionEvaluator ourInstance = new ConditionEvaluator();

    private ConditionEvaluator() {
    }

    public static ConditionEvaluator getInstance() {
        return ourInstance;
    }

    private static void startLog(String url, XPathNode node) {
        if (logger.isDebugEnabled()) {
            logger.debug("Evaluating \'{}\' at {}.", url, node.getPath());
        }
    }

    private static void endLog(String url, XPathNode node, boolean value) {
        if (logger.isDebugEnabled()) {
            logger.debug("Evaluated \'{}\' at \'{}\', and result is {}.", url, node.getPath(), value);
        }
    }

    public boolean evaluate(ICondition condition, String url) throws ConfigurationException {
        boolean value;
        if (StringUtils.isEmpty(condition.getConditionClass())) {
            if (condition instanceof IContains) {
                value = evaluateContains((IContains) condition, url);
            } else if (condition instanceof IEndsWith) {
                value = evaluateEndsWith((IEndsWith) condition, url);
            } else if (condition instanceof IEquals) {
                value = evaluateEquals((IEquals) condition, url);
            } else if (condition instanceof IInTheListOf) {
                value = evaluateInTheListOf((IInTheListOf) condition, url);
            } else if (condition instanceof IRegexMatches) {
                value = evaluateRegexMatches((IRegexMatches) condition, url);
            } else if (condition instanceof IStartsWith) {
                value = evaluateStartsWith((IStartsWith) condition, url);
            } else {
                if (logger.isErrorEnabled()) {
                    logger.error("Unsupported Condition \'{}\' at {}", condition.getClass().getSimpleName(), condition.getPath());
                }
                throw new ConfigurationException("Unsupported Condition \'"
                        + condition.getClass().getSimpleName() + "\' at " + condition.getPath());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating \'{}\' using customized Condition class \'{}\' at {}", url, condition.getConditionClass(), condition.getPath());
            }
            value = ClassUtils.customizedCondition(condition.getConditionClass()).evaluate(url);
            endLog(url, condition, value);
        }
        return value;
    }

    private boolean evaluateContains(IContains contains, String url) throws ConfigurationException {
        startLog(url, contains);

        if (StringUtils.isEmpty(contains.getText())) {
            if (logger.isErrorEnabled()) {
                logger.error("Text is not specified in Contains at \'{}\'", contains.getPath());
            }
            throw new ConfigurationException("Text is not specified in Contains");
        } else {
            boolean value;
            if (contains.isCaseSensitive()) {
                value = url.contains(contains.getText());
            } else {
                value = url.toLowerCase().contains(contains.getText().toLowerCase());
            }
            endLog(url, contains, value);
            return value;
        }
    }

    private boolean evaluateEndsWith(IEndsWith endsWith, String url) throws ConfigurationException {
        startLog(url, endsWith);

        if (StringUtils.isEmpty(endsWith.getSuffix())) {
            if (logger.isErrorEnabled()) {
                logger.error("Suffix is not specified in EndsWith at \'{}\'", endsWith.getPath());
            }
            throw new ConfigurationException("Suffix is not specified in EndsWith");
        } else {
            boolean value;
            if (endsWith.isCaseSensitive()) {
                value = url.endsWith(endsWith.getSuffix());
            } else {
                value = url.toLowerCase().endsWith(endsWith.getSuffix().toLowerCase());
            }
            endLog(url, endsWith, value);
            return value;
        }
    }

    private boolean evaluateEquals(IEquals equals, String url) throws ConfigurationException {
        startLog(url, equals);

        if (StringUtils.isEmpty(equals.getText())) {
            if (logger.isErrorEnabled()) {
                logger.error("Text is not specified in Equals at \'{}\'", equals.getPath());
            }
            throw new ConfigurationException("Text is not specified in Equals");
        } else {
            boolean value;
            if (equals.isCaseSensitive()) {
                value = url.equals(equals.getText());
            } else {
                value = url.equalsIgnoreCase(equals.getText());
            }
            endLog(url, equals, value);
            return value;
        }
    }

    private boolean evaluateInTheListOf(IInTheListOf inTheListOf, String url) throws ConfigurationException {
        startLog(url, inTheListOf);

        if (inTheListOf.getUrl() == null || inTheListOf.getUrl().isEmpty()) {
            if (logger.isErrorEnabled()) {
                logger.error("Url is not specified in InTheListOf at \'{}\'", inTheListOf.getPath());
            }
            throw new ConfigurationException("Url is not specified in InTheListOf");
        } else {
            for (String tmp : inTheListOf.getUrl()) {
                if (url.equals(tmp)) {
                    endLog(url, inTheListOf, true);
                    return true;
                }
            }
        }
        endLog(url, inTheListOf, false);
        return false;
    }

    private boolean evaluateRegexMatches(IRegexMatches regexMatches, String url) throws ConfigurationException {
        startLog(url, regexMatches);

        if (StringUtils.isEmpty(regexMatches.getRegex())) {
            if (logger.isErrorEnabled()) {
                logger.error("Regex is not specified in RegexMatches at \'{}\'", regexMatches.getPath());
            }
            throw new ConfigurationException("Regex is not specified in RegexMatches");
        } else {
            boolean value = url.matches(regexMatches.getRegex());
            endLog(url, regexMatches, value);
            return value;
        }
    }

    private boolean evaluateStartsWith(IStartsWith startsWith, String url) throws ConfigurationException {
        startLog(url, startsWith);

        if (StringUtils.isEmpty(startsWith.getPrefix())) {
            if (logger.isErrorEnabled()) {
                logger.error("Prefix is not specified in StartsWith at \'{}\'", startsWith.getPath());
            }
            throw new ConfigurationException("Prefix is not specified in StartsWith");
        } else {
            boolean value;
            if (startsWith.isCaseSensitive()) {
                value = url.startsWith(startsWith.getPrefix());
            } else {
                value = url.toLowerCase().startsWith(startsWith.getPrefix().toLowerCase());
            }
            endLog(url, startsWith, value);
            return value;
        }
    }
}
