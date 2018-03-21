/**
 * @File: XPathNode.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/21 下午7:51
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration.api;

/**
 * @version ${project.version}
 * @Description
 */
public interface XPathNode {
    XPathNode ROOT = new XPathNode() {
        public XPathNode getParent() {
            return null;
        }

        public String getPath() {
            return "//XmlConfiguration";
        }
    };

    XPathNode getParent();

    String getPath();
}
