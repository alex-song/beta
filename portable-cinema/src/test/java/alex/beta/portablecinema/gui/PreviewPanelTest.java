package alex.beta.portablecinema.gui;

import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class PreviewPanelTest {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetBMPDimension() throws Exception {
        File bmpFile = new File(Resources.getResource("panda.bmp").toURI());
        Dimension dBmp = PreviewPanel.getImageDimension(bmpFile);
        Assert.assertEquals(512, dBmp.getHeight(), 0);
        Assert.assertEquals(512, dBmp.getWidth(), 0);

        File gifFile = new File(Resources.getResource("dolphin.gif").toURI());
        Dimension dGif = PreviewPanel.getImageDimension(gifFile);
        Assert.assertEquals(168, dGif.getHeight(), 0);
        Assert.assertEquals(295, dGif.getWidth(), 0);
    }

//    @Test
//    public void testDislpayBMP() throws Exception {
//        //ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(Resources.getResource("panda.bmp")));
//        ImageIcon icon = new ImageIcon(ImageIO.read(Resources.getResource("panda.bmp")));
//        JOptionPane.showMessageDialog(null, new JLabel(icon));
//    }
//
//    @Test
//    public void testDislpayGIF() {
//        ImageIcon icon = new ImageIcon(Toolkit.getDefaultToolkit().createImage(Resources.getResource("dolphin.gif")));
//        //ImageIcon icon = new ImageIcon(ImageIO.read(Resources.getResource("panda.bmp")));
//        JOptionPane.showMessageDialog(null, new JLabel(icon));
//    }
}
