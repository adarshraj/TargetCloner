package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.bo.Report;
import in.adarshr.targetgen.dto.TargetVO;
import input.targetgen.adarshr.in.input.ComponentInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ReportUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ReportUtils.class);


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
        List<Report> deliveryReportData = targetVO.getDeliveryReportData();
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        Map<String, List<Repo>> repoMap = getRepoMapList(componentInfo);
        Set<Repo> jarUrls = new HashSet<>();
        if (deliveryReportData != null) {
            deliveryReportData.forEach(report -> {
                if(StringUtils.isNotEmpty(report.getGroup())
                        && StringUtils.isNotEmpty(report.getArtifact())
                        && StringUtils.isNotEmpty(report.getVersion())) {
                    repoMap.forEach((component, repoList) -> repoList.forEach(repo -> {
                        if (report.getGroup().equals(repo.getGroup())
                                && report.getArtifact().equals(repo.getArtifact())) {
                            repo.setLocation(createRepoUrl(repo, report, componentInfo));
                            jarUrls.add(repo);
                        }
                    }));
                }
            });
        }
        LOG.info("Jar urls from report: {}", jarUrls);
        return jarUrls;
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

    private static final String ARTIFACT_PLACEHOLDER = "$ARTIFACT$";
    private static final String GROUP_PLACEHOLDER = "$GROUP$";
    private static final String VERSION_PLACEHOLDER = "$VERSION$";
    private static final String PATH_SEPARATOR = "/";

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
            location = location.replace(VERSION_PLACEHOLDER, version);
        }
        String repoUrl = location + "/" + "content.jar";
        LOG.info("After repo URL: {}", repoUrl);
        return repoUrl;
    }
}

