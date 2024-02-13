package in.adarshr.targetgen.build;

import in.adarshr.targetgen.build.TargetBuilder;
import in.adarshr.targetgen.dto.TargetVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import output.targetgen.adarshr.in.output.Target;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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