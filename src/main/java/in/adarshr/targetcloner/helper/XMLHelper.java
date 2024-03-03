package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.constants.TargetClonerConstants;
import in.adarshr.targetcloner.constants.XmlConstants;
import in.adarshr.targetcloner.data.Target;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class contains the methods to parse the jar XML and other XML related operations
 */
public class XMLHelper {
    private static final Logger LOG = LoggerFactory.getLogger(XMLHelper.class);

    /**
     * Parse content.xml to get the list of units
     *
     * @param xml XML stream
     * @return List of unit
     */
    public static List<RepoUnit> parseXml(String xml) {
        if (xml == null || xml.isBlank()) {
            LOG.error("XML is null or empty");
            return new ArrayList<>();
        }
        List<RepoUnit> repoUnitList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();
            // Normalize the XML structure
            doc.getDocumentElement().normalize();
            // Here we choose to get NodeList of all "unit" elements directly
            NodeList nodeList = doc.getElementsByTagName(XmlConstants.XML_ELEMENT_UNIT);
            //Iterate through all "unit" elements
            int nodeListLength = nodeList.getLength();
            while (nodeListLength-- > 0) {
                Element element = (Element) nodeList.item(nodeListLength);
                // Getting attributes of 'repoUnit' element
                RepoUnit repoUnit = getUnit(element);
                //LOG.info("RepoUnit: {}", repoUnit);
                repoUnitList.add(repoUnit);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error(">>> Failed to parse XML: {}. XML Content: {}", e.getMessage(), xml);
        }
        return repoUnitList;
    }

    /**
     * Get unit from element
     *
     * @param element Element
     * @return RepoUnit
     */
    private static RepoUnit getUnit(Element element) {
        String id = element.getAttribute(XmlConstants.XML_ATTRIBUTE_ID);
        String version = element.getAttribute(XmlConstants.XML_ATTRIBUTE_VERSION);
        String singleton = element.getAttribute(XmlConstants.XML_ATTRIBUTE_SINGLETON);
        String generation = element.getAttribute(XmlConstants.XML_ATTRIBUTE_GENERATION);

        RepoUnit repoUnit = new RepoUnit();
        repoUnit.setId(id);
        repoUnit.setVersion(version);
        repoUnit.setSingleton(singleton);
        repoUnit.setGeneration(generation);

        return repoUnit;
    }

    public static Map<RepoData, List<RepoUnit>> parseAllXml(Map<RepoData, String> xmlInputStreamMap) {
        return xmlInputStreamMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .parallel()  // the thread-safety of parseXml needs to be guaranteed
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> parseXml(entry.getValue())));
    }

    /**
     * Create XML file
     *
     * @param target Target
     * @return String
     */
    public static String createXmlFile(Target target) {
        String xml = null;
        try {
            xml = JaxbHelper.marshalWithInstruction(target, Target.class);
        } catch (Exception e) {
            LOG.error(">>> Failed to create XML file: {}", e.getMessage());
        }
        return xml;
    }

    /**
     * Create XML files
     *
     * @param stringTargetMap Map of target name and target
     */
    public static void saveFilesToDisk(Map<String, Target> stringTargetMap) {
        Map<String, Boolean> fileSaveStatus = stringTargetMap.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> saveToFile(createXmlFile(entry.getValue()), entry.getKey())));
        if (LOG.isInfoEnabled()) {
            fileSaveStatus.forEach((key, value) -> {
                if (value) {
                    LOG.info(">>> File created successfully: {}", key);
                } else {
                    LOG.error(">>> Failed to create file: {}", key);
                }
            });
        }
    }

    /**
     * Save the content to a file
     *
     * @param content  Content
     * @param fileName File name
     * @return boolean
     */
    private static boolean saveToFile(String content, String fileName) {
        // Get the current working directory
        String currentWorkingDir = System.getProperty(TargetClonerConstants.USER_DIRECTORY);
        String fileSep = FileSystems.getDefault().getSeparator();
        // Define the path to the file
        String fileLocation = currentWorkingDir + fileSep + TargetClonerConstants.OUTPUT_DIRECTORY + fileSep + fileName;
        // Make sure to create the directory if it doesn't exit
        Path currentOutputPath = Paths.get(currentWorkingDir + fileSep + TargetClonerConstants.OUTPUT_DIRECTORY + fileSep);
        try {
            if (!Files.exists(currentOutputPath)) {
                Files.createDirectories(currentOutputPath);
            } else {
                Files.write(Paths.get(fileLocation), content.getBytes());
                LOG.info(">>> File created successfully: {}", fileLocation);
            }
        } catch (IOException e) {
            LOG.error(">>> Failed to create directory: {}", e.getMessage());
            return false;
        }
        return true;
    }
}
