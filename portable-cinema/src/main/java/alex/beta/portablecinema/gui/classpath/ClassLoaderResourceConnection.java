package alex.beta.portablecinema.gui.classpath;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Resources;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ClassLoaderResourceConnection extends URLConnection {

    private LoadingCache<String, byte[]> cache;

    public ClassLoaderResourceConnection(URL u) {
        super(u);
        cache = CacheBuilder.newBuilder()
                .recordStats()
                .maximumSize(100)
                .refreshAfterWrite(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, byte[]>() {
                    @Override
                    public byte[] load(String resourcePath) throws Exception {
                        return Resources.toByteArray(Resources.getResource(resourcePath));
                    }
                });
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
