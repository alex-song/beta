/**
 * @File: HousekeepingJob.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/14 下午9:41
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services;

import alex.beta.filerepository.config.xmlbeans.IFrsConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @version ${project.version}
 * @Description
 */

@Component
@ConditionalOnProperty(value = "frs.housekeeping.enabled", havingValue = "true")
public class HousekeepingJob {
    private static final Logger logger = LoggerFactory.getLogger(HousekeepingJob.class);

    private IFrsConfig frsConfig;

    private FileRepositoryService fileRepositoryService;

    private QuotaService quotaService;

    public HousekeepingJob(IFrsConfig frsConfig, FileRepositoryService fileRepositoryService, QuotaService quotaService) {
        this.frsConfig = frsConfig;
        this.fileRepositoryService = fileRepositoryService;
        this.quotaService = quotaService;
    }

    @ConditionalOnProperty(value = "frs.housekeeping.enabled", havingValue = "true")
    @Scheduled(fixedRate = 60 * 60 * 1000L, initialDelay = 30000L)
    public void executeHousekeepingJob() {
        Set<String> appids = fileRepositoryService.findAllAppid();
        List<String> changedApp = new ArrayList<>();

        if (frsConfig.isDeleteExpiredFiles()) {
            for (String appid : appids) {
                int deletedFiles = fileRepositoryService.deleteExpiredFiles(appid, null);
                if (deletedFiles > 0) {
                    if (logger.isInfoEnabled()) {
                        logger.info("Deleted {} expired files in app \'{}\'", deletedFiles, appid);
                    }
                    changedApp.add(appid);
                } else {
                    if (logger.isInfoEnabled()) {
                        logger.info("No expired files in app \'{}\'", appid);
                    }
                }
            }
        } else {
            changedApp.addAll(appids);
        }

        if (frsConfig.isRecalculateQuotas()) {
            if (logger.isInfoEnabled()) {
                logger.info("Recalculating all quotas");
            }
            quotaService.recalculateQuota(changedApp.toArray(new String[]{}));
        }
    }
}
