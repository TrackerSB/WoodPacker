package bayern.steinbrecher.woodPacker.screens;

import bayern.steinbrecher.screenSwitcher.Screen;
import bayern.steinbrecher.woodPacker.WoodPacker;
import bayern.steinbrecher.woodPacker.data.Plank;
import org.jetbrains.annotations.NotNull;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreen extends Screen<PlankDemandScreenController> {
    private final Plank basePlank;

    public PlankDemandScreen(Plank basePlank) {
        super(PlankDemandScreen.class.getResource("PlankDemandScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
        this.basePlank = basePlank;
    }

    @Override
    protected void afterControllerIsInitialized(@NotNull PlankDemandScreenController controller) {
        super.afterControllerIsInitialized(controller);
        controller.setBasePlank(basePlank);
    }
}
