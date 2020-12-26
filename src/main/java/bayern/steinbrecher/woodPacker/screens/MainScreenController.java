package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class MainScreenController extends ScreenController {
    @FXML
    private ListView<?> requiredBoardList;
    @FXML
    private Canvas visualBoardPlan;

    @FXML
    private void initialize(){
        GraphicsContext graphicsContext = visualBoardPlan.getGraphicsContext2D();
        graphicsContext.setFill(Color.GHOSTWHITE);
        graphicsContext.fillRect(0, 0, visualBoardPlan.getWidth(), visualBoardPlan.getHeight());
    }
}
