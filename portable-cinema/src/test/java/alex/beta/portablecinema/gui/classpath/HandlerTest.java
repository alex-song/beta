package alex.beta.portablecinema.gui.classpath;

import org.junit.*;

import javax.swing.*;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;

@Ignore
public class HandlerTest {
    @Before
    public void setUp() {
        Handler.install();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testEmbeddedImage() throws Exception {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextPane edit = new JTextPane();
        frame.getContentPane().add(edit);
        edit.setContentType("text/html");

        //String html = "<html><body>Local image<br><img src=\"data:image/png;charset=utf-8;base64,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\"></body></html>";
        String html = "<html><body>Local image<br>" +
                "<img src=\"classpath:images/Scan-icon.png\" title=\"SCAN\">" +
                "</body></html>";

        edit.setText(html);
        frame.setSize(500, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        HTMLDocument doc = (HTMLDocument) edit.getStyledDocument();
        Assert.assertEquals("head", doc.getDefaultRootElement().getElement(0).getName());
        Assert.assertEquals("body", doc.getDefaultRootElement().getElement(1).getName());

        Assert.assertEquals("p-implied", doc.getDefaultRootElement().getElement(1).getElement(0).getName());
        Assert.assertEquals("img", doc.getDefaultRootElement().getElement(1).getElement(0).getElement(2).getName());

        Element imgElement = doc.getDefaultRootElement().getElement(1).getElement(0).getElement(2);
        Assert.assertEquals("classpath:images/Scan-icon.png", imgElement.getAttributes().getAttribute(HTML.Attribute.SRC));

//        Thread.sleep(50000);
    }

    public void testTableWithStyle() throws Exception {
        String html = "<html>" +
                "<head>" +
                "<style>" +
                ".alnright { text-align: right; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">" +
                "<tr>" +
                "<td style=\"text-align: right\" width=\"200\">RIGHT</td>" +
                "<td width=\"200\">DEFAULT</td>" +
                "<td class=\"alnright\" width=\"200\">RIGHT</td>" +
                "</tr>" +
                "</table>" +
                "</body>" +
                "</html>";
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JTextPane edit = new JTextPane();
        frame.getContentPane().add(edit);
        edit.setContentType("text/html");
        edit.setText(html);
        frame.setSize(600, 300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

//        Thread.sleep(5000);
    }
}
