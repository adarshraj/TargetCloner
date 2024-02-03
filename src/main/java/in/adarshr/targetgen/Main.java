package in.adarshr.targetgen;

import in.adarshr.targetgen.marshal.JaxbUtils;
import input.targetgen.adarshr.in.input.ComponentsInformation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import output.targetgen.adarshr.in.output.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Main {
    List<Unit> units = new ArrayList<>();
    public static void main(String[] args) {
        String repoUrlString = "https://download.eclipse.org/egit/updates-6.7/";
        String jarName = "content.jar";

        try {
            Main main = new Main();
            main.downloadAndParseXmlFromJar(repoUrlString, jarName);
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void downloadAndParseXmlFromJar(String repoUrl, String jarName) throws IOException, ParserConfigurationException, SAXException {
        Path jarPath = Paths.get(jarName);
        URL url = new URL(repoUrl + jarName);

        // Download the jar file
        try (InputStream in = url.openStream()) {
            Files.copy(in, jarPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.out.println("Failed to download jar file: " + e.getMessage());
            return;
        }

        // Open and read the jar file
        try (JarFile jarFile = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".xml")) {
                   InputStream stream = jarFile.getInputStream(entry);
                        parseXml3(stream);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read jar file: " + e.getMessage());
        }

        // Clean up the jar file
        Files.delete(jarPath);

        // Create xml file from jaxb
        Target target = createTarget();
        target.getLocations().getLocation().get(0).getUnit().addAll(units);
        createXmlFile(target);
    }

    private Target createTarget() {
        ObjectFactory ObjectFactory = new ObjectFactory();
        Target target =  ObjectFactory.createTarget();
        target.setName("Test");
        target.setIncludeMode("include");
        target.setSequenceNumber("1.0");
        target.setTargetJRE(createTargetJRE());
        target.setEnvironment(createEnvironment());
        target.setLocations(createLocations());
        return target;
    }

    private Locations createLocations() {
        Locations locations = new Locations();
        locations.getLocation().add(createLocation());
        return locations;
    }

    private Location createLocation() {
        Location location = new Location();
        location.setIncludeMode("include");
        location.setType("dir");
        location.setRepository(createTargetRepository());
        location.setIncludeAllPlatforms("true");
        location.setIncludeConfigurePhase("true");
        location.getUnit().add(createUnit());
        return location;
    }

    private TargetRepository createTargetRepository() {
        TargetRepository targetRepository = new TargetRepository();
        targetRepository.setLocation("https://download.eclipse.org/egit/updates-6.7/");
        return targetRepository;
    }

    private Unit createUnit() {
        Unit unit = new Unit();
        unit.setId("org.eclipse.egit");
        unit.setVersion("5.11.0.202105071451-r");
        return unit;
    }

    private Environment createEnvironment() {
        Environment environment = new Environment();
        environment.setNl("en_US");
        return environment;
    }

    private TargetJRE createTargetJRE() {
        TargetJRE targetJRE = new TargetJRE();
        targetJRE.setPath("C:\\Program Files\\Java\\jdk-11.0.11");
        targetJRE.setValue("11.0.11");
        return targetJRE;
    }

    private void createXmlFile(Target target) {
        try {
            String xml = JaxbUtils.marshal(target, Target.class);
            System.out.println(xml);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

     private void parseXml3(InputStream xmlStream) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;

        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(xmlStream);

            // Normalize the XML structure
            doc.getDocumentElement().normalize();

            // Here we choose to get NodeList of all "unit" elements directly
            NodeList nodeList = doc.getElementsByTagName("unit");


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
                units.add(unit);

                // Printing out the details
                System.out.println("Unit ID : " + id);
                System.out.println("Version : " + version);
                System.out.println("Singleton: " + singleton);
                System.out.println("Generation : " + generation);

                // If there are more details you want,
                // just use element.getAttribute with the attribute name as argument.

                System.out.println("");

                File xmlFile = new File("input.xsd.xml");
                ComponentsInformation componentsInformation
                        = JaxbUtils.unmarshal(xmlFile, ComponentsInformation.class);

                System.out.println(componentsInformation.getVersion());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
