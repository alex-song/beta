/**
 * @File: QuotaServiceImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午1:27
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services.impl;

import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.models.QuotaModel;
import alex.beta.filerepository.persistence.entity.Quota;
import alex.beta.filerepository.persistence.repository.FileInfoCustomizedRepository;
import alex.beta.filerepository.persistence.repository.QuotaRepository;
import alex.beta.filerepository.services.QuotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;

import static alex.beta.filerepository.SecurityConfig.*;

/**
 * @version ${project.version}
 * @Description
 */

@Service("quotaService")
public class QuotaServiceImpl implements QuotaService {

    private static final Logger logger = LoggerFactory.getLogger(QuotaServiceImpl.class);

    private QuotaRepository quotaRepository;

    private FileInfoCustomizedRepository fileInfoCustomizedRepository;

    @Autowired
    public QuotaServiceImpl(QuotaRepository quotaRepository, FileInfoCustomizedRepository fileInfoCustomizedRepository) {
        this.quotaRepository = quotaRepository;
        this.fileInfoCustomizedRepository = fileInfoCustomizedRepository;
    }

    //TODO UT
    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public void useQuota(@Nonnull String appid, long points) throws QuotaExceededException {
        if (logger.isDebugEnabled()) {
            logger.debug("Use quota {} of {}", points, appid);
        }
        Quota quota = quotaRepository.findOneOrCreateOneByAppidIgnoreCase(appid);

        if (points + quota.getUsedQuota() > quota.getMaxQuota()) {
            throw new QuotaExceededException(appid, points, quota.getUsedQuota(), quota.getMaxQuota());
        } else {
            quota = quotaRepository.findAndIncreaseUsedQuotaByAppidIgnoreCase(appid, points);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Current quota of {} is {}", appid, quota);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public void releaseQuota(@Nonnull String appid, long points) {
        if (quotaRepository.findAndIncreaseUsedQuotaByAppidIgnoreCase(appid, -1 * points) == null
                && logger.isInfoEnabled()) {
            logger.info("Quota of {} doesn't exist", appid);
        }
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public long getUsedQuota(@Nonnull String appid) {
        Quota quota = quotaRepository.findOneByAppidIgnoreCase(appid);
        if (quota == null && logger.isInfoEnabled()) {
            logger.info("Quota of {} doesn't exist", appid);
        }
        return quota == null ? Long.MIN_VALUE : quota.getUsedQuota();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void setMaxQuota(@Nonnull String appid, long points) {
        quotaRepository.findAndModifyMaxQuotaByAppidIgnoreCase(appid, points);
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public long getMaxQuota(@Nonnull String appid) {
        Quota quota = quotaRepository.findOneByAppidIgnoreCase(appid);
        if (quota == null && logger.isInfoEnabled()) {
            logger.info("Quota of {} doesn't exist", appid);
        }
        return quota == null ? Long.MIN_VALUE : quota.getMaxQuota();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void recalculateQuota(@Nonnull String... appid) {
        if (appid.length == 0) {
            return;
        }
        Map<String, Long> results = quotaRepository.aggregateUsedQuotaByAppidIgnoreCase(appid);
        Map<String, Boolean> found = new HashMap<>(appid.length);
        for (String id : appid) {
            found.put(id.toLowerCase(), Boolean.FALSE);
        }
        for (String s : results.keySet()) {
            found.put(s.toLowerCase(), Boolean.TRUE);
        }

        for (Map.Entry<String, Boolean> entry : found.entrySet()) {
            if (entry.getValue()) {
                quotaRepository.findAndModifyUsedQuotaByAppidIgnoreCase(entry.getKey(), results.get(entry.getKey()));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No file of {} is found", entry.getKey());
                }
                quotaRepository.findAndModifyUsedQuotaByAppidIgnoreCase(entry.getKey(), 0L);
            }
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void recalculateAllQuotas() {
        Set<String> appidInQuota = quotaRepository.findAllAppid();
        Set<String> appidInFileInfo = fileInfoCustomizedRepository.findAllAppid();
        Set<String> merged = new HashSet<>();
        merged.addAll(appidInQuota);
        merged.addAll(appidInFileInfo);

        recalculateQuota(merged.toArray(new String[]{}));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public List<QuotaModel> createQuota(@Nonnull Quota... quotas) {
        List<QuotaModel> qs = new ArrayList<>(quotas.length);
        for (Quota q : quotas) {
            qs.add(new QuotaModel(quotaRepository.findOneOrCreateOne(q)));
        }
        return qs;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void resetUsedQuota(@Nonnull String... appids) {
        for (String appid : appids) {
            quotaRepository.findAndModifyUsedQuotaByAppidIgnoreCase(appid, 0L);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void resetAllUsedQuotas() {
        Set<String> appidInQuota = quotaRepository.findAllAppid();
        Set<String> appidInFileInfo = fileInfoCustomizedRepository.findAllAppid();
        Set<String> merged = new HashSet<>();
        merged.addAll(appidInQuota);
        merged.addAll(appidInFileInfo);

        resetUsedQuota(merged.toArray(new String[]{}));
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public QuotaModel findByAppidIgnoreCase(@Nonnull String appid) {
        Quota quota = quotaRepository.findOneByAppidIgnoreCase(appid);
        if (quota == null) {
            return null;
        } else {
            return new QuotaModel(quota);
        }
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public List<QuotaModel> findAll() {
        List<Quota> quotas = quotaRepository.findAll();
        if (quotas == null || quotas.isEmpty()) {
            return Collections.emptyList();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("There are {} quotas", quotas.size());
        }
        List<QuotaModel> models = new ArrayList<>(quotas.size());
        quotas.forEach(quota -> models.add(new QuotaModel(quota)));
        return models;
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public QuotaModel update(@Nonnull Quota quota) {
        Quota q;
        if (StringUtils.isEmpty(quota.getId())) {
            Objects.requireNonNull(quota.getAppid());

            q = quotaRepository.findOneByAppidIgnoreCase(quota.getAppid());
        } else {
            q = quotaRepository.get(quota.getId());
        }
        if (q != null) {
            q.setMaxQuota(quota.getMaxQuota());
            q.setUsedQuota(quota.getUsedQuota());
            quotaRepository.save(q);
        }
        return q == null ? null : new QuotaModel(q);
    }
}
