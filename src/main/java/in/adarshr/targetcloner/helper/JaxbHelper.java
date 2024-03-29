package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.constants.SeparatorConstants;
import in.adarshr.targetcloner.constants.XmlConstants;
import in.adarshr.targetcloner.filter.NameSpaceFilter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;

import static in.adarshr.targetcloner.constants.TargetClonerConstants.JAXB_PACKAGE;
import static in.adarshr.targetcloner.constants.TargetClonerConstants.JAXB_SCHEMA;

/**
 * This class provides utility methods for JAXB operations.
 */
public class JaxbHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(JaxbHelper.class);

    /**
     * Private constructor to prevent instantiation
     */
    private JaxbHelper() {
    }

    /**
     * Marshals the given object to XML.
     *
     * @param object the object to marshal
     * @param clazz  the class of the object
     * @param <T>    the type of the object
     * @return the XML string
     * @throws JAXBException if an error occurs during marshalling
     */
    public static <T> String marshal(T object, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.marshal(object, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Marshals the given object to XML with the given instruction.
     *
     * @param object the object to marshal
     * @param clazz  the class of the object
     * @param <T>    the type of the object
     * @return the XML string
     * @throws JAXBException if an error occurs during marshalling
     */
    public static <T> String marshalWithInstruction(T object, Class<T> clazz) throws JAXBException {
        String xml = marshal(object, clazz);
        int insertIndex = xml.indexOf(XmlConstants.XML_INSTRUCTION_MARKER) + 2;
        String xmlString = xml.substring(0, insertIndex) + SeparatorConstants.LINE_BREAK + XmlConstants.PDE_VERSION_INSTRUCTION + xml.substring(insertIndex);
        xmlString = xmlString.replaceAll(XmlConstants.XML_NAMESPACE_PREFIX, SeparatorConstants.EMPTY_STRING);
        xmlString = xmlString.replaceAll(XmlConstants.XML_NAMESPACE_TARGET_CLONER, SeparatorConstants.EMPTY_STRING);
        return xmlString;
    }

    /**
     * Unmarshal the given XML file to an object.
     *
     * @param xmlFile the XML file
     * @param clazz   the class of the object
     * @param <T>     the type of the object
     * @return the unmarshalled object
     */
    public static <T> T unmarshall(File xmlFile, Class<T> clazz) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return clazz.cast(jaxbUnmarshaller.unmarshal(xmlFile));
        } catch (JAXBException e) {
            LOG.error(">>> Failed to unmarshal input XML file: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Unmarshal and validates the given XML file against the given XSD file to an object.
     *
     * @param xmlFile the XML file
     * @param xsdFile the XSD file
     * @param clazz   the class of the object
     * @param <T>     the type of the object
     * @return the unmarshalled and validated object
     * @throws JAXBException if an error occurs during unmarshalling
     * @throws SAXException  if an error occurs during validation
     */
    public static <T> T unmarshallAndValidate(File xmlFile, File xsdFile, Class<T> clazz)
            throws JAXBException, SAXException {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = sf.newSchema(xsdFile);
        JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        unmarshaller.setSchema(schema);
        return unmarshaller.unmarshal(new StreamSource(xmlFile), clazz).getValue();
    }

    /**
     * Unmarshal the given XML file to an object without namespace.
     *
     * @param xmlFile the XML file
     * @param <T>     the type of the object
     * @return the unmarshalled object
     */
    @SuppressWarnings({"unchecked", "unused"})
    public static <T> T unmarshallWithoutNamespace(File xmlFile, Class<T> clazz) {
        try {
            //Prepare JAXB objects
            JAXBContext jc = JAXBContext.newInstance(JAXB_PACKAGE);
            Unmarshaller u = jc.createUnmarshaller();
            //Create an XMLReader to use with our filter
            XMLReader reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
            //Create the filter (to add namespace) and set the xmlReader as its parent.
            NameSpaceFilter inFilter = new NameSpaceFilter(JAXB_SCHEMA, true);
            inFilter.setParent(reader);
            //Prepare the input, in this case a java.io.File (output)
            InputSource is = new InputSource(new FileInputStream(xmlFile));
            //Create a SAXSource specifying the filter
            SAXSource source = new SAXSource(inFilter, is);
            //Do unmarshalling
            return (T) u.unmarshal(source);
        } catch (JAXBException | IOException | SAXException | ParserConfigurationException e) {
            LOG.error(">>> Using alternate method since in.adarshr.targetcloner.helper.JaxbHelper.unmarshallWithoutNamespace failed to unmarshal input XML file: {}", e.getMessage());
            return unmarshall(xmlFile, clazz);
        }
    }
}
