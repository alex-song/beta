/**
 * <p>
 * File Name: BaiduKey.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/22 上午8:41
 * </p>
 */
package alex.beta.onlinetranslation.services.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author alexsong
 * @version ${project.version}
 */
@Component
@ConfigurationProperties
@PropertySource("classpath:/baidu.key")
public class BaiduKey {
    private String appid;
    private String securityKey;

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getSecurityKey() {
        return securityKey;
    }

    public void setSecurityKey(String securityKey) {
        this.securityKey = securityKey;
    }
}
