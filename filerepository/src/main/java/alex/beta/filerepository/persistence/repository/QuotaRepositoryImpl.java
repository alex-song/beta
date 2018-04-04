/**
 * @File: QuotaRepositoryImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午10:22
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.QuotaConfig;
import alex.beta.filerepository.persistence.entity.Quota;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * @version ${project.version}
 * @Description
 */

@Repository
public class QuotaRepositoryImpl implements QuotaRepository {

    private static final Logger logger = LoggerFactory.getLogger(QuotaRepositoryImpl.class);

    private MongoOperations mongoOperations;

    private QuotaConfig quotaConfig;

    private long defaultMax = 1000 * 1000 * 1000L;

    private ConcurrentMap<String, Long> defaultAppMaxQuotaMap;

    @Autowired
    public QuotaRepositoryImpl(MongoOperations mongoOperations, QuotaConfig quotaConfig) {
        this.mongoOperations = mongoOperations;
        this.quotaConfig = quotaConfig;

        // 组建默认值Map
        defaultAppMaxQuotaMap = new ConcurrentHashMap<>(quotaConfig.getMax().size());
        for (Map<String, String> config : quotaConfig.getMax()) {
            if (!config.isEmpty()) {
                String key = config.keySet().iterator().next();
                String tmp = config.get(key);
                if ("default".equalsIgnoreCase(key)) {
                    if (!StringUtils.isEmpty(tmp)) {
                        try {
                            defaultMax = Long.parseLong(tmp) * 1000 * 1000;
                        } catch (NumberFormatException ex) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Invalid default max quota configuration ({}), use default 1000 instead.", tmp, ex);
                            }
                        }
                    }
                    defaultAppMaxQuotaMap.put("default", defaultMax);
                } else {
                    if (!StringUtils.isEmpty(tmp)) {
                        try {
                            defaultAppMaxQuotaMap.put(key.toLowerCase(), Long.parseLong(tmp) * 1000 * 1000L);
                        } catch (NumberFormatException ex) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Invalid default max quota configuration ({}) of appid {}.", tmp, key, ex);
                            }
                        }
                    }
                }
            }
        }
        // output default max quota setting
        Iterator<String> keys = defaultAppMaxQuotaMap.keySet().iterator();
        logger.debug("Default max quotas:");
        while(keys.hasNext()){
            String key = keys.next();
            Long value = defaultAppMaxQuotaMap.get(key);
            logger.debug("{} : {}", key, value);
        }
    }

    @Override
    @Transactional
    public Quota findOneOrCreateOneByAppidIgnoreCase(@Nonnull String appid) {
        Quota quota = findOneByAppidIgnoreCase(appid);
        if (quota == null) {
            logger.info("Quota of {} doesn't exist, will create one according to default configuration.", appid);
            initializeQuota(appid);
            quota = findOneByAppidIgnoreCase(appid);
        }
        return quota;
    }

    @Override
    @Transactional
    public Quota findOneOrCreateOne(@Nonnull Quota quota) {
        Objects.requireNonNull(quota.getAppid());

        Quota q = findOneByAppidIgnoreCase(quota.getAppid());
        if (q == null) {
            logger.info("Quota of {} doesn't exist, will create one according to given quota setting.", quota.getAppid());
            initializeQuota(quota);
            quota = findOneByAppidIgnoreCase(quota.getAppid());
        }
        return quota;
    }

    @Override
    @Transactional
    public Quota findAndIncreaseUsedQuotaByAppidIgnoreCase(@Nonnull String appid, long points) {
        return mongoOperations.findAndModify(
                new Query(Criteria.where("appid").regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                new Update().inc("usedQuota", points),
                new FindAndModifyOptions().returnNew(true),
                Quota.class);
    }

    @Override
    @Transactional
    public Quota findAndModifyMaxQuotaByAppidIgnoreCase(@Nonnull String appid, long points) {
        return mongoOperations.findAndModify(
                new Query(Criteria.where("appid").regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                new Update().set("maxQuota", points),
                new FindAndModifyOptions().returnNew(true),
                Quota.class);
    }

    @Override
    public Quota findOneByAppidIgnoreCase(@Nonnull String appid) {
        return mongoOperations.findOne(
                new Query(Criteria.where("appid").regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                Quota.class);
    }

    @Override
    @Transactional
    public void resetUsedQuota(String... quotas) {
        for (String appid : quotas) {
            mongoOperations.findAndModify(
                    new Query(Criteria.where("appid").regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                    new Update().set("usedQuota", 0L),
                    Quota.class);
        }
    }

    private synchronized boolean initializeQuota(@Nonnull String appid) {
        try {
            Quota q = Quota.builder().appid(appid).usedQuota(0L).maxQuota(getDefaultMaxQuota(appid)).build();
            mongoOperations.insert(q, "Quota");
            return true;
        } catch (DuplicateKeyException ex) {
            logger.info("Quota of {} already exists", appid);
            return false;
        }
    }

    private synchronized boolean initializeQuota(Quota quota) {
        try {
            Quota q = Quota.builder().appid(quota.getAppid())
                    .usedQuota(quota.getUsedQuota())
                    .maxQuota(quota.getMaxQuota()).build();
            mongoOperations.insert(q, "Quota");
            return true;
        } catch (DuplicateKeyException ex) {
            logger.info("Quota of {} already exists", quota.getAppid());
            return false;
        }
    }

    private long getDefaultMaxQuota(@Nonnull String appid) {
        return defaultAppMaxQuotaMap.containsKey(appid.toLowerCase())
                ? defaultAppMaxQuotaMap.get(appid.toLowerCase())
                : defaultMax;
    }
}
