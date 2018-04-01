/**
 * @File: MongoConfiguration.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/1 上午10:58
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository;

import alex.beta.filerepository.mongo.FileInfoCascadingMongoEventListener;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @version ${project.version}
 * @Description
 */

@Configuration
@EnableAutoConfiguration
public class MongoConfiguration {

    @Bean
    public FileInfoCascadingMongoEventListener cascadingMongoEventListener() {
        return new FileInfoCascadingMongoEventListener();
    }
}
