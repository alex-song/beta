package alex.beta.portablecinema.gui;

import alex.beta.portablecinema.PortableCinemaConfig;
import alex.beta.portablecinema.filesystem.FileSystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.awt.Image.SCALE_DEFAULT;
import static java.awt.Image.SCALE_SMOOTH;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * https://stackoverflow.com/questions/4096433/making-jfilechooser-show-image-thumbnails
 * <p>
 * Known issues:
 * <p>
 * 1) We don't maintain the image's aspect ratio when scaling. Doing so could result in icons with strange dimensions
 * that will break the alignment of the list view.
 * The solution is probably to create a new BufferedImage that is 16x16 and render the scaled image on top of it,
 * centered.
 * Can try StretchIcon, but not urgent, because it's small.
 * <p>
 * 2) If a file is not an image, or is corrupted, no icon will be shown at all. It looks like the program only detects
 * this error while rendering the image, not when we load or scale it, so we can't detect this in advance.
 * However, we might detect it if we fix issue 1.
 */
public class ThumbnailFileChooser extends JFileChooser {
    private static final Logger logger = LoggerFactory.getLogger(ThumbnailFileChooser.class);

    /**
     * All preview icons will be this width and height
     */
    private static final int ICON_SIZE = 16;

    /**
     * This blank icon will be used while previews are loading
     */
    private static final Image LOADING_IMAGE = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);

    /**
     * Use a weak hash map to cache images until the next garbage collection (saves memory)
     */
    private final Map<File, ImageIcon> imageMap = new WeakHashMap();

    private PortableCinemaConfig config;

    public ThumbnailFileChooser(PortableCinemaConfig config) {
        super();
        this.config = config;
        this.setFileView(new ThumbnailFileView());
    }

    private class ThumbnailFileView extends FileView {
        /**
         * This thread pool is where the thumbnail icon loaders run
         */
        private final ExecutorService executor = Executors.newCachedThreadPool();

        public Icon getIcon(File file) {
            if (file.isDirectory()) {
                return super.getIcon(file);
            } else if (!FileSystemUtils.isImageFile(config, file)) {
                return super.getIcon(file);
            }
            // Our cache makes browsing back and forth lightning-fast! :D
            synchronized (imageMap) {
                ImageIcon icon = imageMap.get(file);
                if (icon == null) {
                    // Create a new icon with the default image
                    icon = new ImageIcon(LOADING_IMAGE);
                    // Add to the cache
                    imageMap.put(file, icon);
                    // Submit a new task to load the image and update the icon
                    executor.submit(new ThumbnailIconLoader(icon, file));
                }
                return icon;
            }
        }

        private class ThumbnailIconLoader implements Runnable {
            private final File file;
            private ImageIcon icon;

            public ThumbnailIconLoader(ImageIcon i, File f) {
                icon = i;
                file = f;
            }

            public void run() {
                try {
                    // Load and scale the image down, then replace the icon, or icon's old image, with the new one.
                    if (file.exists() && file.isFile()) {
                        String suffix = FileSystemUtils.getFileExtension(file);
                        if (isBlank(suffix)) {
                            return;
                        } else {
                            Image img;
                            if ("bmp".equalsIgnoreCase(suffix)) {
                                //Has to use BufferedImage for BMP images
                                img = ImageIO.read(file);
                                icon = new ImageIcon(img.getScaledInstance(ICON_SIZE, ICON_SIZE, SCALE_SMOOTH));
                                imageMap.put(file, icon);
                            } else if ("gif".equalsIgnoreCase(suffix)) {
                                // TODO - gif animation
                                img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
                                icon = new ImageIcon(img.getScaledInstance(ICON_SIZE, ICON_SIZE, SCALE_DEFAULT));
                                imageMap.put(file, icon);
                            } else {
                                img = Toolkit.getDefaultToolkit().createImage(file.getAbsolutePath());
                                icon.setImage(img.getScaledInstance(ICON_SIZE, ICON_SIZE, SCALE_SMOOTH));
                            }
                        }
                    }
                    // Repaint the dialog so we see the new icon.
                    SwingUtilities.invokeLater(() -> repaint());
                } catch (Exception ex) {
                    logger.error("Failed to render image icon of [{}] in file chooser", file, ex);
                }
            }
        }
    }
}
