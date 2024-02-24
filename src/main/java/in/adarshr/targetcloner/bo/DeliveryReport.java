package in.adarshr.targetcloner.bo;

import lombok.Data;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is used to store the delivery report information
 */
@Data
public class DeliveryReport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeliveryReport.class);

    private String status;
    private String group;
    private String artifact;
    private String version;
    private String classifier;
    private String extension;

    //no-arg constructor
    public DeliveryReport() {
    }

    public DeliveryReport(String status, String group, String artifact, String version,
                          String classifier, String extension) {
        this.status = status;
        this.group = group;
        this.artifact = artifact;
        this.version = version;
        this.classifier = classifier;
        this.extension = extension;
    }

    public static DeliveryReport fromDelimitedString(String currentLine, String delimiter) {
        String[] parts = currentLine.split(delimiter);
        parts = Arrays.stream(parts).map(String::trim).toArray(String[]::new);
        if (parts.length < 4) {
            LOG.error("Insufficient fields to create a Report object");
            throw new IllegalArgumentException("Insufficient fields to create a Report object");
        }
        return new DeliveryReport(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryReport deliveryReport = (DeliveryReport) o;
        return group.equals(deliveryReport.group) &&
                artifact.equals(deliveryReport.artifact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact);
    }

    @Override
    public String toString() {
        return "Report: " + group + ":" + artifact;
    }
}
