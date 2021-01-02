package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.Screen;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.Plank;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreen extends Screen<PlankDemandScreenController> {
    private final Plank basePlank;

    public PlankDemandScreen(Plank basePlank) {
        super(PlankDemandScreen.class.getResource("PlankDemandScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
        this.basePlank = Objects.requireNonNull(basePlank);
    }

    @Override
    protected void afterControllerIsInitialized(@NotNull PlankDemandScreenController controller) {
        super.afterControllerIsInitialized(controller);
        controller.setBasePlank(basePlank);
    }
}
