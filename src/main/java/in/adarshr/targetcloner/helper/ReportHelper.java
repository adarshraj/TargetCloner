package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.constants.ReportSource;
import in.adarshr.targetcloner.constants.SeperatorConstants;
import in.adarshr.targetcloner.data.*;
import in.adarshr.targetcloner.dto.TargetData;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static in.adarshr.targetcloner.constants.TargetClonerConstants.*;

public class ReportHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ReportHelper.class);

    /**
     * Get jar urls
     *
     * @param targetData TargetData
     * @return Set
     */
    public static Set<RepoData> getJarUrls(TargetData targetData) {
        Map<String, Map<String, RepoData>> componentRepoMap = createRepoDataMap(targetData);
        targetData.setComponentRepoDataMap(componentRepoMap);
        Set<RepoData> jarUrls = new HashSet<>();

        componentRepoMap.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                value.forEach((k, v) -> {
                    if (v != null && v.getLocation() != null && v.getLocation().endsWith(SeperatorConstants.LOCATION_SEPARATOR)) {
                        v.setLocation(v.getLocation() + CONTENT_JAR);
                    } else if (v != null && v.getLocation() != null && !v.getLocation().endsWith(SeperatorConstants.LOCATION_SEPARATOR)) {
                        v.setLocation(v.getLocation() + CONTENT_JAR_WITH_SEPARATOR);
                    }
                    jarUrls.add(v);
                });
            }
        });
        LOG.info("Jar urls from report: {}", jarUrls);
        return jarUrls;
    }

    private static Map<String, Map<String, RepoData>> createRepoDataMap(TargetData targetData) {
        Map<String, Map<String, DeliveryReport>> mapOfDeliveryReportForInputTargets = createDeliveryReportForInputTargets(targetData);
        Map<String, Map<String, RepoData>> compoenentRepoDataMap = new HashMap<>();
        List<Target> inputTargets = targetData.getInputTargets();
        for (Target inputTarget : inputTargets) {
            Map<String, RepoData> repoDataMap = new HashMap<>();
            if (inputTarget.getLocations() != null
                    && CollectionUtils.isNotEmpty(inputTarget.getLocations().getLocation())) {
                List<Location> inputLocations = inputTarget.getLocations().getLocation();
                RepoData repoData;
                for (Location inputLocation : inputLocations) {
                    Map<String, DeliveryReport> newUrlDeliveryReportMap = mapOfDeliveryReportForInputTargets.get(inputLocation.getRepository().getLocation());
                    if (newUrlDeliveryReportMap != null && !newUrlDeliveryReportMap.isEmpty()) {
                        for (Map.Entry<String, DeliveryReport> entry : newUrlDeliveryReportMap.entrySet()) {
                            DeliveryReport deliveryReport = entry.getValue();
                            if (deliveryReport != null && deliveryReport.getGroup() != null && deliveryReport.getArtifact() != null) {
                                repoData = createRepoData(deliveryReport, inputLocation, entry.getKey());
                                repoDataMap.put(repoData.getLocation(), repoData);
                            }
                        }
                    }
                }
            }
            compoenentRepoDataMap.put(inputTarget.getName(), repoDataMap);
        }
        return compoenentRepoDataMap;
    }

    /**
     * Get delivery report for location
     *
     * @param targetData TargetData
     * @return DeliveryReport
     */
    private static Map<String, Map<String, DeliveryReport>> createDeliveryReportForInputTargets(TargetData targetData) {
        List<Target> inputTargets = targetData.getInputTargets();
        Map<String, DeliveryReport> deliveryReportMap = targetData.getDeliveryReportMap();
        Map<String, Map<String, DeliveryReport>> targetDeliveryReportMap = new HashMap<>();
        for (Map.Entry<String, DeliveryReport> entry : deliveryReportMap.entrySet()) {
            DeliveryReport deliveryReport = entry.getValue();
            if (deliveryReport != null && deliveryReport.getGroup() != null && deliveryReport.getArtifact() != null) {
                for (Target inputTarget : inputTargets) {
                    if (inputTarget.getLocations() != null
                            && CollectionUtils.isNotEmpty(inputTarget.getLocations().getLocation())) {
                        for (Location inputLocation : inputTarget.getLocations().getLocation()) {
                            String currentUrl = inputLocation.getRepository().getLocation();
                            if (!targetDeliveryReportMap.containsKey(currentUrl)) {
                                deliveryReport = getDeliveryReportForLocation(inputLocation, deliveryReport, targetData);
                                if (deliveryReport != null) {
                                    String newUrl = getNewUrlForLocation(inputLocation, deliveryReport, targetData);
                                    if (StringUtils.isNotEmpty(currentUrl) && StringUtils.isNotEmpty(newUrl)) {
                                        Map<String, DeliveryReport> targetDeliveryReport = new HashMap<>();
                                        targetDeliveryReport.put(newUrl, deliveryReport);
                                        targetDeliveryReportMap.put(currentUrl, targetDeliveryReport);

                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return targetDeliveryReportMap;
    }

    /**
     * Get delivery report for location
     *
     * @param inputLocation Location
     * @param targetData    TargetData
     * @return DeliveryReport
     */
    private static DeliveryReport getDeliveryReportForLocation(Location inputLocation, DeliveryReport deliveryReport, TargetData targetData) {
        List<Pattern> patterns = targetData.getTargetDetails().getRepoUrlPatterns().getPattern();
        String repoUrl = inputLocation.getRepository().getLocation();
        if (deliveryReport != null && deliveryReport.getGroup() != null && deliveryReport.getArtifact() != null) {
            for (Pattern pattern : patterns) {
                String group = formatDeliveryData(deliveryReport.getGroup(), pattern.getCurrentGroupUrlPattern(), pattern.getFutureGroupUrlPattern());
                String artifact = formatDeliveryData(deliveryReport.getArtifact(), pattern.getCurrentArtifactUrlPattern(), pattern.getFutureArtifactUrlPattern());
                if (repoUrl.contains(group) && repoUrl.contains(artifact)) {
                    return deliveryReport;
                }
            }
        }

        return null;
    }

    /**
     * Get new URL for location
     *
     * @param inputLocation  Location
     * @param deliveryReport DeliveryReport
     * @param targetData     TargetData
     * @return String
     */
    private static String getNewUrlForLocation(Location inputLocation, DeliveryReport deliveryReport, TargetData targetData) {
        List<Pattern> patterns = targetData.getTargetDetails().getRepoUrlPatterns().getPattern();
        String repoUrl = inputLocation.getRepository().getLocation();
        for (Pattern pattern : patterns) {
            String group = formatDeliveryData(deliveryReport.getGroup(), pattern.getCurrentGroupUrlPattern(), pattern.getFutureGroupUrlPattern());
            String artifact = formatDeliveryData(deliveryReport.getArtifact(), pattern.getCurrentArtifactUrlPattern(), pattern.getFutureArtifactUrlPattern());
            String version = formatDeliveryData(deliveryReport.getVersion(), pattern.getCurrentVersionUrlPattern(), pattern.getFutureVersionUrlPattern());
            if (repoUrl.contains(group) && repoUrl.contains(artifact)) {
                return pattern.getUrlPattern().replace(PLACEHOLDER_GROUP, group)
                        .replace(PLACEHOLDER_ARTIFACT, artifact)
                        .replace(PLACEHOLDER_VERSION, version);
            }
        }
        return StringUtils.EMPTY;
    }


    private static String formatDeliveryData(String deliveryData, String currentFormat, String futureFormat) {
        if (deliveryData != null) {
            return deliveryData.replace(currentFormat, futureFormat);
        }
        return StringUtils.EMPTY;
    }

    /**
     * Update delivery report for non report case
     *
     * @param targetData        TargetData
     * @param deliveryReportMap DeliveryReport map
     */
    private static Map<String, DeliveryReport> updateDeliveryReportForNonReportCase(TargetData targetData, Map<String, DeliveryReport> deliveryReportMap) {
        List<Pattern> patterns = targetData.getTargetDetails().getRepoUrlPatterns().getPattern();
        for (Pattern pattern : patterns) {
            if (!pattern.isUseDeliveryReport()) {
                DeliveryReport deliveryReport =
                        new DeliveryReport(null, pattern.getGroupId(), pattern.getArtifact(), pattern.getVersion(), null, null);
                deliveryReportMap.put(deliveryReport.getGroup() + deliveryReport.getArtifact(), deliveryReport);
            }
        }
        return deliveryReportMap;
    }

    /**
     * Create repo data
     *
     * @param deliveryReport DeliveryReport
     * @param location       Location
     * @param newUrl         New URL
     * @return RepoData
     */
    private static RepoData createRepoData(DeliveryReport deliveryReport, Location location, String newUrl) {
        RepoData repoData = new RepoData();
        repoData.setGroup(deliveryReport.getGroup());
        repoData.setArtifact(deliveryReport.getArtifact());
        repoData.setVersion(deliveryReport.getVersion());
        repoData.setLocation(newUrl);
        repoData.setRepoUnits(setRepoUnits(location));
        return repoData;
    }


    /**
     * Set repo units
     *
     * @param location Location
     * @return List
     */
    private static List<RepoUnit> setRepoUnits(Location location) {
        List<RepoUnit> repoUnits = new ArrayList<>();
        for (Unit unit : location.getUnit()) {
            RepoUnit repoUnit = new RepoUnit();
            repoUnit.setId(unit.getId());
            repoUnit.setVersion(unit.getVersion());
            repoUnits.add(repoUnit);
        }
        return repoUnits;
    }


    /**
     * Get report data from the delivery report file
     *
     * @param reportFileLocation Report file location
     * @param linesToSkip        Lines to skip
     * @return Map of DeliveryReport
     */
    public static Map<String, DeliveryReport> getReportData(String reportFileLocation, int linesToSkip, int sourceType) {
        Map<String, DeliveryReport> deliveryReportMap = new HashMap<>();
        try {
            String reportFile;
            if (sourceType == ReportSource.URL.getValue()) {
                reportFile = readFileFromUrl(reportFileLocation, linesToSkip);
            } else {
                reportFile = readFileFromDirectory(reportFileLocation, linesToSkip);
            }

            if (reportFile == null) {
                LOG.error("Report file is null/empty or cannot be read");
                throw new RuntimeException("Report file is null/empty or cannot be read");
            } else {
                for (String line : reportFile.split(SeperatorConstants.LINE_BREAK)) {
                    DeliveryReport deliveryReport = DeliveryReport.fromDelimitedString(line, SeperatorConstants.FIELD_DELIMITER_SEMICOLON);
                    deliveryReportMap.put(deliveryReport.getGroup() + deliveryReport.getArtifact(), deliveryReport);
                }
            }
            return deliveryReportMap;
        } catch (IOException e) {
            LOG.error("Failed to read file from directory: {}", e.getMessage());
            return Collections.emptyMap();
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
            return reader.lines().skip(linesToSkip).collect(Collectors.joining(SeperatorConstants.LINE_BREAK));
        }
    }

    /**
     * Get report data
     *
     * @param targetData TargetData
     * @return Set
     */
    public static Map<String, DeliveryReport> getReportData(TargetData targetData) {
        Map<String, DeliveryReport> deliveryReportMap;
        TargetDetails targetDetails = targetData.getTargetDetails();
        if (isUrl(targetDetails.getReportLocation())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("*** Delivery Report Data from URL. ***");
            }
            String reportLocation = targetDetails.getReportLocation();
            String deliveryReportUrl = createDeliveryReportUrl(
                    reportLocation, targetDetails.getVersion());
            deliveryReportMap = getReportData(deliveryReportUrl, 2, ReportSource.URL.getValue());
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("*** Delivery Report Data from File. ***");
            }
            deliveryReportMap = getReportData(targetDetails.getReportLocation(), 2, ReportSource.FILE.getValue());
        }
        return updateDeliveryReportForNonReportCase(targetData, deliveryReportMap);
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
        if (reportLocation != null && reportLocation.contains(PLACEHOLDER_VERSION)) {
            reportLocation = reportLocation.replace(PLACEHOLDER_VERSION, version);
        }
        if (LOG.isInfoEnabled()) {
            LOG.info("*** Delivery Report URL: {} ***", reportLocation);
        }
        return reportLocation;
    }
}