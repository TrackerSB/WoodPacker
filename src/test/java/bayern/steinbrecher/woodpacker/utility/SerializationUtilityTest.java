package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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

/**
 * @author Stefan Huber
 * @since 0.1
 */
@Test(groups = {"serialization"})
public final class SerializationUtilityTest {
    private static final Logger LOGGER = Logger.getLogger(SerializationUtilityTest.class.getName());
    private static final Set<String> SERIALIZATION_VERSIONS = Set.of("v0.1");
    private static final BasePlank BASE_PLANK_REFERENCE
            = new BasePlank("reference", 86, 42, PlankGrainDirection.IRRELEVANT, PlankMaterial.UNDEFINED);
    private static final String BASE_PLANK_REFERENCE_PATH_PATTERN = "serializedBasePlank_%s.bin";

    private static <T> Optional<String> comparePublicValues(Class<T> typeDummy, T actual, T expected) {
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

    @Test
    public void checkBasePlankSerializationCurrentVersion() throws IOException, ClassNotFoundException {
        final byte[] serializedBasePlank = SerializationUtility.serialize(BASE_PLANK_REFERENCE);
        final BasePlank deserializedBasePlank = SerializationUtility.deserialize(serializedBasePlank);
        final Optional<String> failMessage
                = comparePublicValues(BasePlank.class, deserializedBasePlank, BASE_PLANK_REFERENCE);
        Assert.assertTrue(failMessage.isEmpty(),
                "Could not serialize and deserialize reference base plank with current application version: "
                        + failMessage.orElse("<Could not retrieve fail message>"));
    }

    @Test
    public void checkBasePlankDeserializationBackwardCompatibility()
            throws IOException, ClassNotFoundException, URISyntaxException {
        boolean allVersionsCompatible = true;
        for (final String version : SERIALIZATION_VERSIONS) {
            final Path versionFilePath = Path.of(
                    SerializationUtilityTest.class
                            .getResource(BASE_PLANK_REFERENCE_PATH_PATTERN.formatted(version))
                            .toURI());
            final byte[] serializedBasePlank = Files.readAllBytes(versionFilePath);
            final BasePlank deserializedBasePlank = SerializationUtility.deserialize(serializedBasePlank);
            final Optional<String> failMessage
                    = comparePublicValues(BasePlank.class, deserializedBasePlank, BASE_PLANK_REFERENCE);
            if (failMessage.isPresent()) {
                allVersionsCompatible = false;
                LOGGER.log(Level.INFO,
                        String.format(
                                "Deserialization not compatible with version %s (%s)", version, failMessage.get()));
            }
        }
        Assert.assertTrue(allVersionsCompatible, "Backwards compatibility of deserialization is broken");
    }
}
