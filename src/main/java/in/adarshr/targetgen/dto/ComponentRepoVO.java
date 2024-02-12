package in.adarshr.targetgen.dto;

import in.adarshr.targetgen.bo.Repo;
import input.targetgen.adarshr.in.input.Component;
import lombok.Data;

import java.util.List;

/**
 * This class is used to store the component and its repos
 */
@Data
public class ComponentRepoVO {
    private String componentName;
    private Component component;
    private List<Repo> repos;
}
