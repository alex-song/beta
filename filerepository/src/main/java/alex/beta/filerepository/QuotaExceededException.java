/**
 * @File: QuotaExceededException.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午1:19
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

/**
 * @Description
 * @version ${project.version}
 */
public class QuotaExceededException extends Exception {

    public QuotaExceededException(String appid, long points, long used, long max) {
        super("File repository quota of \'" + appid + "\' is " + max + ", and it's used " + used
                + ". Cannot allocate " + points + ".");
    }

    public QuotaExceededException(String message) {
        super(message);
    }
}
