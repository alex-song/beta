/**
 * @File: FileRepositoryServiceImpl.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/2 下午10:38
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services.impl;

import alex.beta.filerepository.ContentValidationException;
import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.models.FileInfoModel;
import alex.beta.filerepository.models.FileStoreModel;
import alex.beta.filerepository.persistence.entity.FileInfo;
import alex.beta.filerepository.persistence.entity.FileStore;
import alex.beta.filerepository.persistence.repository.FileInfoCustomizedRepository;
import alex.beta.filerepository.persistence.repository.FileInfoRepository;
import alex.beta.filerepository.services.FileRepositoryService;
import alex.beta.filerepository.services.QuotaService;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Nonnull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static alex.beta.filerepository.SecurityConfig.*;

/**
 * @version ${project.version}
 * @Description
 */

@Service("fileRepositoryService")
public class FileRepositoryServiceImpl implements FileRepositoryService {

    private static final Logger logger = LoggerFactory.getLogger(FileRepositoryServiceImpl.class);

    private FileInfoRepository fileInfoRepository;

    private FileInfoCustomizedRepository fileInfoCustomizedRepository;

    private QuotaService quotaService;

    private Meter fileMeter;

    @Autowired
    public FileRepositoryServiceImpl(FileInfoRepository fileInfoRepository, QuotaService quotaService, FileInfoCustomizedRepository fileInfoCustomizedRepository, MetricRegistry metricRegistry) {
        this.fileInfoRepository = fileInfoRepository;
        this.fileInfoCustomizedRepository = fileInfoCustomizedRepository;
        this.quotaService = quotaService;
        this.fileMeter = metricRegistry.meter("frs");
        if (fileMeter == null && logger.isWarnEnabled()) {
            logger.warn("Metering is disabled for FileRepositoryService");
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public FileInfoModel add(@Nonnull String appid, @Nonnull String name, String description, String contentType,
                             LocalDateTime expiredDate, String md5, byte[] content)
            throws ContentValidationException, QuotaExceededException {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding file {} to repository", name);
        }
        if (fileMeter != null) {
            // Bypass metering in test
            fileMeter.mark();
        }
        FileInfo fileInfo = FileInfo.builder()
                .name(name)
                .appid(appid)
                .description(description)
                .expiredDate(expiredDate)
                .contentType(contentType)
                .size(content == null ? 0 : content.length)
                .build();

        if (content != null) {
            FileStore.FileStoreBuilder fileStore = FileStore.builder().content(content);
            String calculatedMd5 = DigestUtils.md5DigestAsHex(content);
            if (StringUtils.isEmpty(md5)) {
                fileInfo.setMd5(calculatedMd5);
            } else if (!calculatedMd5.equalsIgnoreCase(md5)) {
                throw new ContentValidationException(md5, calculatedMd5);
            } else {
                fileInfo.setMd5(md5);
            }
            fileInfo.setFileStore(fileStore.build());
        }

        FileInfoModel fim = new FileInfoModel(fileInfoRepository.save(fileInfo));
        if (fileInfo.getSize() > 0) {
            quotaService.useQuota(appid, fileInfo.getSize());
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Saved {}, and use {} points of {}", fim, fileInfo.getSize(), appid);
        }
        return fim;
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public List<FileInfoModel> find(String appid, String name, @Min(0) int skip, @Max(1000) @Min(0) int limit) {
        List<FileInfo> fis = fileInfoCustomizedRepository.findByAppidAndNameContainsIgnoreCase(appid, name, skip, limit);
        List<FileInfoModel> fims = new ArrayList<>(fis.size());
        fis.forEach(fileInfo -> fims.add(new FileInfoModel(fileInfo)));
        return fims;
    }

    //TODO 好好想想怎么用page
    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public List<FileInfoModel> page(String appid, String name, @Min(0) int pageNum) {
        List<FileInfo> fis = fileInfoRepository.findByAppidAndNameIgnoreCase(appid, name,
                new PageRequest(pageNum, 50, new Sort(Sort.Direction.ASC, "createDate"))).getContent();
        List<FileInfoModel> fims = new ArrayList<>(fis.size());
        fis.forEach(fileInfo -> fims.add(new FileInfoModel(fileInfo)));
        return fims;
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public Set<String> findAllAppid() {
        return fileInfoCustomizedRepository.findAllAppid();
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public FileInfoModel get(@Nonnull String fileInfoId) {
        return new FileInfoModel(fileInfoRepository.findOne(fileInfoId));
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_OPERATOR + "')")
    public FileInfoModel delete(@Nonnull String fileInfoId) {
        FileInfo fi = fileInfoRepository.findOne(fileInfoId);
        if (fi != null) {
            fileInfoRepository.delete(fileInfoId);
            quotaService.releaseQuota(fi.getAppid(), fi.getSize());
            return new FileInfoModel(fi);
        } else {
            return null;
        }
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public int deleteAppid(@Nonnull String appid) {
        List<FileInfo> deletedFiles = fileInfoCustomizedRepository.deleteByAppid(appid);
        int size = deletedFiles == null ? 0 : deletedFiles.size();
        if (logger.isDebugEnabled()) {
            logger.debug("Deleted {} files", size);
        }
        quotaService.resetUsedQuota(appid);
        return size;
    }

    @Override
    @PreAuthorize("hasRole('" + ROLE_FRS_GUEST + "')")
    public FileStoreModel getFile(@Nonnull String fileInfoId) {
        FileInfo fileInfo = fileInfoRepository.findOne(fileInfoId);
        if (fileInfo == null || fileInfo.getFileStore() == null) {
            return null;
        }
        return new FileStoreModel(fileInfo.getFileStore());
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('" + ROLE_FRS_ADMIN + "')")
    public int deleteExpiredFiles(@Nonnull String appid, LocalDateTime time) {
        List<FileInfo> files = fileInfoCustomizedRepository.findAllAndRemoveByAppidIgnoreCaseAndExpiredDateLessThan(appid, time == null ? LocalDateTime.now() : time);
        quotaService.releaseQuota(appid, files.stream().mapToLong(FileInfo::<Integer>getSize).sum());
        return files.size();
    }
}
