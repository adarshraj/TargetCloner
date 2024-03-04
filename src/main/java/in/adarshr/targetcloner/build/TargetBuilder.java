package in.adarshr.targetcloner.build;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.data.*;
import in.adarshr.targetcloner.dto.TargetData;
import in.adarshr.targetcloner.utils.TargetClonerUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static in.adarshr.targetcloner.constants.SeparatorConstants.EMPTY_STRING;
import static in.adarshr.targetcloner.constants.SeparatorConstants.FIELD_DELIMITER_UNDERSCORE;
import static in.adarshr.targetcloner.constants.TargetClonerConstants.*;

/**
 * This class is used to build the target
 */
public class TargetBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(TargetBuilder.class);

    /**
     * Get target name
     *
     * @param componentName Component name
     * @param version       Version
     * @return String
     */
    public static String getTargetName(String componentName, String version) {
        return componentName + FIELD_DELIMITER_UNDERSCORE+ version;
    }

    private static Optional<RepoUnit> getInputUnit(RepoData repoData, TargetData targetData, Unit unit) {
        Map<RepoData, List<RepoUnit>> repoUnitsMap = targetData.getRepoUnitsMap();
        List<RepoUnit> repoUnits = repoUnitsMap.get(repoData);
        for (RepoUnit repoUnit : repoUnits) {
            if (repoUnit.getId().equals(unit.getId())) {
                return Optional.of(repoUnit);
            }
        }
        return Optional.empty();
    }

    /**
     * This method is used to format the target name
     *
     * @param targetData TargetData
     * @param targetName String
     * @return String
     */
    public static String getTargetNameFormatted(TargetData targetData, String targetName) {
        if (targetName.contains(PLACEHOLDER_COMPONENT)) {
            targetName = targetName.replace(PLACEHOLDER_COMPONENT, targetData.getCurrentComponentName());
        }
        if (targetName.contains(PLACEHOLDER_VERSION)) {
            targetName = targetName.replace(PLACEHOLDER_VERSION, targetData.getVersion());
        }
        return targetName;
    }

    /**
     * This method is used to build the targets
     *
     * @param targetData TargetData
     * @return Map<String, Target>
     */
    public Map<String, Target> buildTargets(TargetData targetData) {
        Map<String, Target> outputTargets = new HashMap<>();
        for (Target inpTarget : targetData.getInputTargets()) {
            setTargetVOData(inpTarget, targetData);
            targetData.setTargetName(getTargetName(targetData.getCurrentComponentName(), targetData.getVersion()));
            targetData.setCurrentComponentName(targetData.getCurrentComponentName());
            Target target = createTarget(targetData, inpTarget);
            outputTargets.put(createTargetFileName(targetData), target);
        }
        return outputTargets;
    }

    private void setTargetVOData(Target inpTarget, TargetData targetData) {
        String[] componentVersion = inpTarget.getName().split(FIELD_DELIMITER_UNDERSCORE);
        targetData.setCurrentComponentName(componentVersion[0]);
        targetData.setVersion(targetData.getTargetDetails().getVersion());
    }

    /**
     * This method is used to create the target file name
     *
     * @param targetData TargetData
     * @return String
     */
    private String createTargetFileName(TargetData targetData) {
        String targetNameFormatted = getTargetNameFormatted(targetData, targetData.getTargetSaveFormat());
        return targetNameFormatted + TARGET_FILE_SUFFIX;
    }

    /**
     * This method is used to create the target
     *
     * @param targetData TargetData
     * @return Target
     */
    private Target createTarget(TargetData targetData, Target iTarget) {
        Target outTarget = null;
        for (Target inpTarget : targetData.getInputTargets()) {
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
                outTarget.setIncludeBundles(createIncludeBundles(inpTarget, targetData));
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
        Map<String, DeliveryReport> deliveryReportMap = targetData.getDeliveryReportMap();
        Locations locations = new Locations();
        if (inpTarget.getLocations() != null && CollectionUtils.isNotEmpty(inpTarget.getLocations().getLocation())) {
            List<Location> inputLocations = inpTarget.getLocations().getLocation();
            Map<String, Map<String, RepoData>> componentRepoDataMap = targetData.getComponentRepoDataMap();
            for (Location inpLocation : inputLocations) {
                String inputLocationUrl = inpLocation.getRepository().getLocation();
                Optional<String> repoLocation =
                        componentRepoDataMap.get(inpTarget.getName())
                                .keySet().stream().filter(oldLocationUrl -> filterUrl(inputLocationUrl).equals(oldLocationUrl)).findFirst();
                if (repoLocation.isPresent()) {
                    RepoData repoData = componentRepoDataMap.get(inpTarget.getName()).get(repoLocation.get());
                    if (repoData == null) {
                        LOG.error(">>> Repo data not found for target: {}", inpTarget.getName());
                        continue;
                    }
                    DeliveryReport deliveryReport = deliveryReportMap.get(TargetClonerUtil.deliveryReportKey(repoData.getGroup(),repoData.getArtifact(),repoData.getVersion()));
                    if (deliveryReport != null) {
                        locations.getLocation().add(createLocation(inpLocation, repoData, targetData));
                    }else{
                        LOG.error(">>> Delivery report not found for repoData: {}", repoData);
                    }
                }else{
                    LOG.error(">>> Repo location not found for target: {}", inpTarget.getName());
                }
            }
        }
        return locations;
    }

    /**
     * This method is used to filter the URL
     *
     * @param inpRepoLocation String
     * @return CharSequence
     */
    private CharSequence filterUrl(String inpRepoLocation) {
        if (inpRepoLocation.contains(CONTENT_JAR)) {
            return inpRepoLocation.replace(CONTENT_JAR, EMPTY_STRING);
        }
        return inpRepoLocation;
    }

    /**
     * This method is used to create the location
     *
     * @return Location
     */
    private Location createLocation(Location inputLocation, RepoData repoData, TargetData targetData) {
        Location outputLocation = new Location();
        outputLocation.setIncludeMode(inputLocation.getIncludeMode());
        outputLocation.setType(inputLocation.getType());
        outputLocation.setIncludeAllPlatforms(inputLocation.getIncludeAllPlatforms());
        outputLocation.setIncludeConfigurePhase(inputLocation.getIncludeConfigurePhase());
        outputLocation.setRepository(createTargetRepository(repoData));

        List<Unit> inputLocationUnits = inputLocation.getUnit();
        for (Unit unit : inputLocationUnits) {
            Optional<RepoUnit> unitBo = getInputUnit(repoData, targetData, unit);
            if (unitBo.isPresent()) {
                RepoUnit boRepoUnit = unitBo.get();
                outputLocation.getUnit().add(createUnit(boRepoUnit));
            }
        }

        return outputLocation;
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
        return location.replace(CONTENT_JAR, EMPTY_STRING);
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
        if (inputTarget.getEnvironment() != null) {
            Environment inputTargetEnvironment = inputTarget.getEnvironment();
            environment.setNl(inputTargetEnvironment.getNl());
            environment.setArch(inputTargetEnvironment.getArch());
            environment.setOs(inputTargetEnvironment.getOs());
            environment.setWs(inputTargetEnvironment.getWs());
        }
        return environment;
    }

    /**
     * This method is used to create the include bundles
     *
     * @return IncludeBundles
     */
    private IncludeBundles createIncludeBundles(Target inpTarget, TargetData targetData) {
        IncludeBundles outIncludeBundles = null;
        IncludeBundles inpIncludeBundles = inpTarget.getIncludeBundles();
        Map<RepoData, List<RepoUnit>> repoUnitsMap = targetData.getRepoUnitsMap();

        //Create a single map of repo units with Unit ID as key and Unit as value from repoUnitsMap
        //handle key collison by taking the latest value
        Map<String, RepoUnit> unitMap = repoUnitsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toMap(RepoUnit::getId, repoUnit -> repoUnit, (repoUnit1, repoUnit2) -> repoUnit2));

        //Iterate through the input include bundles, check in the unitMap and create the output include bundles
        if (inpIncludeBundles != null && CollectionUtils.isNotEmpty(inpIncludeBundles.getPlugin())) {
            outIncludeBundles = new IncludeBundles();
            for (Plugin plugin : inpIncludeBundles.getPlugin()) {
                if (unitMap.containsKey(plugin.getId())) {
                    RepoUnit unit = unitMap.get(plugin.getId());
                    Plugin outPlugin = new Plugin();
                    outPlugin.setId(unit.getId());
                    if (plugin.getVersion() != null) {
                        outPlugin.setVersion(unit.getVersion());
                    }
                    outIncludeBundles.getPlugin().add(outPlugin);
                }
            }
        }
        return outIncludeBundles;
    }
}
