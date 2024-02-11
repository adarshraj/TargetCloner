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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class XMLUtils {
    private static final Logger LOG = LoggerFactory.getLogger(XMLUtils.class);
    public static List<Unit> parseXml(InputStream xmlStream) {
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
            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

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
                return unitList;
            }
        } catch (Exception e) {
            LOG.error("Failed to parse XML: {}", e.getMessage());
        }
        return unitList;
    }

    public static Map<Repo, List<Unit>> parseAllXml(Map<Repo, InputStream> repoInputStreamMap) {
        return repoInputStreamMap.entrySet().stream()
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

    //Save XML file to disk
    public static void saveXmlFile(String xml, String fileName) {
        File file = new File(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(xml);
            LOG.info("XML file {} saved to disk", fileName);
        } catch (IOException e) {
            LOG.error("Failed to write to file: {}", e.getMessage());
        }
    }

    public static void createXmlFiles(List<Target> targets) {
        for (Target target : targets) {
            String xml = createXmlFile(target);
            saveXmlFile(xml, target.getName() + ".xml");
        }
    }
}
