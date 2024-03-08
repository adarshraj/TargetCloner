package in.adarshr.targetcloner.utils;

import in.adarshr.targetcloner.data.TargetDetails;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * This class contains tests for the TargetClonerUtil class, specifically the getTargetDetails method.
 */
@MockitoSettings(strictness = Strictness.LENIENT)
class TargetClonerUtilUnitTest {

    @Mock
    File mockFile;

    @Test
    void testGetTargetDetailsValidInput() {
        when(mockFile.exists()).thenReturn(true);
        TargetDetails details = TargetClonerUtil.getTargetDetails("input/appdata/TargetCloner.xml", "schema/TargetCloner.xsd");
        assertNotNull(details);
        //assertEquals("4.30.0", details.getVersion());
    }

    @Test
    void testGetTargetDetailsInvalidInput() {
        when(mockFile.exists()).thenReturn(false);
        TargetDetails targetDetails = TargetClonerUtil.getTargetDetails("invalidInput.xml", "validSchema.xsd");
        assertNull(targetDetails);
    }

    @Test
    void testGetTargetDetailsMalformedInput() {
        when(mockFile.exists()).thenReturn(true);
        TargetDetails targetDetails = TargetClonerUtil.getTargetDetails("malformedInput.xml", "validSchema.xsd");
        assertNull(targetDetails);
    }

    @Test
    void testGetTargetDetailsInvalidSchema() {
        when(mockFile.exists()).thenReturn(true);
        TargetDetails targetDetails = TargetClonerUtil.getTargetDetails("validInput.xml", "invalidSchema.xsd");
        assertNull(targetDetails);
    }
}