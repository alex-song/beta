package alex.beta.webcrawler.configuration.xmlbeans;

import alex.beta.webcrawler.configuration.api.IConfiguration;
import alex.beta.webcrawler.configuration.api.PathSupport;

/**
 * Created by songlip on 2018/3/22.
 */
public abstract class XmlConfiguration implements IConfiguration {
    public static final PathSupport ROOT = new PathSupport() {
        public String getPath() {
            return "//XmlConfiguration";
        }
    };
}
