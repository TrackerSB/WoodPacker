package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.screenSwitcher.Screen;
import bayern.steinbrecher.woodPacker.WoodPacker;

public class MainScreen extends Screen<MainScreenController> {
    public MainScreen() {
        super(MainScreen.class.getResource("MainScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
    }
}
