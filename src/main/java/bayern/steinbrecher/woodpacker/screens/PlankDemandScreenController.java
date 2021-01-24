package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import bayern.steinbrecher.woodpacker.elements.PlankList;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import bayern.steinbrecher.woodpacker.utility.DrawActionGenerator;
import bayern.steinbrecher.woodpacker.utility.FileSystemUtility;
import bayern.steinbrecher.woodpacker.utility.SerializationUtility;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(PlankDemandScreenController.class.getName());
    private static final Preferences USER_PREFERENCES_ROOT = Preferences.userRoot()
            .node("bayern/steinbrecher/woodpacker");
    private static final Preferences USER_DEFINED_BASE_PLANKS = USER_PREFERENCES_ROOT.node("baseplanks");

    @FXML
    private PlankList<BasePlank> basePlankList;
    @FXML
    private PlankList<RequiredPlank> requiredPlanksView;
    @FXML
    private ScaledCanvas visualPlankCuttingPlan;
    private final PlankProblem plankProblem = new PlankProblem();
    private final ReadOnlyBooleanWrapper plankProblemValid = new ReadOnlyBooleanWrapper();

    private void readUserDefinedBasePlanks() {
        // FIXME Show graphical feedback to user in any case where a logger is used
        try {
            for (String basePlankName : USER_DEFINED_BASE_PLANKS.keys()) {
                byte[] serializedBasePlank = USER_DEFINED_BASE_PLANKS.getByteArray(basePlankName, null);
                if (serializedBasePlank == null) {
                    LOGGER.log(Level.WARNING,
                            String.format("The serialized data for '%s' is not available", basePlankName));
                } else {
                    try {
                        BasePlank basePlank = SerializationUtility.deserialize(serializedBasePlank);
                        basePlankList.getPlanks()
                                .add(basePlank);
                    } catch (IOException | ClassNotFoundException ex) {
                        LOGGER.log(Level.WARNING, String.format("Failed to deserialize '%s'", basePlankName), ex);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, "Could not access the storage of predefined base planks", ex);
        }
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        // Ensure planks being sorted
        ObservableSet<BasePlank> sortedBasePlanks
                = FXCollections.observableSet(new TreeSet<>(basePlankList.getPlanks()));
        basePlankList.setPlanks(sortedBasePlanks);
        ObservableSet<RequiredPlank> sortedRequiredPlanks
                = FXCollections.observableSet(new TreeSet<>(requiredPlanksView.getPlanks()));
        requiredPlanksView.setPlanks(sortedRequiredPlanks);

        readUserDefinedBasePlanks();

        basePlankList.planksProperty()
                .addListener((SetChangeListener<? super Plank>) change -> {
                    if (change.wasAdded()) {
                        try {
                            USER_DEFINED_BASE_PLANKS.putByteArray(
                                    change.getElementAdded().getId(),
                                    SerializationUtility.serialize(change.getElementAdded()));
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Could not persistently store new base plank", ex);
                            // FIXME Show stacktrace alert to user
                        }
                    }
                    if (change.wasRemoved()) {
                        USER_DEFINED_BASE_PLANKS.remove(change.getElementRemoved().getId());
                    }
                    // FIXME Treat change.wasUpdated()?
                });
        // Sync selected base plank <--> plank problem base plank
        basePlankList.selectedPlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank)
                        -> plankProblem.setBasePlank(currentBasePlank.orElse(null)));
        plankProblem.basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank) -> {
                    basePlankList.getPlanks()
                            .add(currentBasePlank); // Ensure the base plank to select exists
                    basePlankList.setSelectedPlank(currentBasePlank);
                });

        // Sync requiredPlanksView <--> plankProblem
        requiredPlanksView.planksProperty()
                .addListener((SetChangeListener<? super RequiredPlank>) change -> {
                    if (change.wasAdded()) {
                        plankProblem.getRequiredPlanks()
                                .add(change.getElementAdded());
                    }
                    if (change.wasRemoved()) {
                        plankProblem.getRequiredPlanks()
                                .remove(change.getElementRemoved());
                    }
                });
        plankProblem.requiredPlanksProperty()
                .addListener((SetChangeListener<? super RequiredPlank>) change -> {
                    if (change.wasAdded()) {
                        requiredPlanksView.getPlanks()
                                .add(change.getElementAdded());
                    }
                    if (change.wasRemoved()) {
                        requiredPlanksView.getPlanks()
                                .remove(change.getElementRemoved());
                    }
                });

        // Trigger updates of visual cutting plank
        plankProblem.basePlankProperty()
                .addListener((obs, oldBasePlank, newBasePlank) -> {
                    Pair<List<PlankSolutionRow>, Set<RequiredPlank>> proposedSolution = plankProblem
                            .getProposedSolution();
                    updateVisualPlankCuttingPlan(newBasePlank, proposedSolution.getKey());
                });
        plankProblem.proposedSolutionProperty()
                .addListener((obs, oldSolution, newSolution)
                        -> updateVisualPlankCuttingPlan(plankProblem.getBasePlank(), newSolution.getKey()));
        // Ensure initial state
        Pair<List<PlankSolutionRow>, Set<RequiredPlank>> proposedSolution = plankProblem.getProposedSolution();
        updateVisualPlankCuttingPlan(plankProblem.getBasePlank(), proposedSolution.getKey());

        // Creating binding signaling when a cutting plan should be drawn
        plankProblemValid.bind(
                plankProblem.basePlankProperty().isNotNull()
                        .and(plankProblem.requiredPlanksProperty().emptyProperty().not()));
    }

    private void updateVisualPlankCuttingPlan(BasePlank basePlank, Iterable<PlankSolutionRow> placedPlankRows) {
        // Update cutting plan preview
        if (basePlank == null) {
            visualPlankCuttingPlan.theoreticalWidthProperty()
                    .bind(visualPlankCuttingPlan.widthProperty());
            visualPlankCuttingPlan.theoreticalHeightProperty()
                    .bind(visualPlankCuttingPlan.heightProperty());
            visualPlankCuttingPlan.setDrawingActions(gc -> {
                gc.setFill(Color.GRAY);
                gc.fillRect(0, 0, visualPlankCuttingPlan.getTheoreticalWidth(),
                        visualPlankCuttingPlan.getTheoreticalHeight());
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(visualPlankCuttingPlan.getTheoreticalHeight() / 10));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);
                gc.fillText(WoodPacker.LANGUAGE_BUNDLE.getString("noBasePlankSelected"),
                        visualPlankCuttingPlan.getTheoreticalWidth() / 2,
                        visualPlankCuttingPlan.getTheoreticalHeight() / 2,
                        visualPlankCuttingPlan.getTheoreticalWidth());
            });
        } else {
            visualPlankCuttingPlan.theoreticalWidthProperty()
                    .unbind();
            visualPlankCuttingPlan.setTheoreticalWidth(basePlank.getWidth());
            visualPlankCuttingPlan.theoreticalHeightProperty()
                    .unbind();
            visualPlankCuttingPlan.setTheoreticalHeight(basePlank.getHeight());

            Consumer<GraphicsContext> basePlankActions = DrawActionGenerator.forBasePlank(basePlank);
            Consumer<GraphicsContext> requiredPlanksActions
                    = DrawActionGenerator.forRequiredPlanks(basePlank, placedPlankRows);
            Consumer<GraphicsContext> drawingActions = gc -> {
                basePlankActions.accept(gc);
                requiredPlanksActions.accept(gc);
            };
            visualPlankCuttingPlan.setDrawingActions(drawingActions);
        }
    }

    public void loadPlankProblem(PlankProblem setup) {
        plankProblem.setBasePlank(setup.getBasePlank());
        plankProblem.setRequiredPlanks(setup.getRequiredPlanks());
    }

    @SuppressWarnings("unused")
    @FXML
    private void askUserExportPlankProblem() throws IOException {
        Optional<File> exportFile = FileSystemUtility.askForSavePath(requiredPlanksView.getScene().getWindow());
        if (exportFile.isPresent()) {
            byte[] serializedSnapshot = SerializationUtility.serialize(plankProblem);
            Files.write(exportFile.get().toPath(), serializedSnapshot);
        }
    }

    public ReadOnlyBooleanProperty plankProblemValidProperty() {
        return plankProblemValid.getReadOnlyProperty();
    }

    public boolean isPlankProblemValid() {
        return plankProblemValidProperty().get();
    }
}
