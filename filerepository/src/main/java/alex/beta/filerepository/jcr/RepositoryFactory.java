/**
 * @File: RepositoryFactory.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/25 下午1:50
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.jcr;

import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.config.ConfigurationException;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.io.*;

/**
 * @version ${project.version}
 * @Description
 */

@Component
@EnableAutoConfiguration
public class RepositoryFactory {

    private static final Logger logger = LoggerFactory.getLogger(RepositoryFactory.class);

    @Value("${repository.home:#{null}}")
    private String repoHomePath;

    @Value("${repository.config}")
    private String repoConfigPath;

    @Value("${repository.name:frs}")
    private String repoName;

    @Profile("dev")
    @Bean(name = "frsRepository")
    public Repository devRepository() throws RepositoryException {
        if (this.repoHomePath == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("repository.home is not set, and use java.io.tmpdir ({}) in dev profile.", System.getProperty("java.io.tmpdir"));
            }
            this.repoHomePath = System.getProperty("java.io.tmpdir");
        }
        return repository();
    }

    @Profile("dock")
    @Bean(name = "frsRepository")
    public Repository dockRepository() throws RepositoryException {
        return repository();
    }

    private Repository repository() throws RepositoryException {
        File repoHome;
        try {
            repoHome = (new File(this.repoHomePath)).getCanonicalFile();
        } catch (IOException ex) {
            logger.error("Repository home configuration failure: {}", this.repoHomePath, ex);
            throw new ConfigurationException("Repository home configuration failure: " + this.repoHomePath, ex);
        }

        if (this.repoConfigPath != null) {
            logger.info("Load repository configuration file {}", this.repoConfigPath);
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource in = resolver.getResource(this.repoConfigPath);
            if (in == null) {
                InputStream inputStream;
                try {
                    inputStream = new FileInputStream(new File(this.repoConfigPath));
                } catch (FileNotFoundException ex) {
                    try {
                        inputStream = new FileInputStream(new File(repoHome, this.repoConfigPath));
                    } catch (FileNotFoundException iex) {
                        logger.error("Repository configuration not found: {}", this.repoConfigPath, ex);
                        throw new ConfigurationException("Repository configuration not found: " + this.repoConfigPath, ex);
                    }
                }
                return this.createRepository(new InputSource(inputStream), repoHome);
            } else {
                try {
                    return this.createRepository(new InputSource(new FileInputStream(in.getFile())), repoHome);
                } catch (IOException ex) {
                    logger.error("Cannot read repository configuration {}", this.repoConfigPath, ex);
                    throw new ConfigurationException("Cannot read repository configuration " + this.repoConfigPath, ex);
                }
            }
        } else {
            logger.error("Missing repository configuration.");
            throw new ConfigurationException("Missing repository configuration.");
        }
    }

    private Repository createRepository(InputSource is, File homedir) throws RepositoryException {
        RepositoryConfig config = RepositoryConfig.create(is, homedir.getAbsolutePath());
        return RepositoryImpl.create(config);
    }
}
