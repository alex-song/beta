/**
 * @File: FileInfoCascadingMongoEventListener.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/1 上午10:43
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.mongo;

import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.entity.FileStore;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.MappingException;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeConvertEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @version ${project.version}
 * @Description
 */
public class FileInfoCascadingMongoEventListener extends AbstractMongoEventListener<FileInfo> {
    private static Logger logger = LoggerFactory.getLogger(FileInfoCascadingMongoEventListener.class);

    private static final String FILESTORE_FIELD_NAME = "fileStore";

    @Autowired
    private MongoOperations mongoOperations;

    @Override
    public void onBeforeConvert(BeforeConvertEvent<FileInfo> event) {
        final FileInfo source = event.getSource();
        ReflectionUtils.doWithFields(FileInfo.class, field -> {
            ReflectionUtils.makeAccessible(field);
            if (FILESTORE_FIELD_NAME.equalsIgnoreCase(field.getName())
                    && field.isAnnotationPresent(DBRef.class)
                    && field.isAnnotationPresent(Cascade.class)) {
                final FileStore fileStore = (FileStore) field.get(source);
                // 无论FileStore是否在数据库中已经存在，都先保存FileStore。
                if (fileStore != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cascade saving {} inside of FileInfo", fileStore);
                    }
                    DbRefFieldCallback callback = new DbRefFieldCallback();
                    ReflectionUtils.doWithFields(FileStore.class, callback);
                    if (!callback.isIdFound()) {
                        throw new MappingException("Cannot perform cascade save on child object without id set");
                    }
                    mongoOperations.save(fileStore);
                }
            }
        });
    }

    @Override
    public void onAfterSave(AfterSaveEvent<FileInfo> event) {
        final FileInfo source = event.getSource();
        ReflectionUtils.doWithFields(FileInfo.class, field -> {
            ReflectionUtils.makeAccessible(field);
            if (FILESTORE_FIELD_NAME.equalsIgnoreCase(field.getName())
                    && field.isAnnotationPresent(DBRef.class)
                    && field.isAnnotationPresent(Cascade.class)) {
                FileStore fileStore = (FileStore) field.get(source);
                // 如果FileStore中infoId和source中的不一致，那就把FileStore里的infoId改掉，让它跟随source。
                if (fileStore != null && !source.getId().equals(fileStore.getInfoId())) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Set infoId to {} of {} inside of FileInfo", source.getId(), fileStore);
                    }
                    fileStore.setInfoId(source.getId());
                    mongoOperations.save(fileStore);
                }
            }
        });
    }

    @Override
    public void onAfterDelete(AfterDeleteEvent<FileInfo> event) {
        DBObject obj = event.getSource();
        String infoId = obj == null ? null : (String) obj.get("id");
        if (infoId != null) {
            ReflectionUtils.doWithFields(FileInfo.class, field -> {
                ReflectionUtils.makeAccessible(field);
                if (FILESTORE_FIELD_NAME.equalsIgnoreCase(field.getName())
                        && field.isAnnotationPresent(DBRef.class)
                        && field.isAnnotationPresent(Cascade.class)
                        && field.getDeclaredAnnotation(Cascade.class).delete()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cascade deleting FileStore, whose infoId is {}", infoId);
                    }
                    List<FileStore> deletedFileStores = mongoOperations.findAllAndRemove(new Query(Criteria.where("infoId").is(infoId)), FileStore.class, "FileStore");
                    if (logger.isDebugEnabled()) {
                        logger.debug("Deleted FileStore({}) which is linked to FileInfo({})",
                                deletedFileStores.stream().map(FileStore::<String>getId).collect(Collectors.joining(", ")), infoId);
                    }
                }
            });
        }
    }

    private static class DbRefFieldCallback implements ReflectionUtils.FieldCallback {
        private boolean idFound;

        @Override
        public void doWith(Field field) throws IllegalAccessException {
            ReflectionUtils.makeAccessible(field);

            if (field.isAnnotationPresent(Id.class)) {
                idFound = true;
            }
        }

        boolean isIdFound() {
            return idFound;
        }
    }
}
