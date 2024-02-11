package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.ComponentRepo;
import in.adarshr.targetgen.bo.Report;
import input.targetgen.adarshr.in.input.ComponentInfo;
import input.targetgen.adarshr.in.input.Repo;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TargetUtils {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TargetUtils.class);

    public static Set<in.adarshr.targetgen.bo.Repo> getJarUrls(ComponentInfo componentInfo) {
        return getDistinctRepoList(componentInfo);
    }

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

    private static Set<in.adarshr.targetgen.bo.Repo> getDistinctRepoList(ComponentInfo componentInfo) {
        Map<String, List<in.adarshr.targetgen.bo.Repo>> repoMapList = getRepoMapList(componentInfo);
        Set<in.adarshr.targetgen.bo.Repo> distinctRepoList = new HashSet<>();
        repoMapList.forEach((component, repoList) -> distinctRepoList.addAll(repoList));
        return distinctRepoList;
    }

    public Map<String, ComponentRepo> getComponentRepoMap(ComponentInfo componentInfo) {
        Map<String, ComponentRepo> componentRepoMap = new HashMap<>();
        if((componentInfo == null) || (componentInfo.getComponents() == null)) {
            LOG.error("ComponentInfo is null");
            throw new RuntimeException("ComponentInfo is null");
        }else {
            componentInfo.getComponents().getComponent().forEach(component -> {
                ComponentRepo componentRepo = new ComponentRepo();
                componentRepo.setComponent(component);
                componentRepo.setRepos(component.getRepository().getRepos().getRepo());
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
        if(location != null && location.contains("<ARTIFACT>")){
            location = location.replace("<ARTIFACT>", artifact);
        }
        if(location != null && location.contains("<GROUP>")){
            location = location.replace("<GROUP>", group);
        }

        return location + "/" +
                "content.jar";
    }

    /**
     * Get report data from the delivery report file
     * @param reportFileLocation Report file location
     * @param linesToSkip Lines to skip
     * @return List of Report
     */
    public static List<Report> getReportData(String reportFileLocation, int linesToSkip) {
        List<Report> reports = new ArrayList<>();
        try {
            String reportFile = readFileFromDirectory(reportFileLocation, linesToSkip);
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
    public static String readFileFromDirectory(String fileName, final int linesToSkip) throws IOException {
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
    public static String readFileFromUrl(String fileUrl, final int linesToSkip) throws IOException {
        URI uri = URI.create(fileUrl);
        URL url = uri.toURL();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
            return reader.lines().skip(linesToSkip).collect(Collectors.joining("\n"));
        }
    }
}
