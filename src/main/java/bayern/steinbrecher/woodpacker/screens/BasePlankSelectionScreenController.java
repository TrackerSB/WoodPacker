package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import bayern.steinbrecher.woodpacker.elements.PlankGrainDirectionIndicatorSkin;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlankSelectionScreenController extends ScreenController {
    private static final Preferences USER_PREFERENCES_ROOT = Preferences.userRoot()
            .node("bayern/steinbrecher/woodpacker");
    private static final Preferences USER_DEFINED_BASE_PLANKS = USER_PREFERENCES_ROOT.node("baseplanks");
    private static final Logger LOGGER = Logger.getLogger(BasePlankSelectionScreenController.class.getName());

    @FXML
    private ListView<Plank> predefinedBasePlanksView;
    @FXML
    private ScaledCanvas basePlankPreview;
    @FXML
    public PlankField newBasePlankField;
    @FXML
    public CheckedTextField basePlankNameField;

    private void readUserDefinedBasePlanks() {
        // FIXME Show graphical feedback to user in any case where a logger is used
        try {
            for (String basePlankName : USER_DEFINED_BASE_PLANKS.keys()) {
                byte[] serializedBasePlank = USER_DEFINED_BASE_PLANKS.getByteArray(basePlankName, null);
                if (serializedBasePlank == null) {
                    LOGGER.log(Level.WARNING,
                            String.format("The serialized data for '%s' is not available", basePlankName));
                } else {
                    try (ObjectInputStream deserializer
                            = new ObjectInputStream(new ByteArrayInputStream(serializedBasePlank))) {
                        Object deserializedObject = deserializer.readObject();
                        if (deserializedObject instanceof Plank) {
                            Plank basePlank = (Plank) deserializedObject;
                            predefinedBasePlanksView.getItems()
                                    .add(basePlank);
                        } else {
                            LOGGER.log(Level.WARNING,
                                    String.format("The serialized object for '%s' is no '%s'",
                                            basePlankName, Plank.class.getCanonicalName()));
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        LOGGER.log(Level.WARNING, String.format("Failed to deserialize '%s'", basePlankName), ex);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, "Could not access the storage of predefined base planks", ex);
        }
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        predefinedBasePlanksView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Plank item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("");
                    setGraphic(null);
                } else {
                    setText(String.format("\"%s\": %d mm x %d mm", item.getId(), item.getWidth(), item.getHeight()));
                    ImageView grainDirectionIcon
                            = PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection());
                    setGraphic(grainDirectionIcon);
                }
            }
        });
        predefinedBasePlanksView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);

        readUserDefinedBasePlanks();
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void createBasePlank() {
        Plank newBasePlank = newBasePlankField.createPlank(basePlankNameField.getText());
        predefinedBasePlanksView.getItems()
                .add(newBasePlank);
        ByteArrayOutputStream serializedBasePlank = new ByteArrayOutputStream();
        try (ObjectOutputStream serializer = new ObjectOutputStream(serializedBasePlank)) {
            serializer.writeObject(newBasePlank);
            USER_DEFINED_BASE_PLANKS.putByteArray(newBasePlank.getId(), serializedBasePlank.toByteArray());
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Could not persistently store new base plank", ex);
            // FIXME Show stacktrace alert to user
        }
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void confirmBasePlank() throws ScreenSwitchFailedException {
        Plank selectedBasePlank = predefinedBasePlanksView.getSelectionModel()
                .getSelectedItem();
        // FIXME Show warning to user
        getScreenManager()
                .switchTo(new PlankDemandScreen(selectedBasePlank));
    }
}
