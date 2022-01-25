package alex.beta.portablecinema.gui.classpath;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * Fixed name convention!
 * Package name is the protocol name
 * File name must be Handler
 */
public class Handler extends URLStreamHandler {

    public static void install() {
        String pkgName = Handler.class.getPackage().getName();
        String pkg = pkgName.substring(0, pkgName.lastIndexOf('.'));

        String protocolHandlers = System.getProperty("java.protocol.handler.pkgs", "");
        if (!protocolHandlers.contains(pkg)) {
            if (!protocolHandlers.isEmpty()) {
                protocolHandlers += "|";
            }
            protocolHandlers += pkg;
            System.setProperty("java.protocol.handler.pkgs", protocolHandlers);
        }
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        return new ClasspathResourceConnection(u);
    }
}
