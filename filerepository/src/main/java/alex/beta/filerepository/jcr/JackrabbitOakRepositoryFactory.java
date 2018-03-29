package alex.beta.filerepository.jcr;


import com.mongodb.DB;
import com.mongodb.Mongo;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;

/**
 * Created by songlip on 2018/3/29.
 */

@EnableAutoConfiguration
@Component
public class JackrabbitOakRepositoryFactory {
    private static final Logger logger = LoggerFactory.getLogger(JackrabbitOakRepositoryFactory.class);

    @Value("${repository.home:#{null}}")
    private String repoHomePath;

    @Value("${repository.config}")
    private String repoConfigPath;

    @Value("${repository.name:frs}")
    private String repoName;

    @Profile("dev")
    @Bean(name = "frsRepository")
    public Repository devRepository() throws IOException, InvalidFileStoreVersionException {
        if (repoHomePath == null) {
            logger.warn("Repository home is set, use tmp directory {}", System.getProperty("java.io.tmpdir"));
            repoHomePath = System.getProperty("java.io.tmpdir");
        }

        FileStore fs = FileStoreBuilder.fileStoreBuilder(new File(repoHomePath)).build();
        SegmentNodeStore ns = SegmentNodeStoreBuilders.builder(fs).build();
        return new Jcr(new Oak(ns)).createRepository();
    }

    @Profile("docker")
    @Bean(name = "frsRepository")
    public Repository dockerRepository() throws IOException, InvalidFileStoreVersionException {
        DB db = new Mongo("127.0.0.1", 27017).getDB("test2");
        DocumentNodeStore ns = new MongoDocumentNodeStoreBuilder().setMongoDB(db).build();
        return new Jcr(new Oak(ns)).createRepository();
    }
}
