package bayern.steinbrecher.woodpacker;

import bayern.steinbrecher.javaUtility.DialogGenerator;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.screens.WelcomeScreen;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

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
    private static final String DEFAULT_STYLESHEET_PATH = WoodPacker.class
            .getResource("styles.css")
            .toExternalForm();
    public static /*final*/ DialogGenerator DIALOG_GENERATOR;

    @Override
    public void start(final Stage primaryStage) throws ScreenSwitchFailedException {
        DIALOG_GENERATOR = new DialogGenerator(
                primaryStage, Modality.APPLICATION_MODAL, StageStyle.UTILITY, DEFAULT_STYLESHEET_PATH);

        final ScreenManager screenManager = new ScreenManager(primaryStage);
        primaryStage.getScene()
                .getStylesheets()
                .add(DEFAULT_STYLESHEET_PATH);
        screenManager.switchTo(new WelcomeScreen());
        primaryStage.setFullScreen(true);
        primaryStage.fullScreenProperty()
                .addListener((obs, wasFullScreen, isFullScreen) -> {
                    if (wasFullScreen) {
                        primaryStage.setMaximized(true);
                    }
                });
        primaryStage.getIcons()
                .add(new Image(getClass().getResource("logo.png").toExternalForm()));
        primaryStage.show();
    }

    public static String getResource(final String resourceKey, final Object... arguments) {
        String resource;
        if (LANGUAGE_BUNDLE.containsKey(resourceKey)) {
            resource = new MessageFormat(LANGUAGE_BUNDLE.getString(resourceKey))
                    .format(arguments);
        } else {
            LOGGER.log(Level.WARNING, String.format("Could not find resource for key '%s'", resourceKey));
            resource = resourceKey;
        }
        return resource;
    }
}
