package in.adarshr.targetcloner;

import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.build.TargetBuilder;
import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.data.TargetDetails;
import in.adarshr.targetcloner.dto.TargetData;
import in.adarshr.targetcloner.helper.ConnectionHelper;
import in.adarshr.targetcloner.helper.ReportHelper;
import in.adarshr.targetcloner.helper.XMLHelper;
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

        // Set global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> LOG.error("Global exception handler caught: ", exception));

        //Read input xml file
        String xmlFile = "input/appdata/TargetCloner.xml";
        String inputSchemaLocation = "schema/TargetCloner.xsd";
        TargetDetails targetDetails = TargetClonerUtil.getTargetDetails(xmlFile, inputSchemaLocation);

        //Proceed only when we have the Input XML data
        if (targetDetails != null) {
            //Read the input target files
            TargetData targetData = new TargetData();
            List<File> targetFilesToCopy = TargetClonerUtil.getTargetFilesToCopy("input/targets/");
            List<Target> targets = TargetClonerUtil.unmarshalTargetFiles(targetFilesToCopy);
            if (CollectionUtils.isNotEmpty(targets)) {
                //Set the data to TargetData for target file generation
                targetData.setTargetDetails(targetDetails);

                //Set the target data
                targetData.setTargets(targets);

                /*Set delivery report data. If report location is URL, then create URL and get report data
                If report location is file, then get report data from file
                 */
                targetData.setDeliveryReports(ReportHelper.getReportData(targetDetails));
                LOG.info("*** Delivery report data set completed. ***");

                //Get repository jar urls
                Set<RepoData> repoDataJarUrls = ReportHelper.getJarUrls(targetData);
                LOG.info("*** Repo Jar Urls creation completed. ***");
                targetData.setRepoDataUrlSet(repoDataJarUrls);

                if (repoDataJarUrls.isEmpty()) {
                    LOG.error("*** No jar urls found. Exiting the application. ***");
                    return;
                }

                //Download jar and get input stream
                Map<RepoData, String> repoStringMap = ConnectionHelper.downloadAllJars(repoDataJarUrls);
                LOG.info("*** Repo Jar Urls download completed. ***");

                // Parse the XML from the jar file
                Map<RepoData, List<RepoUnit>> repoListMap = XMLHelper.parseAllXml(repoStringMap);
                targetData.setRepoUnitsMap(repoListMap);
                LOG.info("*** Repo Jar Urls parsing completed. ***");

                //Set Repo List. This is used to create location in target file
                targetData.setRepoMap(ReportHelper.getRepoMap(targetData, targetDetails));
                LOG.info("*** Repo Map List created completed. ***");

                //Set version. Use to create target file name
                targetData.setVersion(targetDetails.getVersion());
                targetData.setTargetSaveFormat(targetDetails.getTargetSaveFormat());

                // Create target files
                TargetBuilder targetBuilder = new TargetBuilder();
                Map<String, Target> stringTargetMap = targetBuilder.buildTargets(targetData);
                LOG.info("*** Target file creation completed. ***");

                //Write the target files to disk
                XMLHelper.saveFilesToDisk(stringTargetMap);
                LOG.info("*** Target files are written to disk. ***");
                LOG.info("*** All tasks completed. ***");
            } else {
                LOG.error("*** No target files found to copy. Exiting the application. ***");
            }
        } else {
            LOG.error("*** Unable to parse input XML file. Exiting the application. ***");
        }
    }
}
