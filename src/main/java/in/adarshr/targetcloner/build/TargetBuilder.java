package in.adarshr.targetcloner.build;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.data.*;
import in.adarshr.targetcloner.dto.TargetData;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * This class is used to build the target
 */
public class TargetBuilder {

    /**
     * This method is used to build the targets
     *
     * @param targetData TargetData
     * @return Map<String, Target>
     */
    public Map<String, Target> buildTargets(TargetData targetData) {
        Map<String, Target> outputTargets = new HashMap<>();
        for(Target inpTarget: targetData.getTargets()){
            setTargetVOData(inpTarget, targetData);
            targetData.setTargetName(getTargetName(targetData.getCurrentComponentName(), targetData.getVersion()));
            targetData.setCurrentComponentName(inpTarget.getName());
            Target target = createTarget(targetData, inpTarget);
            outputTargets.put(createTargetFileName(targetData), target);
        }
        return outputTargets;
    }

    private void setTargetVOData(Target inpTarget, TargetData targetData) {
        String[] componentVersion = inpTarget.getName().split("_");
        targetData.setCurrentComponentName(componentVersion[0]);
        targetData.setVersion(targetData.getTargetDetails().getVersion());
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
     * This method is used to create the target file name
     *
     * @param targetData TargetData
     * @return String
     */
    private String createTargetFileName(TargetData targetData) {
        String targetNameFormatted = getTargetNameFormatted(targetData, targetData.getTargetSaveFormat());
        return targetNameFormatted + ".target";
    }

    /**
     * This method is used to create the target
     *
     * @param targetData TargetData
     * @return Target
     */
    private Target createTarget(TargetData targetData, Target iTarget) {
        Target outTarget = null;
        for (Target inpTarget : targetData.getTargets()) {
            if (inpTarget.getName().equals(iTarget.getName())) {
                ObjectFactory ObjectFactory = new ObjectFactory();
                outTarget = ObjectFactory.createTarget();
                outTarget.setName(createTargetName(targetData));
                outTarget.setIncludeMode(inpTarget.getIncludeMode());
                outTarget.setSequenceNumber(inpTarget.getSequenceNumber());
                outTarget.setLauncherArgs(inpTarget.getLauncherArgs());
                outTarget.setTargetJRE(inpTarget.getTargetJRE());
                outTarget.setEnvironment(createEnvironment(inpTarget));
                outTarget.setLocations(createLocations(inpTarget, targetData));
            }
        }
        return outTarget;
    }

    /**
     * This method is used to create the target name
     *
     * @param targetData TargetData
     * @return String
     */
    private String createTargetName(TargetData targetData) {
        String targetName = targetData.getTargetName();
        return getTargetNameFormatted(targetData, targetName);
    }


    /**
     * This method is used to create the locations
     *
     * @return Locations
     */
    private Locations createLocations(Target inpTarget, TargetData targetData) {
        Set<DeliveryReport> deliveryReports = targetData.getDeliveryReports();
        Locations locations = new Locations();
        if(inpTarget.getLocations() != null && CollectionUtils.isNotEmpty(inpTarget.getLocations().getLocation())) {
            List<Location> inputLocations = inpTarget.getLocations().getLocation();
            Set<RepoData> repoDataSet = targetData.getRepoDataUrlSet();
            for (Location inpLocation : inputLocations) {
                String inpRepoLocation = inpLocation.getRepository().getLocation();
                Optional<RepoData> repo = repoDataSet.stream()
                        .filter(repos -> repos.getLocation().contains(inpRepoLocation))
                        .findFirst();
                if(repo.isPresent()) {
                    RepoData repoData = repo.get();
                    Optional<DeliveryReport> reportData = deliveryReports.stream().filter(deliveryReport -> deliveryReport.getGroup().equals(repoData.getGroup())
                                    && deliveryReport.getArtifact().equals(repoData.getArtifact()))
                            .findFirst();
                    reportData.ifPresent(deliveryReport -> locations.getLocation().add(createLocation(inpLocation, repoData, targetData)));
                }
            }
        }
        return locations;
    }

    /**
     * This method is used to create the location
     *
     * @return Location
     */
    private Location createLocation(Location inpLocation, RepoData repoData, TargetData targetData) {
        Location outLocation = new Location();
        outLocation.setIncludeMode(inpLocation.getIncludeMode());
        outLocation.setType(inpLocation.getType());
        outLocation.setIncludeAllPlatforms(inpLocation.getIncludeAllPlatforms());
        outLocation.setIncludeConfigurePhase(inpLocation.getIncludeConfigurePhase());
        outLocation.setRepository(createTargetRepository(repoData));

        List<Unit> locationUnits = inpLocation.getUnit();

        for (Unit unit: locationUnits) {
            Optional<RepoUnit> unitBo = targetData.getRepoUnitsMap().get(repoData).stream()
                    .filter(units -> units.getId().equals(unit.getId()))
                    .findFirst();
            if(unitBo.isPresent()) {
                RepoUnit boRepoUnit = unitBo.get();
                outLocation.getUnit().add(createUnit(boRepoUnit));
            }
        }

        return outLocation;
    }

    /**
     * This method is used to create the target repository
     *
     * @param currentRepoData Repo
     * @return RepositoryLocation
     */
    private RepositoryLocation createTargetRepository(RepoData currentRepoData) {
        RepositoryLocation repositoryLocation = new RepositoryLocation();
        repositoryLocation.setLocation(setLocationUrl(currentRepoData.getLocation()));
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
     * @param boRepoUnit in.adarshr.targetgen.bo.RepoUnit
     * @return RepoUnit
     */
    private Unit createUnit(RepoUnit boRepoUnit) {
        Unit unit = new Unit();
        unit.setId(boRepoUnit.getId());
        unit.setVersion(boRepoUnit.getVersion());
        return unit;
    }

    /**
     * This method is used to create the environment
     *
     * @return Environment
     */
    private Environment createEnvironment(Target inputTarget) {
        Environment environment = new Environment();
        if(inputTarget.getEnvironment() !=null ) {
            Environment inputTargetEnvironment = inputTarget.getEnvironment();
            environment.setNl(inputTargetEnvironment.getNl());
            environment.setArch(inputTargetEnvironment.getArch());
            environment.setOs(inputTargetEnvironment.getOs());
            environment.setWs(inputTargetEnvironment.getWs());
        }
        return environment;
    }


    /**
     * This method is used to format the target name
     *
     * @param targetData   TargetData
     * @param targetName String
     * @return String
     */
    public static String getTargetNameFormatted(TargetData targetData, String targetName) {
        if (targetName.contains("$COMPONENT$")) {
            targetName = targetName.replace("$COMPONENT$", targetData.getCurrentComponentName());
        }
        if (targetName.contains("$VERSION$")) {
            targetName = targetName.replace("$VERSION$", targetData.getVersion());
        }
        return targetName;
    }
}
