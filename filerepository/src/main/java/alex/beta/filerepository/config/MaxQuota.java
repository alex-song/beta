package alex.beta.filerepository.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by songlip on 2018/4/8.
 */
@AllArgsConstructor
public class MaxQuota {
    @Getter
    private String appid;
    @Getter
    private long maxQuota;

    public static class DefaultMaxQuota extends MaxQuota {
        public DefaultMaxQuota(long maxQuota) {
            super("default", maxQuota);
        }
    }
}
