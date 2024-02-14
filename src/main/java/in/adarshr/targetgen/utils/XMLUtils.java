package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.bo.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import output.targetgen.adarshr.in.output.Target;

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
public class XMLUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);

    /**
     * Parse content.xml to get the list of units
     *
     * @param xml XML stream
     * @return List of unit
     */
    public static List<Unit> parseXml(String xml) {
        if (xml == null || xml.isBlank()) {
            LOG.error("XML is null or empty");
            return new ArrayList<>();
        }
        List<Unit> unitList = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            doc.getDocumentElement().normalize();

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Here we choose to get NodeList of all "unit" elements directly
            NodeList nodeList = doc.getElementsByTagName("unit");

            //Iterate through all "unit" elements
            int nodeListLength = nodeList.getLength();
            while (nodeListLength-- > 0) {
                Element element = (Element) nodeList.item(nodeListLength);
                // Getting attributes of 'unit' element
                Unit unit = getUnit(element);
                //LOG.info("Unit: {}", unit);
                unitList.add(unit);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.error("Failed to parse XML: {}. XML Content: {}", e.getMessage(), xml);
        }
        return unitList;
    }

    /**
     * Get unit from element
     *
     * @param element Element
     * @return Unit
     */
    private static Unit getUnit(Element element) {
        String id = element.getAttribute("id");
        String version = element.getAttribute("version");
        String singleton = element.getAttribute("singleton");
        String generation = element.getAttribute("generation");

        Unit unit = new Unit();
        unit.setId(id);
        unit.setVersion(version);
        unit.setSingleton(singleton);
        unit.setGeneration(generation);
        return unit;
    }

    public static Map<Repo, List<Unit>> parseAllXml(Map<Repo, String> xmlInputStreamMap) {
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
            xml = JaxbUtils.marshalWithInstruction(target, Target.class);
            //LOG.info("Output XML: \n{}", xml);
        } catch (Exception e) {
            LOG.error("Failed to create XML file: {}", e.getMessage());
        }
        return xml;
    }

    /**
     * Create XML files
     *
     * @param stringTargetMap Map of target name and target
     */
    public static void createXmlFiles(Map<String, Target> stringTargetMap) {
        for (Map.Entry<String, Target> entry : stringTargetMap.entrySet()) {
            String xml = createXmlFile(entry.getValue());
            boolean is = saveToFile(xml, entry.getKey());
            if (is) {
                LOG.info("XML file created successfully");
            } else {
                LOG.error("Failed to create XML file");
            }
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
        String currentWorkingDir = System.getProperty("user.dir");

        String fileSep = FileSystems.getDefault().getSeparator();

        // Define the path to the file
        String fileLocation = currentWorkingDir + fileSep + "output" + fileSep + fileName;

        // Make sure to create the directory if it doesn't exit
        Path currentOutputPath = Paths.get(currentWorkingDir + fileSep + "output" + fileSep);
        try {
            if (!Files.exists(currentOutputPath)) {
                Files.createDirectories(currentOutputPath);
            } else {
                // Create the file and write the content
                Files.write(Paths.get(fileLocation), content.getBytes());
                LOG.info("File created successfully {}", fileLocation);
            }
        } catch (IOException e) {
            LOG.error("Failed to create directory: {}", e.getMessage());
            return false;
        }
        return true;
    }

}
