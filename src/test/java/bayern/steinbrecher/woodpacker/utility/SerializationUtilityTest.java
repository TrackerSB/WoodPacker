package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import bayern.steinbrecher.woodpacker.test.utility.ComparisonUtility;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    @Test
    public void checkBasePlankSerializationCurrentVersion() throws IOException, ClassNotFoundException {
        final byte[] serializedBasePlank = SerializationUtility.serialize(BASE_PLANK_REFERENCE);
        final BasePlank deserializedBasePlank = SerializationUtility.deserialize(serializedBasePlank);
        final Optional<String> failMessage
                = ComparisonUtility.comparePublicValues(BasePlank.class, deserializedBasePlank, BASE_PLANK_REFERENCE);
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
            final Optional<String> failMessage = ComparisonUtility.comparePublicValues(
                    BasePlank.class, deserializedBasePlank, BASE_PLANK_REFERENCE);
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
