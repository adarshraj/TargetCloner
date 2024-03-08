package in.adarshr.targetcloner;

import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.build.TargetBuilder;
import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.data.TargetDetails;
import in.adarshr.targetcloner.dto.TargetData;
import in.adarshr.targetcloner.helper.*;
import in.adarshr.targetcloner.utils.TargetClonerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main class of the application.
 */
public class TargetCloner {
    private static final Logger LOG = LoggerFactory.getLogger(TargetCloner.class);

    private static int step = 1;

    public static void main(String[] args) {
        TargetClonerUtil.printBanner();
        LOG.info("!!! Starting TargetCloner application. !!!");
        ArgumentParser argumentParser = new ArgumentParser(args);
        // Set global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> LOG.error("Global exception handler caught: ", exception));

        //Read input xml file
        String xmlFile = "input/appdata/TargetCloner.xml";
        String inputSchemaLocation = "schema/TargetCloner.xsd";
        TargetDetails targetDetails = TargetClonerUtil.getTargetDetails(xmlFile, inputSchemaLocation);

        //Proceed only when we have the Input XML data
        if (targetDetails != null) {
            LOG.info("*** Step {} *** TargetDetails input successfully parsed ***", stepCount());
            //Read the input target files
            TargetData targetData = new TargetData();
            List<File> targetFilesToCopy = TargetClonerUtil.getTargetFilesToCopy("input/targets/");
            List<Target> targets = TargetClonerUtil.unmarshalTargetFiles(targetFilesToCopy);
            if (CollectionUtils.isNotEmpty(targets)) {
                LOG.info("*** Step {} *** Input target files successfully parsed ***", stepCount());
                //Set the data to TargetData for target file generation
                targetData.setTargetDetails(targetDetails);

                //Set the target data
                targetData.setInputTargets(targets);

                //Delivery report data
                targetData.setDeliveryReportMap(ReportHelper.getDeliveryReport(targetData));
                LOG.info("*** Step {} *** Delivery report data obtained ***", stepCount());

                //Get repository jar urls
                Set<RepoData> repoDataJarUrls = ReportHelper.getJarUrls(targetData);
                if (repoDataJarUrls.isEmpty()) {
                    LOG.error("*** Error *** No jar urls found. Exiting the application. ***");
                    return;
                } else {
                    LOG.info("*** Step {} *** Repo Jar Urls creation completed. ***", stepCount());
                    targetData.setRepoDataUrlSet(repoDataJarUrls);
                }

                //Download jar and get input stream
                Map<RepoData, String> repoStringMap = ConnectionHelper.downloadAllJars(repoDataJarUrls);
                LOG.info("*** Step {} ***  Repo Jar download completed. ***", stepCount());

                // Parse the XML from the jar file
                targetData.setRepoUnitsMap(XMLHelper.parseAllXml(repoStringMap));
                LOG.info("*** Step {} ***  Repo Jar Urls parsing completed. ***", stepCount());

                if (targetData.getRepoUnitsMap().isEmpty()) {
                    LOG.error("!!! Error. No XML files found in the jar. Exiting the application. !!!");
                    return;
                }

                //Set version. Use to create target file name
                targetData.setVersion(targetDetails.getVersion());
                targetData.setTargetSaveFormat(targetDetails.getTargetSaveFormat());

                // Create target files
                TargetBuilder targetBuilder = new TargetBuilder();
                Map<String, Target> stringTargetMap = targetBuilder.buildTargets(targetData);
                LOG.info("*** Step {} *** Target file creation completed. ***", stepCount());

                //Write the target files to disk
                XMLHelper.saveFilesToDisk(stringTargetMap);
                LOG.info("*** Step {} ***  Target files are written to disk. ***", stepCount());

                if (argumentParser.isCompare()) {
                    //Compare the target files
                    CompareHelper.compareTargetFiles(stringTargetMap, targets);
                    LOG.info("*** Step {} ***  Target files are compared. ***", stepCount());
                }
                LOG.info("!!! All tasks completed. !!!");
            } else {
                LOG.error("!!! No target files found to copy. Exiting the application. !!!");
            }
        } else {
            LOG.error("!!! Unable to parse input XML file. Exiting the application. !!!");
        }
    }

    /**
     * Method to count the steps
     *
     * @return int
     */
    private static int stepCount() {
        return step++;
    }
}
