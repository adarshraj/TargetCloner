package in.adarshr.targetgen.dto;

import in.adarshr.targetgen.bo.Repo;
import lombok.Data;

import java.util.List;

/**
 * This class is used to store the component and its repos
 */
@Data
public class ComponentRepoVO {
    private String componentName;
    private List<Repo> repos;
}
