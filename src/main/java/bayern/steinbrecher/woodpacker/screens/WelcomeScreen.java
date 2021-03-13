package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenswitcher.Screen;
import bayern.steinbrecher.woodpacker.WoodPacker;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class WelcomeScreen extends Screen<WelcomeScreenController> {
    public WelcomeScreen() {
        super(WelcomeScreen.class.getResource("WelcomeScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
    }
}
