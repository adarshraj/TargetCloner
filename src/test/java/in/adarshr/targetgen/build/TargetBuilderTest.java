package in.adarshr.targetgen.build;

import in.adarshr.targetgen.dto.TargetVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TargetBuilderTest {
    private TargetBuilder targetBuilder;
    private TargetVO targetVO;

    @BeforeEach
    void setUp() {
        targetBuilder = new TargetBuilder();
        targetVO = new TargetVO();
        // TODO: Initialize targetVO with necessary data
    }

    @Test
    void testBuildTargets() {
        // Map<String, Target> targets = targetBuilder.buildTargets(targetVO);
        // TODO: Add assertions to check the targets map
    }
}