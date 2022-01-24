/**
 * @File: UserEntity.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/6 下午9:16
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.onlinetranslation.persistence;

import javax.persistence.*;
import java.util.List;

/**
 * @version ${project.version}
 * @Description
 */

@Entity
@Table(name = "User",
        indexes = {@Index(columnList = "name"), @Index(columnList = "path, password")})
public class UserEntity {
    @Id
    private int id;

    @Column(name = "name", length = 64, unique = true, nullable = false)
    private String name;

    @Column(name = "password", length = 64)
    private String password;

    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "UserRole", joinColumns = {@JoinColumn(name = "user_id")},
            indexes = {@Index(columnList = "user_id")})
    private List<String> roles;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
