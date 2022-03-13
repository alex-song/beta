package alex.beta.portablecinema.gui;

import com.google.common.io.Resources;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.io.File;

public class CoverImagePanelTest {
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetBMPDimension() throws Exception {
        File bmpFile = new File(Resources.getResource("panda.bmp").toURI());
        Dimension dBmp = CoverImagePanel.getImageDimension(bmpFile);
        Assert.assertEquals(512, dBmp.getHeight(), 0);
        Assert.assertEquals(512, dBmp.getWidth(), 0);

        File gifFile = new File(Resources.getResource("dolphin.gif").toURI());
        Dimension dGif = CoverImagePanel.getImageDimension(gifFile);
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
//
//
//    public static void main(String[] args) {
//        JPanel p = new JPanel(new BorderLayout());
//        ImageIcon icon = new StretchIcon(Toolkit.getDefaultToolkit().createImage(Resources.getResource("dolphin.gif")));
//        JLabel l = new JLabel();
//        l.setIcon(icon);
//        p.add( l, BorderLayout.CENTER);
//        JFrame f = new JFrame();
//        f.getContentPane().add(p);
//        f.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
//        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//        f.setLocationRelativeTo(null);
//        f.setVisible(true);
//    }
}
