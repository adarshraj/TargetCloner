package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.data.Location;
import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.dto.TargetData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CompareHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CompareHelper.class);

    public static void compareTargetFiles(Map<String, Target> stringTargetMap, List<Target> targets, TargetData targetData) {
        for (Target target : targets) {
            Map<String, RepoData> repoMap = targetData.getComponentRepoDataMap().get(target.getName());
            String[] tarName = target.getName().split("_");
            for (Map.Entry<String, Target> entry : stringTargetMap.entrySet()) {
                if (tarName[0].contains(entry.getKey())) {
                    List<Location> inputTargetLocation = target.getLocations().getLocation();
                    List<Location> createdTargetLocations = entry.getValue().getLocations().getLocation();
                    //location content will be different but want to compare the locations.getUnits same for both
                    for (Location inputLocation : inputTargetLocation) {
                        for (Location createdLocation : createdTargetLocations) {
                            // loop repoMap
                            for (Map.Entry<String, RepoData> repoEntry : repoMap.entrySet()) {
                                if (repoEntry.getKey().contains(inputLocation.getRepository().getLocation())) {
                                    //compare the repoData
                                    RepoData inputRepoData = repoEntry.getValue();
                                    if (createdLocation.getRepository().getLocation().contains(inputRepoData.getLocation())) {
                                        //compare the unit size first then compare the unit ids
                                        if (inputLocation.getUnit().size() == createdLocation.getUnit().size()) {
                                            for (int i = 0; i < inputLocation.getUnit().size(); i++) {
                                                if (createdLocation.getUnit().get(i).getId().contains(createdLocation.getUnit().get(i).getId())) {
                                                    LOG.info("Unit id and size is same");
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}
