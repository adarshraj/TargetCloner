package in.adarshr.targetcloner.constants;

public final class TargetClonerConstants {
    public static final String JAXB_PACKAGE = "in.adarshr.targetcloner.data";
    public static final String JAXB_SCHEMA = "https://in.adarshr.targetcloner.data/TargetCloner.xsd";
    public static final String CONTENT_JAR = "content.jar";
    public static final String CONTENT_JAR_WITH_SEPARATOR = "/content.jar";
    public static final String PLACEHOLDER_GROUP = "$GROUP$";
    public static final String PLACEHOLDER_ARTIFACT = "$ARTIFACT$";
    public static final String PLACEHOLDER_VERSION = "$VERSION$";
    public static final String PLACEHOLDER_COMPONENT = "$COMPONENT$";
    public static final String TARGET_FILE_SUFFIX = ".target";
    public static final String OUTPUT_DIRECTORY = "output";
    public static final String USER_DIRECTORY = "user.dir";
    /**
     * Private constructor to prevent instantiation
     */
    private TargetClonerConstants() {
    }
}
