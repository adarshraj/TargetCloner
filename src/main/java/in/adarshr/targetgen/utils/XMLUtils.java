package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.bo.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import output.targetgen.adarshr.in.output.Target;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XMLUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);

    public static List<Unit> parseXml(InputStream xmlStream) {
        if (xmlStream == null || xmlStream.equals(new ByteArrayInputStream(new byte[0]))) {
            LOG.error("XML stream is null or empty");
            return new ArrayList<>();
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        List<Unit> unitList = new ArrayList<>();

        try {

            db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlStream);

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Here we choose to get NodeList of all "unit" elements directly
            NodeList nodeList = doc.getElementsByTagName("unit");

            //Iterate through all "unit" elements
            int nodeListLength = nodeList.getLength();
            while (nodeListLength-- > 0) {
                Element element = (Element) nodeList.item(nodeListLength);

                // Getting attributes of 'unit' element
                String id = element.getAttribute("id");
                String version = element.getAttribute("version");
                String singleton = element.getAttribute("singleton");
                String generation = element.getAttribute("generation");

                Unit unit = new Unit();
                unit.setId(id);
                unit.setVersion(version);
                unit.setSingleton(singleton);
                unit.setGeneration(generation);

                LOG.info("Unit ID : {}", id);
                LOG.info("Version : {}", version);
                LOG.info("Singleton: {}", singleton);
                LOG.info("Generation : {}", generation);

                unitList.add(unit);
                LOG.info("");
            }
        } catch (Exception e) {
            LOG.error("Failed to parse XML: {}", e.getMessage());
        }
        return unitList;
    }

    public static Map<Repo, List<Unit>> parseAllXml(Map<Repo, InputStream> repoInputStreamMap) {
        return repoInputStreamMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .parallel() // Enable parallel processing
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> parseXml(entry.getValue())));
    }

    public static String createXmlFile(Target target) {
        String xml = null;
        try {
            xml = JaxbUtils.marshal(target, Target.class);
            LOG.info("XML: {}", xml);
        } catch (Exception e) {
            LOG.error("Failed to create XML file: {}", e.getMessage());
        }
        return xml;
    }

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
            }
        } catch (IOException e) {
            LOG.error("Failed to create directory: {}", e.getMessage());
            return false;
        }
        return true;
    }

}
