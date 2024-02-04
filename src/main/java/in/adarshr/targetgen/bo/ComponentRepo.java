package in.adarshr.targetgen.bo;

import input.targetgen.adarshr.in.input.Component;
import input.targetgen.adarshr.in.input.Repo;
import lombok.Data;

import java.util.List;

@Data
public class ComponentRepo {
    private Component component;
    private List<Repo> repos;
}
