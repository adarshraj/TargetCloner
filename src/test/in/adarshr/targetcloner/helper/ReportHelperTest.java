package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.bo.DeliveryReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class ReportHelperTest {
    private DeliveryReport deliveryReport;

    @BeforeEach
    public void setup() {
        deliveryReport = Mockito.mock(DeliveryReport.class);
    }

    @Test
    public void testCreateDeliveryReportUrl() {
        // Setup your mocks and expected results
        when(deliveryReport.getGroup()).thenReturn("group");
        when(deliveryReport.getArtifact()).thenReturn("artifact");
        when(deliveryReport.getVersion()).thenReturn("1.0.0");

        String deliveryReportUrl = "http://localhost:8081/artifactory/group/artifact/$VERSION$/artifact-$VERSION$.xml";
        String inputLocation = "1.0.0";
        String formattedUrl = "http://localhost:8081/artifactory/group/artifact/1.0.0/artifact-1.0.0.xml";

        // Call the method under test
        String result = ReportHelper.createDeliveryReportUrl(deliveryReportUrl, inputLocation);

        // Assert the result
        assertEquals(formattedUrl, result);
    }
}