/**
 * <p>
 * File Name: TranslationServiceImpl.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/19 下午3:08
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.models.TranslationResult;
import alex.beta.onlinetranslation.persistence.HousekeepingRepository;
import alex.beta.onlinetranslation.persistence.Translation;
import alex.beta.onlinetranslation.persistence.TranslationRepository;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
import alex.beta.onlinetranslation.services.TranslationService;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static alex.beta.onlinetranslation.persistence.Translation.TEXT_MAXLENGTH;
import static alex.beta.onlinetranslation.persistence.Translation.TRANSLATED_TEXT_MAXLENGTH;

/**
 * @author alexsong
 * @version ${project.version}
 */

@SuppressWarnings("squid:S3776")
@Service
public class TranslationServiceImpl implements TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationServiceImpl.class);

    private TranslationRepository translationRepository;

    private HousekeepingRepository housekeepingRepository;

    private ConnectionManagerHolder connectionManagerHolder;

    private BaiduKey baiduKey;

    @Autowired
    public TranslationServiceImpl(TranslationRepository translationRepository,
                                  HousekeepingRepository housekeepingRepository,
                                  ConnectionManagerHolder connectionManagerHolder,
                                  BaiduKey baiduKey) {
        this.translationRepository = translationRepository;
        this.housekeepingRepository = housekeepingRepository;
        this.connectionManagerHolder = connectionManagerHolder;
        this.baiduKey = baiduKey;
    }

    @Override
    @Transactional
    public TranslationResult submit(String fromLanguage, String toLanguage, String text) {
        Objects.requireNonNull(text);

        return new TranslationResult(translationRepository.saveAndFlush(
                new Translation(TranslationStatus.SUBMITTED,
                        fromLanguage == null ? "auto" : fromLanguage,
                        toLanguage,
                        text.length() > TEXT_MAXLENGTH ?
                                text.substring(0, TEXT_MAXLENGTH) : text))
        );
    }

    private Translation updateTranslationRequest(Translation request) {
        return updateTranslationRequest(request, 0);
    }

    @Override
    @Transactional
    public Translation updateTranslationRequest(Translation request, long delay) {
        Objects.requireNonNull(request);

        Translation persistedT = translationRepository.findOne(request.getUuid());
        if (persistedT == null) {
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
        if (request.getTranslatedText() != null) {
            persistedT.setTranslatedText(request.getTranslatedText());
        }
        persistedT.setLastUpdatedOn(new Date(System.currentTimeMillis() + delay));

        return translationRepository.saveAndFlush(persistedT);
    }

    @Override
    public TranslationResult getTranslation(String uuid) {
        Objects.requireNonNull(uuid);

        Translation tmp = translationRepository.findOne(uuid);
        return tmp == null ? null : new TranslationResult(tmp);
    }

    @Override
    public List<Translation> findRequestsToTranslate() {
        Date filterDate = new Date();
        if (logger.isDebugEnabled()) {
            logger.debug("To find un-translated requests before {}.", filterDate);
        }
        List<Translation> requests = translationRepository.findFirst5ByStatusAndLastUpdatedOnLessThanOrderByLastUpdatedOnAsc(
                TranslationStatus.SUBMITTED, filterDate);
        if (logger.isInfoEnabled()) {
            logger.info("Found {} un-translated translation request(s).", requests == null ? 0 : requests.size());
        }
        if (logger.isDebugEnabled() && requests != null && !requests.isEmpty()) {
            logger.debug("To translate:\n{}", requests.stream().map(Translation::<String>getUuid).collect(Collectors.joining(System.lineSeparator())));
        }
        return requests;
    }

    @Override
    @Transactional
    @Async("translationJobExecutor")
    public void performTranslation(Translation request) {
        if (logger.isDebugEnabled()) {
            logger.debug("Translating request {}.", request.getUuid());
        }
        if (connectionManagerHolder == null || connectionManagerHolder.getConnectionManager() == null ||
                baiduKey == null || baiduKey.getSecurityKey() == null || baiduKey.getAppid() == null) {
            return;
        }
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        String body = null;
        try {
            httpclient = HttpClients.custom().setConnectionManager(connectionManagerHolder.getConnectionManager())
                    .setConnectionManagerShared(true).build();
            HttpPost httpPost = new HttpPost("https://api.fanyi.baidu.com/api/trans/vip/translate");
            List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("q", request.getText()));
            nvps.add(new BasicNameValuePair("from", "auto"));
            nvps.add(new BasicNameValuePair("to", request.getToLanguage()));
            nvps.add(new BasicNameValuePair("appid", baiduKey.getAppid()));
            String salt = String.valueOf(System.currentTimeMillis());
            nvps.add(new BasicNameValuePair("salt", salt));
            nvps.add(new BasicNameValuePair("sign",
                    BaiduMD5.md5(baiduKey.getAppid() + request.getText() + salt + baiduKey.getSecurityKey())));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                //按指定编码转换结果实体为String类型
                body = EntityUtils.toString(entity, "UTF-8");
                if (logger.isDebugEnabled()) {
                    logger.debug("Response from Baidu Fanyi, status code : {}, body : {}", response.getStatusLine().getStatusCode(), body);
                }
            }
            EntityUtils.consume(entity);
        } catch (SocketTimeoutException ex) {
            logger.error("Timeout to translate request {}.", request.getUuid(), ex);
            request.setStatus(TranslationStatus.TIMEOUT);
            request.setMessage(ex.getMessage() != null && ex.getMessage().length() > 250 ?
                    ex.getMessage().substring(0, 250) : ex.getMessage());
            updateTranslationRequest(request);
            return;
        } catch (Exception ex) {
            logger.error("Failed to translate request {}", request.getUuid(), ex);
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage(ex.getMessage() != null && ex.getMessage().length() > 250 ?
                    ex.getMessage().substring(0, 250) : ex.getMessage());
            updateTranslationRequest(request);
            return;
        } finally {
            //释放链接
            try {
                if (response != null) {
                    response.close();
                }
                if (httpclient != null) {
                    httpclient.close();
                }
            } catch (IOException ex) {
                logger.warn("Failed to close HttpResponse.", ex);
            }
        }

        if (body != null && !body.trim().isEmpty()) {
            try {
                if (body.startsWith("{\"error_code\"") || body.startsWith("{\"error_msg\"")) {
                    //百度返回错误消息
                    if (logger.isWarnEnabled()) {
                        logger.warn("Baidu Fanyi returns error.\n{}", body);
                    }

                    BaiduTranslationError baiduError = BaiduTranslationError.fromString(body);

                    if (BaiduTranslationError.INTERNAL_ERROR.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.TIMEOUT.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.TOO_FREQUENT.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.LONG_QUERY_TOO_FREQUENT.equalsIgnoreCase(baiduError.getErrorCode())) {
                        request.setStatus(TranslationStatus.SUBMITTED);
                        updateTranslationRequest(request, 10 * 1000L);
                        return;
                    } else if (BaiduTranslationError.UNAUTHORIZED_USER.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.INVALID_SIGN.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.INVALID_IP.equalsIgnoreCase(baiduError.getErrorCode())) {
                        request.setStatus(TranslationStatus.NOT_AUTHORIZED);
                        request.setMessage(baiduError.getErrorCode() + " - " + baiduError.getErrorMessage());
                    } else {
                        request.setStatus(TranslationStatus.ERROR);
                        String tmp = (baiduError.getErrorMessage() != null && baiduError.getErrorMessage().length() > 240 ?
                                baiduError.getErrorMessage().substring(0, 240) : baiduError.getErrorMessage());
                        request.setMessage(baiduError.getErrorCode() + " - " + tmp);
                    }
                } else {
                    //百度返回正确消息
                    BaiduTranslation bt = BaiduTranslation.fromString(body);
                    request.setFromLanguage(bt.getFrom());
                    if (bt.joinAllDstsWithLineSeparator() != null && bt.joinAllDstsWithLineSeparator().length() > TRANSLATED_TEXT_MAXLENGTH) {
                        request.setStatus(TranslationStatus.WARNING);
                        request.setTranslatedText(bt.joinAllDstsWithLineSeparator().substring(0, TRANSLATED_TEXT_MAXLENGTH));
                        request.setMessage("Translated text is truncated, " + bt.joinAllDstsWithLineSeparator().length());
                    } else {
                        request.setStatus(TranslationStatus.READY);
                        request.setTranslatedText(bt.joinAllDstsWithLineSeparator());
                    }
                }
            } catch (IOException ex) {
                //无法解析百度返回消息
                logger.error("Failed to parse Baidu Fanyi response.\n{}", body, ex);
                request.setStatus(TranslationStatus.ERROR);
                request.setMessage(ex.getMessage() != null && ex.getMessage().length() > 250 ?
                        ex.getMessage().substring(0, 250) : ex.getMessage());
            }
        } else {
            //百度返回空的消息，无法解析
            logger.error("Baidu Fanyi response is empty.");
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage("Baidu Fanyi response is empty.");
        }
        if (logger.isInfoEnabled()) {
            logger.info("Translated request {}.", request.getUuid());
        }
        updateTranslationRequest(request);
    }

    @Override
    @Transactional
    @Async("housekeepingJobExecutor")
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000, initialDelay = 25000) // every 12 hours, with initial delay 25 seconds
    public void performHousekeeping() {
        if (logger.isDebugEnabled()) {
            logger.debug("To start housekeeping job.");
        }
        int deleteCount = housekeepingRepository.removeExpiredTranslationRequests(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        if (logger.isInfoEnabled()) {
            logger.info("Housekeeping deleted {} requests.", deleteCount);
        }
    }
}