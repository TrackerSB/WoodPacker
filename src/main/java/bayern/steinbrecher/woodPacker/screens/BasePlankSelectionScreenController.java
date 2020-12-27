package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodPacker.data.Plank;
import bayern.steinbrecher.woodPacker.elements.PlankGrainDirectionIndicator;
import javafx.fxml.FXML;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlankSelectionScreenController extends ScreenController {
    @FXML
    private CheckedIntegerSpinner basePlankWidthField;
    @FXML
    private CheckedIntegerSpinner basePlankHeightField;
    @FXML
    private PlankGrainDirectionIndicator basePlankGrainDirIndicator;

    public BasePlankSelectionScreenController() {
    }

    @FXML
    private void confirmBasePlank() throws ScreenSwitchFailedException {
        Plank basePlank = new Plank(
                basePlankHeightField.getValue(),
                basePlankWidthField.getValue(),
                basePlankGrainDirIndicator.getValue()
        );

        // FIXME Notify user about occurred exceptions
        getScreenManager()
                .switchTo(new PlankDemandScreen(basePlank));
    }
}
