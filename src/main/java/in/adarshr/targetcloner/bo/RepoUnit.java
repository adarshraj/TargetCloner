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
}
