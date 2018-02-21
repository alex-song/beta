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
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.*;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Service
public class TranslationServiceImpl implements TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationServiceImpl.class);

    @Autowired
    private TranslationRepository repository;

    private PoolingHttpClientConnectionManager cm;

    private Properties baiduKey;

    /**
     * 绕过验证
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
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
    public Translation submit(String fromLanguage, String toLanguage, String text) {
        String sl = fromLanguage == null ? "auto" : fromLanguage;
        return repository.saveAndFlush(new Translation(TranslationStatus.SUBMITTED, sl, toLanguage, text));
    }

    @Override
    public Translation updateTranslationRequest(Translation request, boolean flush) {
        Objects.requireNonNull(request);

        Translation persistedT = repository.findOne(request.getUuid());
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

        return flush ? repository.saveAndFlush(persistedT) : repository.save(persistedT);
    }

    @Override
    public Translation getTranslation(String uuid) {
        Objects.requireNonNull(uuid);

        return repository.findOne(uuid);
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
                        cm.setMaxTotal(100);
                        HttpHost host = new HttpHost("api.fanyi.baidu.com", 80);
                        cm.setMaxPerRoute(new HttpRoute(host), 5);
                        HttpHost shost = new HttpHost("api.fanyi.baidu.com", 443);
                        cm.setMaxPerRoute(new HttpRoute(shost), 5);
                    } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                        logger.error("Failed to instantiate PoolingHttpClientConnectionManager. No translation job will be executed.", ex);
                        cm = null;
                        return;
                    }
                }
            }
        }

        if (baiduKey == null) {
            synchronized (this) {
                if (baiduKey == null) {
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Read baidu.key file");
                        }
                        baiduKey = new Properties();
                        baiduKey.load(new ClassPathResource("baidu.key").getInputStream());

                        if (!baiduKey.containsKey("appid") || !baiduKey.containsKey("securityKey")) {
                            logger.error("Make sure both \'appid\' and \'securityKey\' are in classpath:baidu.key");
                            baiduKey = null;
                            return;
                        }
                    } catch (IOException ex) {
                        logger.error("Make sure baidu.key is at following place classpath:baidu.key", ex);
                        baiduKey = null;
                        return;
                    }
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Translation job starts at {}.", new Date());
        }
        try {
            List<Translation> requests = repository.findFirst5ByStatusOrderByLastUpdatedOnAsc(TranslationStatus.SUBMITTED);
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

    //------------ private methods -------------

    @Override
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
            String appid = baiduKey.getProperty("appid");
            nvps.add(new BasicNameValuePair("appid", appid));
            String salt = String.valueOf(System.currentTimeMillis());
            nvps.add(new BasicNameValuePair("salt", salt));
            String securityKey = baiduKey.getProperty("securityKey");
            nvps.add(new BasicNameValuePair("sign", BaiduMD5.md5(appid + request.getText() + salt + securityKey)));

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
                request.setStatus(TranslationStatus.READY);
                request.setTranslatedText(bt.getFirstDst());
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
}
