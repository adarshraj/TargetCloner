package in.adarshr.targetcloner.build;

import in.adarshr.targetcloner.dto.TargetData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TargetBuilderTest {
    private TargetBuilder targetBuilder;
    private TargetData targetData;

    @BeforeEach
    void setUp() {
        targetBuilder = new TargetBuilder();
        targetData = new TargetData();
        // TODO: Initialize targetData with necessary data
    }

    @Test
    void testBuildTargets() {
        // Map<String, Target> targets = targetBuilder.buildTargets(targetData);
        // TODO: Add assertions to check the targets map
    }
}
