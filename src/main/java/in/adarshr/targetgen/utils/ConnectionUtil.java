package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public class ConnectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtil.class);

    private static InputStream downloadJar(String jarUrl) throws IOException {
        URI uri = URI.create(jarUrl);
        URL url = uri.toURL();
        try (InputStream in = url.openStream()) { // Use URL directly for conciseness
            try (JarInputStream jarStream = new JarInputStream(in)) {
                JarEntry entry;
                while ((entry = jarStream.getNextJarEntry()) != null) {
                    if (entry.getName().endsWith(".xml")) {
                        return jarStream; // Return immediately upon finding XML
                    }
                }
            }
        }
         // return an empty stream if no XML found
        return new ByteArrayInputStream(new byte[0]); // empty InputStream
    }

    public static Map<Repo, InputStream> downloadSpecificXMLFromJar(Set<Repo> distinctRepos) {
        return distinctRepos.stream()
                .parallel() // Enable parallel processing
                .collect(Collectors.toMap(repo -> repo, repo -> {
                    try {
                        return downloadJar(repo.getLocation());
                    } catch (IOException e) {
                        LOG.error("Failed to download JAR from URL: {}", repo.getLocation());
                        return new ByteArrayInputStream(new byte[0]); // empty InputStream;
                    }
                }));
    }
}
