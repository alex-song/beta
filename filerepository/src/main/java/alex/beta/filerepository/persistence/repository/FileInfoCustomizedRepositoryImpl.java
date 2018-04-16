/**
 * @File: FileInfoRepositoryImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/6 上午9:22
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.persistence.entity.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @version ${project.version}
 * @Description
 */

@Repository
public class FileInfoCustomizedRepositoryImpl implements FileInfoCustomizedRepository {

    private static final String APPID_FIELD_NAME = "appid";

    private MongoOperations mongoOperations;

    @Autowired
    public FileInfoCustomizedRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Set<String> findAllAppid() {
        List dbresult = mongoOperations.getCollection("FileInfo").distinct(APPID_FIELD_NAME);
        Set<String> result = new HashSet<>(dbresult.size());
        for (Object obj : dbresult) {
            result.add(obj.toString().toLowerCase());
        }
        return result;
    }

    @Override
    public List<FileInfo> deleteByAppid(@Nonnull String appid) {
        Query query = new Query(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE)));
        return mongoOperations.findAllAndRemove(query, FileInfo.class);
    }

    @Override
    public List<FileInfo> findByAppidAndNameContainsIgnoreCase(String appid, String name, int skip, int limit) {
        Query query = new Query();
        if (!StringUtils.isEmpty(appid)) {
            query.addCriteria(Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE)));
        }
        if (!StringUtils.isEmpty(name)) {
            // 模糊匹配
            query.addCriteria(Criteria.where("name").regex(Pattern.compile("^.*?" + name + ".*$", Pattern.CASE_INSENSITIVE)));
        }
        query.with(new Sort(Sort.Direction.ASC, "createDate"));
        query.skip(skip);
        query.limit(Math.min(1000, limit));
        return mongoOperations.find(query, FileInfo.class);
    }

    @Override
    public List<FileInfo> findAllAndRemoveByAppidIgnoreCaseAndExpiredDateLessThan(@Nonnull String appid, @Nonnull LocalDateTime dateTime) {
        Criteria appidC = Criteria.where(APPID_FIELD_NAME).regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE));
        Criteria expiredC = Criteria.where("expiredDate").lt(dateTime);

        Query query = new Query(new Criteria().andOperator(appidC, expiredC));
        return mongoOperations.findAllAndRemove(query, FileInfo.class);
    }
}
