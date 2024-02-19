package in.adarshr.targetgen.build;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.dto.ComponentRepoVO;
import in.adarshr.targetgen.dto.TargetVO;
import in.adarshr.targetgen.utils.TargetUtils;
import input.targetgen.adarshr.in.input.ComponentInfo;
import input.targetgen.adarshr.in.input.SelectedGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import output.targetgen.adarshr.in.output.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to build the target
 */
public class TargetBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(TargetBuilder.class);

    /**
     * This method is used to build the targets
     *
     * @param targetVO TargetVO
     * @return Map<String, Target>
     */
    public Map<String, Target> buildTargets(TargetVO targetVO) {
        Map<String, Target> targets = new HashMap<>();
        Map<String, ComponentRepoVO> componentRepoMap = targetVO.getComponentRepoMap();
        componentRepoMap.forEach((componentName, componentRepo) -> {
            targetVO.setTargetName(TargetUtils.getTargetName(componentName, targetVO.getVersion()));
            targetVO.setCurrentComponentName(componentName);
            Target target = createTarget(targetVO);
            targets.put(createTargetFileName(targetVO), target);
        });

        return targets;
    }

    /**
     * This method is used to create the target file name
     *
     * @param targetVO TargetVO
     * @return String
     */
    private String createTargetFileName(TargetVO targetVO) {
        String targetNameFormatted = TargetUtils.getTargetNameFormatted(targetVO, targetVO.getTargetSaveFormat());
        return targetNameFormatted + ".target";
    }

    /**
     * This method is used to create the target
     *
     * @param targetVO TargetVO
     * @return Target
     */
    private Target createTarget(TargetVO targetVO) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        ObjectFactory ObjectFactory = new ObjectFactory();
        Target target = ObjectFactory.createTarget();
        target.setName(createTargetName(targetVO));
        target.setIncludeMode(componentInfo.getTargetIncludeMode());
        target.setSequenceNumber(componentInfo.getSequenceNumber());
        target.setLauncherArgs(createLauncherArgs(componentInfo));
        target.setTargetJRE(createTargetJRE(componentInfo));
        target.setEnvironment(createEnvironment(targetVO));
        target.setLocations(createLocations(targetVO));
        return target;
    }

    /**
     * This method is used to create the launcher args
     *
     * @param componentInfo ComponentInfo
     * @return LauncherArgs
     */
    private LauncherArgs createLauncherArgs(ComponentInfo componentInfo) {
        LauncherArgs launcherArgs = new LauncherArgs();
        launcherArgs.setProgramArgs(componentInfo.getProgramArguments());
        launcherArgs.setVmArgs(componentInfo.getVmArguments());
        return launcherArgs;
    }

    /**
     * This method is used to create the target name
     *
     * @param targetVO TargetVO
     * @return String
     */
    private String createTargetName(TargetVO targetVO) {
        String targetName = targetVO.getTargetName();
        return TargetUtils.getTargetNameFormatted(targetVO, targetName);
    }


    /**
     * This method is used to create the locations
     *
     * @param targetVO TargetVO
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
     *
     * @param targetVO TargetVO
     * @param repo     Repo
     * @return Location
     */
    private Location createLocation(TargetVO targetVO, in.adarshr.targetgen.bo.Repo repo) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        Map<String, List<Repo>> repoMapList = targetVO.getRepoMapList();

        Location location = new Location();
        location.setIncludeMode(componentInfo.getLocationIncludeMode());
        location.setType(componentInfo.getTargetLocationType());
        location.setIncludeAllPlatforms(componentInfo.getIncludeAllPlatforms());
        location.setIncludeConfigurePhase(componentInfo.getIncludeConfigurePhase());

        Map<Repo, List<in.adarshr.targetgen.bo.Unit>> repoUnitMap = targetVO.getRepoUnitMap();
        List<Repo> repos = repoMapList.get(targetVO.getCurrentComponentName());
        if (repos != null && !repos.isEmpty()) {
            repos.forEach(currentRepo -> {
                if (currentRepo != null && currentRepo.equals(repo)) {
                    location.setRepository(createTargetRepository(currentRepo));
                    if (repoUnitMap != null && repoUnitMap.get(currentRepo) != null) {
                        repoUnitMap.get(currentRepo).forEach(unit -> {
                                    if (filterUnit(unit, targetVO)) {
                                        location.getUnit().add(createUnit(unit));
                                    }else{
                                        LOG.info("Unit is not valid, filtering: {}", unit);

                                    }
                                }
                        );
                    }
                } else {
                    //LOG.error("Repo not found in the repo map list: {}", currentRepo);
                }
            });
        }

        return location;
    }

    /**
     * This method is used to filter the unit
     *
     * @param unit     in.adarshr.targetgen.bo.Unit
     * @param targetVO TargetVO
     * @return boolean
     */
    private boolean filterUnit(in.adarshr.targetgen.bo.Unit unit, TargetVO targetVO) {
        SelectedGroups includeGroups = targetVO.getComponentInfo().getIncludeGroups();
        SelectedGroups excludeGroups = targetVO.getComponentInfo().getExcludeGroups();
        if (includeGroups != null && includeGroups.getGroup() != null && !includeGroups.getGroup().isEmpty()) {
            return includeGroups.getGroup().stream().anyMatch(unit.getId()::contains);
        }
        if (excludeGroups != null && excludeGroups.getGroup() != null && !excludeGroups.getGroup().isEmpty()) {
            boolean isMatched = excludeGroups.getGroup().stream().anyMatch(unit.getId()::contains);
            return !isMatched;
        }
        return true;

    }

    /**
     * This method is used to create the target repository
     *
     * @param currentRepo Repo
     * @return RepositoryLocation
     */
    private RepositoryLocation createTargetRepository(Repo currentRepo) {
        RepositoryLocation repositoryLocation = new RepositoryLocation();
        repositoryLocation.setLocation(setLocationUrl(currentRepo.getLocation()));
        return repositoryLocation;
    }

    /**
     * This method is used to set the location url
     *
     * @param location String
     * @return String
     */
    private String setLocationUrl(String location) {
        return location.replace("content.jar", "");
    }


    /**
     * This method is used to create the unit
     *
     * @param boUnit in.adarshr.targetgen.bo.Unit
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
     *
     * @param targetVO TargetVO
     * @return Environment
     */
    private Environment createEnvironment(TargetVO targetVO) {
        Environment environment = new Environment();
        ComponentRepoVO componentRepoVO = targetVO.getComponentRepoMap().get(targetVO.getCurrentComponentName());
        input.targetgen.adarshr.in.input.Environment environmentBo = componentRepoVO.getComponent().getEnvironment();
        environment.setNl(environmentBo.getNl());
        environment.setArch(ArchitectureType.fromValue(environmentBo.getArch()));
        environment.setOs(OperatingSystem.fromValue(environmentBo.getOs()));
        environment.setWs(environmentBo.getWs());
        return environment;
    }

    /**
     * This method is used to create the target JRE
     *
     * @param componentInfo ComponentInfo
     * @return TargetJRE
     */
    private TargetJRE createTargetJRE(ComponentInfo componentInfo) {
        TargetJRE targetJRE = new TargetJRE();
        targetJRE.setPath(componentInfo.getJrePath());
        return targetJRE;
    }
}
