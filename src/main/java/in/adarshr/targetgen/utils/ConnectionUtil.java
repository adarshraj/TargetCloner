package in.adarshr.targetgen.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class ConnectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtil.class);

    public static InputStream downloadJar(String jarUrl) throws IOException {
        URI uri = URI.create(jarUrl);
        URL url = uri.toURL();
        HttpURLConnection http = (HttpURLConnection)url.openConnection();

        // Open the InputStream directly from the URL Connection
        try (InputStream in = http.getInputStream();
             JarInputStream jarStream = new JarInputStream(in)) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    return jarStream;
                }
                jarStream.closeEntry();
            }
        }
        return null;
    }
    public static List<InputStream> downloadJars(List<String> jarUrls) throws IOException {
        List<InputStream> jarStreams = new ArrayList<>();
        for (String jarUrl : jarUrls) {
            jarStreams.add(downloadJar(jarUrl));
        }
        return jarStreams;
    }
}
