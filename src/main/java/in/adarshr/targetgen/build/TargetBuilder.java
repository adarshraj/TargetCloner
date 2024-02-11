package in.adarshr.targetgen.build;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.dto.ComponentRepoVO;
import in.adarshr.targetgen.dto.TargetVO;
import in.adarshr.targetgen.utils.TargetUtils;
import input.targetgen.adarshr.in.input.ComponentInfo;
import output.targetgen.adarshr.in.output.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to build the target
 */
public class TargetBuilder {

    /**
     * This method is used to build the targets
     * @param targetVO  TargetVO
     * @return Map<String, Target>
     */
    public Map<String, Target> buildTargets(TargetVO targetVO) {
        Map<String, Target> targets = new HashMap<>();
        Map<String, ComponentRepoVO> componentRepoMap = targetVO.getComponentRepoMap();
        componentRepoMap.forEach((componentName, componentRepo) -> {
            targetVO.setTargetName(TargetUtils.getTargetName(componentName, targetVO.getVersion()));
            targetVO.setCurrentComponentName(componentName);
            Target target = createTarget(targetVO);
            targets.put(targetVO.getTargetName(), target);
        });

        return targets;
    }

    /**
     * This method is used to create the target
     * @param targetVO  TargetVO
     * @return Target
     */
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

    /**
     * This method is used to create the locations
     * @param targetVO  TargetVO
     * @return Locations
     */
    private Locations createLocations(TargetVO targetVO) {
        Locations locations = new Locations();
        //Each location is a Repo
        ComponentRepoVO componentRepo = targetVO.getComponentRepoMap().get(targetVO.getCurrentComponentName());
        componentRepo.getRepos().forEach(repo -> locations.getLocation().add(createLocation(targetVO, repo)));
        return locations;
    }

    /**
     * This method is used to create the location
     * @param targetVO  TargetVO
     * @param repo  Repo
     * @return Location
     */
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
                repoUnitMap.get(currentRepo).forEach(unit -> location.getUnit().add(createUnit(unit))
                );
            }
        });
        return location;
    }

    /**
     * This method is used to create the target repository
     * @param repo  Repo
     * @return TargetRepository
     */
    private TargetRepository createTargetRepository(Repo repo) {
        TargetRepository targetRepository = new TargetRepository();
        targetRepository.setLocation(repo.getLocation());
        return targetRepository;
    }

    /**
     * This method is used to create the unit
     * @param boUnit  in.adarshr.targetgen.bo.Unit
     * @return Unit
     */
    private Unit createUnit(in.adarshr.targetgen.bo.Unit boUnit) {
        Unit unit = new Unit();
        unit.setId(boUnit.getId());
        unit.setVersion(boUnit.getVersion());
        return unit;
    }

    /**
     * This method is used to create the environment
     * @param componentInfo  ComponentInfo
     * @return Environment
     */
    private Environment createEnvironment(ComponentInfo componentInfo) {
        Environment environment = new Environment();
        environment.setNl(componentInfo.getEnvironment());
        return environment;
    }

    /**
     * This method is used to create the target JRE
     * @param componentInfo  ComponentInfo
     * @return TargetJRE
     */
    private TargetJRE createTargetJRE(ComponentInfo componentInfo) {
        TargetJRE targetJRE = new TargetJRE();
        targetJRE.setPath(componentInfo.getJrePath());
        targetJRE.setValue(componentInfo.getJreValue());
        return targetJRE;
    }
}
