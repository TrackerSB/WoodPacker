package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodPacker.data.Plank;
import bayern.steinbrecher.woodPacker.data.PlankProblem;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicator;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicatorSkin;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.List;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class MainScreenController extends ScreenController {
    @FXML
    private ListView<Plank> requiredPlanksList;
    @FXML
    private Canvas visualPlankCuttingPlan;
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
                .addListener((obs, oldBasePlank, newBasePlank) -> {
                    updateVisualPlankCuttingPlan(newBasePlank, plankProblem.getProposedSolution());
                });
        plankProblem.proposedSolutionProperty()
                .addListener((obs, oldSolution, newSolution) -> {
                    updateVisualPlankCuttingPlan(plankProblem.getBasePlank(), newSolution);
                });
    }

    private void drawPlankCuttingPlanBackground(Plank basePlank) {
        visualPlankCuttingPlan.setHeight(basePlank.getHeight());
        visualPlankCuttingPlan.setWidth(basePlank.getWidth());
        GraphicsContext graphicsContext = visualPlankCuttingPlan.getGraphicsContext2D();
        graphicsContext.setFill(Color.BURLYWOOD);
        graphicsContext.fillRect(0, 0, basePlank.getWidth(), basePlank.getHeight());
        final double backgroundAngleBottom = Math.toRadians(45);
        final double topBottomXDelta = basePlank.getHeight() / Math.tan(backgroundAngleBottom);
        final int stepSize = 10;
        graphicsContext.setStroke(Color.GRAY);
        for (double x = -topBottomXDelta + stepSize; x < basePlank.getWidth(); x += stepSize) {
            graphicsContext.strokeLine(x, basePlank.getHeight(), x + topBottomXDelta, 0);
        }
    }

    private void updateVisualPlankCuttingPlan(
            Plank newBasePlank, Optional<List<Pair<Plank, Point2D>>> proposedSolution) {
        if (newBasePlank != null) {
            drawPlankCuttingPlanBackground(newBasePlank);
            if (proposedSolution.isPresent()) {
                // TODO Draw proposed
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
        plankProblem.getRequiredPlanks()
                .add(new Plank(
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
