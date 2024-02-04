package in.adarshr.targetgen.utils;

import in.adarshr.targetgen.bo.ComponentRepo;
import input.targetgen.adarshr.in.input.ComponentInfo;
import input.targetgen.adarshr.in.input.Repo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TargetUtils {
    public static List<String> getJarUrls(ComponentInfo componentInfo) {
        List<ComponentRepo> componentRepos = getComponentRepos(componentInfo);
        Set<String> repoUrls = getRepoUrls(componentRepos);
        List<String> jarUrls = new ArrayList<>();
        repoUrls.forEach(repoUrl -> {
            jarUrls.add(repoUrl + "content.jar");
        });
        return jarUrls;
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
        componentRepos.forEach(componentRepo -> {
            componentRepo.getRepos().forEach(repo -> {
                String repoUrl = createRepoUrl(repo);
                repoUrls.add(repoUrl);
            });
        });
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
