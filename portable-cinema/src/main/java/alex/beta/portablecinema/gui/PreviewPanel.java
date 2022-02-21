package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import static java.awt.Image.SCALE_SMOOTH;
import static javax.imageio.ImageIO.read;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Setter
@Getter
@NoArgsConstructor
@SuppressWarnings({"squid:S1948", "squid:S3776"})
public class PreviewPanel extends JPanel {

    public static final int THUMBNAIL_IMAGE_SIZE = 100;
    private static Logger logger = LoggerFactory.getLogger(PreviewPanel.class);
    private FileInfo fileInfo;
    private PortableCinemaConfig config;

    private JLabel photographLabel;
    private JToolBar buttonBar;
    private MissingIcon placeholderIcon;
    private String currentImg;

    public PreviewPanel(PortableCinemaConfig config, FileInfo fileInfo, int width, int height) {
        super(new BorderLayout());
        this.config = config;
        this.fileInfo = fileInfo;
        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));

        createUIComponents();

        /**
         * SwingWorker class that loads the images a background thread and calls publish
         * when a new one is ready to be displayed.
         * <p>
         * We use Void as the first SwingWorker param as we do not need to return
         * anything from doInBackground().
         *
         * Start the image loading SwingWorker in a background thread.
         */
        new SwingWorker<Void, ThumbnailAction>() {

            private boolean isFirstImageReady = false;

            /**
             * Creates big size and thumbnail versions of the target image files.
             */
            @Override
            protected Void doInBackground() {
                if (fileInfo == null || (isBlank(fileInfo.getCover1()) && isBlank(fileInfo.getCover2()))) {
                    publish(new ThumbnailAction(placeholderIcon, placeholderIcon, "没有预览图片"));
                } else {
                    if (fileInfo.getCover1() != null) {
                        publishImage(fileInfo.getPath(), fileInfo.getCover1());
                    }
                    if (fileInfo.getCover2() != null) {
                        publishImage(fileInfo.getPath(), fileInfo.getCover2());
                    }
                }
                return null;
            }

            /**
             * Create full size image and thumbnail image using give cover image
             * @param folderPath
             * @param coverImageName
             */
            private void publishImage(String folderPath, @NonNull String coverImageName) {
                String imgPath;
                if (isBlank(folderPath)) {
                    imgPath = coverImageName;
                } else {
                    imgPath = folderPath + File.separator + coverImageName;
                }
                //Big pic
                ImageIcon icon = createImageIcon(imgPath, coverImageName);
                ThumbnailAction thumbAction;
                if (icon != null) {
                    //Small pic
                    ImageIcon thumbnailIcon = new ImageIcon(getScaledImage(icon.getImage(), THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE));
                    thumbAction = new ThumbnailAction(icon, thumbnailIcon, imgPath);
                } else {
                    // the image failed to load for some reason
                    // so load a placeholder instead
                    thumbAction = new ThumbnailAction(placeholderIcon, placeholderIcon, "");
                }
                publish(thumbAction);
            }

            /**
             * Process all loaded images, and display the first image that is ready loaded
             */
            @Override
            protected void process(java.util.List<ThumbnailAction> chunks) {
                for (ThumbnailAction thumbAction : chunks) {
                    JButton thumbButton = new JButton(thumbAction);
                    // Add the new button BEFORE the last glue
                    // This centers the buttons in the toolbar
                    buttonBar.add(thumbButton, buttonBar.getComponentCount() - 1);
                    if (!isFirstImageReady) {
                        photographLabel.setIcon(thumbAction.displayPhoto);
                        currentImg = String.valueOf(thumbAction.getValue(SHORT_DESCRIPTION));
                        isFirstImageReady = true;
                    }
                }
            }
        }.execute();
    }

    public static void showDialog(Frame owner, FileInfo fileInfo, PortableCinemaConfig config) {
        JOptionPane.showOptionDialog(owner,
                new PreviewPanel(config, fileInfo, 800, 700),
                fileInfo.getName(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);
    }

    private void createUIComponents() {
        photographLabel = new JLabel();
        buttonBar = new JToolBar();
        placeholderIcon = new MissingIcon();

        // A label for displaying the pictures
        photographLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        photographLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        photographLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (config.getBaiduOCR() != null && isNotBlank(config.getBaiduOCR().getAppId()))
            photographLabel.addMouseListener(new ImageOcrActionHandler(config, this));

        // We add two glue components. Later in process() we will add thumbnail buttons
        // to the toolbar inbetween thease glue compoents. This will center the
        // buttons in the toolbar.
        buttonBar.setFloatable(false);
        buttonBar.add(Box.createGlue());
        buttonBar.add(Box.createGlue());

        add(buttonBar, BorderLayout.SOUTH);
        add(new JScrollPane(photographLabel), BorderLayout.CENTER);
    }

    /**
     * Creates an ImageIcon if the path is valid, and adjust image size to fit the screen.
     *
     * @param path        - resource path
     * @param description - description of the file
     */
    private ImageIcon createImageIcon(String path, String description) {
        try {
            BufferedImage img = null;
            File imgFile = new File(path);
            if (imgFile.exists() && imgFile.isFile()) {
                img = read(imgFile);
            }

            if (img != null) {
                if (img.getWidth() > (this.getWidth() - 20.0d) || img.getHeight() > (this.getHeight() - THUMBNAIL_IMAGE_SIZE - 30.0d)) {
                    double ratio = Math.min((this.getWidth() - 20.0d) / img.getWidth(), (this.getHeight() - THUMBNAIL_IMAGE_SIZE - 30.0d) / img.getHeight());
                    return new ImageIcon(img.getScaledInstance((int) (img.getWidth() * ratio), (int) (img.getHeight() * ratio), SCALE_SMOOTH), description);
                } else
                    return new ImageIcon(img, description);
            } else {
                logger.info("Cover image [{}] does not exist, or it's not a valid image", path);
                return null;
            }
        } catch (Exception ex) {
            logger.info("Cover image [{}] does not exist, or it's not a valid image", path);
            return null;
        }
    }

    /**
     * Resizes an image using a Graphics2D object backed by a BufferedImage.
     *
     * @param srcImg - source image to scale
     * @param w      - desired width
     * @param h      - desired height
     * @return - the new resized image
     */
    private Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = resizedImg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();
        return resizedImg;
    }

    /**
     * Action class that shows the image specified in it's constructor.
     */
    private class ThumbnailAction extends AbstractAction {

        /**
         * The icon if the full image we want to display.
         */
        private Icon displayPhoto;

        /**
         * @param photo   - The full size photo to show in the button.
         * @param thumb   - The thumbnail to show in the button.
         * @param imgPath - The description of the icon.
         */
        public ThumbnailAction(Icon photo, Icon thumb, String imgPath) {
            displayPhoto = photo;

            // The short description becomes the tooltip of a button.
            putValue(SHORT_DESCRIPTION, imgPath);

            // The LARGE_ICON_KEY is the key for setting the
            // icon when an Action is applied to a button.
            putValue(LARGE_ICON_KEY, thumb);
        }

        /**
         * Shows the big image in the main area.
         */
        public void actionPerformed(ActionEvent e) {
            photographLabel.setIcon(displayPhoto);
            currentImg = String.valueOf(getValue(SHORT_DESCRIPTION));
        }
    }

    /**
     * black border, white background, and red X
     */
    private class MissingIcon implements Icon {

        private int width = PreviewPanel.THUMBNAIL_IMAGE_SIZE;
        private int height = PreviewPanel.THUMBNAIL_IMAGE_SIZE;

        private BasicStroke stroke = new BasicStroke(4);

        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setColor(Color.WHITE);
            g2d.fillRect(x + 1, y + 1, width - 2, height - 2);

            g2d.setColor(Color.BLACK);
            g2d.drawRect(x + 1, y + 1, width - 2, height - 2);

            g2d.setColor(Color.RED);

            g2d.setStroke(stroke);
            g2d.drawLine(x + 10, y + 10, x + width - 10, y + height - 10);
            g2d.drawLine(x + 10, y + height - 10, x + width - 10, y + 10);

            g2d.dispose();
        }

        public int getIconWidth() {
            return width;
        }

        public int getIconHeight() {
            return height;
        }
    }
}
