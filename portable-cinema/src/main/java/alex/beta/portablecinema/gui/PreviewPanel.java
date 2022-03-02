package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.command.EditCommand;
import alex.beta.portablecinema.command.ViewCommand;
import alex.beta.portablecinema.pojo.FileInfo;
import alex.beta.simpleocr.baidu.BaiduOcr;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import static alex.beta.portablecinema.command.EditCommand.UPDATE_SUCCESS;
import static alex.beta.portablecinema.command.EditCommand.resultText;
import static alex.beta.portablecinema.gui.TagSuggestionPanel.*;
import static alex.beta.simpleocr.OcrFactory.PROXY_HOST;
import static alex.beta.simpleocr.OcrFactory.PROXY_PORT;
import static java.awt.Image.SCALE_DEFAULT;
import static java.awt.Image.SCALE_SMOOTH;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@SuppressWarnings({"squid:S1948", "squid:S3776"})
public class PreviewPanel extends JPanel {

    private static final int THUMBNAIL_IMAGE_SIZE = 100;
    private static Logger logger = LoggerFactory.getLogger(PreviewPanel.class);
    private FileInfo fileInfo;
    private PortableCinemaConfig config;

    private BaiduOcr ocrClient;

    private JLabel photographLabel;
    private JToolBar buttonBar;
    private MissingIcon placeholderIcon = new MissingIcon();
    private ThumbnailAction currentAction;
    private boolean isUpdated = false;

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
                if (fileInfo == null || !fileInfo.hasCover()) {
                    publish(new ThumbnailAction(placeholderIcon, placeholderIcon, placeholderIcon, ""));
                } else {
                    if (isNotBlank(fileInfo.getCover1())) {
                        publishImage(fileInfo.getPath(), fileInfo.getCover1());
                    }
                    if (isNotBlank(fileInfo.getCover2())) {
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
                ImageIcon[] icons = createImageIcons(imgPath);
                if (icons.length == 3) {
                    //Full size pic
                    ImageIcon fullsizeIcon = icons[0];
                    //Big pic
                    ImageIcon scaledImageIcon = icons[1];
                    //Thumbnail pic
                    ImageIcon thumbnailIcon = icons[2];
                    publish(new ThumbnailAction(fullsizeIcon, scaledImageIcon, thumbnailIcon, imgPath));
                } else {
                    publish(new ThumbnailAction(placeholderIcon, placeholderIcon, placeholderIcon, ""));
                }
            }

            /**
             * Process all loaded images, and display the first image that is ready loaded
             */
            @Override
            protected void process(List<ThumbnailAction> chunks) {
                for (ThumbnailAction thumbAction : chunks) {
                    JButton thumbButton = new JButton(thumbAction);
                    // Add the new button BEFORE the last glue
                    // This centers the buttons in the toolbar
                    buttonBar.add(thumbButton, buttonBar.getComponentCount() - 1);
                    if (!isFirstImageReady) {
                        photographLabel.setIcon(thumbAction.scaledImage);
                        currentAction = thumbAction;
                        isFirstImageReady = true;
                    }
                }
            }
        }.execute();

        PortableCinemaConfig.BaiduOCR ocrConfig = config.getBaiduOCR();
        if (ocrConfig != null && isNotBlank(ocrConfig.getAppId())) {
            initOcrClient();
        } else {
            ocrClient = null;
        }

        // short cut key to OCR, ctrl+R
        if (ocrClient != null)
            registerKeyboardAction(ae -> doOCR(), KeyStroke.getKeyStroke("Ctrl R"), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * @param owner
     * @param fileInfo
     * @param config
     * @return true, if file info is updated
     */
    public static boolean showDialog(Frame owner, FileInfo fileInfo, PortableCinemaConfig config) {
        PreviewPanel pp = new PreviewPanel(config, fileInfo, 800, 700);
        Object[] options;
        if (pp.ocrClient != null) {
            options = new Object[]{"确定", "文字识别(ctrl + R)"};
        } else {
            options = new Object[]{"确定"};
        }
        JOptionPane jop = new JOptionPane(pp, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, options[0]);
        JDialog dialog = new JDialog(owner, fileInfo.getName(), true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setContentPane(jop);

        jop.addPropertyChangeListener(evt -> {
            if (JOptionPane.VALUE_PROPERTY.equals(evt.getPropertyName())) {
                if (options[0].equals(evt.getNewValue()) || evt.getNewValue().equals(JOptionPane.DEFAULT_OPTION) || evt.getNewValue().equals(JOptionPane.OK_OPTION)) {
                    dialog.dispose();
                } else if (options.length > 1 && options[1].equals(evt.getNewValue()) && isNotBlank(pp.getCurrentImg())) {
                    if (pp.ocrClient != null) {
                        pp.doOCR();
                    } else {
                        JOptionPane.showMessageDialog(pp, "请检查OCR程序配置", fileInfo.getName(), JOptionPane.PLAIN_MESSAGE, null);
                    }
                }
            }
            jop.setValue(JOptionPane.UNINITIALIZED_VALUE);
        });
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.dispose();
        return pp.isUpdated;
    }

    /**
     * Gets image dimensions for given file
     *
     * @param imgFile image file
     * @return dimensions of image
     * @throws IOException if the file is not a known image
     */
    static Dimension getImageDimension(File imgFile) throws IOException {
        String suffix = imgFile.getName().substring(imgFile.getName().lastIndexOf('.') + 1);
        Iterator<ImageReader> iter = getImageReadersBySuffix(suffix);
        while (iter.hasNext()) {
            ImageReader reader = iter.next();
            try (ImageInputStream stream = new FileImageInputStream(imgFile)) {
                reader.setInput(stream);
                return new Dimension(reader.getWidth(reader.getMinIndex()), reader.getHeight(reader.getMinIndex()));
            } catch (IOException ex) {
                if (logger.isInfoEnabled())
                    logger.info("Failed to read {} using {}", imgFile.getCanonicalPath(), reader.getClass().getSimpleName(), ex);
            } finally {
                reader.dispose();
            }
        }
        throw new IOException("Not a known image file: " + imgFile.getCanonicalPath());
    }

    private void doOCR() {
        int option = TagSuggestionPanel.showDialog(this, ocrClient, getCurrentImg());
        if (option == SAVE_CHANGES_OPTION || option == SAVE_CHANGES_OPEN_EDITOR_OPTION) {
            int result = new EditCommand(fileInfo).execute(config);
            logger.debug("Update tags of file info [{}], result is [{}]", fileInfo, result);
            if (option == SAVE_CHANGES_OPTION)
                JOptionPane.showMessageDialog(this, resultText(result), fileInfo.getName(), JOptionPane.PLAIN_MESSAGE, null);
            else {
                //open editor
                showEditorDialog(fileInfo.getOtid());
            }
            this.isUpdated = true;
        } else if (option == DISCARD_CHANGES_OPEN_EDITOR_OPTION || option == NO_CHANGE_OPEN_EDITOR_OPTION) {
            //open editor
            this.isUpdated = showEditorDialog(fileInfo.getOtid());
        }
    }

    private boolean showEditorDialog(String otid) {
        boolean isUpdated2 = false;
        FileInfo fileInfoToEdit = new ViewCommand(otid).execute(config);
        if (FileInfoEditPanel.showDialog(this, fileInfoToEdit)) {
            int result = new EditCommand(fileInfoToEdit).execute(config);
            logger.debug("Update file info [{}], result is [{}]", fileInfoToEdit, result);
            if (result == UPDATE_SUCCESS) {
                this.fileInfo = fileInfoToEdit;
                isUpdated2 = true;
            }
            JOptionPane.showMessageDialog(this, resultText(result), fileInfoToEdit.getName(), JOptionPane.PLAIN_MESSAGE, null);
        }
        return isUpdated2;
    }

    private String getCurrentImg() {
        return this.currentAction != null ? String.valueOf(this.currentAction.getValue(SHORT_DESCRIPTION)) : null;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    private void initOcrClient() {
        PortableCinemaConfig.BaiduOCR ocrConfig = config.getBaiduOCR();
        Properties ocrProps = new Properties();
        ocrProps.setProperty(BaiduOcr.BAIDU_API_KEY, ocrConfig.getApiKey());
        ocrProps.setProperty(BaiduOcr.BAIDU_SECRET_KEY, ocrConfig.getSecretKey());
        ocrProps.setProperty(BaiduOcr.BAIDU_APP_ID, ocrConfig.getAppId());
        if (isNotBlank(ocrConfig.getProxyHost())) {
            ocrProps.setProperty(PROXY_HOST, ocrConfig.getProxyHost());
            if (ocrConfig.getProxyPort() == 0) {
                ocrProps.setProperty(PROXY_PORT, "80");
            } else {
                ocrProps.setProperty(PROXY_PORT, String.valueOf(ocrConfig.getProxyPort()));
            }
        }
        this.ocrClient = new BaiduOcr(ocrProps);
    }

    private void createUIComponents() {
        photographLabel = new JLabel();
        buttonBar = new JToolBar();

        // A label for displaying the pictures
        photographLabel.setVerticalTextPosition(SwingConstants.BOTTOM);
        photographLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        photographLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photographLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        photographLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                final String currentImg = getCurrentImg();
                if ((event.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(event) && isNotBlank(currentImg))) {
                    if (photographLabel.getIcon() == currentAction.fullsize) {
                        photographLabel.setIcon(currentAction.scaledImage);
                    } else {
                        photographLabel.setIcon(currentAction.fullsize);
                    }
                    photographLabel.repaint();
                }
            }
        });

        // We add two glue components. Later in process() we will add thumbnail buttons
        // to the toolbar in between thease glue compoents. This will center the
        // buttons in the toolbar.
        buttonBar.setFloatable(false);
        buttonBar.add(Box.createGlue());
        buttonBar.add(Box.createGlue());

        add(buttonBar, BorderLayout.SOUTH);
        add(new JScrollPane(photographLabel), BorderLayout.CENTER);
    }

    /**
     * Creates 3 ImageIcons if the path is valid.
     * [0] - Full size image
     * [1] - Image size to fit the screen.
     * [2] - Image size to fit thumbnail button.
     *
     * @param path - resource path
     */
    private ImageIcon[] createImageIcons(String path) {
        try {
            Image img = null;
            int hints = SCALE_SMOOTH;
            File imgFile = new File(path);
            if (imgFile.exists() && imgFile.isFile()) {
                int pos = imgFile.getName().lastIndexOf('.');
                if (pos < 0) {
                    logger.info("No extension for file [{}]", path);
                    return new ImageIcon[]{};
                } else {
                    String suffix = imgFile.getName().substring(pos + 1);
                    if ("bmp".equalsIgnoreCase(suffix)) {
                        //Has to use BufferedImage for BMP images
                        img = ImageIO.read(imgFile);
                    } else {
                        if ("gif".equalsIgnoreCase(suffix)) {
                            hints = SCALE_DEFAULT;
                        }
                        img = Toolkit.getDefaultToolkit().createImage(path);
                    }
                }
            }

            if (img != null) {
                Dimension imgDimension = getImageDimension(imgFile);
                boolean respectWidth = ((imgDimension.getWidth() / imgDimension.getHeight()) > ((this.getWidth() - 20.0d) / (this.getHeight() - THUMBNAIL_IMAGE_SIZE - 30.0d)));

                if (imgDimension.getWidth() <= this.getWidth() - 20 && imgDimension.getHeight() <= this.getHeight() - THUMBNAIL_IMAGE_SIZE - 30) {
                    return new ImageIcon[]{
                            new ImageIcon(img),
                            new ImageIcon(img),
                            new ImageIcon(img.getScaledInstance(THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE, hints))};
                } else if (respectWidth) {
                    return new ImageIcon[]{
                            new ImageIcon(img),
                            new ImageIcon(img.getScaledInstance(this.getWidth() - 20, -1, hints)),
                            new ImageIcon(img.getScaledInstance(THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE, hints))};
                } else {
                    return new ImageIcon[]{
                            new ImageIcon(img),
                            new ImageIcon(img.getScaledInstance(-1, this.getHeight() - THUMBNAIL_IMAGE_SIZE - 30, hints)),
                            new ImageIcon(img.getScaledInstance(THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE, hints))};
                }
            } else {
                logger.info("Cover image [{}] does not exist, or it's not a valid image", path);
                return new ImageIcon[]{};
            }
        } catch (Exception ex) {
            logger.info("Cover image [{}] does not exist, or it's not a valid image", path);
            return new ImageIcon[]{};
        }
    }

    /**
     * Action class that shows the image specified in it's constructor.
     */
    private class ThumbnailAction extends AbstractAction {

        /**
         * The icon if the scaled image we want to display.
         */
        private Icon scaledImage;

        /**
         * The icon if the full size image we want to display
         */
        private Icon fullsize;

        /**
         * @param fullsize    - The original size image to show in preview
         * @param scaledImage - The scaled image to show in preview.
         * @param thumb       - The thumbnail to show in the button.
         * @param imgPath     - The path of the image.
         */
        public ThumbnailAction(Icon fullsize, Icon scaledImage, Icon thumb, String imgPath) {
            this.fullsize = fullsize;
            this.scaledImage = scaledImage;

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
            photographLabel.setIcon(scaledImage);
            currentAction = this;
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
