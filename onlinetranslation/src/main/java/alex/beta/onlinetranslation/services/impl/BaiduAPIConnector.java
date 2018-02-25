/**
 * <p>
 * File Name: BaiduAPIConnector.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/25 下午3:34
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import alex.beta.onlinetranslation.persistence.TranslationEntity;
import alex.beta.onlinetranslation.persistence.TranslationLineEntity;
import alex.beta.onlinetranslation.persistence.TranslationStatus;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static alex.beta.onlinetranslation.persistence.TranslationEntity.TRANSLATED_TEXT_MAXLENGTH;

/**
 * @author alexsong
 * @version ${project.version}
 */

@SuppressWarnings("squid:S3776")
@Component
@ConfigurationProperties
@PropertySource("classpath:/baidu.key")
public class BaiduAPIConnector {
    private static final Logger logger = LoggerFactory.getLogger(BaiduAPIConnector.class);

    private ConnectionManagerHolder connectionManagerHolder;

    @Value("${appid}")
    private String appid;

    @Value("${securityKey}")
    private String securityKey;

    @Autowired
    public BaiduAPIConnector(ConnectionManagerHolder connectionManagerHolder) {
        this.connectionManagerHolder = connectionManagerHolder;
    }

    public TranslationEntity translate(TranslationEntity request) {
        Objects.requireNonNull(request);

        if (logger.isDebugEnabled()) {
            logger.debug("Translating request {} using Baidu Fanyi API.", request.getUuid());
        }
        //检查依赖的组件
        if (connectionManagerHolder == null
                || connectionManagerHolder.getConnectionManager() == null) {
            logger.warn("PoolingHttpClientConnectionManager hasn't initialized.");
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage("Internal server error, PoolingHttpClientConnectionManager hasn't initialized.");
            return request;
        }
        if (appid == null || securityKey == null || appid.isEmpty() || securityKey.isEmpty()) {
            logger.warn("appid or securityKey is missing in classpath:/baidu.key.");
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage("Internal server error, appid or securityKey is missing in classpath:/baidu.key.");
            return request;
        }
        //开始调用翻译API
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
            nvps.add(new BasicNameValuePair("appid", appid));
            String salt = String.valueOf(System.currentTimeMillis());
            nvps.add(new BasicNameValuePair("salt", salt));
            nvps.add(new BasicNameValuePair("sign",
                    BaiduMD5.md5(appid + request.getText() + salt + securityKey)));
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
            return request;
        } catch (Exception ex) {
            logger.error("Failed to translate request {}.", request.getUuid(), ex);
            request.setStatus(TranslationStatus.ERROR);
            request.setMessage(ex.getMessage() != null && ex.getMessage().length() > 250 ?
                    ex.getMessage().substring(0, 250) : ex.getMessage());
            return request;
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
        //百度API处理结束
        //开始处理返回结果
        if (body != null && !body.trim().isEmpty()) {
            try {
                if (body.startsWith("{\"error_code\"") || body.startsWith("{\"error_msg\"")) {
                    //百度返回错误消息
                    logger.warn("Baidu Fanyi returns error.\n{}", body);
                    BaiduTranslationError baiduError = BaiduTranslationError.fromString(body);
                    if (BaiduTranslationError.INTERNAL_ERROR.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.TIMEOUT.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.TOO_FREQUENT.equalsIgnoreCase(baiduError.getErrorCode())
                            || BaiduTranslationError.LONG_QUERY_TOO_FREQUENT.equalsIgnoreCase(baiduError.getErrorCode())) {
                        request.setStatus(TranslationStatus.SUBMITTED);
                        request.setLastUpdatedOn(new Date(System.currentTimeMillis() + 10 * 1000L));//retry after 10 seconds
                        return request;
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
                    //标示位，是否有翻译结果长度越界
                    boolean isOverSize = false;
                    if (bt.getTransResult() != null && !bt.getTransResult().isEmpty()) {
                        List<TranslationLineEntity> lines = new ArrayList<>(bt.getTransResult().size());
                        for (BaiduTranslationResult r : bt.getTransResult()) {
                            if (r.getDst() != null && r.getDst().length() > TRANSLATED_TEXT_MAXLENGTH) {
                                isOverSize = true;
                                lines.add(new TranslationLineEntity(r.getSrc(), r.getDst().substring(0, TRANSLATED_TEXT_MAXLENGTH)));
                            } else {
                                lines.add(new TranslationLineEntity(r.getSrc(), r.getDst()));
                            }
                        }
                        request.setTranslationLines(lines);
                        if (isOverSize) {
                            request.setStatus(TranslationStatus.WARNING);
                            request.setMessage("Translated text is truncated");
                        } else {
                            request.setStatus(TranslationStatus.READY);
                        }
                    } else {
                        request.setStatus(TranslationStatus.WARNING);
                        request.setTranslationLines(null);
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
            logger.info("Translated request {} using Baidu API.", request.getUuid());
        }
        return request;
    }
}
