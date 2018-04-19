/**
 * @File: QuotaMetrics.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/18 下午1:38
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.models.QuotaModel;
import alex.beta.filerepository.services.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @version ${project.version}
 * @Description
 */
@Component
public class QuotaMetrics implements PublicMetrics {

    private QuotaService quotaService;

    @Autowired
    public QuotaMetrics(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    private static void addMetrics(Set<Metric<?>> qms, @Nonnull String appid, long usedQuota, long maxQuota) {
        qms.add(new Metric("frs." + appid + ".usedQuota", usedQuota));
        qms.add(new Metric("frs." + appid + ".maxQuota", maxQuota));
    }

    @Override
    public Collection<Metric<?>> metrics() {
        List<QuotaModel> quotas = quotaService.findAll();
        Set<Metric<?>> qms = new LinkedHashSet<>(quotas.size());
        quotas.forEach(quota -> addMetrics(qms, quota.getAppid(), quota.getUsedQuota(), quota.getMaxQuota()));
        return qms;
    }
}
