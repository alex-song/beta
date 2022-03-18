package alex.beta.portablecinema;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ImageCache {
    private static final Logger logger = LoggerFactory.getLogger(ImageCache.class);

    private static ImageCache inventory;

    private LoadingCache<String, byte[]> cache;

    private ImageCache() {
        cache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(100)
                .refreshAfterWrite(30, TimeUnit.MINUTES)
                .build(CacheLoader.from(this::resourceToByteArray));
    }

    public static synchronized ImageCache getCache() {
        if (inventory == null) {
            inventory = new ImageCache();
        }
        return inventory;
    }

    private byte[] resourceToByteArray(@NonNull String resourcePath) {
        try {
            return Resources.toByteArray(Resources.getResource(resourcePath));
        } catch (IOException ex) {
            logger.error("Failed to load resource [{}]", resourcePath, ex);
            return new byte[0];
        }
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
        Toolkit tk = Toolkit.getDefaultToolkit();
        if (tk != null) {
            byte[] data = getImageData(name);
            if (data != null && data.length > 0) {
                return tk.createImage(data);
            }
        }
        return null;
    }

    public Image getImage(@NonNull String name, int width, int height, int scaleHints) {
        Image image = getImage(name);
        if (image != null) {
            return image.getScaledInstance(width, height, scaleHints);
        }
        return null;
    }
}
