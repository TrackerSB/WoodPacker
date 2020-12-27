package bayern.steinbrecher.woodPacker;

import bayern.steinbrecher.screenSwitcher.ScreenManager;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodPacker.screens.PlankDemandScreen;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.ResourceBundle;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class WoodPacker extends Application {
    public static final ResourceBundle LANGUAGE_BUNDLE = ResourceBundle.getBundle("bayern.steinbrecher.woodPacker.language");

    @Override
    public void start(Stage primaryStage) throws ScreenSwitchFailedException {
        ScreenManager screenManager = new ScreenManager(primaryStage);
        screenManager.switchTo(new PlankDemandScreen());
        primaryStage.show();
    }
}
