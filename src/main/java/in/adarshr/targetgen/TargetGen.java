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

public class TargetGen {
    private static final Logger LOG = LoggerFactory.getLogger(TargetGen.class);

    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> LOG.error("Global exception handler caught: {}", exception.getMessage()));

        ComponentInfo componentInfo = null;
        //Read input xml
        try {
            File xmlFile = new File("input/input.xml");
            File inputSchemaFile = new File("src/main/resources/schema/input.xsd");
            componentInfo = JaxbUtils.unmarshal(xmlFile, ComponentInfo.class);
            Object jaxbUtils = JaxbUtils.unmarshallAndValidate(xmlFile, inputSchemaFile, ComponentInfo.class);
            LOG.info("JAXBUtils: {}", jaxbUtils);
        } catch (JAXBException | SAXException e) {
            LOG.error("Failed to unmarshal input XML file: {}", e.getMessage());
        }

        if(componentInfo != null) {
            TargetVO targetVO = new TargetVO();
            targetVO.setComponentInfo(componentInfo);

            //Get jar urls
            Set<Repo> repoJarUrls = TargetUtils.getJarUrls(componentInfo);

            //Download jar and get input stream
            Map<Repo, InputStream> repoInputStreamMap = ConnectionUtil.downloadSpecificXMLFromJar(repoJarUrls);

            // Parse the XML from the jar file
            Map<Repo, List<Unit>> repoListMap = XMLUtils.parseAllXml(repoInputStreamMap);
            Map<Repo, List<Unit>> filteredRepoListMap = TargetUtils.filterRepoUnits(repoListMap);
            targetVO.setRepoUnitMap(filteredRepoListMap);

            //Set delivery report data
            if(TargetUtils.isUrl(componentInfo.getReportLocation())){
                String reportLocation = componentInfo.getReportLocation();
                String createRepoUrl = TargetUtils.createDeliveryReportUrl(reportLocation, componentInfo.getVersion());
                List<Report> reportDataFromUrl = TargetUtils.getReportData(createRepoUrl, 1, 1);
                targetVO.setDeliveryReportData(reportDataFromUrl);
            }else {
                List<Report> reportData = TargetUtils.getReportData(componentInfo.getReportLocation(), 2, 2);
                targetVO.setDeliveryReportData(reportData);
            }

            //Set Repo List
            targetVO.setRepoMapList(TargetUtils.getRepoMapList(componentInfo));

            //Set Component Repos
            targetVO.setComponentRepoMap(TargetUtils.getComponentRepoMap(componentInfo));

            //Set version
            targetVO.setVersion(componentInfo.getVersion());

            // Create xml file from jaxb
            TargetBuilder targetBuilder = new TargetBuilder();
            Map<String, Target> stringTargetMap = targetBuilder.buildTargets(targetVO);
            XMLUtils.createXmlFiles(stringTargetMap);
        }else {
            LOG.error("ComponentInfo is null");
        }
    }



}
