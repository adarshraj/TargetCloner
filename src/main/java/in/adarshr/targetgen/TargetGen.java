package in.adarshr.targetgen;

import in.adarshr.targetgen.bo.*;
import in.adarshr.targetgen.dto.TargetVO;
import in.adarshr.targetgen.build.TargetBuilder;
import in.adarshr.targetgen.utils.ConnectionUtil;
import in.adarshr.targetgen.utils.JaxbUtils;
import in.adarshr.targetgen.utils.TargetUtils;
import in.adarshr.targetgen.utils.XMLUtils;
import input.targetgen.adarshr.in.input.ComponentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import output.targetgen.adarshr.in.output.Target;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main class of the application.
 */
public class TargetGen {
    private static final Logger LOG = LoggerFactory.getLogger(TargetGen.class);

    public static void main(String[] args) {
        LOG.info("*** Starting TargetGen application. ***");

        // Set global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> LOG.error("Global exception handler caught: {}", exception.getMessage()));

        //Read input xml file
        ComponentInfo componentInfo = null;
        try {
            File xmlFile = new File("input/input.xml");
            File inputSchemaFile = new File("src/main/resources/schema/input.xsd");
            //componentInfo = JaxbUtils.unmarshal(xmlFile, ComponentInfo.class);
            componentInfo = JaxbUtils.unmarshallAndValidate(xmlFile, inputSchemaFile, ComponentInfo.class);
            LOG.info("JAXBUtils: {}", componentInfo);
        } catch (JAXBException | SAXException e) {
            LOG.error("Failed to unmarshal input XML file: {}", e.getMessage());
        }

        //Proceed only when we have the Input XML data
        if(componentInfo != null) {
            //Set the data to TargetVO for target file generation
            TargetVO targetVO = new TargetVO();
            targetVO.setComponentInfo(componentInfo);

            //Get jar urls
            Set<Repo> repoJarUrls = TargetUtils.getJarUrls(componentInfo);
            LOG.info("*** Repo Jar Urls creation completed. ***");

            //Download jar and get input stream
            Map<Repo, String> repoInputStreamMap = ConnectionUtil.downloadSpecificXMLFromJar(repoJarUrls);
            LOG.info("*** Repo Jar Urls download completed. ***");

            // Parse the XML from the jar file
            Map<Repo, List<Unit>> repoListMap = XMLUtils.parseAllXml(repoInputStreamMap);
            Map<Repo, List<Unit>> filteredRepoListMap = TargetUtils.filterRepoUnits(repoListMap);
            targetVO.setRepoUnitMap(filteredRepoListMap);
            LOG.info("*** Repo Jar Urls parsing completed. ***");

            //Set delivery report data. If report location is URL, then create URL and get report data
            //If report location is file, then get report data from file
            if(TargetUtils.isUrl(componentInfo.getReportLocation())){
                String reportLocation = componentInfo.getReportLocation();
                String createRepoUrl = TargetUtils.createDeliveryReportUrl(reportLocation, componentInfo.getVersion());
                List<Report> reportDataFromUrl = TargetUtils.getReportData(createRepoUrl, 1, 1);
                targetVO.setDeliveryReportData(reportDataFromUrl);
                LOG.info("*** Delivery Report Data from URL: {} ***", reportDataFromUrl);
            }else {
                List<Report> reportData = TargetUtils.getReportData(componentInfo.getReportLocation(), 2, 2);
                targetVO.setDeliveryReportData(reportData);
                LOG.info("*** Delivery Report Data from File: {} ***", reportData);
            }

            //Set Repo List. This is used to create location in target file
            targetVO.setRepoMapList(TargetUtils.getRepoMapList(componentInfo));
            LOG.info("*** Repo Map List created completed. ***");

            //Set Component Repos. This is used to create location in target file
            targetVO.setComponentRepoMap(TargetUtils.getComponentRepoMap(componentInfo));
            LOG.info("*** Component Repo Map created completed. ***");

            //Set version
            targetVO.setVersion(componentInfo.getVersion());

            // Create xml file from jaxb
            TargetBuilder targetBuilder = new TargetBuilder();
            Map<String, Target> stringTargetMap = targetBuilder.buildTargets(targetVO);
            LOG.info("*** Target file creation completed. ***");

            XMLUtils.createXmlFiles(stringTargetMap);
            LOG.info("*** Target files are written to disk. ***");
            LOG.info("*** All tasks completed. ***");
        }else {
            LOG.error("ComponentInfo is null");
        }
    }
}
