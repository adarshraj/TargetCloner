package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Unit;
import in.adarshr.targetgen.dto.ComponentRepoVO;
import in.adarshr.targetgen.dto.TargetVO;
import input.targetgen.adarshr.in.input.ComponentInfo;
import input.targetgen.adarshr.in.input.Repo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains the utility methods for the application which are used to generate the target file.
 */
public class TargetUtils {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TargetUtils.class);

    /**
     * This method is used to build the targets
     *
     * @param targetVO TargetVO
     * @return Map<String, Target>
     */
    public static Set<in.adarshr.targetgen.bo.Repo> getJarUrls(TargetVO targetVO) {
        return getDistinctRepoList(targetVO.getComponentInfo());
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
                        repoBo.setLocation(createRepoUrl(repo, componentInfo));
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
     *
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
     *
     * @param componentInfo ComponentInfo
     * @return Map
     */
    public static Map<String, ComponentRepoVO> getComponentRepoMap(ComponentInfo componentInfo) {
        Map<String, ComponentRepoVO> componentRepoMap = new HashMap<>();
        if ((componentInfo == null) || (componentInfo.getComponents() == null)) {
            LOG.error("ComponentInfo is null");
            throw new RuntimeException("ComponentInfo is null");
        } else {
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
     *
     * @param repo Repo
     * @return String
     */
    private static String createRepoUrl(Repo repo, ComponentInfo componentInfo) {
        String group = repo.getGroup();
        String artifact = repo.getArtifact();
        String location = repo.getLocation();
        if (location != null && location.contains("$ARTIFACT$")) {
            String artifactUrlPattern = componentInfo.getArtifactUrlPattern();
            if (artifactUrlPattern != null && artifactUrlPattern.contains("/")) {
                location = location.replace("$ARTIFACT$", artifact.replaceAll("\\.", "/"));
            } else {
                location = location.replace("$ARTIFACT$", artifact);
            }
        }
        if (location != null && location.contains("$GROUP$")) {
            String groupUrlPattern = componentInfo.getGroupUrlPattern();
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
                location = location.replace("$VERSION$", componentInfo.getVersion());
            }
        }

        String repoUrl = location + "/" + "content.jar";
        LOG.info("Repo URL: {}", repoUrl);
        return repoUrl;
    }

    /**
     * Get target name
     *
     * @param componentName Component name
     * @param version       Version
     * @return String
     */
    public static String getTargetName(String componentName, String version) {
        return componentName + "_" + version;
    }

    /**
     * Check if the location is URL
     *
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
     *
     * @param reportLocation Report location
     * @param version        Version
     * @return String
     */
    public static String createDeliveryReportUrl(String reportLocation, String version) {
        if (reportLocation != null && reportLocation.contains("$VERSION$")) {
            reportLocation = reportLocation.replace("$VERSION$", version);
        }
        LOG.info("*** Delivery Report URL: {} ***", reportLocation);
        return reportLocation;
    }

    /**
     * Filter repo units.
     *
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
     *
     * @param unit Unit
     * @return boolean
     */
    private static boolean isUnitValid(Unit unit) {
        if (isValidUnitConditions(unit)) {
            return true;
        } else {
            LOG.info("Unit is not valid, filtering: {}", unit);
            return false;
        }
    }

    private static boolean isValidUnitConditions(Unit unit) {
        return unit.getSingleton().equals("true");
    }

    /**
     * This method is used to format the target name
     *
     * @param targetVO   TargetVO
     * @param targetName String
     * @return String
     */
    public static String getTargetNameFormatted(TargetVO targetVO, String targetName) {
        if (targetName.contains("$COMPONENT$")) {
            targetName = targetName.replace("$COMPONENT$", targetVO.getCurrentComponentName());
        }
        if (targetName.contains("$VERSION$")) {
            targetName = targetName.replace("$VERSION$", targetVO.getVersion());
        }
        return targetName;
    }
}
