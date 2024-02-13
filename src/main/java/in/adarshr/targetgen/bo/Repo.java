package in.adarshr.targetgen.bo;

import lombok.Data;

import java.util.Objects;

/**
 * This class is used to store the repo information
 */
@Data
public class Repo {
    private String group;
    private String artifact;
    private String location;
    private String version;

    public String toString() {
        return "Repo: " + group + ":" + artifact;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Repo repo = (Repo) o;
        return Objects.equals(group, repo.group) &&
                Objects.equals(artifact, repo.artifact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact);
    }
}


