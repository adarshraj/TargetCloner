package in.adarshr.targetgen.dto;

import in.adarshr.targetgen.bo.Repo;
import in.adarshr.targetgen.bo.Report;
import in.adarshr.targetgen.bo.Unit;
import input.targetgen.adarshr.in.input.ComponentInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TargetVO {
    private String targetName;
    private String currentComponentName;
    private ComponentInfo componentInfo;
    private Map<Repo, List<Unit>> repoUnitMap;
    private Map<String, ComponentRepoVO> componentRepoMap;
    private List<Report> deliveryReportData;
    private Map<String, List<Repo>> repoMapList;
}
