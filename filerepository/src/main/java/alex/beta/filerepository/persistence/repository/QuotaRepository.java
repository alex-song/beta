/**
 * @File: QuotaRepository.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/3 下午1:27
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.persistence.repository;

import alex.beta.filerepository.persistence.entity.Quota;

import javax.annotation.Nonnull;

/**
 * @version ${project.version}
 * @Description
 */
public interface QuotaRepository {
    /**
     * Find quota according to given appid. Create a new one, according to QuotaConfig, if it's not found.
     * @param appid
     * @return
     */
    Quota findOneOrCreateOneByAppidIgnoreCase(@Nonnull String appid);

    /**
     * Create new quota if it doesn't exist, otherwise return existing quota
     * @param quota
     * @return
     */
    Quota findOneOrCreateOne(@Nonnull Quota quota);

    /**
     * Find quota according to given appid, and increase the used quota points
     * @param appid
     * @param points points to increase as used quota
     * @return null if quota doesn't exist
     */
    Quota findAndIncreaseUsedQuotaByAppidIgnoreCase(@Nonnull String appid, long points);

    /**
     *
     * @param appid
     * @return null, if no quota is defined for given appid
     */
    Quota findOneByAppidIgnoreCase(@Nonnull String appid);

    /**
     * Find quota according to given appid, and set max quota as per given points
     * @param appid
     * @param points
     * @return
     */
    Quota findAndModifyMaxQuotaByAppidIgnoreCase(@Nonnull String appid, long points);

    /**
     * Set used quota to 0 of given appid
     * @param quotas
     */
    void resetUsedQuota(String... quotas);
}
