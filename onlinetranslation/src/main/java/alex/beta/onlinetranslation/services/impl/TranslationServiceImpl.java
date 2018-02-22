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
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static alex.beta.onlinetranslation.persistence.Translation.TEXT_MAXLENGTH;
import static alex.beta.onlinetranslation.persistence.Translation.TRANSLATED_TEXT_MAXLENGTH;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Service
public class TranslationServiceImpl implements TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationServiceImpl.class);

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private HousekeepingRepository housekeepingRepository;

    @Autowired
    private BaiduKey baiduKey;

    @Value("${TranslationJobConfiguration.numOfThreads:2}")
    private int numOfThreads;

    private PoolingHttpClientConnectionManager cm;

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sc = SSLContext.getInstance("TLS");

        // 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
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

    @Override
    @Transactional
    public Translation updateTranslationRequest(Translation request, boolean flush) {
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
        persistedT.setLastUpdatedOn(new Date());

        return flush ? translationRepository.saveAndFlush(persistedT) : translationRepository.save(persistedT);
    }

    @Override
    public TranslationResult getTranslation(String uuid) {
        Objects.requireNonNull(uuid);

        Translation tmp = translationRepository.findOne(uuid);
        return tmp == null ? null : new TranslationResult(tmp);
    }

    @Override
    @Async("translationJobExecutor")
    @Scheduled(fixedRate = 1000, initialDelay = 30000) // every 1 second, with initial delay 30 seconds
    public void executeTranslationJob() {
        if (cm == null) {
            synchronized (this) {
                if (cm == null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Instantiate PoolingHttpClientConnectionManager");
                    }
                    try {
                        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                                .register("http", PlainConnectionSocketFactory.INSTANCE)
                                .register("https", new SSLConnectionSocketFactory(createIgnoreVerifySSL()))
                                .build();
                        cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                        cm.setMaxTotal(Math.max(100, numOfThreads * 20));
                        HttpHost host = new HttpHost("api.fanyi.baidu.com", 80);
                        cm.setMaxPerRoute(new HttpRoute(host), Math.min(5, numOfThreads));
                        HttpHost shost = new HttpHost("api.fanyi.baidu.com", 443);
                        cm.setMaxPerRoute(new HttpRoute(shost), Math.min(5, numOfThreads));
                    } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                        logger.error("Failed to instantiate PoolingHttpClientConnectionManager. No translation job will be executed.", ex);
                        cm = null;
                        return;
                    }
                }
            }
        }

        if (baiduKey == null || baiduKey.getAppid() == null || baiduKey.getSecurityKey() == null) {
            logger.error("Make sure \'classpath:/baidu.key\' exists, both \'appid\' and \'securityKey\' are defined.");
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Translation job starts at {}.", new Date());
        }
        try {
            List<Translation> requests = translationRepository.findFirst5ByStatusOrderByLastUpdatedOnAsc(TranslationStatus.SUBMITTED);
            if (requests == null || requests.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("No pending request");
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Find {} translation request(s)", requests.size());
                }
                for (Translation request : requests) {
                    try {
                        performTranslation(request);
                    } catch (Exception ex) {//this should not happen
                        logger.error("Unexpected error when initiating async job for request {}", request.getUuid(), ex);
                        //continue processing next request
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Failed to find first 5 unproceeded requests", ex);
        }
    }

    @Override
    @Transactional
    @Async("translationJobExecutor")
    public void performTranslation(Translation request) {
        if (logger.isInfoEnabled()) {
            logger.info("Translating request {}", request.getUuid());
        }
        CloseableHttpClient httpclient = null;
        CloseableHttpResponse response = null;
        String body = null;
        try {
            request.setStatus(TranslationStatus.PROCESSING);
            request = updateTranslationRequest(request, true);

            httpclient = HttpClients.custom().setConnectionManager(cm).setConnectionManagerShared(true).build();
            //httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost("http://api.fanyi.baidu.com/api/trans/vip/translate");
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
            //TODO 错误码 http://api.fanyi.baidu.com/api/trans/product/apidoc
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
            logger.error("Timeout to translate request {}", request.getUuid(), ex);
            request.setStatus(TranslationStatus.TIMEOUT);
            request.setMessage(ex.getMessage());
            updateTranslationRequest(request, true);
            return;
        } catch (Exception ex) {
            logger.error("Failed to translate request {}", request.getUuid(), ex);
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage(ex.getMessage());
            updateTranslationRequest(request, true);
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
                logger.warn("Failed to close HttpResponse", ex);
            }
        }

        if (body != null && !body.trim().isEmpty()) {
            try {
                BaiduTranslation bt = BaiduTranslation.fromString(body);
                request.setFromLanguage(bt.getFrom());
                if (bt.getFirstDst() != null && bt.getFirstDst().length() > TRANSLATED_TEXT_MAXLENGTH) {
                    request.setStatus(TranslationStatus.WARNING);
                    request.setTranslatedText(bt.getFirstDst().substring(0, TRANSLATED_TEXT_MAXLENGTH));
                    request.setMessage("Translated text is truncated, " + bt.getFirstDst().length());
                } else {
                    request.setStatus(TranslationStatus.READY);
                    request.setTranslatedText(bt.getFirstDst());
                }
                updateTranslationRequest(request, true);
            } catch (IOException ex) {
                logger.error("Failed to parse Baidu Fanyi response.\n{}", body, ex);
                request.setStatus(TranslationStatus.ERROR);
                request.setMessage(ex.getMessage());
                updateTranslationRequest(request, true);
            }
        } else {
            logger.error("Baidu Fanyi response is empty.");
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage("Baidu Fanyi response is empty.");
            updateTranslationRequest(request, true);
        }
    }

    @Override
    @Transactional
    @Async("housekeepingJobExecutor")
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000, initialDelay = 30000) // every 12 hours, with initial delay 30 seconds
    public void performHousekeeping() {
        if (logger.isDebugEnabled()) {
            logger.debug("Housekeeping starts at {}", new Date());
        }
        int deleteCount = housekeepingRepository.removeExpiredTranslationRequests(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
        if (logger.isInfoEnabled()) {
            logger.info("Housekeeping deleted {} requests.", deleteCount);
        }
    }
}