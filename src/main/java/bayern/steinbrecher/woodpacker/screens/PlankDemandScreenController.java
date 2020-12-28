package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankRow;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import bayern.steinbrecher.woodpacker.elements.PlankGrainDirectionIndicatorSkin;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import javafx.fxml.FXML;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.List;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreenController extends ScreenController {
    @FXML
    private ListView<Plank> requiredPlanksList;
    @FXML
    private ScaledCanvas visualPlankCuttingPlan;
    @FXML
    private PlankField newPlankField;
    private final PlankProblem plankProblem = new PlankProblem();

    @FXML
    private void initialize() {
        // FIXME Adapt size to available size on screen
        visualPlankCuttingPlan.setMaxWidth(100);
        visualPlankCuttingPlan.setMaxHeight(100);
        visualPlankCuttingPlan.setMaxWidth(800);
        visualPlankCuttingPlan.setMaxHeight(800);

        // Setup required planks list and its visualization
        requiredPlanksList.itemsProperty()
                .bind(plankProblem.requiredPlanksProperty());
        requiredPlanksList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Plank item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null) {
                    setText("");
                    setGraphic(null);
                } else {
                    setText(String.format("%d mm x %d mm", item.getHeight(), item.getWidth()));
                    setGraphic(PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection()));
                }
            }
        });

        // Trigger updates of visual cutting plank
        plankProblem.basePlankProperty()
                .addListener((obs, oldBasePlank, newBasePlank)
                        -> updateVisualPlankCuttingPlan(newBasePlank, plankProblem.getProposedSolution()));
        plankProblem.proposedSolutionProperty()
                .addListener((obs, oldSolution, newSolution)
                        -> updateVisualPlankCuttingPlan(plankProblem.getBasePlank(), newSolution));
    }

    private void updateVisualPlankCuttingPlan(
            Plank newBasePlank, Pair<List<PlankRow>, List<Plank>> proposedSolution) {
        if (newBasePlank != null) {
            visualPlankCuttingPlan.setTheoreticalWidth(newBasePlank.getWidth());
            visualPlankCuttingPlan.setTheoreticalHeight(newBasePlank.getHeight());

            Consumer<GraphicsContext> drawingActions = gc -> {
                // Draw background
                gc.setFill(Color.BURLYWOOD);
                gc.fillRect(0, 0, newBasePlank.getWidth(), newBasePlank.getHeight());
                final double backgroundAngleBottom = Math.toRadians(45);
                final double topBottomXDelta = newBasePlank.getHeight() / Math.tan(backgroundAngleBottom);
                final int stepSize = 10;
                gc.setStroke(Color.GRAY);
                for (double x = -topBottomXDelta + stepSize; x < newBasePlank.getWidth(); x += stepSize) {
                    gc.strokeLine(x, newBasePlank.getHeight(), x + topBottomXDelta, 0);
                }

                // Draw planks
                List<PlankRow> placedPlankRows = proposedSolution.getKey();
                if (placedPlankRows != null) {
                    gc.setStroke(Color.BLACK);
                    for (PlankRow row : placedPlankRows) {
                        double currentStartX = 0;
                        for (Plank plank : row.getPlanks()) {
                            gc.beginPath();
                            gc.rect(currentStartX, row.getStartY(), plank.getWidth(), plank.getHeight());
                            gc.stroke();
                            gc.fill();
                            currentStartX += plank.getWidth();
                        }
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

    @FXML
    private void addPlank() {
        plankProblem.getRequiredPlanks()
                .add(newPlankField.createPlank());
    }

    @FXML
    private void clearAllPlanks() {
        plankProblem.requiredPlanksProperty()
                .clear();
    }
}
