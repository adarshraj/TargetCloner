package in.adarshr.targetgen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConnectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtil.class);
    public static void downloadJar(String jarUrl) throws IOException {
        URI uri = URI.create(jarUrl);
        URL url = uri.toURL();
        HttpURLConnection http = (HttpURLConnection)url.openConnection();
        String urlPath = url.getPath();
        String jarId = urlPath.substring(urlPath.lastIndexOf('/') + 1);
        Path jarPath = Paths.get(jarId + "_content.jar");

        // Download the JAR file.
        try (InputStream in = http.getInputStream()) {
            Files.copy(in, jarPath, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Downloaded JAR file: {}", jarPath);
        }

        // Extract the XML file from the JAR.
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            JarEntry entry = jarFile.getJarEntry("content.xml");
            if (entry != null) {
                InputStream stream = jarFile.getInputStream(entry);
                Files.copy(stream, Paths.get(jarId + "_content.xml"), StandardCopyOption.REPLACE_EXISTING);
                LOG.info("Extracted XML file from JAR: {}", jarId + "_content.xml");
            }
        }
    }

    public static void downloadJar(List<String> jarUrls) throws IOException {
        // Download the JAR files.
        for (String jarUrl : jarUrls) {
            downloadJar(jarUrl);
        }
    }
}
