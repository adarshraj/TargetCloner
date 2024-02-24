package in.adarshr.targetcloner.utils;


import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.data.TargetDetails;
import in.adarshr.targetcloner.helper.JaxbHelper;
import jakarta.xml.bind.JAXBException;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
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
            LOG.info("JAXBUtils: {}", targetDetails);
        } catch (JAXBException | SAXException e) {
            LOG.error("Failed to unmarshal input XML file: {}", e.getMessage());
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
                    .filter(p -> p.toString().endsWith(".target"))
                    .map(Path::toFile).toList();
        } catch (IOException e) {
            LOG.error("Failed to get target files to copy: {}", e.getMessage());
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
        return JaxbHelper.unmarshallWithoutNamespace(file, Target.class);
    }
}
