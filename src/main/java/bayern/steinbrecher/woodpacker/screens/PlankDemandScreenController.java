package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import bayern.steinbrecher.woodpacker.elements.PlankGrainDirectionIndicatorSkin;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
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

    // NOTE Can this method be reused for showing the same ID circles on the visual plank cutting plan?
    private Node createIdCircle(int id){
        Circle idContainer = new Circle(10, Color.BLACK);
        Text idText = new Text(String.valueOf(id));
        idText.setFill(Color.WHITE);
        idText.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        return new StackPane(idContainer, idText);
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        visualPlankCuttingPlan.setMinWidth(300);
        visualPlankCuttingPlan.setMinHeight(300);
        visualPlankCuttingPlan.setMaxWidth(800);
        visualPlankCuttingPlan.sceneProperty()
                .addListener((obs, previousScene, currentScene) -> {
                    visualPlankCuttingPlan.maxHeightProperty()
                            .bind(currentScene.heightProperty());
                });

        // Setup required planks list and its visualization
        requiredPlanksList.itemsProperty()
                .bind(plankProblem.requiredPlanksProperty());
        requiredPlanksList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Plank item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("");
                    setGraphic(null);
                } else {
                    setText(String.format("%d mm x %d mm", item.getHeight(), item.getWidth()));
                    ImageView grainDirectionIcon
                            = PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection());
                    setGraphic(new HBox(createIdCircle(item.getId()), grainDirectionIcon));
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
            Plank newBasePlank, Pair<List<PlankSolutionRow>, List<Plank>> proposedSolution) {
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
                List<PlankSolutionRow> placedPlankRows = proposedSolution.getKey();
                if (placedPlankRows != null) {
                    for (PlankSolutionRow row : placedPlankRows) {
                        double currentStartX = 0;
                        for (Plank plank : row.getPlanks()) {
                            gc.beginPath();
                            gc.rect(currentStartX, row.getStartY(), plank.getWidth(), plank.getHeight());
                            gc.setStroke(Color.BLACK);
                            gc.stroke();
                            gc.setFill(Color.BURLYWOOD);
                            gc.fill();
                            gc.setFill(Color.BLACK);
                            gc.fillText(
                                    String.valueOf(plank.getId()),
                                    currentStartX + plank.getWidth() / 2d,
                                    row.getStartY() + plank.getHeight() / 2d
                            );
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
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void addPlank() {
        plankProblem.getRequiredPlanks()
                .add(newPlankField.createPlank(plankProblem.getRequiredPlanks().size() + 1));
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void clearAllPlanks() {
        plankProblem.requiredPlanksProperty()
                .clear();
    }
}
