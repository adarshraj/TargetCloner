package in.adarshr.targetcloner.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class is used to store the delivery report information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryReport {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DeliveryReport.class);

    private String status;
    private String group;
    private String artifact;
    private String version;
    private String classifier;
    private String extension;
    private boolean externalEntry;

    /**
     * Create a report object from a delimited string
     *
     * @param currentLine the current line
     * @param delimiter   the delimiter
     * @return the report object
     */
    public static DeliveryReport fromDelimitedString(String currentLine, String delimiter) {
        String[] parts = currentLine.split(delimiter);
        parts = Arrays.stream(parts).map(String::trim).toArray(String[]::new);
        if (parts.length < 4) {
            LOG.error("!!! Insufficient fields to create a Report object !!!");
            throw new IllegalArgumentException("!!! Insufficient fields to create a Report object !!!");
        }
        return new DeliveryReport(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], false);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryReport deliveryReport = (DeliveryReport) o;
        return group.equals(deliveryReport.group) &&
                artifact.equals(deliveryReport.artifact) &&
                version.equals(deliveryReport.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, artifact, version);
    }

    @Override
    public String toString() {
        return "Report: " + group + ":" + artifact + ":" + version;
    }
}
