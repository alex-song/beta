package alex.beta.filerepository;

import alex.beta.filerepository.models.QuotaModel;
import alex.beta.filerepository.services.QuotaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.PublicMetrics;
import org.springframework.boot.actuate.metrics.Metric;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by songlip on 2018/4/18.
 */
@Component
public class QuotaMetrics implements PublicMetrics {

    private QuotaService quotaService;

    @Autowired
    public QuotaMetrics(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @Override
    public Collection<Metric<?>> metrics() {
        List<QuotaModel> quotas = quotaService.findAll();
        List<Metric<?>> qms = new ArrayList<>(quotas.size());
        quotas.forEach(quota -> addMetrics(qms, quota.getAppid(), quota.getUsedQuota(), quota.getMaxQuota()));
        return qms;
    }

    private static void addMetrics(List<Metric<?>> qms, @Nonnull String appid, long usedQuota, long maxQuota) {
        qms.add(new Metric("frs." + appid + ".usedQuota", usedQuota));
        qms.add(new Metric("frs." + appid + ".maxQuota", maxQuota));
    }
}
