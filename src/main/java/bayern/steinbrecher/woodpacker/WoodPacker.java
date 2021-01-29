package bayern.steinbrecher.woodpacker;

import bayern.steinbrecher.screenSwitcher.ScreenManager;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.screens.WelcomeScreen;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class WoodPacker extends Application {
    /**
     * Only subclasses of {@link Screen} should acces this constant directly when calling {@code super}. For all other
     * use cases {@link #getResource(String, Object...)} should be used.
     */
    public static final ResourceBundle LANGUAGE_BUNDLE
            = ResourceBundle.getBundle("bayern.steinbrecher.woodpacker.language");
    private static final Logger LOGGER = Logger.getLogger(WoodPacker.class.getName());

    @Override
    public void start(Stage primaryStage) throws ScreenSwitchFailedException {
        ScreenManager screenManager = new ScreenManager(primaryStage);
        primaryStage.getScene()
                .getStylesheets()
                .add(getClass().getResource("styles.css").toExternalForm());
        screenManager.switchTo(new WelcomeScreen());
        primaryStage.setFullScreen(true);
        primaryStage.getIcons()
                .add(new Image(getClass().getResource("logo.png").toExternalForm()));
        primaryStage.show();
    }

    public static String getResource(String resourceKey, Object... arguments) {
        if (LANGUAGE_BUNDLE.containsKey(resourceKey)) {
            return new MessageFormat(LANGUAGE_BUNDLE.getString(resourceKey))
                    .format(arguments);
        } else {
            LOGGER.log(Level.WARNING, String.format("Could not find resource for key '%s'", resourceKey));
            return resourceKey;
        }
    }
}
