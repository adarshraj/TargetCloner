package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.ComponentRepo;
import input.targetgen.adarshr.in.input.ComponentInfo;
import input.targetgen.adarshr.in.input.Repo;

import java.util.*;

public class TargetUtils {
    public static List<String> getJarUrls(ComponentInfo componentInfo) {
        List<ComponentRepo> componentRepos = getComponentRepos(componentInfo);
        Set<String> repoUrls = getRepoUrls(componentRepos);
        List<String> jarUrls = new ArrayList<>();
        repoUrls.forEach(repoUrl -> jarUrls.add(repoUrl + "content.jar"));
        return jarUrls;
    }

    private static Map<String, Map<Repo, String>> getRepoMap(ComponentInfo componentInfo) {
        Map<String, Map<Repo, String>> repoMap = new HashMap<>();
        componentInfo.getComponents().getComponent().forEach(component -> {
            Map<Repo, String> repoStringMap = new HashMap<>();
            component.getRepository().getRepos().getRepo().forEach(repo -> {
                String repoUrl = createRepoUrl(repo);
                repoStringMap.put(repo, repoUrl);
            });
            repoMap.put(component.getName(), repoStringMap);
        });
        return repoMap;
    }

    //create distinct repo. means no duplicate repo
    public static Map<Repo, String> getDistinctRepoMap(ComponentInfo componentInfo) {
        Map<String, Map<Repo, String>> repoMap = getRepoMap(componentInfo);
        Map<Repo, String> distinctRepoMap = new HashMap<>();
        repoMap.forEach((component, repoStringMap) -> repoStringMap.forEach((repo, repoUrl) -> {
            if (!distinctRepoMap.containsKey(repo)) {
                distinctRepoMap.put(repo, repoUrl);
            }
        }));
        return distinctRepoMap;
    }

    public static List<ComponentRepo> getComponentRepos(ComponentInfo componentInfo) {
        List<ComponentRepo> componentRepos = new ArrayList<>();
        componentInfo.getComponents().getComponent().forEach(component -> {
            ComponentRepo componentRepo = new ComponentRepo();
            componentRepo.setComponent(component);
            componentRepo.setRepos(component.getRepository().getRepos().getRepo());
            componentRepos.add(componentRepo);
        });
        return componentRepos;
    }

    public static Set<String> getRepoUrls(List<ComponentRepo> componentRepos) {
        Set<String> repoUrls = new HashSet<>();
        componentRepos.forEach(componentRepo -> componentRepo.getRepos().forEach(repo -> {
            String repoUrl = createRepoUrl(repo);
            repoUrls.add(repoUrl);
        }));
        return repoUrls;
    }

    public static String createRepoUrl(Repo repo) {
        String group = repo.getGroup();
        String artifact = repo.getArtifact();
        String location = repo.getLocation();
        if(location != null && location.contains("<ARTIFACT>")){
            location = location.replace("<ARTIFACT>", artifact);
        }
        if(location != null && location.contains("<GROUP>")){
            location = location.replace("<GROUP>", group);
        }
        return location;
    }

}
