/**
 * @File: HousekeepingRepository.java
 * @Project: onlinetranslation
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/2/22 上午9:28
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

/**
 * @version ${project.version}
 * @Description
 */
public interface HousekeepingRepository {
    /**
     * Delete translation requests, whose lastUpdatedOn and createdOn is before this timestamp
     *
     * @param timestamp
     * @return number of requests that are deleted
     */
    int removeExpiredTranslationRequests(long timestamp);
}
