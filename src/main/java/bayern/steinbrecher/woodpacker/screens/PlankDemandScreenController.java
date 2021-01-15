package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreenController extends ScreenController {

    @FXML
    private PlankList requiredPlanksView;
    @FXML
    private ScaledCanvas visualPlankCuttingPlan;
    private final PlankProblem plankProblem = new PlankProblem();
    private final ReadOnlyBooleanWrapper plankProblemValid = new ReadOnlyBooleanWrapper();

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
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

        // Sync requiredPlanksView --> plankProblem
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

        plankProblemValid.bind(
                plankProblem.basePlankProperty().isNotNull()
                        .and(plankProblem.requiredPlanksProperty().emptyProperty().not()));
    }

    private void updateVisualPlankCuttingPlan(
            Plank newBasePlank, Iterable<PlankSolutionRow> placedPlankRows, Iterable<Plank> ignoredPlanks) {
        if (newBasePlank != null) {
            visualPlankCuttingPlan.setTheoreticalWidth(newBasePlank.getWidth());
            visualPlankCuttingPlan.setTheoreticalHeight(newBasePlank.getHeight());

            Consumer<GraphicsContext> basePlankActions = DrawActionGenerator.forBasePlank(newBasePlank);
            Consumer<GraphicsContext> drawingActions = gc -> {
                basePlankActions.accept(gc);

                // Draw planks
                if (placedPlankRows != null) {
                    gc.setTextAlign(TextAlignment.CENTER);
                    final double fontSize = newBasePlank.getHeight() / 20d;
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

    /**
     * NOTE Only {@link PlankDemandScreen} should be allowed to call this method.
     */
    void setBasePlank(Plank basePlank) {
        plankProblem.setBasePlank(basePlank);
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
