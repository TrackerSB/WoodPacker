package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.elements.PlankList;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import bayern.steinbrecher.woodpacker.utility.DrawActionGenerator;
import bayern.steinbrecher.woodpacker.utility.FileSystemUtility;
import bayern.steinbrecher.woodpacker.utility.SerializationUtility;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
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
    private PlankList basePlankList;

    @FXML
    private PlankList requiredPlanksView;
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
                        Plank basePlank = SerializationUtility.deserialize(serializedBasePlank);
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
                .addListener((obs, previousBasePlank, currentBasePlank)
                        -> basePlankList.setSelectedPlank(currentBasePlank));

        // FIXME Dynamically calculate max width and height
        visualPlankCuttingPlan.setMaxHeight(800);
        visualPlankCuttingPlan.setMaxWidth(800);
        visualPlankCuttingPlan.sceneProperty()
                .addListener((obs, previousScene, currentScene) -> {
                    if (currentScene != null) {
                        visualPlankCuttingPlan.maxHeightProperty()
                                .bind(currentScene.heightProperty());
                    }
                });

        // Sync requiredPlanksView <--> plankProblem
        requiredPlanksView.planksProperty()
                .addListener((SetChangeListener<? super Plank>) change -> {
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
                .addListener((SetChangeListener<? super Plank>) change -> {
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
                    Pair<List<PlankSolutionRow>, Set<Plank>> proposedSolution = plankProblem.getProposedSolution();
                    updateVisualPlankCuttingPlan(newBasePlank, proposedSolution.getKey(), proposedSolution.getValue());
                });
        plankProblem.proposedSolutionProperty()
                .addListener((obs, oldSolution, newSolution)
                        -> updateVisualPlankCuttingPlan(
                        plankProblem.getBasePlank(), newSolution.getKey(), newSolution.getValue()));
        // Ensure initial state
        Pair<List<PlankSolutionRow>, Set<Plank>> proposedSolution = plankProblem.getProposedSolution();
        updateVisualPlankCuttingPlan(
                plankProblem.getBasePlank(), proposedSolution.getKey(), proposedSolution.getValue());

        // Creating binding signaling when a cutting plan should be drawn
        plankProblemValid.bind(
                plankProblem.basePlankProperty().isNotNull()
                        .and(plankProblem.requiredPlanksProperty().emptyProperty().not()));
    }

    private void updateVisualPlankCuttingPlan(
            Plank basePlank, Iterable<PlankSolutionRow> placedPlankRows, Iterable<Plank> ignoredPlanks) {
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
            Consumer<GraphicsContext> drawingActions = gc -> {
                basePlankActions.accept(gc);

                // Draw planks
                if (placedPlankRows != null) {
                    gc.setTextAlign(TextAlignment.CENTER);
                    final double fontSize = basePlank.getHeight() / 20d;
                    gc.setFont(Font.font(fontSize));
                    for (PlankSolutionRow row : placedPlankRows) {
                        Point2D rowToBasePlankOffset = row.getStartOffset();
                        double plankToRowXOffset = 0;
                        double plankToRowYOffset = 0;
                        for (Plank plank : row.getPlanks()) {
                            double plankXPos = rowToBasePlankOffset.getX() + plankToRowXOffset;
                            double plankYPos = rowToBasePlankOffset.getY() + plankToRowYOffset;
                            gc.beginPath();
                            gc.rect(plankXPos, plankYPos, plank.getWidth(), plank.getHeight());
                            gc.setStroke(Color.BLACK);
                            gc.stroke();
                            gc.setFill(Color.BURLYWOOD);
                            gc.fill();
                            gc.setFill(Color.BLACK);
                            gc.fillText(
                                    plank.getId(),
                                    plankXPos + (plank.getWidth() / 2d),
                                    plankYPos + (plank.getHeight() / 2d) + (fontSize / 2)
                            );
                            if (row.addHorizontal()) {
                                plankToRowXOffset += plank.getWidth();
                            } else {
                                plankToRowYOffset += plank.getHeight();
                            }
                        }
                        gc.setStroke(Color.RED);
                        double rowWidth = row.addHorizontal() ? row.getCurrentLength() : row.getBreadth();
                        double rowHeight = row.addHorizontal() ? row.getBreadth() : row.getCurrentLength();
                        gc.strokeRect(rowToBasePlankOffset.getX(), rowToBasePlankOffset.getY(), rowWidth, rowHeight);
                    }
                }
            };
            visualPlankCuttingPlan.setDrawingActions(drawingActions);
        }
    }

    public void loadPlankProblem(PlankProblem.Snapshot snapshot) {
        plankProblem.loadSnapshot(snapshot);
    }

    @SuppressWarnings("unused")
    @FXML
    private void askUserExportPlankProblem() throws IOException {
        Optional<File> exportFile = FileSystemUtility.askForSavePath(requiredPlanksView.getScene().getWindow());
        if (exportFile.isPresent()) {
            byte[] serializedSnapshot = SerializationUtility.serialize(plankProblem.createSnapshot());
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
