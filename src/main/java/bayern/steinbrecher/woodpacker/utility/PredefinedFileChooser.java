package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.woodpacker.BuildConfig;
import bayern.steinbrecher.woodpacker.WoodPacker;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Window;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public enum PredefinedFileChooser {
    CUTTING_PLAN(
            new ExtensionFilter(WoodPacker.getResource("cuttingPlan"), "*.pdf")
    ),
    PLANK_PROBLEM(
            new ExtensionFilter(BuildConfig.APP_NAME, "*.wp")
    );

    private final FileChooser chooser = new FileChooser();

    PredefinedFileChooser(final ExtensionFilter... filters) {
        chooser.getExtensionFilters()
                .addAll(filters);
        chooser.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
    }

    public Optional<File> askForSavePath(final Window owner) {
        return Optional.ofNullable(chooser.showSaveDialog(owner));
    }

    public Optional<File> askForOpenPath(final Window owner) {
        return Optional.ofNullable(chooser.showOpenDialog(owner));
    }
}
