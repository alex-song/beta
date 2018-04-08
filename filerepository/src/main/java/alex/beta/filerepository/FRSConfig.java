package alex.beta.filerepository;

import alex.beta.filerepository.config.MaxQuota;
import alex.beta.filerepository.config.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

/**
 * Created by songlip on 2018/4/8.
 */

@Configuration
public class FRSConfig {

    @Value("${frs.config:'classpath:frs-config-default.properties'}")
    private String frsConfigResource;


    @Bean(name = "frsConfig")
    public Properties frsConfig() throws IOException {
        PropertiesFactoryBean bean = new PropertiesFactoryBean();
        bean.setSingleton(true);
        bean.setIgnoreResourceNotFound(true);
        bean.setLocation(new PathMatchingResourcePatternResolver().getResource(frsConfigResource));
        return bean.getObject();
    }

    @Bean("maxQuotas")
    @Autowired
    public Map<String, MaxQuota> maxQuotas(Properties frsConfig) {
        //TODO
        return null;
    }

    @Bean("operatorUser")
    @Autowired
    public User.OperatorUser operatorUser(Properties frsConfig) {
        return new User.OperatorUser(frsConfig.getProperty("user.operator.username"), frsConfig.getProperty("user.operator.password"));
    }

    @Bean("guestUser")
    @Autowired
    public User.GuestUser guestUser(Properties frsConfig) {
        return new User.GuestUser(frsConfig.getProperty("user.guest.username"), frsConfig.getProperty("user.guest.password"));
    }

    @Bean("adminUser")
    @Autowired
    public User.AdminUser adminUser(Properties frsConfig) {
        return new User.AdminUser(frsConfig.getProperty("user.admin.username"), frsConfig.getProperty("user.admin.password"));
    }
}
