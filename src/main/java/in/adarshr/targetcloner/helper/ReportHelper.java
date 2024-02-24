package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.data.Repo;
import in.adarshr.targetcloner.data.TargetDetails;
import in.adarshr.targetcloner.dto.TargetData;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;

public class ReportHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ReportHelper.class);

    private static final String ARTIFACT_PLACEHOLDER = "$ARTIFACT$";
    private static final String GROUP_PLACEHOLDER = "$GROUP$";
    private static final String VERSION_PLACEHOLDER = "$VERSION$";
    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * Get jar urls
     *
     * @param targetData TargetData
     * @return Set
     */
    public static Set<RepoData> getJarUrls(TargetData targetData) {
        Map<String, List<RepoData>> repoMap = targetData.getRepoMap();
        Set<RepoData> jarUrls = new HashSet<>();
        repoMap.forEach((component, repoList) -> jarUrls.addAll(repoList));
        LOG.info("Jar urls from report: {}", jarUrls);
        return jarUrls;
    }

    /**
     * Get report data from the delivery report file
     *
     * @param reportFileLocation Report file location
     * @param linesToSkip        Lines to skip
     * @return List of Report
     */
    public static List<DeliveryReport> getReportData(String reportFileLocation, int linesToSkip, int sourceType) {
        List<DeliveryReport> deliveryReports = new ArrayList<>();
        try {
            String reportFile;
            if (sourceType == 1) {
                reportFile = readFileFromUrl(reportFileLocation, linesToSkip);
            } else {
                reportFile = readFileFromDirectory(reportFileLocation, linesToSkip);
            }

            if (reportFile == null) {
                LOG.error("Report file is null/empty or cannot be read");
                throw new RuntimeException("Report file is null/empty or cannot be read");
            } else {
                for (String line : reportFile.split("\n")) {
                    DeliveryReport deliveryReport = DeliveryReport.fromDelimitedString(line, ":");
                    deliveryReports.add(deliveryReport);
                }
            }
            return deliveryReports;
        } catch (IOException e) {
            LOG.error("Failed to read file from directory: {}", e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * Read file from directory. For Local testing
     *
     * @param fileName    File name
     * @param linesToSkip Lines to skip
     * @return String
     * @throws IOException Throws IOException
     */
    private static String readFileFromDirectory(String fileName, final int linesToSkip) throws IOException {
        File file = new File(fileName);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.lines().skip(linesToSkip).collect(Collectors.joining("\n"));
        }
    }

    /**
     * Read file from URL. For production
     *
     * @param fileUrl     File URL
     * @param linesToSkip Lines to skip
     * @return String
     * @throws IOException Throws IOException
     */
    private static String readFileFromUrl(String fileUrl, final int linesToSkip) throws IOException {
        URI uri = URI.create(fileUrl);
        URL url = uri.toURL();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return reader.lines().skip(linesToSkip).collect(Collectors.joining("\n"));
        }
    }

    /**
     * Get report data
     *
     * @param targetDetails TargetDetails
     * @return Set
     */
    public static Set<DeliveryReport> getReportData(TargetDetails targetDetails) {
        List<DeliveryReport> deliveryReportData;
        if (isUrl(targetDetails.getReportLocation())) {
            if(LOG.isInfoEnabled()){
                LOG.info("*** Delivery Report Data from URL. ***");
            }
            String reportLocation = targetDetails.getReportLocation();
            String deliveryReportUrl = createDeliveryReportUrl(
                    reportLocation, targetDetails.getVersion());
            deliveryReportData = getReportData(deliveryReportUrl, 2, 1);
        } else {
            if(LOG.isInfoEnabled()){
                LOG.info("*** Delivery Report Data from File. ***");
            }
            deliveryReportData = getReportData(targetDetails.getReportLocation(), 2, 2);
        }
        return new HashSet<>(deliveryReportData);
    }

    /**
     * Check if the location is URL
     *
     * @param location Location
     * @return boolean
     */
    private static boolean isUrl(String location) {
        try {
            new URL(location);
            return true;
        } catch (MalformedURLException e) {
            LOG.error("Invalid delivery report url: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Create delivery report URL
     *
     * @param reportLocation Report location
     * @param version        Version
     * @return String
     */
    public static String createDeliveryReportUrl(String reportLocation, String version) {
        if (reportLocation != null && reportLocation.contains("$VERSION$")) {
            reportLocation = reportLocation.replace("$VERSION$", version);
        }
        if(LOG.isInfoEnabled()){
            LOG.info("*** Delivery Report URL: {} ***", reportLocation);
        }
        return reportLocation;
    }

    /**
     * Get jar urls from report
     *
     * @param targetData TargetData
     * @return Set
     */
    public static Map<String, List<RepoData>> updateRepoMapWithLocation(TargetData targetData) {
        Set<DeliveryReport> deliveryReports = targetData.getDeliveryReports();
        TargetDetails targetDetails = targetData.getTargetDetails();
        Map<String, List<RepoData>> repoMap = getRepoMapList(targetDetails);
        for (Map.Entry<String, List<RepoData>> entry : repoMap.entrySet()) {
            List<RepoData> repoDataList = entry.getValue();
            for (RepoData repoData : repoDataList) {
                if (deliveryReports != null) {
                    for (DeliveryReport deliveryReport : deliveryReports) {
                        if (StringUtils.isNotEmpty(deliveryReport.getGroup())
                                && StringUtils.isNotEmpty(deliveryReport.getArtifact())
                                && StringUtils.isNotEmpty(deliveryReport.getVersion())) {
                            if (deliveryReport.getGroup().equals(repoData.getGroup())
                                    && deliveryReport.getArtifact().equals(repoData.getArtifact())) {
                                repoData.setLocation(createRepoUrl(repoData, deliveryReport, targetDetails));
                            }
                        }
                    }
                }
            }
        }

        return repoMap;
    }

    /**
     * Create repo URL
     *
     * @param repoData         Repo
     * @param deliveryReport       Report
     * @param targetDetails TargetDetails
     * @return String
     */
    private static String createRepoUrl(RepoData repoData, DeliveryReport deliveryReport, TargetDetails targetDetails) {
        if(LOG.isInfoEnabled()){
            LOG.info("Before repo URL: {} ", repoData.getLocation());
        }
        String group = repoData.getGroup();
        String artifact = repoData.getArtifact();
        String location = repoData.getLocation();

        location = replacePlaceholders(location, ARTIFACT_PLACEHOLDER, artifact, targetDetails.getArtifactUrlPattern());
        location = replacePlaceholders(location, GROUP_PLACEHOLDER, group, targetDetails.getGroupUrlPattern());

        if (location != null && location.contains(VERSION_PLACEHOLDER)) {
            String version = (repoData.getVersion() != null && !repoData.getVersion().isEmpty()) ? repoData.getVersion() : deliveryReport.getVersion();
            location = location.replace(VERSION_PLACEHOLDER, version);
        }
        String repoUrl = location + "/" + "content.jar";
        if(LOG.isInfoEnabled()){
            LOG.info("After repo URL: {}", repoUrl);
        }
        return repoUrl;
    }

    /**
     * Replace placeholders
     *
     * @param location   Location
     * @param placeholder Placeholder
     * @param value      Value
     * @param pattern    Pattern
     * @return String
     */
    private static String replacePlaceholders(String location, String placeholder, String value, String pattern) {
        if (location != null && location.contains(placeholder)) {
            if (pattern != null && pattern.contains(PATH_SEPARATOR)) {
                location = location.replace(placeholder, value.replaceAll("\\.", PATH_SEPARATOR));
            } else {
                location = location.replace(placeholder, value);
            }
        }
        return location;
    }

    public static Map<String, List<RepoData>> getRepoMap(TargetData targetData,  TargetDetails targetDetails) {
        if(targetDetails.isCreateBasedOnReport()){
            return updateRepoMapWithLocation(targetData);
        }
        return getRepoMapList(targetDetails);
    }

    /**
     * Get repo map list
     *
     * @return Map
     */
    public static Map<String, List<RepoData>> getRepoMapList(TargetDetails targetDetails) {
        Map<String, List<RepoData>> repoMap = new HashMap<>();
        if ((targetDetails == null) || (targetDetails.getComponents() == null)) {
            LOG.error("TargetDetails is null");
            throw new RuntimeException("TargetDetails is null");
        } else {
            targetDetails.getComponents().getComponent().forEach(component -> {
                List<RepoData> repoDataStore = new ArrayList<>();
                if (component.getRepository() == null || component.getRepository().getRepos() == null) {
                    LOG.error("Repository is null for component: {}", component.getName());
                    throw new RuntimeException("Repository is null for component: " + component.getName());
                } else {
                    component.getRepository().getRepos().getRepo().forEach(repo -> {
                        RepoData repoDataBo = new RepoData();
                        repoDataBo.setArtifact(repo.getArtifact());
                        repoDataBo.setGroup(repo.getGroup());
                        repoDataBo.setVersion(repo.getVersion());
                        if(targetDetails.isCreateBasedOnReport()){
                            repoDataBo.setLocation(createRepoUrl(repo, targetDetails));
                        }else{
                            repoDataBo.setLocation(repo.getLocation());
                        }
                        repoDataBo.setLocation(repo.getLocation());
                        repoDataStore.add(repoDataBo);
                    });
                    repoMap.put(component.getName(), repoDataStore);
                }
            });
        }
        return repoMap;
    }

    /**
     * Create repo URL
     *
     * @param repo Repo
     * @return String
     */
    private static String createRepoUrl(Repo repo, TargetDetails targetDetails) {
        String group = repo.getGroup();
        String artifact = repo.getArtifact();
        String location = repo.getLocation();
        if (location != null && location.contains("$ARTIFACT$")) {
            String artifactUrlPattern = targetDetails.getArtifactUrlPattern();
            if (artifactUrlPattern != null && artifactUrlPattern.contains("/")) {
                location = location.replace("$ARTIFACT$", artifact.replaceAll("\\.", "/"));
            } else {
                location = location.replace("$ARTIFACT$", artifact);
            }
        }
        if (location != null && location.contains("$GROUP$")) {
            String groupUrlPattern = targetDetails.getGroupUrlPattern();
            if (groupUrlPattern != null && groupUrlPattern.contains("/")) {
                location = location.replace("$GROUP$", group.replaceAll("\\.", "/"));
            } else {
                location = location.replace("$GROUP$", group);
            }
        }
        if (location != null && location.contains("$VERSION$")) {
            if (repo.getVersion() != null && !repo.getVersion().isEmpty()) {
                location = location.replace("$VERSION$", repo.getVersion());
            } else {
                location = location.replace("$VERSION$", targetDetails.getVersion());
            }
        }

        String repoUrl = location + "/" + "content.jar";
        LOG.info("Repo URL: {}", repoUrl);
        return repoUrl;
    }
}