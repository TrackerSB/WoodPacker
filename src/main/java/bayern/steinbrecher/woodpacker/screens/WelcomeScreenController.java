package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.utility.FileSystemUtility;
import bayern.steinbrecher.woodpacker.utility.SerializationUtility;
import javafx.fxml.FXML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class WelcomeScreenController extends ScreenController {

    @FXML
    private void switchToBasePlankSelection() throws ScreenSwitchFailedException {
        getScreenManager()
                .switchTo(new PlankDemandScreen());
    }

    @FXML
    private void askUserImportPlankProblem() throws IOException, ClassNotFoundException, ScreenSwitchFailedException {
        Optional<File> openPath = FileSystemUtility.askForOpenPath(null);// FIXME Specify owner
        if (openPath.isPresent()) {
            byte[] deserializedSnapshot = Files.readAllBytes(openPath.get().toPath());
            PlankProblem.Snapshot snapshot = SerializationUtility.deserialize(deserializedSnapshot);
            getScreenManager()
                    .switchTo(new PlankDemandScreen(snapshot));
            // FIXME Load required planks
        }
    }

    @FXML
    private void switchToAbout() throws ScreenSwitchFailedException {
        getScreenManager()
                .switchTo(new AboutScreen());
    }
}
