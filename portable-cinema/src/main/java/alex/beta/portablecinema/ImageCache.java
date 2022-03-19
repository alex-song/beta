package alex.beta.portablecinema;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;
import lombok.NonNull;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.awt.Image.*;

public class ImageCache {
    private static final Logger logger = LoggerFactory.getLogger(ImageCache.class);

    private static ImageCache inventory;

    private final LoadingCache<String, byte[]> cache;

    private final LoadingCache<ImageInfo, Image> imgCache;

    private ImageCache() {
        cache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(100)
                .refreshAfterWrite(30, TimeUnit.MINUTES)
                .build(CacheLoader.from(this::resourceToByteArray));

        imgCache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(100)
                .refreshAfterWrite(20, TimeUnit.MINUTES)
                .build(CacheLoader.from(this::byteArrayToImage));
    }

    public static synchronized ImageCache getCache() {
        if (inventory == null) {
            inventory = new ImageCache();
        }
        return inventory;
    }

    public byte[] getImageData(@NonNull String name) {
        try {
            return cache.get(name);
        } catch (Exception ex) {
            logger.error("Failed to get image data [{}]", name, ex);
            return new byte[0];
        }
    }

    public Image getImage(@NonNull String name) {
        return getImage(name, 0, 0, 0);
    }

    public Image getImage(@NonNull String name, int width, int height, int scaleHints) {
        try {
            return imgCache.get(new ImageInfo(name, width, height, scaleHints));
        } catch (ExecutionException ex) {
            logger.error("Failed to get image [{}]", name, ex);
            return null;
        }
    }

    private byte[] resourceToByteArray(@NonNull String resourcePath) {
        try {
            return Resources.toByteArray(Resources.getResource(resourcePath));
        } catch (IOException ex) {
            logger.error("Failed to load resource [{}]", resourcePath, ex);
            return new byte[0];
        }
    }

    private Image byteArrayToImage(@NonNull ImageInfo imageInfo) {
        Image originalImage = byteArrayToImage(imageInfo.name);
        if (originalImage != null) {
            if (imageInfo.width > 0 && imageInfo.height > 0) {
                int hints = imageInfo.scaleHints;
                if (hints != SCALE_SMOOTH
                        && hints != SCALE_DEFAULT
                        && hints != SCALE_FAST
                        && hints != SCALE_REPLICATE
                        && hints != SCALE_AREA_AVERAGING) {
                    hints = SCALE_SMOOTH;
                }
                return originalImage.getScaledInstance(imageInfo.width, imageInfo.height, hints);
            } else {
                return originalImage;
            }
        }
        return null;
    }

    private Image byteArrayToImage(@NonNull String name) {
        byte[] data = getImageData(name);
        if (data == null || data.length == 0) {
            return null;
        }
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk != null) {
            return tk.createImage(data);
        } else {
            try (InputStream inputStream = new ByteArrayInputStream(data)) {
                return ImageIO.read(inputStream);
            } catch (IOException ex) {
                logger.error("Failed to create image [{}] from byte array", name, ex);
                return null;
            }
        }
    }

    private static class ImageInfo implements Serializable {
        String name;
        int width;
        int height;
        int scaleHints;

        ImageInfo(String name, int width, int height, int scaleHints) {
            this.name = name;
            this.width = width;
            this.height = height;
            this.scaleHints = scaleHints;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            ImageInfo imageInfo = (ImageInfo) o;

            return new EqualsBuilder().append(width, imageInfo.width).append(height, imageInfo.height).append(scaleHints, imageInfo.scaleHints).append(name, imageInfo.name).isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37).append(name).append(width).append(height).append(scaleHints).toHashCode();
        }
    }
}
