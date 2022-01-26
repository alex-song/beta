package alex.beta.portablecinema.gui.classpath;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClasspathResourceConnection extends URLConnection {

    private static final Logger logger = LoggerFactory.getLogger(ClasspathResourceConnection.class);

    private LoadingCache<String, byte[]> cache;

    public ClasspathResourceConnection(URL u) {
        super(u);
        cache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(100)
                .refreshAfterWrite(15, TimeUnit.MINUTES)
                .build(CacheLoader.from(this::resourceToByteArray));
    }

    private byte[] resourceToByteArray(@NonNull String resourcePath) {
        try {
            return Resources.toByteArray(Resources.getResource(resourcePath));
        } catch (IOException ex) {
            logger.warn("Failed to load resource [{}]", resourcePath, ex);
            return new byte[0];
        }
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            return new ByteArrayInputStream(cache.get(url.toString().replaceFirst("^.*classpath:", "")));
        } catch (ExecutionException ex) {
            throw new IOException(ex);
        }
    }
}
