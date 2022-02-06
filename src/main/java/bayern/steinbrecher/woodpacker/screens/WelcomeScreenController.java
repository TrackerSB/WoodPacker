package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogFactory;
import bayern.steinbrecher.screenswitcher.ScreenController;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.utility.PredefinedFileChooser;
import bayern.steinbrecher.woodpacker.utility.SerializationUtility;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class WelcomeScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(WelcomeScreenController.class.getName());

    @SuppressWarnings("unused")
    @FXML
    private void switchToPlankDemandScreen() throws ScreenSwitchFailedException {
        getScreenManager()
                .switchTo(new PlankDemandScreen());
    }

    @SuppressWarnings("unused")
    @FXML
    private void askUserImportPlankProblem() throws ScreenSwitchFailedException, DialogCreationException {
        final Optional<File> openPath = PredefinedFileChooser.PLANK_PROBLEM.askForOpenPath(null); // FIXME Specify owner
        if (openPath.isPresent()) {
            try {
                final byte[] deserializedSnapshot = Files.readAllBytes(openPath.get().toPath());
                final PlankProblem snapshot = SerializationUtility.deserialize(deserializedSnapshot);
                getScreenManager()
                        .switchTo(new PlankDemandScreen(snapshot));
            } catch (IOException | ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "Could not import plank problem", ex);
                final Alert importFailedAlert = WoodPacker.getDialogFactory()
                        .createStacktraceAlert(ex, WoodPacker.getResource("importFailed"));
                DialogFactory.showAndWait(importFailedAlert);
            }
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void switchToAbout() throws ScreenSwitchFailedException {
        getScreenManager()
                .switchTo(new AboutScreen());
    }

    @SuppressWarnings("unused")
    @FXML
    private void exitApp() {
        Platform.exit();
    }
}
