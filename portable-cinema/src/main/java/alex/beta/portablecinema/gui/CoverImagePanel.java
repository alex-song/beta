package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.pojo.FileInfo;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static alex.beta.portablecinema.filesystem.AbstractFolderVisitor.doSkip;
import static alex.beta.portablecinema.filesystem.AbstractFolderVisitor.isImageFile;
import static alex.beta.portablecinema.filesystem.FileScan.containsOnlyImages;
import static java.awt.Image.SCALE_DEFAULT;
import static java.awt.Image.SCALE_SMOOTH;
import static javax.imageio.ImageIO.getImageReadersBySuffix;
import static javax.swing.Action.SHORT_DESCRIPTION;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class CoverImagePanel extends JPanel {
    private static final int THUMBNAIL_IMAGE_SIZE = 100;
    private static final Logger logger = LoggerFactory.getLogger(CoverImagePanel.class);

    private final PortableCinemaConfig config;
    private JLabel photographLabel;
    private JToolBar buttonBar;
    private MissingIcon placeholderIcon = new MissingIcon();
    private ThumbnailAction currentAction;

    public CoverImagePanel(PortableCinemaConfig config, FileInfo fileInfo, int width, int height) {
        super(new BorderLayout());
        this.config = config;

        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setBorder(null);

        createUIComponents();
        loadImages(fileInfo);
    }

    void loadImages(FileInfo fileInfo) {
        this.close();

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
                    // publish cover 1 and cover 2 first
                    if (isNotBlank(fileInfo.getCover1())) {
                        publishImage(fileInfo.getPath(), fileInfo.getCover1());
                    }
                    if (isNotBlank(fileInfo.getCover2())) {
                        publishImage(fileInfo.getPath(), fileInfo.getCover2());
                    }

                    // publish images in the same folder, other than cover 1 and cover 2
                    File currentFolder = new File(fileInfo.getPath() == null ? "." : fileInfo.getPath());
                    List<String> images = getAllImages(currentFolder);
                    for (String image : images) {
                        try {
                            publishImage(currentFolder.getCanonicalPath(), image);
                        } catch (Exception ex) {
                            logger.warn("Failed to publish image [{}]", image, ex);
                        }
                    }

                    // publish images in sub folder, that contains only images, if cover 1 or cover 2 is blank
                    if (images.isEmpty() && (isBlank(fileInfo.getCover1()) || isBlank(fileInfo.getCover2()))) {
                        File[] subFolders = currentFolder.listFiles(file -> file.isDirectory() && !doSkip(config, file));
                        if (subFolders != null && subFolders.length == 1 && containsOnlyImages(config, subFolders[0])) {
                            List<String> subImages = getAllImages(subFolders[0]);
                            for (String image : subImages) {
                                try {
                                    publishImage(subFolders[0].getCanonicalPath(), image);
                                } catch (Exception ex) {
                                    logger.warn("Failed to publish image [{}]", image, ex);
                                }
                            }
                        }
                    }
                }
                return null;
            }

            private List<String> getAllImages(@NonNull File folder) {
                List<String> images = new ArrayList<>();
                File[] files = folder.listFiles(file -> isImageFile(config, file) && !doSkip(config, file));
                if (files != null) {
                    for (File f : files) {
                        String fName = f.getName();
                        if (!fName.equalsIgnoreCase(fileInfo.getCover1())
                                && !fName.equalsIgnoreCase(fileInfo.getCover2())) {
                            images.add(fName);
                        }
                    }
                }
                return images;
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

    public void close() {
        if (buttonBar != null)
            for (int i = buttonBar.getComponentCount() - 1; i >= 0; i--) {
                if (buttonBar.getComponent(i) instanceof JButton) {
                    Action a = ((JButton) buttonBar.getComponent(i)).getAction();
                    if (a != null && a instanceof ThumbnailAction) {
                        ((ThumbnailAction) a).close();
                    }
                    buttonBar.remove(i);
                }
            }
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
                final String currentImg = getCurrentImageName();
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
        // to the toolbar in between these glue components. This will center the
        // buttons in the toolbar.
        buttonBar.setFloatable(false);
        buttonBar.add(Box.createGlue());
        buttonBar.add(Box.createGlue());

        JScrollPane jsp = new JScrollPane(buttonBar);
        jsp.setBorder(BorderFactory.createEmptyBorder());
        jsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jsp.getHorizontalScrollBar().setUnitIncrement(10);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setDividerLocation(this.getHeight() - THUMBNAIL_IMAGE_SIZE);
        splitPane.setOneTouchExpandable(false);
        splitPane.setContinuousLayout(true);
        splitPane.setDividerSize(0);

        splitPane.add(new JScrollPane(photographLabel));
        splitPane.add(jsp);
        add(splitPane, BorderLayout.CENTER);
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
                double screenWidth = this.getWidth() - 20.0;
                double screenHeight = this.getHeight() - THUMBNAIL_IMAGE_SIZE - 60.0;
                boolean respectWidth = ((imgDimension.getWidth() / imgDimension.getHeight()) > (screenWidth / screenHeight));
                if (imgDimension.getWidth() <= screenWidth && imgDimension.getHeight() <= screenHeight) {
                    return new ImageIcon[]{
                            new ImageIcon(img),
                            new ImageIcon(img),
                            new ImageIcon(img.getScaledInstance(THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE, hints))};
                } else if (respectWidth) {
                    return new ImageIcon[]{
                            new ImageIcon(img),
                            createStretchIcon(img, (int) screenWidth, -1, hints),
                            new ImageIcon(img.getScaledInstance(THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE, hints))};
                } else {
                    return new ImageIcon[]{
                            new ImageIcon(img),
                            createStretchIcon(img, -1, (int) screenHeight, hints),
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

    private ImageIcon createStretchIcon(Image image, int width, int height, int hints) {
        if (image instanceof BufferedImage) {
            return new ImageIcon(image.getScaledInstance(width, height, hints));
        } else {
            return new StretchIcon(image, true);
        }
    }

    String getCurrentImageName() {
        return this.currentAction != null ? String.valueOf(this.currentAction.getValue(SHORT_DESCRIPTION)) : null;
    }

    /**
     * black border, white background, and red X
     */
    private static class MissingIcon implements Icon {

        private final BasicStroke stroke = new BasicStroke(4);
        private int width = THUMBNAIL_IMAGE_SIZE;
        private int height = THUMBNAIL_IMAGE_SIZE;

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

        /**
         * Dispose images
         */
        public void close() {
            try {
                String sunToolkitImageClassName = "ToolkitImage";
                // ToolkitImage doesn't support getGraphics method
                if (scaledImage != null && scaledImage instanceof ImageIcon) {
                    Image i = ((ImageIcon) scaledImage).getImage();
                    if (i != null && !i.getClass().getSimpleName().contains(sunToolkitImageClassName) && i.getGraphics() != null)
                        i.getGraphics().dispose();
                }
                if (fullsize != null && fullsize instanceof ImageIcon) {
                    Image i = ((ImageIcon) fullsize).getImage();
                    if (i != null && !i.getClass().getSimpleName().contains(sunToolkitImageClassName) && i.getGraphics() != null)
                        i.getGraphics().dispose();
                }
                if (getValue(LARGE_ICON_KEY) != null && getValue(LARGE_ICON_KEY) instanceof ImageIcon) {
                    Image i = ((ImageIcon) getValue(LARGE_ICON_KEY)).getImage();
                    if (i != null && !i.getClass().getSimpleName().contains(sunToolkitImageClassName) && i.getGraphics() != null)
                        i.getGraphics().dispose();
                }
            } catch (Exception ex) {
                logger.warn("Error when disposing ThumbnailAction", ex);
            }
        }
    }
}
