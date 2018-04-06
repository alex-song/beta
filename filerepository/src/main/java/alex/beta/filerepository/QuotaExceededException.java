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

import lombok.Getter;

/**
 * @version ${project.version}
 * @Description
 */
public class QuotaExceededException extends Exception {

    @Getter
    private String appid;

    @Getter
    private long points;

    @Getter
    private long used;

    @Getter
    private long max;

    public QuotaExceededException(String appid, long points, long used, long max) {
        super("Quota of file repository \'" + appid + "\' is " + max + ", and it's been used " + used
                + ". Cannot allocate " + points + ".");
        this.appid = appid;
        this.points = points;
        this.used = used;
        this.max = max;
    }
}
