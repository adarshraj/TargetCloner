package in.adarshr.targetcloner.utils;

import in.adarshr.targetcloner.constants.TargetClonerConstants;
import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.data.TargetDetails;
import in.adarshr.targetcloner.helper.JaxbHelper;
import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * This class contains the utility methods for the application which are used to generate the target file.
 */
public class TargetClonerUtil {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TargetClonerUtil.class);

    /**
     * Unmarshal the given XML file to an object.
     *
     * @param input          the XML file
     * @param schemaLocation the schema location
     * @return the unmarshalled object
     */
    public static TargetDetails getTargetDetails(String input, String schemaLocation) {
        TargetDetails targetDetails = null;
        try {
            File xmlFile = new File(input);
            File inputSchemaFile = new File(schemaLocation);
            targetDetails = JaxbHelper.unmarshallAndValidate(xmlFile, inputSchemaFile, TargetDetails.class);
        } catch (JAXBException | SAXException e) {
            LOG.error(">>> Failed to unmarshal input XML file: {}", e.getMessage());
        }
        return targetDetails;
    }

    /**
     * Get the target files to copy.
     *
     * @param location the location
     * @return the target files
     */
    public static List<File> getTargetFilesToCopy(String location) {
        List<File> targetFiles;
        try (Stream<Path> paths = Files.walk(Paths.get(location))) {
            targetFiles = paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(TargetClonerConstants.TARGET_FILE_SUFFIX))
                    .map(Path::toFile).toList();
        } catch (IOException e) {
            LOG.error(">>> Failed to get target files to copy: {}", e.getMessage());
            return Collections.emptyList();
        }
        return targetFiles;
    }

    /**
     * Unmarshal the given XML file to an object without namespace.
     *
     * @param targetFiles the XML files
     * @return the unmarshalled object
     */
    public static List<Target> unmarshalTargetFiles(List<File> targetFiles) {
        return targetFiles.stream()
                .map(TargetClonerUtil::apply)
                .toList();
    }

    /**
     * Unmarshal the given XML file to an object without namespace.
     *
     * @param file the XML file
     * @return the unmarshalled object
     */
    private static Target apply(File file) {
        return JaxbHelper.unmarshall(file, Target.class);
    }

    /**
     * Delivery report key
     *
     * @param group    Group
     * @param artifact Artifact
     * @param version  Version
     * @return String
     */
    public static String deliveryReportKey(String group, String artifact, String version) {
        return group + artifact + version;
    }

    /**
     * Print the banner. won't be logged
     */
    public static void printBanner() {
        String fileName = "banner.txt";
        try (InputStream inputStream = TargetClonerUtil.class.getClassLoader().getResourceAsStream(fileName)) {
            assert inputStream != null;
            try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
                reader.lines().forEach(System.out::println);
            }
        } catch (Exception e) {
            LOG.error(">>> Failed to print banner: {}", e.getMessage());
        }
    }

    /**
     * Check if the location is URL
     *
     * @param location Location
     * @return boolean
     */
    public static boolean isUrl(String location) {
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            LOG.error(">>> Invalid delivery report url: {}", e.getMessage());
            return false;
        }
    }
}
