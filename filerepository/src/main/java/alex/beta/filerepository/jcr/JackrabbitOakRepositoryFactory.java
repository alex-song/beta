package alex.beta.filerepository.jcr;


import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.oak.Oak;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.plugins.document.DocumentNodeStore;
import org.apache.jackrabbit.oak.plugins.document.mongo.MongoDocumentNodeStoreBuilder;
import org.apache.jackrabbit.oak.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;

/**
 * Created by songlip on 2018/3/29.
 */

@Configuration
public class JackrabbitOakRepositoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitOakRepositoryFactory.class);

    @Value("${repository.home:#{null}}")
    private String repoHomePath;

    @Value("${repository.config}")
    private String repoConfigPath;

    @Value("${repository.name:frs}")
    private String repoName;

    @Value("${repository.mongo.host:127.0.0.1}")
    private String mongoHost;

    @Value("${repository.mongo.port:27017}")
    private int mongoPort;

    private Repository repository;

    @Profile("dev")
    @Bean(name = "frsRepository")
    public Repository devRepository() throws IOException, InvalidFileStoreVersionException {
        if (repoHomePath == null) {
            logger.warn("Repository home is set, use tmp directory {}", System.getProperty("java.io.tmpdir"));
            repoHomePath = System.getProperty("java.io.tmpdir");
        }

        FileStore fs = FileStoreBuilder.fileStoreBuilder(new File(repoHomePath)).build();
        SegmentNodeStore ns = SegmentNodeStoreBuilders.builder(fs).build();
        repository = new Jcr(new Oak(ns)).with(repoName).createRepository();
        return repository;
    }

    @Profile("docker")
    @Bean(name = "frsRepository")
    public Repository dockerRepository() {
        DB db = new MongoClient(mongoHost, mongoPort).getDB(repoName);
        DocumentNodeStore ns = new MongoDocumentNodeStoreBuilder().setMongoDB(db).build();
        repository = new Jcr(new Oak(ns)).createRepository();
        return repository;
    }

    @PreDestroy
    public synchronized void destroy() {
        if (repository instanceof JackrabbitRepository) {
            ((JackrabbitRepository) repository).shutdown();
            logger.info("Repository shutdown complete");
            repository = null;
        }
    }
}
