package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Unit;
import in.adarshr.targetgen.dto.ComponentRepoVO;
import in.adarshr.targetgen.bo.Report;
import in.adarshr.targetgen.dto.TargetVO;
import input.targetgen.adarshr.in.input.ComponentInfo;
import input.targetgen.adarshr.in.input.Repo;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains the utility methods for the application which are used to generate the target file.
 */
public class TargetUtils {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TargetUtils.class);

    /**
     * Get jar urls
     * @param componentInfo ComponentInfo
     * @return Set
     */
    public static Set<in.adarshr.targetgen.bo.Repo> getJarUrls(ComponentInfo componentInfo) {
        return getDistinctRepoList(componentInfo);
    }

    /**
     * Get repo map list
     * @param componentInfo ComponentInfo
     * @return Map
     */
    public static Map<String, List<in.adarshr.targetgen.bo.Repo>> getRepoMapList(ComponentInfo componentInfo) {
        Map<String, List<in.adarshr.targetgen.bo.Repo>> repoMap = new HashMap<>();
        if((componentInfo == null) || (componentInfo.getComponents() == null)) {
            LOG.error("ComponentInfo is null");
            throw new RuntimeException("ComponentInfo is null");
        }else {
            componentInfo.getComponents().getComponent().forEach(component -> {
                List<in.adarshr.targetgen.bo.Repo> repoStore = new ArrayList<>();
                if(component.getRepository() == null || component.getRepository().getRepos() == null) {
                    LOG.error("Repository is null for component: {}", component.getName());
                    throw new RuntimeException("Repository is null for component: " + component.getName());
                }else {
                    component.getRepository().getRepos().getRepo().forEach(repo -> {
                        in.adarshr.targetgen.bo.Repo repoBo = new in.adarshr.targetgen.bo.Repo();
                        repoBo.setArtifact(repo.getArtifact());
                        repoBo.setGroup(repo.getGroup());
                        repoBo.setLocation(createRepoUrl(repo));
                        repoStore.add(repoBo);
                    });
                    repoMap.put(component.getName(), repoStore);
                }
            });
        }
        return repoMap;
    }

    /**
     * Get distinct repo list
     * @param componentInfo ComponentInfo
     * @return Set
     */
    private static Set<in.adarshr.targetgen.bo.Repo> getDistinctRepoList(ComponentInfo componentInfo) {
        Map<String, List<in.adarshr.targetgen.bo.Repo>> repoMapList = getRepoMapList(componentInfo);
        Set<in.adarshr.targetgen.bo.Repo> distinctRepoList = new HashSet<>();
        repoMapList.forEach((component, repoList) -> distinctRepoList.addAll(repoList));
        return distinctRepoList;
    }

    /**
     * Get component repo map
     * @param componentInfo ComponentInfo
     * @return Map
     */
    public static Map<String, ComponentRepoVO> getComponentRepoMap(ComponentInfo componentInfo) {
        Map<String, ComponentRepoVO> componentRepoMap = new HashMap<>();
        if((componentInfo == null) || (componentInfo.getComponents() == null)) {
            LOG.error("ComponentInfo is null");
            throw new RuntimeException("ComponentInfo is null");
        }else {
            componentInfo.getComponents().getComponent().forEach(component -> {
                ComponentRepoVO componentRepo = new ComponentRepoVO();
                componentRepo.setComponentName(component.getName());
                componentRepo.setComponent(component);
                componentRepo.setRepos(getRepoMapList(componentInfo).get(component.getName()));
                componentRepoMap.put(component.getName(), componentRepo);
            });
        }
        return componentRepoMap;
    }

    /**
     * Create repo URL
     * @param repo Repo
     * @return String
     */
    private static String createRepoUrl(Repo repo) {
        String group = repo.getGroup();
        String artifact = repo.getArtifact();
        String location = repo.getLocation();
        if(location != null && location.contains("$ARTIFACT$")){
            location = location.replace("$ARTIFACT$", artifact);
        }
        if(location != null && location.contains("$GROUP$")){
            location = location.replace("$GROUP$", group);
        }

        String repoUrl = location + "/" + "content.jar";
        LOG.info("Repo URL: {}", repoUrl);
        return repoUrl;
    }

    /**
     * Get report data from the delivery report file
     * @param reportFileLocation Report file location
     * @param linesToSkip Lines to skip
     * @return List of Report
     */
    public static List<Report> getReportData(String reportFileLocation, int linesToSkip, int sourceType) {
        List<Report> reports = new ArrayList<>();
        try {
            String reportFile;
            if(sourceType == 1) {
                reportFile = readFileFromUrl(reportFileLocation, linesToSkip);
            }else {
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
     * @param fileName File name
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
     * @param fileUrl File URL
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
     * Get target name
     * @param componentName Component name
     * @param version Version
     * @return String
     */
    public static String getTargetName(String componentName, String version) {
        return componentName + "_" + version;
    }

    /**
     * Check if the location is URL
     * @param location Location
     * @return boolean
     */
    public static boolean isUrl(String location) {
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
     * @param reportLocation Report location
     * @param version Version
     * @return String
     */
    public static String createDeliveryReportUrl(String reportLocation, String version) {
        if(reportLocation != null && reportLocation.contains("<VERSION>")){
            reportLocation = reportLocation.replace("<VERSION>", version);
        }
        return reportLocation;
    }

    /**
     * Filter repo units.
     * @param repoListMap Repo list map
     * @return Map
     */
    @SuppressWarnings("unused")
    public static Map<in.adarshr.targetgen.bo.Repo, List<Unit>> filterRepoUnits(Map<in.adarshr.targetgen.bo.Repo, List<Unit>> repoListMap) {
        Map<in.adarshr.targetgen.bo.Repo, List<Unit>> filteredRepoListMap = new HashMap<>();
        repoListMap.forEach((repo, units) -> {
            List<Unit> filteredUnits = units.stream()
                    .filter(TargetUtils::isUnitValid)
                    .collect(Collectors.toList());
            filteredRepoListMap.put(repo, filteredUnits);
        });
        return filteredRepoListMap;
    }

    /**
     * Check if the unit is valid
     * @param unit Unit
     * @return boolean
     */
    private static boolean isUnitValid(Unit unit) {
        if(isValidUnitConditions(unit)){
            return true;
        }else{
            LOG.info("Unit is not valid, filtering: {}", unit);
            return false;
        }
    }

    private static boolean isValidUnitConditions(Unit unit) {
       return unit.getSingleton().equals("true");
    }

    /**
     * This method is used to format the target name
     * @param targetVO  TargetVO
     * @param targetName  String
     * @return String
     */
    public static String getTargetNameFormatted(TargetVO targetVO, String targetName) {
        if(targetName.contains("$COMPONENT$")) {
            targetName = targetName.replace("$COMPONENT$", targetVO.getCurrentComponentName());
        }
        if(targetName.contains("$VERSION$")) {
            targetName = targetName.replace("$VERSION$", targetVO.getVersion());
        }
        return targetName;
    }
}
