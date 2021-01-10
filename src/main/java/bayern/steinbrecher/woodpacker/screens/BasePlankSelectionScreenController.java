package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.screenSwitcher.ScreenSwitchFailedException;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import bayern.steinbrecher.woodpacker.elements.PlankList;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import bayern.steinbrecher.woodpacker.utility.DrawActionGenerator;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
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
    @FXML
    private PlankList basePlankList;

    @FXML
    private ScaledCanvas basePlankPreview;
    @FXML
    private CheckedComboBox<PlankMaterial> materialSelection;

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
                            basePlankList.getPlanks()
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

    private void setupBasePlankPreviewUpdate() {
        // FIXME Specify max width and height of base plank preview dynamically
        basePlankPreview.setMaxWidth(800);
        basePlankPreview.setMaxHeight(800);
        ChangeListener<Optional<Plank>> updateBasePlankPreview = (obs, previousPlank, currentPlank) -> {
            if (currentPlank.isEmpty()) {
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
                    gc.fillText(WoodPacker.LANGUAGE_BUNDLE.getString("noBasePlankSelected"),
                            basePlankPreview.getTheoreticalWidth() / 2,
                            basePlankPreview.getTheoreticalHeight() / 2,
                            basePlankPreview.getTheoreticalWidth());
                });
            } else {
                basePlankPreview.theoreticalWidthProperty().unbind();
                basePlankPreview.theoreticalHeightProperty().unbind();
                basePlankPreview.setTheoreticalWidth(currentPlank.get().getWidth());
                basePlankPreview.setTheoreticalHeight(currentPlank.get().getHeight());
                basePlankPreview.setDrawingActions(DrawActionGenerator.forBasePlank(currentPlank.get()));
            }
        };
        basePlankList.selectedPlankProperty()
                .addListener(updateBasePlankPreview);
        updateBasePlankPreview.changed(
                null, null, basePlankList.getSelectedPlank()); // Ensure initial state
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        basePlankList.planksProperty()
                .addListener((SetChangeListener<? super Plank>) change -> {
                    if (change.wasAdded()) {
                        ByteArrayOutputStream serializedBasePlank = new ByteArrayOutputStream();
                        try (ObjectOutputStream serializer = new ObjectOutputStream(serializedBasePlank)) {
                            serializer.writeObject(change.getElementAdded());
                            USER_DEFINED_BASE_PLANKS.putByteArray(change.getElementAdded().getId(),
                                    serializedBasePlank.toByteArray());
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Could not persistently store new base plank", ex);
                            // FIXME Show stacktrace alert to user
                        }
                    }
                    if (change.wasRemoved()) {
                        USER_DEFINED_BASE_PLANKS.remove(change.getElementRemoved().getId());
                    }
                    // FIXME Treat change.wasUpdated()?
                });
        setupBasePlankPreviewUpdate();

        // Configure dropdown element for the base planks material
        materialSelection.setItems(FXCollections.observableArrayList(PlankMaterial.values()));
        materialSelection.setEditable(false);
        materialSelection.getSelectionModel().select(PlankMaterial.UNDEFINED); // Ensure initial state

        readUserDefinedBasePlanks();
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void confirmBasePlank() throws ScreenSwitchFailedException {
        Optional<Plank> selectedBasePlank = basePlankList.getSelectedPlank();
        if (selectedBasePlank.isPresent()) {
            // FIXME Show warning to user
            getScreenManager()
                    .switchTo(new PlankDemandScreen(selectedBasePlank.get()));
        }
    }
}
