package in.adarshr.targetgen;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.bo.Report;
import in.adarshr.targetgen.bo.Unit;
import in.adarshr.targetgen.build.TargetBuilder;
import in.adarshr.targetgen.dto.TargetVO;
import in.adarshr.targetgen.utils.*;
import input.targetgen.adarshr.in.input.ComponentInfo;
import jakarta.xml.bind.JAXBException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import output.targetgen.adarshr.in.output.Target;

import java.io.File;
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
            File inputSchemaFile = new File("schema/input.xsd");
            componentInfo = JaxbUtils.unmarshallAndValidate(xmlFile, inputSchemaFile, ComponentInfo.class);
            LOG.info("JAXBUtils: {}", componentInfo);
        } catch (JAXBException | SAXException e) {
            LOG.error("Failed to unmarshal input XML file: {}", e.getMessage());
        }

        //Proceed only when we have the Input XML data
        if (componentInfo != null) {
            //Set the data to TargetVO for target file generation
            TargetVO targetVO = new TargetVO();
            targetVO.setComponentInfo(componentInfo);

            //Set delivery report data. If report location is URL, then create URL and get report data
            //If report location is file, then get report data from file
            if (TargetUtils.isUrl(componentInfo.getReportLocation())) {
                String reportLocation = componentInfo.getReportLocation();
                String deliveryReportUrl = TargetUtils.createDeliveryReportUrl(reportLocation, componentInfo.getVersion());
                LOG.info("*** Delivery Report URL: {} ***", deliveryReportUrl);
                List<Report> reportDataFromUrl = TargetUtils.getReportData(deliveryReportUrl, 2, 1);
                targetVO.setDeliveryReportData(reportDataFromUrl);
                LOG.info("*** Delivery Report Data from URL: {} ***", reportDataFromUrl);
            } else {
                List<Report> reportData = TargetUtils.getReportData(componentInfo.getReportLocation(), 2, 2);
                targetVO.setDeliveryReportData(reportData);
                LOG.info("*** Delivery Report Data from File: {} ***", reportData);
            }

            Set<Repo> repoJarUrls;
            boolean isCreateBasedOnReport = componentInfo.isCreateBasedOnReport();
            if(isCreateBasedOnReport){
                //Get jar urls
                repoJarUrls = ReportUtils.getJarUrls(targetVO);
                LOG.info("*** Repo Jar Urls creation completed. ***");
            }else {
                //Get jar urls
                repoJarUrls = TargetUtils.getJarUrls(targetVO);
                LOG.info("*** Repo Jar Urls creation completed. ***");
            }

            if(repoJarUrls.isEmpty()){
                LOG.error("*** No jar urls found. Exiting the application. ***");
                return;
            }
            //Download jar and get input stream
            Map<Repo, String> repoStringMap = ConnectionUtil.downloadSpecificXMLFromJar(repoJarUrls);
            LOG.info("*** Repo Jar Urls download completed. ***");

            // Parse the XML from the jar file
            Map<Repo, List<Unit>> repoListMap = XMLUtils.parseAllXml(repoStringMap);
            //Map<Repo, List<Unit>> filteredRepoListMap = TargetUtils.filterRepoUnits(repoListMap);
            targetVO.setRepoUnitMap(repoListMap);
            LOG.info("*** Repo Jar Urls parsing completed. ***");



            //Set Repo List. This is used to create location in target file
            targetVO.setRepoMapList(TargetUtils.getRepoMapList(componentInfo));
            LOG.info("*** Repo Map List created completed. ***");

            //Set Component Repos. This is used to create location in target file
            targetVO.setComponentRepoMap(TargetUtils.getComponentRepoMap(componentInfo));
            LOG.info("*** Component Repo Map created completed. ***");

            //Set version
            targetVO.setVersion(componentInfo.getVersion());
            targetVO.setTargetSaveFormat(componentInfo.getTargetSaveFormat());

            // Create xml file from jaxb
            TargetBuilder targetBuilder = new TargetBuilder();
            Map<String, Target> stringTargetMap = targetBuilder.buildTargets(targetVO);
            LOG.info("*** Target file creation completed. ***");

            XMLUtils.createXmlFiles(stringTargetMap);
            LOG.info("*** Target files are written to disk. ***");
            LOG.info("*** All tasks completed. ***");
        } else {
            LOG.error("ComponentInfo is null");
            LOG.error("*** Application is exiting ***");
        }
    }
}
