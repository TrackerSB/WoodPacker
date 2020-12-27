package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.screenSwitcher.Screen;
import bayern.steinbrecher.woodPacker.WoodPacker;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlankSelectionScreen extends Screen<BasePlankSelectionScreenController> {
    public BasePlankSelectionScreen() {
        super(BasePlankSelectionScreen.class.getResource("BasePlankSelectionScreen.fxml"),
                WoodPacker.LANGUAGE_BUNDLE);
    }
}
