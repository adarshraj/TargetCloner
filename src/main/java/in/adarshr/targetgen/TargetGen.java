package in.adarshr.targetgen;

import in.adarshr.targetgen.bo.ComponentRepo;
import in.adarshr.targetgen.bo.Report;
import in.adarshr.targetgen.bo.TargetVO;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

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

        if (componentInfo == null) {
            throw new RuntimeException("ComponentInfo is null");
        } else {
            List<ComponentRepo> componentRepos = TargetUtils.getComponentRepos(componentInfo);
            targetVO.setComponentRepos(componentRepos);
            LOG.info("ComponentInfo is not null");
        }

        List<String> jarUrls = TargetUtils.getJarUrls(componentInfo);
        List<InputStream> jarStreams;
        try {
            jarStreams = ConnectionUtil.downloadJars(jarUrls);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Parse the XML from the jar file
        List<in.adarshr.targetgen.bo.Unit> boUnits = XMLUtils.parseAllXml(jarStreams);
        targetVO.setUnits(boUnits);

        List<Report> reportData = TargetUtils.getReportData("report/DeliveryReport.txt", 2);
        targetVO.setReportData(reportData);

        // Create xml file from jaxb
        TargetBuilder targetBuilder = new TargetBuilder();
        Target target = targetBuilder.buildTarget(targetVO);
        XMLUtils.createXmlFile(target);
    }



}
