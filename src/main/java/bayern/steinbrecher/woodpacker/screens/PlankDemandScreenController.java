package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogGenerator;
import bayern.steinbrecher.screenSwitcher.ScreenController;
import bayern.steinbrecher.woodpacker.BuildConfig;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionCriterion;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import bayern.steinbrecher.woodpacker.elements.PlankList;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import bayern.steinbrecher.woodpacker.utility.DrawActionGenerator;
import bayern.steinbrecher.woodpacker.utility.PredefinedFileChooser;
import bayern.steinbrecher.woodpacker.utility.SerializationUtility;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankDemandScreenController extends ScreenController {
    private static final Logger LOGGER = Logger.getLogger(PlankDemandScreenController.class.getName());
    private static final Preferences USER_PREFERENCES_ROOT = Preferences.userRoot()
            .node("bayern/steinbrecher/woodpacker");
    private static final Preferences USER_DEFINED_BASE_PLANKS = USER_PREFERENCES_ROOT.node("baseplanks");

    @FXML
    private PlankList<BasePlank> basePlankList;
    @FXML
    private PlankList<RequiredPlank> requiredPlanksView;
    @FXML
    private ScaledCanvas visualPlankCuttingPlan;
    @FXML
    private VBox criteriaPane;
    private final PlankProblem plankProblem = new PlankProblem();
    private final ReadOnlyBooleanWrapper plankProblemValid = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper plankProblemSaved = new ReadOnlyBooleanWrapper();

    private void readUserDefinedBasePlanks() {
        // FIXME Show graphical feedback to user in any case where a logger is used
        try {
            for (String basePlankName : USER_DEFINED_BASE_PLANKS.keys()) {
                byte[] serializedBasePlank = USER_DEFINED_BASE_PLANKS.getByteArray(basePlankName, null);
                if (serializedBasePlank == null) {
                    LOGGER.log(Level.WARNING,
                            String.format("The serialized data for '%s' is not available", basePlankName));
                } else {
                    try {
                        BasePlank basePlank = SerializationUtility.deserialize(serializedBasePlank);
                        basePlankList.getPlanks()
                                .add(basePlank);
                    } catch (IOException | ClassNotFoundException ex) {
                        LOGGER.log(Level.WARNING, String.format("Failed to deserialize '%s'", basePlankName), ex);
                    }
                }
            }
        } catch (BackingStoreException ex) {
            LOGGER.log(Level.SEVERE, "Could not access the storage of predefined base planks", ex);
        }
    }

    private void initializeBasePlankList() {
        // Ensure planks being sorted
        ObservableSet<BasePlank> sortedBasePlanks
                = FXCollections.observableSet(new TreeSet<>(basePlankList.getPlanks()));
        basePlankList.setPlanks(sortedBasePlanks);

        readUserDefinedBasePlanks();

        basePlankList.planksProperty()
                .addListener((SetChangeListener<? super Plank>) change -> {
                    if (change.wasAdded()) {
                        try {
                            USER_DEFINED_BASE_PLANKS.putByteArray(
                                    change.getElementAdded().getId(),
                                    SerializationUtility.serialize(change.getElementAdded()));
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

        // Sync selected base plank <--> plank problem base plank
        basePlankList.selectedPlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank)
                        -> plankProblem.setBasePlank(currentBasePlank.orElse(null)));
        plankProblem.basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank) -> {
                    basePlankList.getPlanks()
                            .add(currentBasePlank); // Ensure the base plank to select exists
                    basePlankList.setSelectedPlank(currentBasePlank);
                });
    }

    private void initializeRequiredPlanksList() {
        // Ensure planks being sorted
        ObservableSet<RequiredPlank> sortedRequiredPlanks
                = FXCollections.observableSet(new TreeSet<>(requiredPlanksView.getPlanks()));
        requiredPlanksView.setPlanks(sortedRequiredPlanks);

        // Sync requiredPlanksView <--> plank problem
        requiredPlanksView.planksProperty()
                .addListener((SetChangeListener<? super RequiredPlank>) change -> {
                    if (change.wasAdded()) {
                        plankProblem.getRequiredPlanks()
                                .add(change.getElementAdded());
                    }
                    if (change.wasRemoved()) {
                        plankProblem.getRequiredPlanks()
                                .remove(change.getElementRemoved());
                    }
                });
        plankProblem.requiredPlanksProperty()
                .addListener((SetChangeListener<? super RequiredPlank>) change -> {
                    if (change.wasAdded()) {
                        requiredPlanksView.getPlanks()
                                .add(change.getElementAdded());
                    }
                    if (change.wasRemoved()) {
                        requiredPlanksView.getPlanks()
                                .remove(change.getElementRemoved());
                    }
                });
    }

    private void initializeCriteriaPane() {
        for (PlankSolutionCriterion criterion : PlankSolutionCriterion.values()) {
            Slider weightControl = new Slider(0, 10, plankProblem.getCriterionWeight(criterion));
            weightControl.setShowTickLabels(true);
            weightControl.setShowTickMarks(true);
            weightControl.setSnapToTicks(true);
            weightControl.setMinorTickCount(4);
            weightControl.setMajorTickUnit(5);

            // Sync slider <--> plank problem
            weightControl.valueProperty()
                    .addListener((obs, previousValue, currentValue)
                            -> plankProblem.setCriterionWeight(criterion, currentValue.doubleValue()));
            plankProblem.criterionWeightsProperty()
                    .addListener((MapChangeListener<? super PlankSolutionCriterion, ? super Double>) change -> {
                        if (change.getKey() == criterion) {
                            double nextValue;
                            if (change.wasAdded()) {
                                nextValue = change.getValueAdded();
                            } else if (change.wasRemoved()) {
                                nextValue = change.getValueRemoved();
                            } else {
                                throw new IllegalStateException("A map change was neither an addition nor a removal");
                            }
                            weightControl.setValue(nextValue);
                        }
                    });

            String criterionDescription = WoodPacker.getResource(criterion.getResourceKey());
            criteriaPane.getChildren()
                    .addAll(new Label(criterionDescription), weightControl);
        }
    }

    private void updateVisualPlankCuttingPlan(
            final BasePlank basePlank, final Iterable<PlankSolutionRow> placedPlankRows) {
        // Update cutting plan preview
        if (basePlank == null) {
            visualPlankCuttingPlan.theoreticalWidthProperty()
                    .bind(visualPlankCuttingPlan.widthProperty());
            visualPlankCuttingPlan.theoreticalHeightProperty()
                    .bind(visualPlankCuttingPlan.heightProperty());
            visualPlankCuttingPlan.setDrawingActions(gc -> {
                gc.setFill(Color.GRAY);
                gc.fillRect(0, 0, visualPlankCuttingPlan.getTheoreticalWidth(),
                        visualPlankCuttingPlan.getTheoreticalHeight());
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font(visualPlankCuttingPlan.getTheoreticalHeight() / 10));
                gc.setTextAlign(TextAlignment.CENTER);
                gc.setTextBaseline(VPos.CENTER);
                gc.fillText(WoodPacker.getResource("noBasePlankSelected"),
                        visualPlankCuttingPlan.getTheoreticalWidth() / 2,
                        visualPlankCuttingPlan.getTheoreticalHeight() / 2,
                        visualPlankCuttingPlan.getTheoreticalWidth());
            });
        } else {
            visualPlankCuttingPlan.theoreticalWidthProperty()
                    .unbind();
            visualPlankCuttingPlan.setTheoreticalWidth(basePlank.getWidth());
            visualPlankCuttingPlan.theoreticalHeightProperty()
                    .unbind();
            visualPlankCuttingPlan.setTheoreticalHeight(basePlank.getHeight());

            Consumer<GraphicsContext> basePlankActions = DrawActionGenerator.forBasePlank(basePlank);
            Consumer<GraphicsContext> requiredPlanksActions
                    = DrawActionGenerator.forRequiredPlanks(basePlank, placedPlankRows);
            Consumer<GraphicsContext> drawingActions = gc -> {
                basePlankActions.accept(gc);
                requiredPlanksActions.accept(gc);
            };
            visualPlankCuttingPlan.setDrawingActions(drawingActions);
        }
    }

    private void setupCuttingPlanPreviewUpdates() {
        // Trigger updates of visual cutting plank
        plankProblem.basePlankProperty()
                .addListener((obs, oldBasePlank, newBasePlank) -> {
                    Pair<List<PlankSolutionRow>, Set<RequiredPlank>> proposedSolution = plankProblem
                            .getProposedSolution();
                    updateVisualPlankCuttingPlan(newBasePlank, proposedSolution.getKey());
                });
        plankProblem.proposedSolutionProperty()
                .addListener((obs, oldSolution, newSolution) -> {
                    updateVisualPlankCuttingPlan(plankProblem.getBasePlank(), newSolution.getKey());
                    plankProblemSaved.set(false);
                });
        // Ensure initial state
        Pair<List<PlankSolutionRow>, Set<RequiredPlank>> proposedSolution = plankProblem.getProposedSolution();
        updateVisualPlankCuttingPlan(plankProblem.getBasePlank(), proposedSolution.getKey());

        // Creating binding signaling whether a cutting plan should be drawn
        plankProblemValid.bind(
                plankProblem.basePlankProperty().isNotNull()
                        .and(plankProblem.requiredPlanksProperty().emptyProperty().not()));
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        initializeBasePlankList();
        initializeRequiredPlanksList();
        initializeCriteriaPane();
        setupCuttingPlanPreviewUpdates();
    }

    public void loadPlankProblem(final PlankProblem setup) {
        plankProblem.setBasePlank(setup.getBasePlank());
        plankProblem.setRequiredPlanks(setup.getRequiredPlanks());
        plankProblem.criterionWeightsProperty()
                .putAll(setup.criterionWeightsProperty());
        plankProblemSaved.set(true);
        updateVisualPlankCuttingPlan(setup.getBasePlank(), setup.getProposedSolution().getKey());
    }

    @SuppressWarnings("unused")
    @FXML
    private boolean askUserExportPlankProblem() throws DialogCreationException {
        boolean exportSucceeded = false;
        Optional<File> exportFile = PredefinedFileChooser.PLANK_PROBLEM
                .askForSavePath(requiredPlanksView.getScene().getWindow());
        if (exportFile.isPresent()) {
            try {
                byte[] serializedSnapshot = SerializationUtility.serialize(plankProblem);
                Files.write(exportFile.get().toPath(), serializedSnapshot);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,
                        String.format("Could export plank problem to '%s'", exportFile.get().getAbsolutePath()));
                Alert exportFailedAlert = WoodPacker.DIALOG_GENERATOR
                        .createStacktraceAlert(ex, WoodPacker.getResource("exportFailed"));
                DialogGenerator.showAndWait(exportFailedAlert);
            }
            plankProblemSaved.set(true);
            exportSucceeded = true;
        }

        return exportSucceeded;
    }

    @SuppressWarnings("unused")
    @FXML
    private void trySwitchToPreviousScreen() {
        if (isPlankProblemSaved() || !isPlankProblemValid()) {
            switchToPreviousScreen();
        } else {
            try {
                Alert unsavedChangesAlert = WoodPacker.DIALOG_GENERATOR
                        .createInteractiveAlert(
                                AlertType.CONFIRMATION, WoodPacker.getResource("unsavedChanges"),
                                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                Optional<ButtonType> userResponse = DialogGenerator.showAndWait(unsavedChangesAlert);
                if (userResponse.isPresent()) {
                    ButtonType buttonType = userResponse.get();
                    if (buttonType == ButtonType.YES) {
                        askUserExportPlankProblem();
                        if (isPlankProblemSaved()) {
                            switchToPreviousScreen();
                        }
                    } else if (buttonType == ButtonType.NO) {
                        switchToPreviousScreen();
                    }
                }
            } catch (DialogCreationException ex) {
                LOGGER.log(Level.WARNING, "Could not ask user for saving unsaved changes");
                switchToPreviousScreen();
            }
        }
    }

    private Image generateCuttingPlan() throws IOException {
        WritableImage snapshot = visualPlankCuttingPlan.snapshotDrawingArea();
        BufferedImage bufferedSnapshot = SwingFXUtils.fromFXImage(snapshot, null);
        boolean rotateVertical = bufferedSnapshot.getWidth() > bufferedSnapshot.getHeight();
        BufferedImage monochromeSnapshot;
        if (rotateVertical) {
            monochromeSnapshot = new BufferedImage(
                    bufferedSnapshot.getHeight(), bufferedSnapshot.getWidth(), BufferedImage.TYPE_BYTE_BINARY);
        } else {
            monochromeSnapshot = new BufferedImage(
                    bufferedSnapshot.getWidth(), bufferedSnapshot.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        }
        Graphics2D monochromeSnapshotGraphics = monochromeSnapshot.createGraphics();
        if (rotateVertical) {
            // Move origin to right upper corner
            monochromeSnapshotGraphics.translate(
                    monochromeSnapshot.getWidth() / 2d, monochromeSnapshot.getHeight() / 2d);
            monochromeSnapshotGraphics.rotate(Math.PI / 2);
            monochromeSnapshotGraphics.translate(
                    -monochromeSnapshot.getHeight() / 2d, -monochromeSnapshot.getWidth() / 2d);
        }
        monochromeSnapshotGraphics.drawImage(bufferedSnapshot, 0, 0, null);
        ByteArrayOutputStream snapshotByteStream = new ByteArrayOutputStream();
        ImageIO.write(monochromeSnapshot, "png", snapshotByteStream);
        Image snapshotImage = new Image(ImageDataFactory.create(snapshotByteStream.toByteArray()));
        snapshotImage.setAutoScale(true);
        return snapshotImage;
    }

    @SuppressWarnings("unused")
    @FXML
    private void printPreview() throws DialogCreationException {
        Optional<File> savePath = PredefinedFileChooser.CUTTING_PLAN
                .askForSavePath(requiredPlanksView.getScene().getWindow());
        if (savePath.isPresent()) {
            try (Document document = new Document(new PdfDocument(new PdfWriter(savePath.get())))) {
                try {
                    PdfDocumentInfo documentInfo = document.getPdfDocument()
                            .getDocumentInfo();
                    documentInfo.setCreator(BuildConfig.APP_NAME + " " + BuildConfig.APP_VERSION);

                    Image cuttingPlan = generateCuttingPlan();
                    float leftMargin = (document.getPdfDocument().getDefaultPageSize().getWidth()
                            - cuttingPlan.getImageWidth()) / 2;
                    cuttingPlan.setMarginLeft(leftMargin);
                    document.add(cuttingPlan);
                } catch (IOException ex) {
                    throw new ExportFailedException("Could not generate snapshot of cutting plan preview");
                }
                try {
                    Desktop.getDesktop()
                            .open(savePath.get());
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not open exported cutting plan to user", ex);
                }
            } catch (FileNotFoundException ex) {
                LOGGER.log(Level.SEVERE,
                        String.format("Could not open '%s' for writing", savePath.get().getAbsolutePath()), ex);
                Alert writeAccessDeniedAlert = WoodPacker.DIALOG_GENERATOR
                        .createErrorAlert(WoodPacker.getResource(
                                "writeAccessDenied", savePath.get().getAbsolutePath()));
                DialogGenerator.showAndWait(writeAccessDeniedAlert);
            } catch (ExportFailedException ex) {
                LOGGER.log(Level.SEVERE, "Could not export cutting plan", ex);
                Alert cuttingPlanExportFailed = WoodPacker.DIALOG_GENERATOR
                        .createStacktraceAlert(ex, WoodPacker.getResource("exportFailed"));
                DialogGenerator.showAndWait(cuttingPlanExportFailed);
            }
        }
    }

    public ReadOnlyBooleanProperty plankProblemValidProperty() {
        return plankProblemValid.getReadOnlyProperty();
    }

    public boolean isPlankProblemValid() {
        return plankProblemValidProperty().get();
    }

    public ReadOnlyBooleanProperty plankProblemSavedProperty() {
        return plankProblemSaved;
    }

    public boolean isPlankProblemSaved() {
        return plankProblemSavedProperty().get();
    }
}
