package in.adarshr.targetcloner.dto;

import in.adarshr.targetcloner.bo.DeliveryReport;
import in.adarshr.targetcloner.bo.RepoData;
import in.adarshr.targetcloner.bo.RepoUnit;
import in.adarshr.targetcloner.data.Target;
import in.adarshr.targetcloner.data.TargetDetails;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is used to store the target information
 */
@Data
public class TargetData {
    private String targetName;
    private String currentComponentName;
    private String version;
    private String targetSaveFormat;
    private TargetDetails targetDetails;
    private Map<String, DeliveryReport> deliveryReportMap;
    private List<Target> inputTargets;
    private Map<RepoData, List<RepoUnit>> repoUnitsMap;
    private Set<RepoData> repoDataUrlSet;
    private Map<String, Map<String, RepoData>> componentRepoDataMap;
}
