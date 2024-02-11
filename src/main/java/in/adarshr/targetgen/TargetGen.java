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


        TargetVO targetVO = new TargetVO();
        ComponentInfo componentInfo = null;
        //Read input xml
        try {
            File xmlFile = new File("input/input.xsd.xml");
            componentInfo = JaxbUtils.unmarshal(xmlFile, ComponentInfo.class);
            targetVO.setComponentInfo(componentInfo);
        } catch (JAXBException e) {
            LOG.error("Failed to unmarshal input XML file: {}", e.getMessage());
        }

        //Get jar urls
        Set<Repo> repoJarUrls = TargetUtils.getJarUrls(componentInfo);

        //Download jar and get input stream
        Map<Repo, InputStream> repoInputStreamMap = ConnectionUtil.downloadSpecificXMLFromJar(repoJarUrls);

        // Parse the XML from the jar file
        Map<Repo, List<Unit>> repoListMap = XMLUtils.parseAllXml(repoInputStreamMap);
        targetVO.setRepoUnitMap(repoListMap);

        //Set delivery report data
        List<Report> reportData = TargetUtils.getReportData("report/DeliveryReport.txt", 2);
        targetVO.setDeliveryReportData(reportData);

        //Set Repo List
        targetVO.setRepoMapList(TargetUtils.getRepoMapList(componentInfo));

        //Set Component Repos
        targetVO.setComponentRepoMap(TargetUtils.getComponentRepoMap(componentInfo));

        // Create xml file from jaxb
        TargetBuilder targetBuilder = new TargetBuilder();
        List<Target> targets = targetBuilder.buildTargets(targetVO);
        XMLUtils.createXmlFiles(targets);
    }



}
