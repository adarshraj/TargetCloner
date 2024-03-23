package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.data.Target;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// NOSONAR
/**
 * This class provides utility methods for comparing target files. TODO: Add more details about the class. // NOSONAR
 */
public class CompareHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CompareHelper.class);
    private static final String FIELD_COMPARISON_FORMAT = "Field Name: %s - Object1: %s | Object2: %s";

    /**
     * Private constructor to prevent instantiation
     */
    private CompareHelper() {
    }

    /**
     * Compares the target files.
     *
     * @param stringTargetMap the string target map
     * @param targets         the targets
     */
    public static void compareTargetFiles(Map<String, Target> stringTargetMap, List<Target> targets) {
        Collection<Target> values = stringTargetMap.values();
        DiffBuilder<List<Target>> diffBuilder = new DiffBuilder<>(values.stream().toList(), targets, ToStringStyle.SIMPLE_STYLE);
        List<Diff<?>> diffs = diffBuilder.build().getDiffs();
        for (Object diff : diffs) {
            if (diff instanceof Diff) {
                LOG.info(diff.toString());//NOSONAR
            }
        }
    }

    /**
     * Compares the fields of two objects.
     *
     * @param obj1 the first object
     * @param obj2 the second object
     */
    @SuppressWarnings("unused")
    public void compareObjectFields(Object obj1, Object obj2) {
        Field[] fields = obj1.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true); //NOSONAR
                Object value1 = field.get(obj1);
                Object value2 = field.get(obj2);
                compareFieldValues(field, value1, value2);
            } catch (IllegalAccessException e) {
                LOG.error("Failed to compare object fields: {}", e.getMessage());
            }
        }
    }

    private void compareFieldValues(Field field, Object value1, Object value2) {
        if (value1 != null && value2 != null && !value1.equals(value2)) {
            logFieldComparison(field, value1, value2);
        }
    }

    private void logFieldComparison(Field field, Object value1, Object value2) {
        LOG.info(String.format(FIELD_COMPARISON_FORMAT, field.getName(), value1, value2)); //NOSONAR
    }
}

