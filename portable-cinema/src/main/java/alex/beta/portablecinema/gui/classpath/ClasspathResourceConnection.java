package alex.beta.portablecinema.gui.classpath;

import alex.beta.portablecinema.ImageCache;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class ClasspathResourceConnection extends URLConnection {

    public ClasspathResourceConnection(URL u) {
        super(u);
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public InputStream getInputStream() {
        byte[] data = ImageCache.getCache().getImageData(url.toString().replaceFirst("^.*classpath:", ""));
        if (data == null) data = new byte[0];
        return new ByteArrayInputStream(data);
    }
}
