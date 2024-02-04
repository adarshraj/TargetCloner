package in.adarshr.targetgen.build;

import in.adarshr.targetgen.bo.TargetVO;
import output.targetgen.adarshr.in.output.*;

public class TargetBuilder {
    public Target buildTarget(TargetVO targetVO) {
        return createTarget(targetVO);
    }
    private Target createTarget(TargetVO targetVO) {
        ObjectFactory ObjectFactory = new ObjectFactory();
        Target target =  ObjectFactory.createTarget();
        target.setName("Test");
        target.setIncludeMode("include");
        target.setSequenceNumber("1.0");
        target.setTargetJRE(createTargetJRE());
        target.setEnvironment(createEnvironment());
        target.setLocations(createLocations());
        return target;
    }

    private Locations createLocations() {
        Locations locations = new Locations();
        locations.getLocation().add(createLocation());
        return locations;
    }

    private Location createLocation() {
        Location location = new Location();
        location.setIncludeMode("include");
        location.setType("dir");
        location.setRepository(createTargetRepository());
        location.setIncludeAllPlatforms("true");
        location.setIncludeConfigurePhase("true");
        location.getUnit().add(createUnit());
        return location;
    }

    private TargetRepository createTargetRepository() {
        TargetRepository targetRepository = new TargetRepository();
        targetRepository.setLocation("https://download.eclipse.org/egit/updates-6.7/");
        return targetRepository;
    }

    private Unit createUnit() {
        Unit unit = new Unit();
        unit.setId("org.eclipse.egit");
        unit.setVersion("5.11.0.202105071451-r");
        return unit;
    }

    private Environment createEnvironment() {
        Environment environment = new Environment();
        environment.setNl("en_US");
        return environment;
    }

    private TargetJRE createTargetJRE() {
        TargetJRE targetJRE = new TargetJRE();
        targetJRE.setPath("C:\\Program Files\\Java\\jdk-11.0.11");
        targetJRE.setValue("11.0.11");
        return targetJRE;
    }

}
