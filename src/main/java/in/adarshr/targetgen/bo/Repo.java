package in.adarshr.targetgen.bo;

import lombok.Data;

/**
 * This class is used to store the repo information
 */
@Data
public class Repo {
    private String group;
    private String artifact;
    private String location;

    public String toString() {
        return "Repo: " + group + ":" + artifact;
    }

    public int hashCode() {
        return group.hashCode() + artifact.hashCode();
    }
}


