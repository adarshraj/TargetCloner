package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.bo.Report;
import in.adarshr.targetgen.dto.TargetVO;
import input.targetgen.adarshr.in.input.ComponentInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ReportUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ReportUtils.class);
    private static final String ARTIFACT_PLACEHOLDER = "$ARTIFACT$";
    private static final String GROUP_PLACEHOLDER = "$GROUP$";
    private static final String VERSION_PLACEHOLDER = "$VERSION$";
    private static final String PATH_SEPARATOR = "/";

    /**
     * Get jar urls
     *
     * @param targetVO TargetVO
     * @return Set
     */
    public static Set<Repo> getJarUrls(TargetVO targetVO) {
        return getJarUrlsFromReport(targetVO);
    }

    /**
     * Get jar urls from report
     *
     * @param targetVO TargetVO
     * @return Set
     */
    public static Set<Repo> getJarUrlsFromReport(TargetVO targetVO) {
        LOG.info("Begin getting jar urls from report");
        Map<String, List<Repo>> repoMap = updateRepoMapWithLocation(targetVO);
        Set<Repo> jarUrls = new HashSet<>();
        repoMap.forEach((component, repoList) -> jarUrls.addAll(repoList));
        LOG.info("Jar urls from report: {}", jarUrls);
        return jarUrls;
    }

    /**
     * Get jar urls from report
     *
     * @param targetVO TargetVO
     * @return Set
     */
    public static Map<String, List<in.adarshr.targetgen.bo.Repo>>updateRepoMapWithLocation(TargetVO targetVO) {
        List<Report> deliveryReportData = targetVO.getDeliveryReportData();
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        Map<String, List<Repo>> repoMap = getRepoMapList(componentInfo);
        repoMap.forEach((component, repoList) -> repoList.forEach(repo -> {
            if (deliveryReportData != null) {
                deliveryReportData.forEach(report -> {
                    if (StringUtils.isNotEmpty(report.getGroup())
                            && StringUtils.isNotEmpty(report.getArtifact())
                            && StringUtils.isNotEmpty(report.getVersion())) {
                        if (report.getGroup().equals(repo.getGroup())
                                && report.getArtifact().equals(repo.getArtifact())) {
                            repo.setLocation(createRepoUrl(repo, report, componentInfo));
                        }
                    }
                });
            }
        }));

        return repoMap;
    }

    /**
     * Get repo map list
     *
     * @param componentInfo ComponentInfo
     * @return Map
     */
    public static Map<String, List<in.adarshr.targetgen.bo.Repo>> getRepoMapList(ComponentInfo componentInfo) {
        Map<String, List<in.adarshr.targetgen.bo.Repo>> repoMap = new HashMap<>();
        if ((componentInfo == null) || (componentInfo.getComponents() == null)) {
            LOG.error("ComponentInfo is null");
            throw new RuntimeException("ComponentInfo is null");
        } else {
            componentInfo.getComponents().getComponent().forEach(component -> {
                List<in.adarshr.targetgen.bo.Repo> repoStore = new ArrayList<>();
                if (component.getRepository() == null || component.getRepository().getRepos() == null) {
                    LOG.error("Repository is null for component: {}", component.getName());
                    throw new RuntimeException("Repository is null for component: " + component.getName());
                } else {
                    component.getRepository().getRepos().getRepo().forEach(repo -> {
                        in.adarshr.targetgen.bo.Repo repoBo = new in.adarshr.targetgen.bo.Repo();
                        repoBo.setArtifact(repo.getArtifact());
                        repoBo.setGroup(repo.getGroup());
                        repoBo.setVersion(repo.getVersion());
                        repoBo.setLocation(repo.getLocation());
                        repoStore.add(repoBo);
                    });
                    repoMap.put(component.getName(), repoStore);
                }
            });
        }
        return repoMap;
    }

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

    private static String createRepoUrl(Repo repo, Report report, ComponentInfo componentInfo) {
        LOG.info("Before repo URL: {} ", repo.getLocation());
        String group = repo.getGroup();
        String artifact = repo.getArtifact();
        String location = repo.getLocation();

        location = replacePlaceholders(location, ARTIFACT_PLACEHOLDER, artifact, componentInfo.getArtifactUrlPattern());
        location = replacePlaceholders(location, GROUP_PLACEHOLDER, group, componentInfo.getGroupUrlPattern());

        if (location != null && location.contains(VERSION_PLACEHOLDER)) {
            String version = (repo.getVersion() != null && !repo.getVersion().isEmpty()) ? repo.getVersion() : report.getVersion();
            LOG.info("Repo Version: {}, Report Version: {}, Final Version: {}", repo.getVersion(), report.getVersion(), version);
            location = location.replace(VERSION_PLACEHOLDER, version);
        }
        String repoUrl = location + "/" + "content.jar";
        LOG.info("After repo URL: {}", repoUrl);
        return repoUrl;
    }


    /**
     * Get report data from the delivery report file
     *
     * @param reportFileLocation Report file location
     * @param linesToSkip        Lines to skip
     * @return List of Report
     */
    public static List<Report> getReportData(String reportFileLocation, int linesToSkip, int sourceType) {
        List<Report> reports = new ArrayList<>();
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
                    Report report = Report.fromDelimitedString(line, ":");
                    reports.add(report);
                }
            }
            return reports;
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
            String reportFromUrl = reader.lines().skip(linesToSkip).collect(Collectors.joining("\n"));
            //LOG.info("Report from URL: {}", reportFromUrl);
            return reportFromUrl;
        }
    }

    public static List<Report> getReportData(ComponentInfo componentInfo) {
        List<Report> reportData;
        if (TargetUtils.isUrl(componentInfo.getReportLocation())) {
            String reportLocation = componentInfo.getReportLocation();
            String deliveryReportUrl = TargetUtils.createDeliveryReportUrl(reportLocation, componentInfo.getVersion());
            reportData = getReportData(deliveryReportUrl, 2, 1);
            LOG.info("*** Delivery Report Data from URL: ***");
        } else {
            reportData = getReportData(componentInfo.getReportLocation(), 2, 2);
            LOG.info("*** Delivery Report Data from File. ***");
        }
        return filterDeliveryReport(reportData);
    }

    /**
     * Filter delivery report
     *
     * @param reportData Report data
     * @return List
     */
    private static List<Report> filterDeliveryReport(List<Report> reportData) {
        return reportData.stream()
                .filter(report -> StringUtils.isNotEmpty(report.getExtension())
                        && report.getExtension().equalsIgnoreCase("zip"))
                .collect(Collectors.toList());
    }
}


