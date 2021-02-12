package bayern.steinbrecher.woodpacker.test.utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ComparisonUtility {
    private static final Logger LOGGER = Logger.getLogger(ComparisonUtility.class.getName());

    private ComparisonUtility() {
        throw new UnsupportedOperationException("Construction of instances is prohibited");
    }

    public static <T> Optional<String> comparePublicValues(Class<T> typeDummy, T actual, T expected) {
        final Set<Method> getterMethods = Stream.of(typeDummy.getMethods())
                .filter(m -> {
                    final String methodName = m.getName()
                            .toLowerCase(Locale.ROOT);
                    return methodName.startsWith("get") || methodName.startsWith("is");
                })
                .filter(m -> m.getParameterCount() <= 0)
                .collect(Collectors.toSet());
        final List<Method> failedComparisons = new ArrayList<>();
        for (final Method getterMethod : getterMethods) {
            try {
                final Object actualValue = getterMethod.invoke(actual);
                final Object expectedValue = getterMethod.invoke(expected);
                if (!actualValue.equals(expectedValue)) {
                    failedComparisons.add(getterMethod);
                }
            } catch (IllegalAccessException | InvocationTargetException ex) {
                LOGGER.log(Level.WARNING,
                        String.format("Could not verify equality of %s for %s",
                                typeDummy.getName(), getterMethod.getName()), ex);
            }
        }
        String failMessage;
        if (failedComparisons.isEmpty()) {
            failMessage = null;
        } else {
            final StringJoiner joiner = new StringJoiner(
                    ", ", "Comparison for following getter methods failed:", "");
            for (final Method failedComparison : failedComparisons) {
                joiner.add(failedComparison.getName());
            }
            failMessage = joiner.toString();
        }
        return Optional.ofNullable(failMessage);
    }
}
