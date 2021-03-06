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

import alex.beta.filerepository.config.xmlbeans.IFrsConfig;
import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.entity.Quota;
import com.mongodb.BasicDBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import static alex.beta.filerepository.config.xmlbeans.AbstractApp.DEFAULT;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;

/**
 * @version ${project.version}
 * @Description
 */

@Repository
public class QuotaRepositoryImpl implements QuotaRepository {

    static final String APPID_FIELD_NAME = "appid";

    static final String USEDQUOTA_FIELD_NAME = "usedQuota";

    static final String QUOTA_COLLECTION_NAME = "Quota";

    private static final Logger logger = LoggerFactory.getLogger(QuotaRepositoryImpl.class);

    private MongoOperations mongoOperations;

    private ConcurrentMap<String, Long> defaultAppMaxQuotaMap;

    @Autowired
    public QuotaRepositoryImpl(MongoOperations mongoOperations, IFrsConfig frsConfig) {
        this.mongoOperations = mongoOperations;

        // 组建默认值Map
        defaultAppMaxQuotaMap = new ConcurrentHashMap<>();
        defaultAppMaxQuotaMap.put(DEFAULT.getAppid(), DEFAULT.getMaxQuotaValue());
        if (frsConfig.getApp() != null && !frsConfig.getApp().isEmpty()) {
            frsConfig.getApp().forEach(app -> defaultAppMaxQuotaMap.put(app.getAppid(), app.getMaxQuotaValue()));
        }

        // output default max quota setting
        Iterator<String> keys = defaultAppMaxQuotaMap.keySet().iterator();
        logger.debug("---=== Default max quotas ===---");
        while (keys.hasNext()) {
            String key = keys.next();
            Long value = defaultAppMaxQuotaMap.get(key);
            logger.debug("{} : {}", key, value);
        }
    }

    @Override
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
    public Quota findOneOrCreateOne(@Nonnull Quota quota) {
        Objects.requireNonNull(quota.getAppid());

        Quota q = findOneByAppidIgnoreCase(quota.getAppid());
        if (q == null) {
            logger.info("Quota of {} doesn't exist, will create one according to given quota setting.", quota.getAppid());
            initializeQuota(quota);
            q = findOneByAppidIgnoreCase(quota.getAppid());
        }
        return q;
    }

    @Override
    public Quota findAndIncreaseUsedQuotaByAppidIgnoreCase(@Nonnull String appid, long points) {
        return mongoOperations.findAndModify(
                new Query(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                new Update().inc(USEDQUOTA_FIELD_NAME, points),
                new FindAndModifyOptions().returnNew(true),
                Quota.class);
    }

    @Override
    public Quota findAndModifyMaxQuotaByAppidIgnoreCase(@Nonnull String appid, long points) {
        return mongoOperations.findAndModify(
                new Query(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                new Update().set("maxQuota", points),
                new FindAndModifyOptions().returnNew(true),
                Quota.class);
    }

    @Override
    public Quota findOneByAppidIgnoreCase(@Nonnull String appid) {
        return mongoOperations.findOne(
                new Query(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                Quota.class);
    }

    @Override
    public Quota findAndModifyUsedQuotaByAppidIgnoreCase(String appid, long points) {
        return mongoOperations.findAndModify(
                new Query(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE))),
                new Update().set(USEDQUOTA_FIELD_NAME, points),
                new FindAndModifyOptions().returnNew(true),
                Quota.class);
    }

    @Override
    public Map<String, Long> aggregateUsedQuotaByAppidIgnoreCase(String... appid) {
        Map<String, Long> results = new HashMap<>(appid.length);
        List<Criteria> cs = new ArrayList<>(appid.length);
        for (String id : appid) {
            if (!StringUtils.isEmpty(id)) {
                cs.add(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(id, Pattern.CASE_INSENSITIVE)));
            }
        }

        TypedAggregation<FileInfo> aggregation = TypedAggregation.newAggregation(
                FileInfo.class,
                match(new Criteria().orOperator(cs.toArray(new Criteria[]{}))),
                group(APPID_FIELD_NAME).sum("size").as(USEDQUOTA_FIELD_NAME));

        List<BasicDBObject> dbresults = mongoOperations.aggregate(aggregation, BasicDBObject.class).getMappedResults();

        for (BasicDBObject dbresult : dbresults) {
            String id = dbresult.getString("_id");
            long uq = dbresult.getLong(USEDQUOTA_FIELD_NAME);
            if (logger.isDebugEnabled()) {
                logger.debug("UsedQuota of {} is {}", id, uq);
            }
            // 如果有大小写
            if (results.containsKey(id.toLowerCase())) {
                results.put(id.toLowerCase(), results.get(id.toLowerCase()) + uq);
            } else {
                results.put(id.toLowerCase(), uq);
            }
        }
        return results;
    }

    @Override
    public Set<String> findAllAppid() {
        List dbresult = mongoOperations.getCollection(QUOTA_COLLECTION_NAME).distinct(APPID_FIELD_NAME);
        Set<String> result = new HashSet<>(dbresult.size());
        for (Object obj : dbresult) {
            result.add(obj.toString().toLowerCase());
        }
        return result;
    }

    @Override
    public List<Quota> findAll() {
        return mongoOperations.findAll(Quota.class);
    }

    @Override
    public Quota get(String id) {
        return mongoOperations.findById(id, Quota.class);
    }

    @Override
    public void save(Quota quota) {
        mongoOperations.save(quota);
    }

    private synchronized boolean initializeQuota(@Nonnull String appid) {
        try {
            Quota q = Quota.builder().appid(appid).usedQuota(0L).maxQuota(getDefaultMaxQuota(appid)).build();
            mongoOperations.insert(q, QUOTA_COLLECTION_NAME);
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
                    .maxQuota(quota.getMaxQuota() <= 0 ? getDefaultMaxQuota(quota.getAppid()) : quota.getMaxQuota()).build();
            mongoOperations.insert(q, QUOTA_COLLECTION_NAME);
            return true;
        } catch (DuplicateKeyException ex) {
            logger.info("Quota of {} already exists", quota.getAppid());
            return false;
        }
    }

    long getDefaultMaxQuota(@Nonnull String appid) {
        return defaultAppMaxQuotaMap.getOrDefault(appid, defaultAppMaxQuotaMap.getOrDefault("default", 100 * 1024L * 1024L));
    }
}
