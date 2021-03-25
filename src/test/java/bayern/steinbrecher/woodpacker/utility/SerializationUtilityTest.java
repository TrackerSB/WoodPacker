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
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * @author Stefan Huber
 * @since 0.1
 */
@Test(groups = {"serialization"})
@SuppressWarnings("PMD")
public final class SerializationUtilityTest {
    private static final String REFERENCE_FILE_PATTERN = "serialized%s%d.bin";

    private <C extends Serializable> void checkSerializationForClass(
            final Map<Long, C> references, final Class<C> typeDummy, final String... methodsToIgnore)
            throws URISyntaxException, IOException, ClassNotFoundException {
        List<String> failMessages = new ArrayList<>();

        // Check given versions
        for (Map.Entry<Long, C> entry : references.entrySet()) {
            final String referenceFileName
                    = String.format(REFERENCE_FILE_PATTERN, typeDummy.getSimpleName(), entry.getKey());
            final Path referenceFilePath = Path.of(
                    SerializationUtilityTest.class.getResource(referenceFileName).toURI());
            final byte[] serializedObject = Files.readAllBytes(referenceFilePath);
            final C deserializedObject = SerializationUtility.deserialize(serializedObject);
            final Optional<String> failMessage = ComparisonUtility.comparePublicValues(
                    typeDummy, deserializedObject, entry.getValue(), methodsToIgnore);
            failMessage.ifPresent(failMessages::add);
        }

        // Check whether current serialization and deserialization are inverse to each other
        final OptionalLong mostRecentVersion = references.keySet()
                .stream()
                .mapToLong(l -> l)
                .max();
        if (mostRecentVersion.isPresent()) {
            final C mostRecentReference = references.get(mostRecentVersion.getAsLong());
            final byte[] serializedObject = SerializationUtility.serialize(mostRecentReference);
            final C deserialized = SerializationUtility.deserialize(serializedObject);
            ComparisonUtility.comparePublicValues(typeDummy, deserialized, mostRecentReference, methodsToIgnore)
                    .ifPresent(failMessages::add);
        } else {
            failMessages.add("Could not check most recent version for correct serialization");
        }

        Assert.assertTrue(failMessages.isEmpty(), String.join("\n", failMessages));
    }

    @Test
    public void checkSerializationForPlanks() {
        // FIXME An anonymous subclass is a problem with serialization
        // checkSerializationForClass(Map.of(
        //         1L, new Plank("PlankReference", 15, 16, PlankGrainDirection.IRRELEVANT, "anonymous plank") {
        //         }
        // ), Plank.class);
    }

    @Test
    public void checkSerializationForBasePlanks() throws URISyntaxException, IOException, ClassNotFoundException {
        checkSerializationForClass(Map.of(
                1L, new BasePlank("BasePlank reference", 86, 42, PlankGrainDirection.IRRELEVANT, PlankMaterial.OAK)
        ), BasePlank.class);
    }

    @Test
    public void checkSerializationForRequiredPlanks() throws URISyntaxException, IOException, ClassNotFoundException {
        checkSerializationForClass(Map.of(
                1L, new RequiredPlank(
                        "RequiredPlank reference", 17, 18, PlankGrainDirection.VERTICAL, "some required plank")
        ), RequiredPlank.class);
    }

    @Test
    public void checkSerializationForPlankProblems() throws URISyntaxException, IOException, ClassNotFoundException {
        final PlankProblem PLANK_PROBLEM_REFERENCE_V1 = new PlankProblem();
        PLANK_PROBLEM_REFERENCE_V1.setBasePlank(
                new BasePlank("BasePlank reference", 86, 42, PlankGrainDirection.IRRELEVANT, PlankMaterial.OAK));
        PLANK_PROBLEM_REFERENCE_V1.setCriterionWeight(PlankSolutionCriterion.BREATH_DIFFERENCES, 1);
        PLANK_PROBLEM_REFERENCE_V1.setCriterionWeight(PlankSolutionCriterion.NUM_PLANKS, 2);
        PLANK_PROBLEM_REFERENCE_V1.setCriterionWeight(PlankSolutionCriterion.ROW_SPACE_WASTE, 3);
        PLANK_PROBLEM_REFERENCE_V1.setRequiredPlanks(FXCollections.observableSet(
                new RequiredPlank("first", 11, 12, PlankGrainDirection.VERTICAL, "first comment"),
                new RequiredPlank("second", 14, 13, PlankGrainDirection.HORIZONTAL, "second comment")));

        final PlankProblem PLANK_PROBLEM_REFERENCE_V2 = new PlankProblem();
        PLANK_PROBLEM_REFERENCE_V2.setBasePlank(new BasePlank(
                "BasePlank reference", 86, 42, PlankGrainDirection.IRRELEVANT, PlankMaterial.OAK));
        PLANK_PROBLEM_REFERENCE_V2.setCriterionWeight(PlankSolutionCriterion.BREATH_DIFFERENCES, 1);
        PLANK_PROBLEM_REFERENCE_V2.setCriterionWeight(PlankSolutionCriterion.NUM_PLANKS, 2);
        PLANK_PROBLEM_REFERENCE_V2.setCriterionWeight(PlankSolutionCriterion.ROW_SPACE_WASTE, 3);
        PLANK_PROBLEM_REFERENCE_V2.setRequiredPlanks(FXCollections.observableSet(
                new RequiredPlank("first", 11, 12, PlankGrainDirection.VERTICAL, "first comment"),
                new RequiredPlank("second", 14, 13, PlankGrainDirection.HORIZONTAL, "second comment")));
        PLANK_PROBLEM_REFERENCE_V2.setCuttingWidth(3);

        checkSerializationForClass(Map.of(
                1L, PLANK_PROBLEM_REFERENCE_V1,
                2L, PLANK_PROBLEM_REFERENCE_V2
        ), PlankProblem.class, "getProposedSolution");
    }
}
