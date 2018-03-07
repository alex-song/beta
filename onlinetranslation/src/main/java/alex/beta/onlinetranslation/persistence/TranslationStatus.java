/**
 * @File: TranslationStatus.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/17 下午9:38
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

/**
 * @Description
 * @version ${project.version}
 */
public enum TranslationStatus {
    SUBMITTED("SUBMITTED"),
    PROCESSING("PROCESSING"),
    READY("READY"),
    ERROR("ERROR"),
    WARNING("WARNING"),
    NOT_AUTHORIZED("NOT_AUTHORIZED"),
    TIMEOUT("TIMEOUT");

    private String value;

    TranslationStatus(String value) {
        this.value = value;
    }

    public static TranslationStatus fromString(String value) {
        if (value == null) {
            return null;
        } else if (value.equalsIgnoreCase("SUBMITTED")) {
            return SUBMITTED;
        } else if (value.equalsIgnoreCase("PROCESSING")) {
            return PROCESSING;
        } else if (value.equalsIgnoreCase("READY")) {
            return READY;
        } else if (value.equalsIgnoreCase("WARNING")) {
            return WARNING;
        } else if (value.equalsIgnoreCase("ERROR")) {
            return ERROR;
        } else if (value.equalsIgnoreCase("NOT_AUTHORIZED")) {
            return NOT_AUTHORIZED;
        } else if (value.equalsIgnoreCase("TIMEOUT")) {
            return TIMEOUT;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return this.value;
    }
}
