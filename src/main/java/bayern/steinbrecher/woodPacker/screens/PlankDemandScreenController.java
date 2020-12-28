package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodPacker.data.Plank;
import bayern.steinbrecher.woodPacker.data.PlankProblem;
import bayern.steinbrecher.woodPacker.data.PlankRow;
import bayern.steinbrecher.woodPacker.elements.PlankField;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicatorSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.NumberBinding;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.List;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreenController extends ScreenController {
    @FXML
    private ListView<Plank> requiredPlanksList;
    @FXML
    private Canvas visualPlankCuttingPlan;
    @FXML
    private StackPane cuttingPlanContainer;
    @FXML
    private PlankField newPlankField;
    private final PlankProblem plankProblem = new PlankProblem();

    @FXML
    private void initialize() {
        GraphicsContext graphicsContext = visualPlankCuttingPlan.getGraphicsContext2D();
        graphicsContext.setFill(Color.GHOSTWHITE);
        graphicsContext.fillRect(0, 0, visualPlankCuttingPlan.getWidth(), visualPlankCuttingPlan.getHeight());

        // Grow visual cutting plan to cutting plan container
        DoubleBinding cuttingPlanWidthRatio = cuttingPlanContainer.widthProperty()
                .divide(visualPlankCuttingPlan.widthProperty());
        DoubleBinding cuttingPlanHeightRatio = cuttingPlanContainer.heightProperty()
                .divide(visualPlankCuttingPlan.heightProperty());
        NumberBinding cuttingPlanFitScale = Bindings.min(cuttingPlanWidthRatio, cuttingPlanHeightRatio);
        visualPlankCuttingPlan.scaleXProperty()
                .bind(cuttingPlanFitScale);
        visualPlankCuttingPlan.scaleYProperty()
                .bind(cuttingPlanFitScale);

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
            visualPlankCuttingPlan.setHeight(newBasePlank.getHeight());
            visualPlankCuttingPlan.setWidth(newBasePlank.getWidth());

            // Draw background
            GraphicsContext graphicsContext = visualPlankCuttingPlan.getGraphicsContext2D();
            graphicsContext.setFill(Color.BURLYWOOD);
            graphicsContext.fillRect(0, 0, newBasePlank.getWidth(), newBasePlank.getHeight());
            final double backgroundAngleBottom = Math.toRadians(45);
            final double topBottomXDelta = newBasePlank.getHeight() / Math.tan(backgroundAngleBottom);
            final int stepSize = 10;
            graphicsContext.setStroke(Color.GRAY);
            for (double x = -topBottomXDelta + stepSize; x < newBasePlank.getWidth(); x += stepSize) {
                graphicsContext.strokeLine(x, newBasePlank.getHeight(), x + topBottomXDelta, 0);
            }

            // Draw planks
            List<PlankRow> placedPlankRows = proposedSolution.getKey();
            if (placedPlankRows != null) {
                graphicsContext.setStroke(Color.BLACK);
                for (PlankRow row : placedPlankRows) {
                    double currentStartX = 0;
                    for (Plank plank : row.getPlanks()) {
                        graphicsContext.beginPath();
                        graphicsContext.rect(currentStartX, row.getStartY(), plank.getWidth(), plank.getHeight());
                        graphicsContext.stroke();
                        graphicsContext.fill();
                        currentStartX += plank.getWidth();
                    }
                }
            }
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
