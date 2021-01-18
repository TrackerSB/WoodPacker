package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.BuildConfig;
import bayern.steinbrecher.woodpacker.WoodPacker;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class AboutScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(AboutScreenController.class.getName());
    public static final String BUILD_TIME = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
            .format(Instant.ofEpochMilli(BuildConfig.BUILD_TIME)
                    .atZone(ZoneId.systemDefault()));
    // List of pairs of resource key for entry and value of entry
    private static final List<Pair<String, String>> BUILD_INFO_ENTRIES = List.of(
            new Pair<>("buildTime", BUILD_TIME),
            new Pair<>("featureSet", "Ultimate")
    );
    // Map from author names to resource keys of their roles
    private static final List<Pair<String, Collection<String>>> AUTHOR_INFO_ENTRIES = List.of(
            new Pair<>("Stefan \"TrackerSB\" Huber", List.of("founder", "mainDeveloper")),
            new Pair<>("\"Smashicons\"", List.of("iconDesigner")),
            new Pair<>("\"Good Ware\"", List.of("iconDesigner")),
            new Pair<>("\"Dimitry Miroliubov\"", List.of("iconDesigner")),
            new Pair<>("\"Freepik\"", List.of("iconDesigner"))
    );
    private static final Path LICENSES_ROOT_DIR = Paths.get("licenses");
    // Map license names to license paths
    private static final Map<String, Path> LICENSES = Map.of(
            "Flaticon", LICENSES_ROOT_DIR.resolve("flaticon.pdf"),
            "Essential collection", LICENSES_ROOT_DIR.resolve("148705-essential-collection.pdf"),
            "Clipboard", LICENSES_ROOT_DIR.resolve("340058-clipboard.pdf"),
            "Edit tools", LICENSES_ROOT_DIR.resolve("764586-edit-tools.pdf"),
            "Carpenter", LICENSES_ROOT_DIR.resolve("2933545-carpenter.pdf")
    );
    private final ReadOnlyStringWrapper appName = new ReadOnlyStringWrapper(BuildConfig.APP_NAME);
    @FXML
    private Text version;
    @FXML
    private GridPane authorInfo;
    @FXML
    private GridPane buildInfo;
    @FXML
    private VBox licensesInfo;

    @FXML
    private void initialize() {
        version.setText(String.format("%s (\"%s\")", BuildConfig.APP_VERSION, BuildConfig.APP_VERSION_NICKNAME));

        int currentRow = 0;
        for (Pair<String, Collection<String>> entry : AUTHOR_INFO_ENTRIES) {
            authorInfo.add(new Text(entry.getKey()), 0, currentRow);
            String rolesList = entry.getValue()
                    .stream()
                    .map(WoodPacker.LANGUAGE_BUNDLE::getString)
                    .collect(Collectors.joining(", "));
            authorInfo.add(new Text(rolesList), 1, currentRow);
            currentRow++;
        }

        currentRow = 0;
        for (Pair<String, String> entry : BUILD_INFO_ENTRIES) {
            buildInfo.add(new Text(WoodPacker.LANGUAGE_BUNDLE.getString(entry.getKey())), 0, currentRow);
            buildInfo.add(new Text(entry.getValue()), 1, currentRow);
            currentRow++;
        }

        LICENSES.forEach((name, path) -> {
            Hyperlink licenseLink = new Hyperlink(name);
            licenseLink.setOnAction(aevt -> {
                try {
                    Desktop.getDesktop().open(path.toFile());
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not show license to user", ex);
                }
            });
            licensesInfo.getChildren()
                    .add(licenseLink);
        });
    }

    public ReadOnlyStringProperty appNameProperty() {
        return appName.getReadOnlyProperty();
    }

    public String getAppName() {
        return appNameProperty().get();
    }
}
