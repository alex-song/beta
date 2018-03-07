/**
 * @File: UserRepository.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/6 下午9:23
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @version ${project.version}
 * @Description
 */
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    UserEntity findTopByNameIgnoreCaseOrderByIdAsc(String name);
}
