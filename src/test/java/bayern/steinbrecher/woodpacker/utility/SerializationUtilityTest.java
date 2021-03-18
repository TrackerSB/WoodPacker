package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.Plank;
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
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author Stefan Huber
 * @since 0.1
 */
@Test(groups = {"serialization"})
public final class SerializationUtilityTest {
    private static final String referenceFilePattern = "serialized%s%d.bin";

    @SuppressWarnings("unchecked")
    private <C extends Serializable> void checkSerializationForClass(
            final Set<Long> versions, final C reference, final Class<C> typeDummy, final String... methodsToIgnore)
            throws URISyntaxException, IOException, ClassNotFoundException {
        List<String> failMessages = new ArrayList<>();

        // Check given versions
        for (final long version : versions) {
            final String referenceFileName = String.format(referenceFilePattern, typeDummy.getSimpleName(), version);
            final Path referenceFilePath = Path.of(
                    SerializationUtilityTest.class.getResource(referenceFileName).toURI());
            final byte[] serializedObject = Files.readAllBytes(referenceFilePath);
            final C deserializedObject = SerializationUtility.deserialize(serializedObject);
            final Optional<String> failMessage
                    = ComparisonUtility.comparePublicValues(typeDummy, deserializedObject, reference, methodsToIgnore);
            failMessage.ifPresent(failMessages::add);
        }

        // Check whether current serialization and deserialization are inverse to each other
        final byte[] serializedObject = SerializationUtility.serialize(reference);
        final C deserialized = SerializationUtility.deserialize(serializedObject);
        ComparisonUtility.comparePublicValues(typeDummy, deserialized, reference, methodsToIgnore)
                .ifPresent(failMessages::add);

        Assert.assertTrue(failMessages.isEmpty(), String.join("\n", failMessages));
    }

    // Serialization of Plank
    private static final Set<Long> PLANK_SERIAL_VERSIONS = Set.of(1L);
    // FIXME An anonymous subclass may be a problem with serialization
    private static final Plank PLANK_REFERENCE = new Plank(
            "PlankReference", 15, 16, PlankGrainDirection.IRRELEVANT, "anonymous plank") {
    };

    @Test
    public void checkSerializationForPlanks() throws URISyntaxException, IOException, ClassNotFoundException {
        checkSerializationForClass(PLANK_SERIAL_VERSIONS, PLANK_REFERENCE, Plank.class);
    }

    // Serialization of BasePlank
    private static final Set<Long> BASE_PLANK_SERIAL_VERSIONS = Set.of(1L);
    private static final BasePlank BASE_PLANK_REFERENCE
            = new BasePlank("BasePlank reference", 86, 42, PlankGrainDirection.IRRELEVANT, PlankMaterial.OAK);

    @Test
    public void checkSerializationForBasePlanks() throws URISyntaxException, IOException, ClassNotFoundException {
        checkSerializationForClass(BASE_PLANK_SERIAL_VERSIONS, BASE_PLANK_REFERENCE, BasePlank.class);
    }

    // Serialization of RequiredPlank
    private static final Set<Long> REQUIRED_PLANK_SERIAL_VERSIONS = Set.of(1L);
    private static final RequiredPlank REQUIRED_PLANK_REFERENCE = new RequiredPlank(
            "RequiredPlank reference", 17, 18, PlankGrainDirection.VERTICAL, "some required plank"
    );

    @Test
    public void checkSerializationForRequiredPlanks() throws URISyntaxException, IOException, ClassNotFoundException {
        checkSerializationForClass(REQUIRED_PLANK_SERIAL_VERSIONS, REQUIRED_PLANK_REFERENCE, RequiredPlank.class);
    }

    // Serialization of PlankProblem
    private static final Set<Long> PLANK_PROBLEM_SERIAL_VERSIONS = Set.of(1L);
    // FIXME An anonymous subclass may be a problem with serialization
    private static final PlankProblem PLANK_PROBLEM_REFERENCE = new PlankProblem() {{
        setBasePlank(BASE_PLANK_REFERENCE);
        setCriterionWeight(PlankSolutionCriterion.BREATH_DIFFERENCES, 1);
        setCriterionWeight(PlankSolutionCriterion.NUM_PLANKS, 2);
        setCriterionWeight(PlankSolutionCriterion.ROW_SPACE_WASTE, 3);
        setRequiredPlanks(FXCollections.observableSet(
                new RequiredPlank("first", 11, 12, PlankGrainDirection.VERTICAL, "first comment"),
                new RequiredPlank("second", 14, 13, PlankGrainDirection.HORIZONTAL, "second comment")));
    }};

    @Test
    public void checkSerializationForPlankProblems() throws URISyntaxException, IOException, ClassNotFoundException {
        checkSerializationForClass(PLANK_PROBLEM_SERIAL_VERSIONS, PLANK_PROBLEM_REFERENCE, PlankProblem.class,
                "getProposedSolution");
    }
}
