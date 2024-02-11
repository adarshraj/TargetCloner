package in.adarshr.targetgen.build;

import in.adarshr.targetgen.dto.ComponentRepoVO;
import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.dto.TargetVO;
import input.targetgen.adarshr.in.input.ComponentInfo;
import output.targetgen.adarshr.in.output.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TargetBuilder {

    public List<Target> buildTargets(TargetVO targetVO) {
        List<Target> targets = new ArrayList<>();
        Map<String, ComponentRepoVO> componentRepoMap = targetVO.getComponentRepoMap();
        componentRepoMap.forEach((componentName, componentRepo) -> {
            targetVO.setCurrentComponentName(componentName);
            Target target = createTarget(targetVO);
            targets.add(target);
        });

        return targets;
    }
    private Target createTarget(TargetVO targetVO) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        ObjectFactory ObjectFactory = new ObjectFactory();
        Target target =  ObjectFactory.createTarget();
        target.setName(componentInfo.getTargetName());
        target.setIncludeMode(componentInfo.getIncludeMode());
        target.setSequenceNumber(componentInfo.getSequenceNumber());
        target.setTargetJRE(createTargetJRE(componentInfo));
        target.setEnvironment(createEnvironment(componentInfo));
        target.setLocations(createLocations(targetVO));
        return target;
    }

    private Locations createLocations(TargetVO targetVO) {
        Locations locations = new Locations();
        //Each location is a Repo
        ComponentRepoVO componentRepo = targetVO.getComponentRepoMap().get(targetVO.getCurrentComponentName());
        componentRepo.getRepos().forEach(repo -> locations.getLocation().add(createLocation(targetVO, repo)));
        return locations;
    }

    private Location createLocation(TargetVO targetVO, in.adarshr.targetgen.bo.Repo repo) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        Map<String, List<Repo>> repoMapList = targetVO.getRepoMapList();

        Location location = new Location();
        location.setIncludeMode(componentInfo.getIncludeMode());
        location.setType(componentInfo.getTargetType());
        location.setIncludeAllPlatforms(componentInfo.getIncludeAllPlatforms());
        location.setIncludeConfigurePhase(componentInfo.getIncludeConfigurePhase());

        Map<Repo, List<in.adarshr.targetgen.bo.Unit>> repoUnitMap = targetVO.getRepoUnitMap();
        repoMapList.get(targetVO.getCurrentComponentName()).forEach(currentRepo -> {
            if(currentRepo.equals(repo)) {
                location.setRepository(createTargetRepository(currentRepo));
                repoUnitMap.get(currentRepo).forEach(unit -> {
                            location.getUnit().add(createUnit(unit));
                        }
                );
            }
        });
        return location;
    }

    private TargetRepository createTargetRepository(Repo repo) {
        TargetRepository targetRepository = new TargetRepository();
        targetRepository.setLocation(repo.getLocation());
        return targetRepository;
    }

    private Unit createUnit(in.adarshr.targetgen.bo.Unit boUnit) {
        Unit unit = new Unit();
        unit.setId(boUnit.getId());
        unit.setVersion(boUnit.getVersion());
        return unit;
    }

    private Environment createEnvironment(ComponentInfo componentInfo) {
        Environment environment = new Environment();
        environment.setNl(componentInfo.getEnvironment());
        return environment;
    }

    private TargetJRE createTargetJRE(ComponentInfo componentInfo) {
        TargetJRE targetJRE = new TargetJRE();
        targetJRE.setPath(componentInfo.getJrePath());
        targetJRE.setValue(componentInfo.getJreValue());
        return targetJRE;
    }
}
