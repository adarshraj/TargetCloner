package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.data.Location;
import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.data.TargetDetails;
import in.adarshr.targetcloner.data.Unit;
import in.adarshr.targetcloner.dto.TargetData;
import org.apache.commons.collections4.CollectionUtils;
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
    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * Get jar urls
     *
     * @param targetData TargetData
     * @return Set
     */
    public static Set<RepoData> getJarUrls(TargetData targetData) {
        Map<String, RepoData> repoDataMap = createRepoDataMap(targetData);
        targetData.setRepoDataMap(repoDataMap);
        Set<RepoData> jarUrls = new HashSet<>();

        repoDataMap.forEach((key, value) -> {
            value.setLocation(value.getLocation() + "content.jar");
            jarUrls.add(value);
        });
        LOG.info("Jar urls from report: {}", jarUrls);
        return jarUrls;
    }

    private static Map<String, RepoData> createRepoDataMap(TargetData targetData) {
        Map<String, RepoData> repoDataMap = new HashMap<>();
        Set<DeliveryReport> deliveryReports = targetData.getDeliveryReports();
        List<Target> targets = targetData.getTargets();
        for(Target target: targets) {
            if(target.getLocations() != null && CollectionUtils.isNotEmpty(target.getLocations().getLocation())) {
                List<Location> locations = target.getLocations().getLocation();
                RepoData repoData;
                for (Location location : locations) {
                    String repoUrl = location.getRepository().getLocation();
                    if (deliveryReports != null) {
                        for (DeliveryReport deliveryReport : deliveryReports) {
                            if (StringUtils.isNotEmpty(deliveryReport.getGroup())
                                    && StringUtils.isNotEmpty(deliveryReport.getArtifact())) {
                                repoData = createRepoData(repoUrl, deliveryReport, location);
                                repoDataMap.put(repoUrl, repoData);
                            }
                        }
                    }
                }
            }
        }
        return repoDataMap;
    }

    private static RepoData createRepoData(String repoUrl, DeliveryReport deliveryReport, Location location) {
            try {
                //https://download.eclipse.org/$GROUP$/$ARTIFACT$/release/$VERSION$
                URL url = new URL(repoUrl);
                // get the path after the domain and remove leading and trailing `/` if any
                String path = url.getPath().replaceAll("^/|/$", "");

                // split the path by '/'
                String[] pathComponents = path.split("/");

                // As per the URL format `https://download.eclipse.org/$GROUP$/$ARTIFACT$/release/$VERSION$`,
                // We know that the $ARTIFACT$ is at 3rd index from last, $VERSION$ at 1st index from last and all before artifact is $GROUP$

                if(pathComponents.length < 4) {
                    LOG.error("URL does not match the expected format");
                    return null;
                }

                String artifact = pathComponents[pathComponents.length - 3];
                String version = pathComponents[pathComponents.length - 1];

                StringBuilder groupBuilder = new StringBuilder();
                for (int i = 0; i < pathComponents.length - 3; i++) {
                    groupBuilder.append(pathComponents[i]);
                    if (i != pathComponents.length - 4) {
                        groupBuilder.append('/');
                    }
                }

                String group = groupBuilder.toString();

                // Construct the original URL
                String newUrl;
                if(deliveryReport.getGroup().equalsIgnoreCase(group.replace("/", "."))
                        && deliveryReport.getArtifact().equalsIgnoreCase(artifact.replace("/", "."))){
                    newUrl = String.format("https://%s/%s/%s/release/%s/", url.getHost(), deliveryReport.getGroup().replaceAll("\\.","/"), deliveryReport.getArtifact(), deliveryReport.getVersion());
                } else {
                    newUrl = String.format("https://%s/%s/%s/release/%s/", url.getHost(), group, artifact, version);
                }
                LOG.info("Original URL: " + repoUrl);
                LOG.info("New URL: " + newUrl);
                LOG.info("Group: " + group);
                LOG.info("Artifact: " + artifact);
                LOG.info("Version: " + version);

                RepoData repoData = new RepoData();
                repoData.setGroup(deliveryReport.getGroup());   //Anyway both should be same
                repoData.setArtifact(deliveryReport.getArtifact());  //Anyway both should be same
                repoData.setVersion(deliveryReport.getVersion());  //Anyway both should be same
                repoData.setLocation(newUrl);

                repoData.setRepoUnits(setRepoUnits(location, deliveryReport));

                return repoData;

            } catch (MalformedURLException e) {
                LOG.error("Invalid URL: {}", e.getMessage());
                throw new RuntimeException(e);
            }
    }

    private static List<RepoUnit> setRepoUnits(Location location, DeliveryReport deliveryReport) {
        List<RepoUnit> repoUnits = new ArrayList<>();
        for (Unit unit : location.getUnit()) {
            RepoUnit repoUnit = new RepoUnit();
            repoUnit.setId(unit.getId());
            repoUnit.setVersion(deliveryReport.getVersion());
            repoUnits.add(repoUnit);
        }
        return repoUnits;
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
     * @param targetData TargetData
     * @return Set
     */
    public static Set<DeliveryReport> getReportData(TargetData targetData) {
        List<DeliveryReport> deliveryReportData;
        TargetDetails targetDetails = targetData.getTargetDetails();
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
}