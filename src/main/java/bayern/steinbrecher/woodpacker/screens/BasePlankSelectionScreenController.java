package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import javafx.fxml.FXML;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlankSelectionScreenController extends ScreenController {
    @FXML
    private PlankField basePlankField;

    @FXML
    private void confirmBasePlank() throws ScreenSwitchFailedException {
        // FIXME Notify user about occurred exceptions
        getScreenManager()
                .switchTo(new PlankDemandScreen(basePlankField.createPlank()));
    }
}
