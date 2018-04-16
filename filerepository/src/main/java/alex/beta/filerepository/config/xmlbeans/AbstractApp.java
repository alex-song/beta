/**
 * @File: AbstractApp.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/8 22:01
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.config.xmlbeans;

import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractApp {
    public static final AbstractApp DEFAULT = new AbstractApp() {
        @Override
        public String getAppid() {
            return "default";
        }

        @Override
        public String getMaxQuota() {
            return "100MB";
        }
    };

    public static long parseSize(@Nonnull String size) {
        size = StringUtils.trimAllWhitespace(size).toUpperCase();
        return size.endsWith("KB")
                ? Long.valueOf(size.substring(0, size.length() - 2)) * 1024L
                : (size.endsWith("MB")
                ? Long.valueOf(size.substring(0, size.length() - 2)) * 1024L * 1024L
                : Long.valueOf(size));
    }

    public abstract String getAppid();

    public abstract String getMaxQuota();

    public long getMaxQuotaValue() {
        return parseSize(this.getMaxQuota());
    }
}
