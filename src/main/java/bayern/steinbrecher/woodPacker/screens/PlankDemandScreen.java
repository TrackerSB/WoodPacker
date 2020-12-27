package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.screenSwitcher.Screen;
import bayern.steinbrecher.woodPacker.WoodPacker;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreen extends Screen<PlankDemandScreenController> {
    public PlankDemandScreen() {
        super(PlankDemandScreen.class.getResource("PlankDemandScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
    }
}
