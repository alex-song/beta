/**
 * @File: AssertionErrorBus.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/24 下午2:20
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @version ${project.version}
 * @Description
 */
class AssertionErrorBus {
    private static AssertionErrorBus ourInstance = new AssertionErrorBus();

    private Map<Integer, AssertionError> errors;

    static AssertionErrorBus getInstance() {
        return ourInstance;
    }

    private AssertionErrorBus() {
        errors = new HashMap<>();
    }

    synchronized void put(int port, AssertionError error) {
        errors.put(port, error);
    }

    void remove(int port) {
        errors.remove(port);
    }

    AssertionError get(int port) {
        return errors.get(port);
    }
}
