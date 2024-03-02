package in.adarshr.targetcloner.bo;

import lombok.Data;

/**
 * This class is used to store the unit information
 */
@Data
public class RepoUnit {
    private String id;
    private String version;
    private String singleton;
    private String generation;

    public String toString() {
        return "RepoUnit: " + id + ":" + version;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RepoUnit repoUnit = (RepoUnit) o;
        return id.equals(repoUnit.id);
    }

    public int hashCode() {
        return id.hashCode();
    }
}
