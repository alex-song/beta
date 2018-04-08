/**
 * @File: QuotaConfig.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午8:31
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * @version ${project.version}
 * @Description
 */

@Configuration
@ConfigurationProperties(prefix = "filerepository.quota")
public class QuotaConfig {
    private List<Map<String, String>> max;

    //TODO UT
    public static long parseSize(@Nonnull String size) {
        size = StringUtils.trimAllWhitespace(size).toUpperCase();
        return size.endsWith("KB")
                ? Long.valueOf(size.substring(0, size.length() - 2)) * 1024L
                : (size.endsWith("MB")
                ? Long.valueOf(size.substring(0, size.length() - 2)) * 1024L * 1024L
                : Long.valueOf(size));
    }

    public List<Map<String, String>> getMax() {
        return max;
    }

    public void setMax(List<Map<String, String>> max) {
        this.max = max;
    }
}
