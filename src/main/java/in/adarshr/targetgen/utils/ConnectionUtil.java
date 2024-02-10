package in.adarshr.targetgen.utils;

import input.targetgen.adarshr.in.input.Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

public class ConnectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtil.class);

    public static InputStream downloadJar(String jarUrl) throws IOException {
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
        return null; // No XML found
    }

    public static List<InputStream> downloadJars(List<String> jarUrls) throws IOException {
        return jarUrls.stream()
                .parallel() // Enable parallel processing
                .map(jarUrl -> {
                    try {
                        // Use downloadSpecificXMLFromJar for optimized download
                        return downloadJar(jarUrl);
                    } catch (IOException e) {
                        LOG.error("Failed to download JAR from URL: {}", jarUrl);
                        return null;
                    }
                })
                .filter(Objects::nonNull) // Filter out any failed downloads
                .collect(Collectors.toList());
    }

    //create a List<Inputstream> from TargetUtils.getDistinctRepoMap() method result
    public static Map<Repo, InputStream> downloadSpecificXMLFromJar(Map<Repo, String> distinctRepoMap) {
        return distinctRepoMap.entrySet().stream()
                .parallel() // Enable parallel processing
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                    try {
                        return downloadJar(entry.getValue());
                    } catch (IOException e) {
                        LOG.error("Failed to download JAR from URL: {}", entry.getValue());
                        return null;
                    }
                }));
    }


    public static String readFile(String fileUrl) throws IOException {
        URI uri = URI.create(fileUrl);
        URL url = uri.toURL();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }
}
