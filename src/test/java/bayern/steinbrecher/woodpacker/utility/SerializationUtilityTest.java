package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionCriterion;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import bayern.steinbrecher.woodpacker.test.utility.ComparisonUtility;
import javafx.collections.FXCollections;
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
    private static final PlankProblem PLANK_PROBLEM_REFERENCE = new PlankProblem(){{
        setBasePlank(BASE_PLANK_REFERENCE);
        setCriterionWeight(PlankSolutionCriterion.BREATH_DIFFERENCES, 1);
        setCriterionWeight(PlankSolutionCriterion.NUM_PLANKS, 2);
        setCriterionWeight(PlankSolutionCriterion.ROW_SPACE_WASTE, 3);
        setRequiredPlanks(FXCollections.observableSet(
                new RequiredPlank("first", 11, 12, PlankGrainDirection.VERTICAL, "first comment"),
                new RequiredPlank("second", 14, 13, PlankGrainDirection.HORIZONTAL, "second comment")));
    }};
    private static final String PLANK_PROBLEM_REFERENCE_PATH_PATTERN = "serializedPlankProblem_%s.wp";

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

    @Test
    public void checkPlankProblemSerializationCurrentVersion() throws IOException, ClassNotFoundException {
        final byte[] serializedPlankProblem = SerializationUtility.serialize(PLANK_PROBLEM_REFERENCE);
        final PlankProblem deserializedPlankProblem = SerializationUtility.deserialize(serializedPlankProblem);
        final Optional<String> failMessage = ComparisonUtility.comparePublicValues(
                PlankProblem.class, deserializedPlankProblem, PLANK_PROBLEM_REFERENCE, "getProposedSolution");
        Assert.assertTrue(failMessage.isEmpty(),
                "Could not serialize and deserialize reference plank problem with current application version: "
                        + failMessage.orElse("<Could not retrieve fail message>"));
    }

    @Test
    public void checkPlankProblemDeserializationBackwardCompatibility()
            throws IOException, ClassNotFoundException, URISyntaxException {
        boolean allVersionsCompatible = true;
        for (final String version : SERIALIZATION_VERSIONS) {
            final Path versionFilePath = Path.of(
                    SerializationUtilityTest.class
                            .getResource(PLANK_PROBLEM_REFERENCE_PATH_PATTERN.formatted(version))
                            .toURI());
            final byte[] serializedPlankProblem = Files.readAllBytes(versionFilePath);
            final PlankProblem deserializedPlankProblem = SerializationUtility.deserialize(serializedPlankProblem);
            final Optional<String> failMessage = ComparisonUtility.comparePublicValues(
                    PlankProblem.class, deserializedPlankProblem, PLANK_PROBLEM_REFERENCE, "getProposedSolution");
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
