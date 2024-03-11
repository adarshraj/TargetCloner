package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.constants.SeparatorConstants;
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

import static in.adarshr.targetcloner.constants.SeparatorConstants.EMPTY_STRING;
import static in.adarshr.targetcloner.constants.SeparatorConstants.LINE_BREAK;

/**
 * This class provides utility methods for downloading JARs and extracting XMLs from them.
 */
public class ConnectionHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ConnectionHelper.class);

    /**
     * Private constructor to prevent instantiation
     */
    private ConnectionHelper() {
    }

    /**
     * Downloads the JAR from the given URL and returns the content of the XML file inside the JAR.
     *
     * @param jarUrl the URL of the JAR
     * @return the content of the XML file inside the JAR
     * @throws IOException if an I/O error occurs
     */
    private static String downloadJar(String jarUrl) throws IOException {
        URI uri = URI.create(jarUrl);
        URL url = uri.toURL();
        StringBuilder xmlContent = new StringBuilder();
        try (
                InputStream in = url.openStream();
                JarInputStream jarStream = new JarInputStream(in);
                BufferedReader reader = new BufferedReader(new InputStreamReader(jarStream))
        ) {
            JarEntry entry;
            while ((entry = jarStream.getNextJarEntry()) != null) {
                if (entry.getName().endsWith(SeparatorConstants.XML_FILE_EXTENSION)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        xmlContent.append(line).append(LINE_BREAK);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(">>> Failed to open or read JAR from URL: {}, {}", jarUrl, e.getMessage());
        }
        return xmlContent.toString();
    }

    /**
     * Downloads the JAR from the URL of each distinct repository and returns a map of the repository and the
     * content of the XML file inside the JAR.
     *
     * @param distinctRepoData the set of distinct repositories
     * @return a map of the repository and the content of the XML file inside the JAR
     */
    public static Map<RepoData, String> downloadAllJars(Set<RepoData> distinctRepoData) {
        return distinctRepoData.parallelStream()
                .collect(Collectors.toMap(
                        repo -> repo,
                        repo -> {
                            try {
                                LOG.info(">>> Downloading JAR from URL: {}", repo.getLocation());
                                return downloadJar(repo.getLocation());
                            } catch (IOException e) {
                                LOG.error(">>> Failed to download JAR from URL: {}", repo.getLocation());
                                return EMPTY_STRING;
                            }
                        },
                        (value1, value2) -> value1 // Merge function for handling key collision
                ));
    }
}