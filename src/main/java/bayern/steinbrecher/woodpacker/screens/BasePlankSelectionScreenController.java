package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlankSelectionScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(BasePlankSelectionScreenController.class.getName());

    @FXML
    private PlankField basePlankField;

    @FXML
    private void confirmBasePlank() {
        try {
            getScreenManager()
                    .switchTo(new PlankDemandScreen(basePlankField.createPlank(-1)));
        } catch (ScreenSwitchFailedException ex) {
            LOGGER.log(Level.SEVERE, "Could not switch to plank demand screen", ex);
            String basePlankConfirmationFailed = WoodPacker.LANGUAGE_BUNDLE.getString("basePlankConfirmationFailed");
            Alert exceptionAlert = null;
            try {
                exceptionAlert = DialogUtility.createStacktraceAlert(ex, basePlankConfirmationFailed);
            } catch (DialogCreationException exx) {
                LOGGER.log(Level.SEVERE, "Could not show exception to user", exx);
            }
            if (exceptionAlert != null) {
                DialogUtility.showAndWait(exceptionAlert);
            }
        }
    }
}
