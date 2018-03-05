/**
 * @File:      TranslationRepository.java
 * @Project:   onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * @Date:      2018/2/18 下午10:24
 * @author:    <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @Description
 * @version ${project.version}
 */
public interface TranslationRepository extends JpaRepository<TranslationEntity, String> {
    List<TranslationEntity> findFirst3ByStatusAndLastUpdatedOnLessThanOrderByLastUpdatedOnAsc(TranslationStatus status, Date beforeDate);
}
