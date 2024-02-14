package in.adarshr.targetgen.utils;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.stream.StreamSource;
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
     * Marshals the given object to XML with  instruction.
     *
     * @param object the object to marshal
     * @param clazz  the class of the object
     * @param <T>    the type of the object
     * @return the XML string
     * @throws JAXBException if an error occurs during marshalling
     */
    @SuppressWarnings("unused")
    public static <T> String marshalWithPde(T object, Class<T> clazz) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        StringWriter sw = new StringWriter();
        XMLOutputFactory xof = XMLOutputFactory.newFactory();
        XMLStreamWriter streamWriter = xof.createXMLStreamWriter(sw);

        streamWriter.writeStartDocument();
        streamWriter.writeProcessingInstruction("pde", "version=\"3.8\"");
        jaxbMarshaller.marshal(object, streamWriter);
        streamWriter.writeEndDocument();

        streamWriter.close();

        return sw.toString();
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
        int insertIndex = xml.indexOf("?>") + 2;
        String xmlString = xml.substring(0, insertIndex) + "\n" + "<?pde version=\"3.8\"?>" + xml.substring(insertIndex);
        xmlString = xmlString.replaceAll("ns2:", "");
        xmlString = xmlString.replaceAll("xmlns:ns2=\"http://in.adarshr.targetgen.output/output.xsd\"", "");
        return xmlString;
    }

    /**
     * Unmarshal the given XML string to an object.
     *
     * @param xml   the XML string
     * @param clazz the class of the object
     * @param <T>   the type of the object
     * @return the unmarshalled object
     * @throws JAXBException if an error occurs during unmarshalling
     */
    @SuppressWarnings("unused")
    public static <T> T unmarshal(String xml, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        StringReader reader = new StringReader(xml);
        return clazz.cast(jaxbUnmarshaller.unmarshal(reader));
    }

    /**
     * Unmarshal the given XML file to an object.
     *
     * @param xmlFile the XML file
     * @param clazz   the class of the object
     * @param <T>     the type of the object
     * @return the unmarshalled object
     * @throws JAXBException if an error occurs during unmarshalling
     */
    @SuppressWarnings("unused")
    public static <T> T unmarshal(File xmlFile, Class<T> clazz) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        return clazz.cast(jaxbUnmarshaller.unmarshal(xmlFile));
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
}
