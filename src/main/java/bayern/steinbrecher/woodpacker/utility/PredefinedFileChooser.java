package bayern.steinbrecher.woodpacker.utility;

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
            new ExtensionFilter(WoodPacker.LANGUAGE_BUNDLE.getString("cuttingPlan"), "*.pdf")
    ),
    PLANK_PROBLEM(
            new ExtensionFilter("WoodPacker", "*.wp")
    );

    private final FileChooser chooser = new FileChooser();

    PredefinedFileChooser(ExtensionFilter... filters) {
        chooser.getExtensionFilters()
                .addAll(filters);
        chooser.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
    }

    public Optional<File> askForSavePath(Window owner) {
        return Optional.ofNullable(chooser.showSaveDialog(owner));
    }

    public Optional<File> askForOpenPath(Window owner) {
        return Optional.ofNullable(chooser.showOpenDialog(owner));
    }
}
