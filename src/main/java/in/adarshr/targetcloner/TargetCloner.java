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

    public static void main(String[] args) {
        LOG.info("*** Starting TargetCloner application. ***");
        ArgumentParser argumentParser = new ArgumentParser(args);
        // Set global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> LOG.error("Global exception handler caught: ", exception));

        //Read input xml file
        String xmlFile = "input/appdata/TargetCloner.xml";
        String inputSchemaLocation = "schema/TargetCloner.xsd";
        TargetDetails targetDetails = TargetClonerUtil.getTargetDetails(xmlFile, inputSchemaLocation);

        //Proceed only when we have the Input XML data
        if (targetDetails != null) {
            LOG.info("*** Step 1 *** TargetDetails input successfully parsed ***");
            //Read the input target files
            TargetData targetData = new TargetData();
            List<File> targetFilesToCopy = TargetClonerUtil.getTargetFilesToCopy("input/targets/");
            List<Target> targets = TargetClonerUtil.unmarshalTargetFiles(targetFilesToCopy);
            if (CollectionUtils.isNotEmpty(targets)) {
                LOG.info("*** Step 2 *** Input target files successfully parsed ***");
                //Set the data to TargetData for target file generation
                targetData.setTargetDetails(targetDetails);

                //Set the target data
                targetData.setInputTargets(targets);

                //Delivery report data
                targetData.setDeliveryReportMap(ReportHelper.getReportData(targetData));
                LOG.info("*** Step 3 *** Delivery report data obtained ***");

                //Get repository jar urls
                Set<RepoData> repoDataJarUrls = ReportHelper.getJarUrls(targetData);
                LOG.info("*** Step 4 *** Repo Jar Urls creation completed. ***");
                targetData.setRepoDataUrlSet(repoDataJarUrls);

                if (repoDataJarUrls.isEmpty()) {
                    LOG.error("*** Error *** No jar urls found. Exiting the application. ***");
                    return;
                }

                //Download jar and get input stream
                Map<RepoData, String> repoStringMap = ConnectionHelper.downloadAllJars(repoDataJarUrls);
                LOG.info("*** Step 5 ***  Repo Jar download completed. ***");

                // Parse the XML from the jar file
                targetData.setRepoUnitsMap(XMLHelper.parseAllXml(repoStringMap));
                LOG.info("*** Step 6 ***  Repo Jar Urls parsing completed. ***");

                if (targetData.getRepoUnitsMap().isEmpty()) {
                    LOG.error("*** Error *** No XML files found in the jar. Exiting the application. ***");
                    return;
                }

                //Set version. Use to create target file name
                targetData.setVersion(targetDetails.getVersion());
                targetData.setTargetSaveFormat(targetDetails.getTargetSaveFormat());

                // Create target files
                TargetBuilder targetBuilder = new TargetBuilder();
                Map<String, Target> stringTargetMap = targetBuilder.buildTargets(targetData);
                LOG.info("*** Step 7 *** Target file creation completed. ***");

                //Write the target files to disk
                XMLHelper.saveFilesToDisk(stringTargetMap);
                LOG.info("*** Step 8 ***  Target files are written to disk. ***");

                if (argumentParser.isCompare()) {
                    //Compare the target files
                    CompareHelper.compareTargetFiles(stringTargetMap, targets, targetData);
                    LOG.info("*** Step 9 ***  Target files are compared. ***");
                }
                LOG.info("*** All tasks completed. ***");
            } else {
                LOG.error("*** No target files found to copy. Exiting the application. ***");
            }
        } else {
            LOG.error("*** Unable to parse input XML file. Exiting the application. ***");
        }
    }
}
