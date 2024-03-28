package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.constants.ReportSource;
import in.adarshr.targetcloner.constants.SeparatorConstants;
import in.adarshr.targetcloner.data.*;
import in.adarshr.targetcloner.dto.TargetData;
import in.adarshr.targetcloner.utils.TargetClonerUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static in.adarshr.targetcloner.constants.TargetClonerConstants.*;

public class ReportHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ReportHelper.class);

    /**
     * Private constructor
     */
    private ReportHelper() {
    }

    /**
     * Get jar urls
     *
     * @param targetData TargetData
     * @return Set
     */
    public static Set<RepoData> getSelectedReposForJarQueryUrls(TargetData targetData) {
        Map<String, Map<String, RepoData>> componentRepoMap = createRepoDataMap(targetData);
        targetData.setComponentRepoDataMap(componentRepoMap);
        Set<RepoData> jarUrls = new HashSet<>();
        componentRepoMap.forEach((key, value) -> {
            if (value != null && !value.isEmpty()) {
                value.forEach((k, v) -> {
                    if (v != null && v.getLocation() != null && v.getLocation().endsWith(SeparatorConstants.LOCATION_SEPARATOR)) {
                        v.setLocation(v.getLocation() + CONTENT_JAR);
                    } else if (v != null && v.getLocation() != null && !v.getLocation().endsWith(SeparatorConstants.LOCATION_SEPARATOR)) {
                        v.setLocation(v.getLocation() + CONTENT_JAR_WITH_SEPARATOR);
                    }
                    jarUrls.add(v);
                });
            }
        });
        LOG.info(">>> Repos to be used to create urls: {}", jarUrls);
        return jarUrls;
    }

    /**
     * Create repo data map. Get delivery report for location. This method just creates a map with URL both old and new as key and
     * RepoData as value. This is used to get the RepoData for the old url when creating the
     * output targets. Just to make life easier. Target name is the key for the outer map and new url is
     * the key for the inner map
     *
     * @param targetData TargetData
     * @return Map
     */
    private static Map<String, Map<String, RepoData>> createRepoDataMap(TargetData targetData) {
        Map<String, Map<String, DeliveryReport>> mapOfDeliveryReportForInputTargets = createDeliveryReportForInputTargets(targetData);
        Map<String, Map<String, RepoData>> compoenentRepoDataMap = new HashMap<>();
        List<Target> inputTargets = targetData.getInputTargets();
        for (Target inputTarget : inputTargets) {
            Map<String, RepoData> repoDataMap = new HashMap<>();
            List<Location> inputLocations = inputTarget.getLocations().getLocation();
            RepoData repoData;
            for (Location inputLocation : inputLocations) {
                Map<String, DeliveryReport> newUrlDeliveryReportMap =
                        mapOfDeliveryReportForInputTargets.get(inputLocation.getRepository().getLocation());
                if (newUrlDeliveryReportMap != null) {
                    for (Map.Entry<String, DeliveryReport> entry : newUrlDeliveryReportMap.entrySet()) {
                        DeliveryReport deliveryReport = entry.getValue();
                        if (deliveryReport != null) {
                            repoData = createRepoData(deliveryReport, inputLocation, entry.getKey());
                            repoDataMap.put(inputLocation.getRepository().getLocation(), repoData); //input url as key
                        }
                    }
                }
            }
            compoenentRepoDataMap.put(inputTarget.getName(), repoDataMap);
        }
        return compoenentRepoDataMap;
    }

    /**
     * Get delivery report for location. This method just creates a map with URL both old and new as key and
     * delivery report as value. This is used to get the delivery report for the old url when creating the
     * output targets. Just to make life easier. Old/Existing url is the key for the outer map and new url is
     * the key for the inner map.
     *
     * @param targetData TargetData
     * @return DeliveryReport
     */
    private static Map<String, Map<String, DeliveryReport>> createDeliveryReportForInputTargets(TargetData targetData) {
        List<Target> inputTargets = targetData.getInputTargets();
        Map<String, DeliveryReport> deliveryReportMap = targetData.getDeliveryReportMap();
        Map<String, Map<String, DeliveryReport>> targetDeliveryReportMap = new HashMap<>();
        for (Target inputTarget : inputTargets) {
            List<Location> inputLocations = inputTarget.getLocations().getLocation();
            for (Location inputLocation : inputLocations) {
                String inputLocationUrl = inputLocation.getRepository().getLocation();
                for (Map.Entry<String, DeliveryReport> entry : deliveryReportMap.entrySet()) {
                    DeliveryReport deliveryReport = entry.getValue();
                    boolean isFound = setDeliveryReportForRelatedRepositoryUrl(targetData, deliveryReport, inputLocationUrl, targetDeliveryReportMap);
                    if (isFound) {
                        break;
                    }
                }
            }
        }
        return targetDeliveryReportMap;
    }

    /**
     * Set delivery report on location
     *
     * @param targetData              TargetData
     * @param deliveryReport          DeliveryReport
     * @param inputLocationUrl        String
     * @param targetDeliveryReportMap Map
     */
    private static boolean setDeliveryReportForRelatedRepositoryUrl(TargetData targetData, DeliveryReport deliveryReport, String inputLocationUrl,
                                                                 Map<String, Map<String, DeliveryReport>> targetDeliveryReportMap) {
        boolean isFound = false;
        deliveryReport = getDeliveryReportForLocation(inputLocationUrl, deliveryReport, targetData);
        if (deliveryReport != null) {
            String newLocationUrl = getNewUrlForLocation(inputLocationUrl, deliveryReport, targetData);
            if (StringUtils.isNotEmpty(inputLocationUrl) && StringUtils.isNotEmpty(newLocationUrl)) {
                Map<String, DeliveryReport> targetDeliveryReport = new HashMap<>();
                targetDeliveryReport.put(newLocationUrl, deliveryReport);
                targetDeliveryReportMap.put(inputLocationUrl, targetDeliveryReport);
                isFound = true;
            }
        }
        return isFound;
    }

    /**
     * Retrieves the delivery report for the location based on pattern matching
     *
     * @param inputLocationUrl String
     * @param targetData       TargetData
     * @return DeliveryReport
     */
    private static DeliveryReport getDeliveryReportForLocation(String inputLocationUrl, DeliveryReport deliveryReport, TargetData targetData) {
        List<Pattern> patterns = targetData.getTargetDetails().getRepoUrlPatterns().getPattern();
        if (deliveryReport != null && deliveryReport.getGroup() != null && deliveryReport.getArtifact() != null) {
            for (Pattern pattern : patterns) {
                String group = formatUrlPatternData(deliveryReport.getGroup(), pattern.getCurrentGroupUrlPattern(), pattern.getFutureGroupUrlPattern());
                String artifact = formatUrlPatternData(deliveryReport.getArtifact(), pattern.getCurrentArtifactUrlPattern(), pattern.getFutureArtifactUrlPattern());
                if ((StringUtils.contains(inputLocationUrl, group) && StringUtils.contains(inputLocationUrl, artifact))){
                    boolean condA = !pattern.isUseDeliveryReport() && deliveryReport.isExternalEntry() && deliveryReport.getVersion().equals(pattern.getVersion());
                    boolean condB = pattern.isUseDeliveryReport() && !deliveryReport.isExternalEntry();
                    if(condA || condB){
                        return deliveryReport;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Create the new url for the location from the pattern
     *
     * @param deliveryReport DeliveryReport
     * @param targetData     TargetData
     * @return String
     */
    private static String getNewUrlForLocation(String inputLocationUrl, DeliveryReport deliveryReport, TargetData targetData) {
        List<Pattern> patterns = targetData.getTargetDetails().getRepoUrlPatterns().getPattern();
        String newUrl = null;
        for (Pattern pattern : patterns) {
            String group = formatUrlPatternData(deliveryReport.getGroup(), pattern.getCurrentGroupUrlPattern(), pattern.getFutureGroupUrlPattern());
            String artifact = formatUrlPatternData(deliveryReport.getArtifact(), pattern.getCurrentArtifactUrlPattern(), pattern.getFutureArtifactUrlPattern());
            String version = formatUrlPatternData(deliveryReport.getVersion(), pattern.getCurrentVersionUrlPattern(), pattern.getFutureVersionUrlPattern());
            if (inputLocationUrl.contains(group) && inputLocationUrl.contains(artifact)) {
                newUrl =  pattern.getUrlPattern().replace(PLACEHOLDER_GROUP, group)
                        .replace(PLACEHOLDER_ARTIFACT, artifact);
                String partialNewUrl = newUrl.substring(0, newUrl.indexOf(PLACEHOLDER_VERSION));
                if(inputLocationUrl.contains(partialNewUrl)) {
                    newUrl = newUrl.replace(PLACEHOLDER_VERSION, version);
                    break;
                }else{
                    newUrl = null;
                }
            }
        }
        return newUrl;
    }

    /**
     * Format the input p data
     *
     * @param deliveryData  Delivery data
     * @param currentFormat Current format
     * @param futureFormat  Future format
     * @return String
     */
    private static String formatUrlPatternData(String deliveryData, String currentFormat, String futureFormat) {
        if (deliveryData != null) {
            return deliveryData.replace(currentFormat, futureFormat).trim();
        }
        return StringUtils.EMPTY;
    }

    /**
     * Updates the delivery report for urls that having no corresponding entries in the report file
     * The data from the pattern is used to create the delivery report
     *
     * @param targetData        TargetData
     * @param deliveryReportMap DeliveryReport map
     */
    private static Map<String, DeliveryReport> updateDeliveryReportForNonReportCase(TargetData targetData, Map<String, DeliveryReport> deliveryReportMap) {
        int initialSize = deliveryReportMap.size();
        List<Pattern> patterns = targetData.getTargetDetails().getRepoUrlPatterns().getPattern();
        for (Pattern pattern : patterns) {
            if (Boolean.FALSE.equals(pattern.isUseDeliveryReport())) {
                DeliveryReport deliveryReport =
                        new DeliveryReport(null, pattern.getGroupId(), pattern.getArtifact(), pattern.getVersion(), null, null, true);
                deliveryReportMap.put(TargetClonerUtil.deliveryReportKey(
                        deliveryReport.getGroup(), deliveryReport.getArtifact(), deliveryReport.getVersion()), deliveryReport);
            }
        }
        int finalSize = deliveryReportMap.size();
        if (finalSize > initialSize) {
            LOG.info(">>> Delivery Report updated for non report case. Initial size: {}, Final size: {}", initialSize, finalSize);
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
    public static Map<String, DeliveryReport> getDeliveryReport(String reportFileLocation, int linesToSkip, int sourceType) {
        Map<String, DeliveryReport> deliveryReportMap = new HashMap<>();
        try {
            String reportFile;
            if (sourceType == ReportSource.URL.getValue()) {
                reportFile = readFileFromUrl(reportFileLocation, linesToSkip);
            } else {
                reportFile = readFileFromDirectory(reportFileLocation, linesToSkip);
            }
            if (reportFile == null) {
                LOG.error("!!! Report file is null/empty or cannot be read. !!!");
                return Collections.emptyMap();
            } else {
                for (String line : reportFile.split(SeparatorConstants.LINE_BREAK)) {
                    DeliveryReport deliveryReport = DeliveryReport.fromDelimitedString(line, SeparatorConstants.FIELD_DELIMITER_SEMICOLON);
                    deliveryReportMap.put(TargetClonerUtil.deliveryReportKey(deliveryReport.getGroup(),
                            deliveryReport.getArtifact(), deliveryReport.getVersion()), deliveryReport);
                }
            }
            return deliveryReportMap;
        } catch (IOException e) {
            LOG.error(">>> Failed to read report file: ", e);
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
            return reader.lines().skip(linesToSkip).collect(Collectors.joining(SeparatorConstants.LINE_BREAK));
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
            return reader.lines().skip(linesToSkip).collect(Collectors.joining(SeparatorConstants.LINE_BREAK));
        }
    }

    /**
     * Get report data
     *
     * @param targetData TargetData
     * @return Map of DeliveryReport with groupId+artifactId+version as key
     */
    public static Map<String, DeliveryReport> getDeliveryReport(TargetData targetData) {
        Map<String, DeliveryReport> deliveryReportMap;
        TargetDetails targetDetails = targetData.getTargetDetails();
        if (TargetClonerUtil.isUrl(targetDetails.getReportLocation())) {
            if (LOG.isInfoEnabled()) {
                LOG.info("!!! Delivery Report Data from URL. !!!");
            }
            String reportLocation = targetDetails.getReportLocation();
            String deliveryReportUrl = createDeliveryReportUrl(reportLocation, targetDetails.getVersion());
            deliveryReportMap = getDeliveryReport(deliveryReportUrl, 2, ReportSource.URL.getValue());
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info("!!! Delivery Report Data from File. !!!");
            }
            deliveryReportMap = getDeliveryReport(targetDetails.getReportLocation(), 2, ReportSource.FILE.getValue());
        }
        return updateDeliveryReportForNonReportCase(targetData, deliveryReportMap);
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