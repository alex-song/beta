package alex.beta.portablecinema.tag.xml;

import com.google.common.io.Resources;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class GlossaryTest {
    private Glossary g;

    @Before
    public void setUp() throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Glossary.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        XMLInputFactory factory = XMLInputFactory.newInstance();

        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

        try (InputStream in = new ByteArrayInputStream(Resources.toByteArray(Resources.getResource("glossary-test1.xml")))) {
            XMLStreamReader reader = factory.createXMLStreamReader(in);
            g = jaxbUnmarshaller.unmarshal(reader, Glossary.class).getValue();
        }
    }

    @Test
    public void testLevel1() {
        Assert.assertEquals(1, g.getActor().size());
        Assert.assertEquals(1, g.getCategory().size());
        Assert.assertEquals(1, g.getProducer().size());
        Assert.assertEquals(1, g.getOther().size());
    }

    @Test
    public void testSet() {
        Assert.assertEquals(3, g.getActor().get(0).getTag().size());
        Assert.assertEquals(1, g.getCategory().get(0).getTag().size());
    }

    @Test
    public void testSimple() {
        Assert.assertEquals(0, g.getProducer().get(0).getTag().size());
    }
}
