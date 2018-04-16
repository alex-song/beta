/**
 * @File: IFrsConfig.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/4/8 22:13
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.filerepository.config.xmlbeans;

import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */
public interface IFrsConfig {
    List<? extends AbstractUser.AdminUser> getAdmin();

    List<? extends AbstractUser.OperatorUser> getOperator();

    List<? extends AbstractUser.GuestUser> getGuest();

    List<? extends AbstractApp> getApp();

    boolean isDeleteExpiredFiles();

    boolean isRecalculateQuotas();
}
