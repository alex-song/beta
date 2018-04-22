/**
 * @File: FRSConfig.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/8 23:07
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.config.xmlbeans.FrsConfig;
import alex.beta.filerepository.config.xmlbeans.IFrsConfig;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.IOException;

/**
 * @version ${project.version}
 * @Description
 */
@Configuration
public class FrsConfigResolver {

    private static final Logger logger = LoggerFactory.getLogger(FrsConfigResolver.class);

    @Setter
    @Getter
    @Value("${frs.defaultConfig}")
    private String defaultConfig;

    @Autowired
    private Environment env;

    @Bean("frsConfig")
    public IFrsConfig getFrsConfig() {
        String frsConfigFile = null;
        String lookup = env == null ? null : env.getProperty("lookup.dir");
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Initializing file repository service using lookup {}, defaultConfig {}", lookup, defaultConfig);
            }
            if (StringUtils.isEmpty(lookup)) {
                frsConfigFile = defaultConfig;
            } else {
                Resource tmp = new PathMatchingResourcePatternResolver().getResource(lookup);
                if (!tmp.exists() || !tmp.getFile().isDirectory()) {
                    logger.warn("Invalid lookup directory configuration, {}. Use default configuration.", lookup);
                    frsConfigFile = defaultConfig;
                } else {
                    if (lookup.endsWith(File.separator)) {
                        frsConfigFile = lookup + "frs-config.xml";
                    } else {
                        frsConfigFile = lookup + File.separator + "frs-config.xml";
                    }
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("frsConfigFile:{}", frsConfigFile);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(FrsConfig.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            Resource res = new PathMatchingResourcePatternResolver().getResource(frsConfigFile);

            if (res == null || !res.exists() || !res.isReadable()) {
                logger.error("Configuration XML file ({}) doesn't exist, or not readable", frsConfigFile);
                throw new InvalidConfigurationException(frsConfigFile);
            } else {
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(res.getInputStream());
                return jaxbUnmarshaller.unmarshal(reader, FrsConfig.class).getValue();
            }
        } catch (IOException | XMLStreamException e1) {
            logger.error("Failed to read configuration XML file, {}", frsConfigFile, e1);
            throw new InvalidConfigurationException(frsConfigFile, e1);
        } catch (JAXBException e3) {
            logger.error("Cannot bind XML {} with Configuration bean", frsConfigFile, e3);
            throw new InvalidConfigurationException(frsConfigFile, e3);
        }
    }
}
