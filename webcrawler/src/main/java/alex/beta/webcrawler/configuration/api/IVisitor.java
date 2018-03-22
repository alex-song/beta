/**
 * @File: IVisitor.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/20 9:13
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

import alex.beta.webcrawler.configuration.xmlbeans.XmlConfiguration;
import edu.uci.ics.crawler4j.crawler.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * @version ${project.version}
 * @Description
 */
public interface IVisitor extends PathSupport {
    String getVisitorClass();

    void visit(Page page);
}
