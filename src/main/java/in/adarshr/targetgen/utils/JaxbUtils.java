package in.adarshr.targetgen.utils;

import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * This class provides utility methods for JAXB operations.
 */
public class JaxbUtils {

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
        StringWriter sw = new StringWriter();
        jaxbMarshaller.marshal(object, sw);
        return sw.toString();
    }

    /**
     * Unmarshals the given XML string to an object.
     *
     * @param xml   the XML string
     * @param clazz the class of the object
     * @param <T>   the type of the object
     * @return the unmarshalled object
     * @throws JAXBException if an error occurs during unmarshalling
     */
    public static <T> T unmarshal(String xml, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        return clazz.cast(jaxbUnmarshaller.unmarshal(reader));
    }

    /**
     * Unmarshals the given XML file to an object.
     *
     * @param xmlFile the XML file
     * @param clazz   the class of the object
     * @param <T>     the type of the object
     * @return the unmarshalled object
     * @throws JAXBException if an error occurs during unmarshalling
     */
    public static <T> T unmarshal(File xmlFile, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return clazz.cast(jaxbUnmarshaller.unmarshal(xmlFile));
    }

    /**
     * Unmarshals and validates the given XML file against the given XSD file to an object.
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
        return (T) unmarshaller.unmarshal(xmlFile);
    }
}
