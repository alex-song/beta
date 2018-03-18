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

    public boolean evaluate(ICondition condition, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(condition.getConditionClass())) {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating {} using Condition {}", url, condition.getClass().getSimpleName());
            }
            if (condition instanceof IContains) {
                return evaluateContains((IContains) condition, url);
            } else if (condition instanceof IEndsWith) {
                return evaluateEndsWith((IEndsWith) condition, url);
            } else if (condition instanceof IEquals) {
                return evaluateEquals((IEquals) condition, url);
            } else if (condition instanceof IInTheListOf) {
                return evaluateInTheListOf((IInTheListOf) condition, url);
            } else if (condition instanceof IRegexMatches) {
                return evaluateRegexMatches((IRegexMatches) condition, url);
            } else if (condition instanceof IStartsWith) {
                return evaluateStartsWith((IStartsWith) condition, url);
            } else {
                throw new ConfigurationException("Unsupported Condition " + condition.getClass().getSimpleName());
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Evaluating {} using customized Condition {}", url, condition.getConditionClass());
            }
            return ClassUtils.customizedCondition(condition.getConditionClass()).evaluate(url);
        }
    }

    private boolean evaluateContains(IContains contains, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(contains.getText())) {
            throw new ConfigurationException("Text is not specified in Contains");
        } else {
            if (contains.isCaseSensitive()) {
                return url.contains(contains.getText());
            } else {
                return url.toLowerCase().contains(contains.getText().toLowerCase());
            }
        }
    }

    private boolean evaluateEndsWith(IEndsWith endsWith, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(endsWith.getSuffix())) {
            throw new ConfigurationException("Suffix is not specified in EndsWith");
        } else {
            if (endsWith.isCaseSensitive()) {
                return url.endsWith(endsWith.getSuffix());
            } else {
                return url.toLowerCase().endsWith(endsWith.getSuffix().toLowerCase());
            }
        }
    }

    private boolean evaluateEquals(IEquals equals, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(equals.getText())) {
            throw new ConfigurationException("Text is not specified in Equals");
        } else {
            if (equals.isCaseSensitive()) {
                return url.equals(equals.getText());
            } else {
                return url.equalsIgnoreCase(equals.getText());
            }
        }
    }

    private boolean evaluateInTheListOf(IInTheListOf inTheListOf, String url) throws ConfigurationException {
        if (inTheListOf.getUrl() == null || inTheListOf.getUrl().isEmpty()) {
            throw new ConfigurationException("Url is not specified in InTheListOf");
        } else {
            for (String tmp : inTheListOf.getUrl()) {
                if (url.equals(tmp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean evaluateRegexMatches(IRegexMatches regexMatches, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(regexMatches.getRegex())) {
            throw new ConfigurationException("Regex is not specified in RegexMatches");
        } else {
            return url.matches(regexMatches.getRegex());
        }
    }

    private boolean evaluateStartsWith(IStartsWith startsWith, String url) throws ConfigurationException {
        if (StringUtils.isEmpty(startsWith.getPrefix())) {
            throw new ConfigurationException("Prefix is not specified in StartsWith");
        } else {
            if (startsWith.isCaseSensitive()) {
                return url.startsWith(startsWith.getPrefix());
            } else {
                return url.toLowerCase().startsWith(startsWith.getPrefix().toLowerCase());
            }
        }
    }
}
