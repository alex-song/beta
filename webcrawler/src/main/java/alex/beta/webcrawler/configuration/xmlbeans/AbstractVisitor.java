/**
 * @File: AbstractVisitor.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/22 22:10
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.xmlbeans;

import alex.beta.webcrawler.configuration.api.IVisitor;
import alex.beta.webcrawler.configuration.api.PathSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.Unmarshaller;

/**
 * @version ${project.version}
 * @Description
 */
public abstract class AbstractVisitor implements IVisitor.InnerVisitor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractVisitor.class);

    private PathSupport parent;

    @SuppressWarnings("squid:S1172")
    public void afterUnmarshal(Unmarshaller u, Object parent) {
        if (parent instanceof PathSupport) {
            this.parent = (PathSupport) parent;
        }
    }

    @Override
    public String getPath() {
        return this.parent.getPath() + "/Visitor";
    }
}
