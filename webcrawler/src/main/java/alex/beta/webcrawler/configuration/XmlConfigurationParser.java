/**
 * @File: XmlConfigurationParser.java
 * @Project: beta
 * @Copyright: Copyright (c) 2018, All Rights Reserved
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * </p>
 * @Date: 2018/3/15 9:21
 * @author: <a target=_blank href="mailto:song_liping@hotmail.com">Alex Song</a>
 */
package alex.beta.webcrawler.configuration;

import alex.beta.webcrawler.configuration.api.IConfiguration;
import com.google.common.io.Resources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.Objects;

/**
 * @version ${project.version}
 * @Description
 */
public class XmlConfigurationParser {

    private static final Logger logger = LoggerFactory.getLogger(XmlConfigurationParser.class);

    private static final String XML_CONFIGURATION_BEAN = "alex.beta.webcrawler.configuration.xmlbeans.Configuration";

    private XmlConfigurationParser() {
        //Hide default public constructor
    }

    public static IConfiguration parse(String filePath) throws ConfigurationException {
        Objects.requireNonNull(filePath);
        try {
            Class<? extends IConfiguration> rootElementClass = Class.forName(XML_CONFIGURATION_BEAN).asSubclass(IConfiguration.class);
            JAXBContext jaxbContext = JAXBContext.newInstance(rootElementClass);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            XMLInputFactory factory = XMLInputFactory.newInstance();
//            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
//            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            XMLStreamReader reader = factory.createXMLStreamReader(Resources.getResource(filePath).openStream());
            return jaxbUnmarshaller.unmarshal(reader, rootElementClass).getValue();
        } catch (IOException | XMLStreamException e1) {
            logger.error("Failed to read configuration XML file, {}.", filePath, e1);
            throw new ConfigurationException(filePath, e1);
        } catch (ClassNotFoundException e2) {
            logger.error("{} is not found.", XML_CONFIGURATION_BEAN, e2);
            throw new InternalError(XML_CONFIGURATION_BEAN, e2);
        } catch (JAXBException e3) {
            logger.error("Cannot bind XML {} with Configuration bean.", filePath, e3);
            throw new ConfigurationException(filePath, e3);
        }
    }
}
