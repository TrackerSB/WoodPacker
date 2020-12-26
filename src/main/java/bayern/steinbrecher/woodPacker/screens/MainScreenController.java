package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodPacker.data.Plank;
import bayern.steinbrecher.woodPacker.data.PlankProblem;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicator;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicatorSkin;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class MainScreenController extends ScreenController {
    @FXML
    private ListView<Plank> requiredPlanksList;
    @FXML
    private Canvas visualBoardPlan;
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
        GraphicsContext graphicsContext = visualBoardPlan.getGraphicsContext2D();
        graphicsContext.setFill(Color.GHOSTWHITE);
        graphicsContext.fillRect(0, 0, visualBoardPlan.getWidth(), visualBoardPlan.getHeight());

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
        plankProblem.addRequiredPlank(
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
