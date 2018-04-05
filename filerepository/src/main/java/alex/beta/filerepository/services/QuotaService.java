/**
 * @File: QuotaService.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午1:03
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.services;

import alex.beta.filerepository.QuotaExceededException;
import alex.beta.filerepository.persistence.entity.Quota;

import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
public interface QuotaService {

    /**
     * Increase points of used quota of given appid. Create quota, if it's not found.
     *
     * @param appid
     * @param points
     * @throws QuotaExceededException
     */
    void useQuota(String appid, long points) throws QuotaExceededException;

    /**
     * Reduce points of used quota of given appid. Do nothing, if it's not found.
     *
     * @param appid
     * @param points
     */
    void releaseQuota(String appid, long points);

    /**
     * @param appid
     * @return 0, if it's not found
     */
    long getUsedQuota(String appid);

    /**
     * @param appid
     * @param points
     */
    void setMaxQuota(String appid, long points);

    /**
     * @param appid
     * @return Long.MIN_VALUE, if it's not found
     */
    long getMaxQuota(String appid);

    /**
     * Calculate used quota of given appid, based on size of FileInfo
     *
     * @param appid
     */
    void recalculateQuota(String... appid);

    /**
     * @param quotas
     * @return Newly created quota or existing quota, according to given quotas
     */
    List<Quota> createQuota(Quota... quotas);

    /**
     * Reset used quota of given quota appid
     *
     * @param quotas
     */
    void resetUsedQuota(String... quotas);

    /**
     * Reset used quota of all appid in Quota and FileInfo
     */
    void resetAllUsedQuota();
}
