package in.adarshr.targetgen.bo;

import input.targetgen.adarshr.in.input.ComponentInfo;
import lombok.Data;

import java.util.List;

@Data
public class TargetVO {
    private ComponentInfo componentInfo;
    private List<Unit> units;
    private List<ComponentRepo> componentRepos;
}
