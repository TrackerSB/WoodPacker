package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodPacker.data.Plank;
import bayern.steinbrecher.woodPacker.data.PlankProblem;
import bayern.steinbrecher.woodPacker.data.PlankRow;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicator;
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
    private CheckedIntegerSpinner plankWidthField;
    @FXML
    private CheckedIntegerSpinner plankHeightField;
    @FXML
    private PlankGrainDirectionIndicator plankGrainDirIndicator;
    @FXML
    private CheckedIntegerSpinner basePlankWidthField;
    @FXML
    private CheckedIntegerSpinner basePlankHeightField;
    @FXML
    private PlankGrainDirectionIndicator basePlankGrainDirIndicator;
    private final PlankProblem plankProblem = new PlankProblem();

    @FXML
    private void initialize() {
        GraphicsContext graphicsContext = visualPlankCuttingPlan.getGraphicsContext2D();
        graphicsContext.setFill(Color.GHOSTWHITE);
        graphicsContext.fillRect(0, 0, visualPlankCuttingPlan.getWidth(), visualPlankCuttingPlan.getHeight());

        DoubleBinding cuttingPlanHeightRatio = cuttingPlanContainer.heightProperty()
                .divide(visualPlankCuttingPlan.heightProperty());
        DoubleBinding cuttingPlanWidthRatio = cuttingPlanContainer.widthProperty()
                .divide(visualPlankCuttingPlan.widthProperty());
        NumberBinding cuttingPlanFitScale = Bindings.min(cuttingPlanHeightRatio, cuttingPlanWidthRatio);
        visualPlankCuttingPlan.scaleXProperty()
                .bind(cuttingPlanFitScale);
        visualPlankCuttingPlan.scaleYProperty()
                .bind(cuttingPlanFitScale);

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

    @FXML
    private void createBasePlank() {
        plankProblem.setBasePlank(
                new Plank(
                        basePlankHeightField.getValue(),
                        basePlankWidthField.getValue(),
                        basePlankGrainDirIndicator.getValue()
                )
        );
    }

    @FXML
    private void addPlank() {
        plankProblem.getRequiredPlanks().add(
                new Plank(
                        plankHeightField.getValue(),
                        plankWidthField.getValue(),
                        plankGrainDirIndicator.getValue()
                )
        );
    }

    @FXML
    private void clearAllPlanks() {
        plankProblem.requiredPlanksProperty()
                .clear();
    }
}
