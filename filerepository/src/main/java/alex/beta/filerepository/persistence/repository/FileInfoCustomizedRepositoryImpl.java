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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    private static final Logger logger = LoggerFactory.getLogger(FileInfoCustomizedRepositoryImpl.class);

    private MongoOperations mongoOperations;

    public FileInfoCustomizedRepositoryImpl(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public Set<String> findAllAppid() {
        List dbresult = mongoOperations.getCollection("FileInfo").distinct("appid");
        Set<String> result = new HashSet<>(dbresult.size());
        for (Object obj : dbresult) {
            result.add(obj.toString().toLowerCase());
        }
        return result;
    }

    @Override
    public List<FileInfo> deleteByAppid(@Nonnull String appid) {
        Query query = new Query(Criteria.where("appid").regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE)));
        return mongoOperations.findAllAndRemove(query, FileInfo.class);
    }

    //TODO 是叫id吗？
    @Override
    public FileInfo update(@Nonnull String fileInfoId, String description, LocalDateTime expiredDate) {
        return mongoOperations.findAndModify(
                new Query(Criteria.where("id").is(fileInfoId)),
                new Update().set("description", description).set("expiredDate", expiredDate),
                new FindAndModifyOptions().returnNew(true),
                FileInfo.class);
    }

    //TODO 匹配UT
    @Override
    public List<FileInfo> findByAppidAndNameIgnoreCase(String appid, String name, int skip, int limit) {
        Query query = new Query();
        if (!StringUtils.isEmpty(appid)) {
            query.addCriteria(Criteria.where("appid").regex(Pattern.compile(appid, Pattern.CASE_INSENSITIVE)));
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
}
