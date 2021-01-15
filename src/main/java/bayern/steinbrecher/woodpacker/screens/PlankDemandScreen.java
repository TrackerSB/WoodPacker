package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.Screen;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import org.jetbrains.annotations.NotNull;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreen extends Screen<PlankDemandScreenController> {
    private final PlankProblem.Snapshot initialSnapshot;

    public PlankDemandScreen() {
        this(null);
    }

    public PlankDemandScreen(PlankProblem.Snapshot initialSnapshot) {
        super(PlankDemandScreen.class.getResource("PlankDemandScreen.fxml"), WoodPacker.LANGUAGE_BUNDLE);
        this.initialSnapshot = initialSnapshot;
    }

    @Override
    protected void afterControllerIsInitialized(@NotNull PlankDemandScreenController controller) {
        super.afterControllerIsInitialized(controller);
        if (initialSnapshot != null) {
            controller.loadPlankProblem(initialSnapshot);
        }
    }
}
