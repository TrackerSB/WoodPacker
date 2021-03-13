package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenswitcher.Screen;
import bayern.steinbrecher.woodpacker.WoodPacker;

public class AboutScreen extends Screen<AboutScreenController> {
    public AboutScreen() {
        super(AboutScreen.class.getResource("AboutScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
    }
}
