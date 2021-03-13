package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenswitcher.Screen;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import org.jetbrains.annotations.NotNull;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreen extends Screen<PlankDemandScreenController> {
    private final PlankProblem initialSetup;

    public PlankDemandScreen() {
        this(null);
    }

    public PlankDemandScreen(final PlankProblem initialSetup) {
        super(PlankDemandScreen.class.getResource("PlankDemandScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
        this.initialSetup = initialSetup;
    }

    @Override
    protected void afterControllerIsInitialized(@NotNull final PlankDemandScreenController controller) {
        super.afterControllerIsInitialized(controller);
        if (initialSetup != null) {
            controller.loadPlankProblem(initialSetup);
        }
    }
}
