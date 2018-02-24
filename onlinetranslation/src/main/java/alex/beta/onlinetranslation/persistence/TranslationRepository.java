/**
 * <p>
 * File Name: TranslationRepository.java
 * </p>
 * <p>
 * Project:   beta
 * </p>
 * <p>
 * Copyright: Copyright (c) 2018, All Rights Reserved
 * E-mail: song_liping@hotmail.com
 * </p>
 * <p>
 * Created on 2018/2/18 下午10:24
 * </p>
 */
package alex.beta.onlinetranslation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

/**
 * @author alexsong
 * @version ${project.version}
 */
public interface TranslationRepository extends JpaRepository<TranslationEntity, String> {
    List<TranslationEntity> findFirst5ByStatusAndLastUpdatedOnLessThanOrderByLastUpdatedOnAsc(TranslationStatus status, Date beforeDate);
}
