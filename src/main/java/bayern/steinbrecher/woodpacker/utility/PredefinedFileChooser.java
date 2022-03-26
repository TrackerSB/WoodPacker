package bayern.steinbrecher.woodpacker.utility;

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
public class PredefinedFileChooser {
    private final FileChooser chooser = new FileChooser();
    private final Window owner;

    public PredefinedFileChooser(final Window owner, final ExtensionFilter... filters) {
        this.owner = owner;

        chooser.getExtensionFilters()
                .addAll(filters);
        chooser.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
    }

    public Optional<File> askForSavePath() {
        return Optional.ofNullable(chooser.showSaveDialog(owner));
    }

    public Optional<File> askForOpenPath() {
        return Optional.ofNullable(chooser.showOpenDialog(owner));
    }
}
