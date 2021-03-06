/**
 * @File: ContentValidationException.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午1:07
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import lombok.Getter;

import javax.annotation.Nonnull;

/**
 * @version ${project.version}
 * @Description
 */
public class ContentValidationException extends Exception {

    @Getter
    private final String expected;

    @Getter
    private final String actual;

    public ContentValidationException(@Nonnull String expected, @Nonnull String actual) {
        super("File content MD5 validation is failed. Expected value is \'" + expected + "\', but actual value is \'" + actual + "\'.");
        this.actual = actual;
        this.expected = expected;
    }
}
