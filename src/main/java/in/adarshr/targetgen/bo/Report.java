package in.adarshr.targetgen.bo;

import lombok.Data;

import java.util.Arrays;

/**
 * This class is used to store the delivery report information
 */
@Data
public class Report {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Report.class);

    private String status;
    private String group;
    private String artifact;
    private String version;
    private String classifier;
    private String extension;

    //no-arg constructor
    public Report() {
    }

    public Report(String status, String group, String artifact, String version,
                  String classifier, String extension) {
        this.status = status;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
    }

    public static Report fromDelimitedString(String currentLine, String delimiter) {
        String[] parts = currentLine.split(delimiter);
        parts = Arrays.stream(parts).map(String::trim).toArray(String[]::new);
        LOG.info("Current Line To Parse: {}  Parts Created: {}", currentLine, parts);
        if (parts.length < 4) {
            LOG.error("Insufficient fields to create a Report object");
            throw new IllegalArgumentException("Insufficient fields to create a Report object");
        }
        return new Report(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }
}
