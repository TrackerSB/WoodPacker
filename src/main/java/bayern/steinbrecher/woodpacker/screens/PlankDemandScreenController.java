package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.DrawActionGenerator;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import bayern.steinbrecher.woodpacker.elements.PlankGrainDirectionIndicatorSkin;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;
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
    private Node createIdCircle(String id) {
        Circle idContainer = new Circle(10, Color.BLACK);
        Text idText = new Text(id);
        idText.setFill(Color.WHITE);
        idText.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
        return new StackPane(idContainer, idText);
    }

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

        // Setup required planks list and its visualization
        plankProblem.requiredPlanksProperty()
                .addListener((SetChangeListener<? super Plank>) change -> {
                    if (change.wasAdded()) {
                        requiredPlanksList.getItems()
                                .add(change.getElementAdded());
                    }
                    if (change.wasRemoved()) {
                        requiredPlanksList.getItems()
                                .remove(change.getElementRemoved());
                    }
                });
        requiredPlanksList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Plank item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("");
                    setGraphic(null);
                } else {
                    setText(item.toString());
                    ImageView grainDirectionIcon
                            = PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection());
                    setGraphic(new HBox(createIdCircle(item.getId()), grainDirectionIcon));
                }
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

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void addPlank() {
        plankProblem.getRequiredPlanks()
                .add(newPlankField.createPlank(
                        String.valueOf(plankProblem.getRequiredPlanks().size() + 1),
                        plankProblem.getBasePlank().getMaterial()));
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void clearAllPlanks() {
        plankProblem.requiredPlanksProperty()
                .clear();
    }
}
