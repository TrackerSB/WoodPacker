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
public final class FileSystemUtility {
    private static final FileChooser plankListFileChooser = new FileChooser();

    static {
        plankListFileChooser.getExtensionFilters()
                .addAll(
                        new ExtensionFilter("WoodPacker", "*.wp")
                );
        plankListFileChooser.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile());
    }

    private FileSystemUtility() {
        throw new UnsupportedOperationException("Construction of instances is prohibited");
    }

    public static Optional<File> askForSavePath(Window owner) {
        return Optional.ofNullable(plankListFileChooser.showSaveDialog(owner));
    }

    public static Optional<File> askForOpenPath(Window owner){
        return Optional.ofNullable(plankListFileChooser.showOpenDialog(owner));
    }
}
