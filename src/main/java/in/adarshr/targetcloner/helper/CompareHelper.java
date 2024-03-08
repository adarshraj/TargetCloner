package in.adarshr.targetcloner.helper;

import in.adarshr.targetcloner.data.Target;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// NOSONAR
/**
 * This class provides utility methods for comparing target files. TODO: Add more details about the class. // NOSONAR
 */
public class CompareHelper {

    private static final Logger LOG = LoggerFactory.getLogger(CompareHelper.class);

    private CompareHelper() {
    }

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

}
