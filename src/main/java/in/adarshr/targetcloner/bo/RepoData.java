package in.adarshr.targetcloner.bo;

import lombok.Data;

import java.util.List;
import java.util.Objects;

/**
 * This class is used to store the repo information
 */
@Data
public class RepoData {
    private String group;
    private String artifact;
    private String location;
    private String version;
    private List<RepoUnit> repoUnits;

    public String toString() {
        return "Repo: " + group + ":" + artifact;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepoData repoData = (RepoData) o;
        return Objects.equals(group, repoData.group) &&
                Objects.equals(artifact, repoData.artifact) &&
                Objects.equals(version, repoData.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact, version);
    }
}


