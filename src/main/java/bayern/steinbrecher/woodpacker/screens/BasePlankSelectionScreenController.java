package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.ReportType;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.elements.PlankField;
import bayern.steinbrecher.woodpacker.elements.PlankGrainDirectionIndicatorSkin;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

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
    // FIXME Move the next property to FXML
    private final ReadOnlyBooleanWrapper basePlankSelected = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper validNewBasePlank = new ReadOnlyBooleanWrapper();
    @FXML
    private BorderPane screenContent;
    @FXML
    private ListView<Plank> predefinedBasePlanksView;
    @FXML
    private ScaledCanvas basePlankPreview;
    @FXML
    private PlankField newBasePlankField;
    @FXML
    private CheckedTextField basePlankNameField;
    @FXML
    private Button selectBasePlank;

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
        basePlankSelected.bind(
                Bindings.select(predefinedBasePlanksView, "selectionModel", "selectedItem")
                        .isNotNull());
        basePlankPreview.sceneProperty()
                .addListener((obs, previousScene, currentScene) -> {
                    if (currentScene != null) {
                        basePlankPreview.maxHeightProperty()
                                .bind(currentScene.heightProperty()
                                        .subtract(selectBasePlank.heightProperty()));
                    }
                });
        ChangeListener<Boolean> updateBasePlankPreview = (obs, wasSelected, isSelected) -> {
            if (isSelected) {
                basePlankPreview.theoreticalWidthProperty().unbind();
                basePlankPreview.theoreticalHeightProperty().unbind();
                Plank selectedBasePlank = predefinedBasePlanksView.getSelectionModel()
                        .getSelectedItem();
                basePlankPreview.setTheoreticalWidth(selectedBasePlank.getWidth());
                basePlankPreview.setTheoreticalHeight(selectedBasePlank.getHeight());
                basePlankPreview.setDrawingActions(gc -> {
                    gc.setFill(Color.BURLYWOOD);
                    gc.fillRect(0, 0, selectedBasePlank.getWidth(), selectedBasePlank.getHeight());
                });
            } else {
                basePlankPreview.theoreticalWidthProperty()
                        .bind(basePlankPreview.widthProperty());
                basePlankPreview.theoreticalHeightProperty()
                        .bind(basePlankPreview.heightProperty());
                basePlankPreview.setDrawingActions(gc -> {
                    gc.setFill(Color.GRAY);
                    gc.fillRect(0, 0, basePlankPreview.getTheoreticalWidth(), basePlankPreview.getTheoreticalHeight());
                    gc.setFill(Color.WHITE);
                    gc.setFont(Font.font(basePlankPreview.getTheoreticalHeight() / 10));
                    gc.setTextAlign(TextAlignment.CENTER);
                    gc.fillText("Nix ausgwäid",
                            basePlankPreview.getTheoreticalWidth() / 2,
                            basePlankPreview.getTheoreticalHeight() / 2); // FIXME Localize text
                });
            }
        };
        basePlankSelected.addListener(updateBasePlankPreview);
        updateBasePlankPreview.changed(null, null, basePlankSelected.get()); // Ensure initial state

        BooleanProperty basePlankNameAlreadyExists = new SimpleBooleanProperty();
        InvalidationListener updateBasePlankNameAlreadyExists = obs -> {
            basePlankNameAlreadyExists.set(
                    predefinedBasePlanksView.getItems()
                            .stream()
                            .anyMatch(plank -> plank.getId().equals(basePlankNameField.getText()))
            );
        };
        basePlankNameField.textProperty()
                .addListener(updateBasePlankNameAlreadyExists);
        predefinedBasePlanksView.itemsProperty()
                .addListener((obs, previousItemList, currentItemList) -> {
                    updateBasePlankNameAlreadyExists.invalidated(null);
                    currentItemList.addListener(updateBasePlankNameAlreadyExists);
                });
        basePlankNameField.addReport(
                new ReportEntry("basePlankNameAlreadyExists", ReportType.ERROR, basePlankNameAlreadyExists));

        validNewBasePlank.bind(
                newBasePlankField.validProperty()
                        .and(basePlankNameField.validProperty()));

        readUserDefinedBasePlanks();
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void createBasePlank() {
        if (isValidNewBasePlank()) {
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
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void confirmBasePlank() throws ScreenSwitchFailedException {
        Plank selectedBasePlank = predefinedBasePlanksView.getSelectionModel()
                .getSelectedItem();
        if (selectedBasePlank != null) {
            // FIXME Show warning to user
            getScreenManager()
                    .switchTo(new PlankDemandScreen(selectedBasePlank));
        }
    }

    public ReadOnlyBooleanProperty basePlankSelectedProperty() {
        return basePlankSelected.getReadOnlyProperty();
    }

    public boolean isBasePlankSelected() {
        return basePlankSelectedProperty().get();
    }

    public ReadOnlyBooleanProperty validNewBasePlankProperty() {
        return validNewBasePlank.getReadOnlyProperty();
    }

    public boolean isValidNewBasePlank() {
        return validNewBasePlankProperty().get();
    }
}
