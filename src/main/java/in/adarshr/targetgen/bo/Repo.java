package in.adarshr.targetgen.bo;

import lombok.Data;

@Data
public class Repo {
    private String group;
    private String artifact;
    private String location;

    public String toString() {
        return "Repo: " + group + ":" + artifact;
    }
}


