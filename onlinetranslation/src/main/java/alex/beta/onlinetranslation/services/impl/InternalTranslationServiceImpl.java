/**
 * @File: InternalTranslationServiceImpl.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018/2/27 下午10:40
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.persistence.*;
import alex.beta.onlinetranslation.services.InternalTranslationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @version ${project.version}
 * @Description
 */

@Service("internalTranslationServiceImpl")
public class InternalTranslationServiceImpl extends TranslationServiceImpl implements InternalTranslationService {

    private static final Logger logger = LoggerFactory.getLogger(InternalTranslationServiceImpl.class);

    private HousekeepingRepository housekeepingRepository;

    private BaiduAPIConnector apiConnector;

    @Autowired
    public InternalTranslationServiceImpl(TranslationRepository translationRepository,
                                          HousekeepingRepository housekeepingRepository,
                                          BaiduAPIConnector apiConnector) {
        super(translationRepository);
        this.housekeepingRepository = housekeepingRepository;
        this.apiConnector = apiConnector;
    }

    @Override
    @Transactional
    public TranslationEntity updateTranslationRequest(TranslationEntity request) {
        Objects.requireNonNull(request);

        TranslationEntity persistedT = translationRepository.findOne(request.getUuid());
        if (persistedT == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Translation {} is not found, cannot update it.", request.getUuid());
            }
            return null;
        }

        if (request.getFromLanguage() != null) {
            persistedT.setFromLanguage(request.getFromLanguage());
        }
        if (request.getToLanguage() != null) {
            persistedT.setToLanguage(request.getToLanguage());
        }
        if (request.getMessage() != null) {
            persistedT.setMessage(request.getMessage());
        }
        if (request.getStatus() != null) {
            persistedT.setStatus(request.getStatus());
        }
        if (request.getText() != null) {
            persistedT.setText(request.getText());
        }
        if (request.getLastUpdatedOn() == null) {
            persistedT.setLastUpdatedOn(new Date());
        } else {
            persistedT.setLastUpdatedOn(request.getLastUpdatedOn());
        }

        if (request.getTranslationLines() != null && !request.getTranslationLines().isEmpty()) {
            List<TranslationLineEntity> lines = new ArrayList<>(request.getTranslationLines().size());
            for (TranslationLineEntity line : request.getTranslationLines()) {
                lines.add(new TranslationLineEntity(line.getSrc(), line.getDst()));
            }
            persistedT.setTranslationLines(lines);
        } else if (request.getTranslationLines() != null) {
            persistedT.setTranslationLines(null);
        }

        return translationRepository.saveAndFlush(persistedT);
    }

    @Override
    public List<TranslationEntity> findRequestsToTranslate() {
        Date filterDate = new Date();
        if (logger.isDebugEnabled()) {
            logger.debug("To find un-proceeded requests before {}.", filterDate);
        }
        List<TranslationEntity> requests = translationRepository.findFirst3ByStatusAndLastUpdatedOnLessThanOrderByLastUpdatedOnAsc(
                TranslationStatus.SUBMITTED, filterDate);
        if (requests == null || requests.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not found un-proceeded translation request.");
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("Found {} un-proceeded translation request(s).", requests.size());
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Request(s) to translate:{}{}", System.lineSeparator(),
                        requests.stream().map(TranslationEntity::<String>getUuid).collect(Collectors.joining(System.lineSeparator())));
            }
        }
        return requests;
    }

    @Override
    @Transactional
    @Async("translationJobExecutor")
    public void asyncPerformTranslation(TranslationEntity request) {
        performTranslation(request);
    }

    @Override
    @Transactional
    public TranslationEntity performTranslation(TranslationEntity request) {
        Objects.requireNonNull(request);

        //Overwrite previous lastUpdatedOn
        request.setLastUpdatedOn(null);
        //clean up translation lines, if there is any
        request.setTranslationLines(Collections.<TranslationLineEntity>emptyList());
        //Call Baidu API and parse the response
        request = apiConnector.translate(request);

        if (logger.isDebugEnabled()) {
            logger.debug("Updating translation request {}.", request.getUuid());
        }
        TranslationEntity te = updateTranslationRequest(request);
        if (logger.isInfoEnabled()) {
            logger.info("Translation request {} has updated.", te.getUuid());
        }
        return te;
    }

    @Override
    @Transactional
    public void performHousekeeping() {
        if (logger.isDebugEnabled()) {
            logger.debug("To start housekeeping job.");
        }
        int deleteCount = housekeepingRepository.removeExpiredTranslationRequests(System.currentTimeMillis() - 24 * 60 * 60 * 1000L);
        if (logger.isInfoEnabled()) {
            logger.info("Housekeeping deleted {} requests.", deleteCount);
        }
    }
}
