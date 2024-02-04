package in.adarshr.targetgen.build;

import in.adarshr.targetgen.bo.TargetVO;
import input.targetgen.adarshr.in.input.ComponentInfo;
import output.targetgen.adarshr.in.output.*;

public class TargetBuilder {
    public Target buildTarget(TargetVO targetVO) {
        return createTarget(targetVO);
    }
    private Target createTarget(TargetVO targetVO) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        ObjectFactory ObjectFactory = new ObjectFactory();
        Target target =  ObjectFactory.createTarget();
        target.setName(componentInfo.getTargetName());
        target.setIncludeMode(componentInfo.getIncludeMode());
        target.setSequenceNumber(componentInfo.getSequenceNumber());
        target.setTargetJRE(createTargetJRE(componentInfo));
        target.setEnvironment(createEnvironment(componentInfo));
        target.setLocations(createLocations(targetVO));
        return target;
    }

    private Locations createLocations(TargetVO targetVO) {
        Locations locations = new Locations();
        locations.getLocation().add(createLocation(targetVO));
        return locations;
    }

    private Location createLocation(TargetVO targetVO) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();

        Location location = new Location();
        location.setIncludeMode(componentInfo.getIncludeMode());
        location.setType(componentInfo.getTargetType());
        location.setRepository(createTargetRepository(targetVO));
        location.setIncludeAllPlatforms(componentInfo.getIncludeAllPlatforms());
        location.setIncludeConfigurePhase(componentInfo.getIncludeConfigurePhase());
        location.getUnit().add(createUnit(targetVO));
        return location;
    }

    private TargetRepository createTargetRepository(TargetVO targetVO) {
        ComponentInfo componentInfo = targetVO.getComponentInfo();
        TargetRepository targetRepository = new TargetRepository();
        targetRepository.setLocation(componentInfo.getTargetLocation());
        return targetRepository;
    }

    private Unit createUnit(TargetVO targetVO) {
        Unit unit = new Unit();
        unit.setId("org.eclipse.egit");
        unit.setVersion("5.11.0.202105071451-r");
        return unit;
    }

    private Environment createEnvironment(ComponentInfo componentInfo) {
        Environment environment = new Environment();
        environment.setNl(componentInfo.getEnvironment());
        return environment;
    }

    private TargetJRE createTargetJRE(ComponentInfo componentInfo) {
        TargetJRE targetJRE = new TargetJRE();
        targetJRE.setPath(componentInfo.getJrePath());
        targetJRE.setValue(componentInfo.getJreValue());
        return targetJRE;
    }

}
