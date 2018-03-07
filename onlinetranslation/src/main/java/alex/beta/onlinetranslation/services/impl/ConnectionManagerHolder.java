/**
 * @File: ConnectionManagerHolder.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * @Date: 2018/2/23 下午7:35
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.services.impl;

import org.apache.http.HttpHost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * @version ${project.version}
 * @Description
 */
@Component
public class ConnectionManagerHolder {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManagerHolder.class);

    private PoolingHttpClientConnectionManager connectionManager;

    @Value("${TranslationJobConfiguration.numOfThreads:2}")
    private int numOfThreads;

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
                //Bypass client trust check
            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] paramArrayOfX509Certificate,
                    String paramString) throws CertificateException {
                //Bypass server trust check
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }
        };

        sc.init(null, new TrustManager[]{trustManager}, null);
        return sc;
    }

    public PoolingHttpClientConnectionManager getConnectionManager() {
        if (this.connectionManager != null) {
            return this.connectionManager;
        } else {
            return lazyInitializeConnections();
        }
    }

    private synchronized PoolingHttpClientConnectionManager lazyInitializeConnections() {
        if (this.connectionManager == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("To instantiate PoolingHttpClientConnectionManager.");
            }
            try {
                Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                        .register("http", PlainConnectionSocketFactory.INSTANCE)
                        .register("https", new SSLConnectionSocketFactory(createIgnoreVerifySSL()))
                        .build();
                this.connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
                this.connectionManager.setMaxTotal(Math.max(100, numOfThreads * 20));
                HttpHost host = new HttpHost("api.fanyi.baidu.com", 80);
                this.connectionManager.setMaxPerRoute(new HttpRoute(host), Math.min(5, numOfThreads));
                HttpHost shost = new HttpHost("api.fanyi.baidu.com", 443);
                this.connectionManager.setMaxPerRoute(new HttpRoute(shost), Math.min(5, numOfThreads));
            } catch (NoSuchAlgorithmException | KeyManagementException ex) {
                logger.error("Failed to instantiate PoolingHttpClientConnectionManager. No Baidu Fanyi request can be executed.", ex);
                this.connectionManager = null;
            }
        }
        return this.connectionManager;
    }
}
