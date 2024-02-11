package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Repo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Collectors;

/**
 * This class provides utility methods for downloading JARs and extracting XMLs from them.
 */
public class ConnectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionUtil.class);

    /**
     * Downloads the JAR from the given URL and returns the InputStream of the XML file inside the JAR.
     *
     * @param jarUrl the URL of the JAR
     * @return the InputStream of the XML file inside the JAR
     * @throws IOException if an I/O error occurs
     */
    private static String downloadJar(String jarUrl) throws IOException {
        URI uri = URI.create(jarUrl);
        URL url = uri.toURL();
        StringBuilder xmlContent = new StringBuilder();

        try (InputStream in = url.openStream();
             JarInputStream jarStream = new JarInputStream(in);
             BufferedReader reader = new BufferedReader(new InputStreamReader(jarStream))) {

            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        xmlContent.append(line).append("\n");
                    }
                    break;
                }
            }
        }
        return xmlContent.toString(); // return XML content
    }

    /**
     * Downloads the JAR from the given URL for each distinct repository and returns a map of the repository and the
     * InputStream of the XML file inside the JAR.
     *
     * @param distinctRepos the set of distinct repositories
     * @return a map of the repository and the InputStream of the XML file inside the JAR
     */
    public static Map<Repo, String> downloadSpecificXMLFromJar(Set<Repo> distinctRepos) {
        return distinctRepos.stream()
                .parallel() // Enable parallel processing
                .collect(Collectors.toMap(repo -> repo, repo -> {
                    try {
                        LOG.info("Downloading JAR from URL: {}", repo.getLocation());
                        return downloadJar(repo.getLocation());
                    } catch (IOException e) {
                        LOG.error("Failed to download JAR from URL: {}", repo.getLocation());
                        return "";
                    }
                }));
    }
}
