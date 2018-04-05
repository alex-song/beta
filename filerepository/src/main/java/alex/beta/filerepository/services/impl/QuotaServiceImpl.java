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
import alex.beta.filerepository.persistence.entity.Quota;
import alex.beta.filerepository.persistence.repository.QuotaRepository;
import alex.beta.filerepository.services.QuotaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    public QuotaServiceImpl(QuotaRepository quotaRepository) {
        this.quotaRepository = quotaRepository;
    }

    //TODO UT
    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public void useQuota(String appid, long points) throws QuotaExceededException {
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
    public void releaseQuota(String appid, long points) {
        if (quotaRepository.findAndIncreaseUsedQuotaByAppidIgnoreCase(appid, -1 * points) == null
                && logger.isInfoEnabled()) {
            logger.info("Quota of {} doesn't exist", appid);

        }
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public long getUsedQuota(String appid) {
        Quota quota = quotaRepository.findOneByAppidIgnoreCase(appid);
        if (quota == null && logger.isInfoEnabled()) {
            logger.info("Quota of {} doesn't exist", appid);
        }
        return quota == null ? Long.MIN_VALUE : quota.getUsedQuota();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void setMaxQuota(String appid, long points) {
        quotaRepository.findAndModifyMaxQuotaByAppidIgnoreCase(appid, points);
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public long getMaxQuota(String appid) {
        Quota quota = quotaRepository.findOneByAppidIgnoreCase(appid);
        if (quota == null && logger.isInfoEnabled()) {
            logger.info("Quota of {} doesn't exist", appid);
        }
        return quota == null ? Long.MIN_VALUE : quota.getMaxQuota();
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void recalculateQuota(String... appid) {
        Map<String, Long> results = quotaRepository.aggregateUsedQuotaByAppidIgnoreCase(appid);
        Map<String, Boolean> found = new HashMap<>(appid.length);
        for (String id : appid) {
            found.put(id.toLowerCase(), Boolean.FALSE);
        }
        for (String s : results.keySet()) {
            found.put(s.toLowerCase(), Boolean.TRUE);
        }

        for (String id : found.keySet()) {
            if (found.get(id)) {
                quotaRepository.findAndModifyUsedQuotaByAppidIgnoreCase(id, results.get(id));
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("No file of {} is found", id);
                }
                quotaRepository.findAndModifyUsedQuotaByAppidIgnoreCase(id, 0L);
            }
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public List<Quota> createQuota(Quota... quotas) {
        List<Quota> qs = new ArrayList<>(quotas.length);
        for (Quota q : quotas) {
            qs.add(quotaRepository.findOneOrCreateOne(q));
        }
        return qs;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void resetUsedQuota(String... quotas) {
        for (String appid : quotas) {
            quotaRepository.findAndModifyUsedQuotaByAppidIgnoreCase(appid, 0L);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public void resetAllUsedQuota() {
        Set<String> appidInQuota = quotaRepository.findAllAppidFromQuota();
        Set<String> appidInFileInfo = quotaRepository.findAllAppidFromFileInfo();
        Set<String> merged = new HashSet<>();
        merged.addAll(appidInQuota);
        merged.addAll(appidInFileInfo);

        resetUsedQuota(merged.toArray(new String[]{}));
    }
}
