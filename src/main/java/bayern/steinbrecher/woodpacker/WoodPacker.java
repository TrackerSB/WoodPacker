package bayern.steinbrecher.woodpacker;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogGenerator;
import bayern.steinbrecher.screenswitcher.ScreenManager;
import bayern.steinbrecher.screenswitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.screens.WelcomeScreen;
import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.MessageFormat;
import java.util.Optional;
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
    private static DialogGenerator dialogGenerator;

    @Override
    public void start(final Stage primaryStage) throws ScreenSwitchFailedException {
        dialogGenerator = new DialogGenerator(
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
        primaryStage.setOnCloseRequest(wevt -> {
            Alert confirmCloseAlert;
            try {
                confirmCloseAlert = dialogGenerator.createInteractiveAlert(
                        AlertType.WARNING, getResource("confirmClose"), ButtonType.YES, ButtonType.NO);
            } catch (DialogCreationException ex) {
                LOGGER.log(Level.WARNING, "Could not warn user graphically before closing the application. "
                        + "Close the application anyways.");
                confirmCloseAlert = null;
            }

            boolean closeApplication;
            if (confirmCloseAlert == null) {
                closeApplication = true;
            } else {
                final Optional<ButtonType> userResponse = DialogGenerator.showAndWait(confirmCloseAlert);
                closeApplication = userResponse.isPresent()
                        && userResponse.get() == ButtonType.YES;
            }

            if (!closeApplication) {
                wevt.consume(); // Block the close request
            }
        });
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

    public static DialogGenerator getDialogGenerator() {
        if (dialogGenerator == null) {
            throw new IllegalStateException("The dialog generator is not initialized yet. This may be the case if the"
                    + " JavaFX application start method was not called yet.");
        }
        return dialogGenerator;
    }
}
