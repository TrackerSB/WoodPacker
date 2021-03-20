package bayern.steinbrecher.woodpacker.screens;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogGenerator;
import bayern.steinbrecher.screenswitcher.ScreenController;
import bayern.steinbrecher.woodpacker.BuildConfig;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.CuttingPlan;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankProblem;
import bayern.steinbrecher.woodpacker.data.PlankSolutionCriterion;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import bayern.steinbrecher.woodpacker.elements.PlankList;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import bayern.steinbrecher.woodpacker.elements.SnapshotPagination;
import bayern.steinbrecher.woodpacker.utility.DrawActionGenerator;
import bayern.steinbrecher.woodpacker.utility.PredefinedFileChooser;
import bayern.steinbrecher.woodpacker.utility.SerializationUtility;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.property.AreaBreakType;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
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
    private SnapshotPagination cuttingPlanPages;
    @FXML
    private CheckedIntegerSpinner oversizeSpinner;
    @FXML
    private VBox criteriaPane;
    private final PlankProblem plankProblem = new PlankProblem();
    private final ReadOnlyBooleanWrapper plankProblemValid = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper plankProblemSaved = new ReadOnlyBooleanWrapper();

    private void readUserDefinedBasePlanks() {
        // FIXME Show graphical feedback to user in any case where a logger is used
        try {
            for (final String basePlankName : USER_DEFINED_BASE_PLANKS.keys()) {
                final byte[] serializedBasePlank = USER_DEFINED_BASE_PLANKS.getByteArray(basePlankName, null);
                if (serializedBasePlank == null) {
                    LOGGER.log(Level.WARNING,
                            String.format("The serialized data for '%s' is not available", basePlankName));
                } else {
                    try {
                        final BasePlank basePlank = SerializationUtility.deserialize(serializedBasePlank);
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
        final ObservableSet<BasePlank> sortedBasePlanks
                = FXCollections.observableSet(new TreeSet<>(basePlankList.getPlanks()));
        basePlankList.setPlanks(sortedBasePlanks);

        readUserDefinedBasePlanks();

        basePlankList.planksProperty()
                .addListener((SetChangeListener<? super Plank>) change -> {
                    if (change.wasAdded()) {
                        try {
                            USER_DEFINED_BASE_PLANKS.putByteArray(
                                    change.getElementAdded().getPlankId(),
                                    SerializationUtility.serialize(change.getElementAdded()));
                        } catch (IOException ex) {
                            LOGGER.log(Level.WARNING, "Could not persistently store new base plank", ex);
                            // FIXME Show stacktrace alert to user
                        }
                    }
                    if (change.wasRemoved()) {
                        USER_DEFINED_BASE_PLANKS.remove(change.getElementRemoved().getPlankId());
                    }
                    // FIXME Treat change.wasUpdated()?
                });

        // Sync selected base plank <--> plank problem base plank
        basePlankList.selectedPlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank)
                        -> plankProblem.setBasePlank(currentBasePlank.orElse(null)));
        plankProblem.basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank) -> {
                    if (currentBasePlank != null) {
                        basePlankList.getPlanks()
                                .add(currentBasePlank); // Ensure the base plank to select exists
                    }
                    basePlankList.setSelectedPlank(currentBasePlank);
                });
    }

    private void initializeRequiredPlanksList() {
        // Ensure planks being sorted
        final ObservableSet<RequiredPlank> sortedRequiredPlanks
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

    private void initializeSettingsPane() {
        // Sync oversize <--> plank problem
        oversizeSpinner.valueProperty()
                .addListener((obs, previousOversize, currentOversize)
                        -> plankProblem.setBasePlankOversize(Objects.requireNonNullElse(currentOversize, 0)));
        plankProblem.basePlankOversizeProperty()
                .addListener((obs, previousOversize, currentOversize)
                        -> oversizeSpinner.getValueFactory().setValue(currentOversize.intValue()));
    }

    private void initializeCriteriaPane() {
        for (final PlankSolutionCriterion criterion : PlankSolutionCriterion.values()) {
            final Slider weightControl = new Slider(0, 10, plankProblem.getCriterionWeight(criterion)); // NOPMD
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

            final String criterionDescription = WoodPacker.getResource(criterion.getResourceKey());
            criteriaPane.getChildren()
                    .addAll(new Label(criterionDescription), weightControl); // NOPMD
        }
    }

    private void updateVisualPlankCuttingPlans(final Collection<CuttingPlan> cuttingPlans) {
        // Update current cutting plan preview
        if (cuttingPlans.isEmpty()) {
            cuttingPlanPages.setPageCount(1);
            cuttingPlanPages.setPageFactory(pageIndex -> {
                final ScaledCanvas cuttingPlanCanvas = new ScaledCanvas();
                if (plankProblem.getBasePlank() == null) {
                    cuttingPlanCanvas.theoreticalWidthProperty()
                            .bind(cuttingPlanCanvas.widthProperty());
                    cuttingPlanCanvas.theoreticalHeightProperty()
                            .bind(cuttingPlanCanvas.heightProperty());
                    cuttingPlanCanvas.setDrawingActions(
                            DrawActionGenerator.forPlaceholder(cuttingPlanCanvas,
                                    WoodPacker.getResource("noBasePlankSelected")));
                } else {
                    cuttingPlanCanvas.theoreticalWidthProperty()
                            .unbind();
                    cuttingPlanCanvas.setTheoreticalWidth(plankProblem.getBasePlank().getWidth());
                    cuttingPlanCanvas.theoreticalHeightProperty()
                            .unbind();
                    cuttingPlanCanvas.setTheoreticalHeight(plankProblem.getBasePlank().getHeight());
                    cuttingPlanCanvas.setDrawingActions(
                            DrawActionGenerator.forBasePlank(
                                    plankProblem.getBasePlank(), plankProblem.getBasePlankOversize()));
                }
                return cuttingPlanCanvas;
            });
        } else {
            // NOTE Page index implicitly given by index in list
            final List<Supplier<Node>> pageActions = new ArrayList<>();
            for (final CuttingPlan plan : cuttingPlans) {
                pageActions.add(() -> {
                    final ScaledCanvas cuttingPlanCanvas = new ScaledCanvas();
                    cuttingPlanCanvas.theoreticalWidthProperty()
                            .unbind();
                    cuttingPlanCanvas.setTheoreticalWidth(plan.getBasePlank().getWidth());
                    cuttingPlanCanvas.theoreticalHeightProperty()
                            .unbind();
                    cuttingPlanCanvas.setTheoreticalHeight(plan.getBasePlank().getHeight());
                    cuttingPlanCanvas.setDrawingActions(DrawActionGenerator.forCuttingPlan(plan));
                    return cuttingPlanCanvas;
                });
            }
            cuttingPlanPages.setPageCount(pageActions.size());
            cuttingPlanPages.setPageFactory(index -> pageActions.get(index).get());
        }
    }

    private void setupCuttingPlanPreviewUpdates() {
        // Trigger updates of visual cutting plank
        plankProblem.proposedSolutionProperty()
                .addListener((obs, oldSolution, newSolution) -> {
                    updateVisualPlankCuttingPlans(newSolution.getKey());
                    plankProblemSaved.set(false);
                });
        /* TODO Can the following listeners be included included into proposed solution such that it triggers a visual
         * cutting plan preview update as well?
         */
        plankProblemValid.addListener(
                obs -> updateVisualPlankCuttingPlans(plankProblem.getProposedSolution().getKey()));
        plankProblem.basePlankOversizeProperty()
                .addListener(
                        obs -> updateVisualPlankCuttingPlans(plankProblem.getProposedSolution().getKey()));

        // Ensure initial state
        final Pair<Collection<CuttingPlan>, Set<RequiredPlank>> proposedSolution = plankProblem.getProposedSolution();
        updateVisualPlankCuttingPlans(proposedSolution.getKey());

        /* Create binding signaling whether the described plank problem is valid. NOTE This does not imply it yields
         * a cutting plan
         */
        plankProblemValid.bind(
                plankProblem.basePlankProperty().isNotNull()
                        .and(plankProblem.requiredPlanksProperty().emptyProperty().not()));
    }

    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void initialize() {
        initializeBasePlankList();
        initializeRequiredPlanksList();
        initializeSettingsPane();
        initializeCriteriaPane();
        setupCuttingPlanPreviewUpdates();
    }

    public void loadPlankProblem(final PlankProblem setup) {
        plankProblem.setBasePlank(setup.getBasePlank());
        plankProblem.setRequiredPlanks(setup.getRequiredPlanks());
        plankProblem.criterionWeightsProperty()
                .putAll(setup.criterionWeightsProperty());
        plankProblem.setBasePlankOversize(setup.getBasePlankOversize());
        plankProblemSaved.set(true);
    }

    @SuppressWarnings("unused")
    @FXML
    private boolean askUserExportPlankProblem() throws DialogCreationException {
        boolean exportSucceeded = false;
        final Optional<File> exportFile = PredefinedFileChooser.PLANK_PROBLEM
                .askForSavePath(requiredPlanksView.getScene().getWindow());
        if (exportFile.isPresent()) {
            try {
                final byte[] serializedSnapshot = SerializationUtility.serialize(plankProblem);
                Files.write(exportFile.get().toPath(), serializedSnapshot);
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE,
                        String.format("Could export plank problem to '%s'", exportFile.get().getAbsolutePath()));
                final Alert exportFailedAlert = WoodPacker.getDialogGenerator()
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
                final Alert unsavedChangesAlert = WoodPacker.getDialogGenerator()
                        .createInteractiveAlert(
                                AlertType.CONFIRMATION, WoodPacker.getResource("unsavedChanges"),
                                ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
                final Optional<ButtonType> userResponse = DialogGenerator.showAndWait(unsavedChangesAlert);
                if (userResponse.isPresent()) {
                    final ButtonType buttonType = userResponse.get();
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

    private Image generatePDFImage(final WritableImage snapshot) throws IOException {
        final BufferedImage bufferedSnapshot = SwingFXUtils.fromFXImage(snapshot, null);
        final boolean rotateVertical = bufferedSnapshot.getWidth() > bufferedSnapshot.getHeight();
        BufferedImage monochromeSnapshot;
        if (rotateVertical) {
            monochromeSnapshot = new BufferedImage(
                    bufferedSnapshot.getHeight(), bufferedSnapshot.getWidth(), BufferedImage.TYPE_BYTE_BINARY);
        } else {
            monochromeSnapshot = new BufferedImage(
                    bufferedSnapshot.getWidth(), bufferedSnapshot.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        }
        final Graphics2D monochromeSnapshotGraphics = monochromeSnapshot.createGraphics();
        if (rotateVertical) {
            // Move origin to right upper corner
            monochromeSnapshotGraphics.translate(
                    monochromeSnapshot.getWidth() / 2d, monochromeSnapshot.getHeight() / 2d);
            monochromeSnapshotGraphics.rotate(Math.PI / 2);
            monochromeSnapshotGraphics.translate(
                    -monochromeSnapshot.getHeight() / 2d, -monochromeSnapshot.getWidth() / 2d);
        }
        monochromeSnapshotGraphics.drawImage(bufferedSnapshot, 0, 0, null);
        final ByteArrayOutputStream snapshotByteStream = new ByteArrayOutputStream();
        ImageIO.write(monochromeSnapshot, "png", snapshotByteStream);
        final Image snapshotImage = new Image(ImageDataFactory.create(snapshotByteStream.toByteArray()));
        snapshotImage.setAutoScale(true);
        return snapshotImage;
    }

    private void generateCuttingPlanDocument(final File savePath) throws DialogCreationException {
        try (Document document = new Document(new PdfDocument(new PdfWriter(savePath)))) {
            final PdfDocument pdfDocument = document.getPdfDocument();
            final PdfDocumentInfo documentInfo = pdfDocument.getDocumentInfo();
            documentInfo.setCreator(BuildConfig.APP_NAME + " " + BuildConfig.APP_VERSION);

            final PageSize pageSize = pdfDocument.getDefaultPageSize();
            final List<WritableImage> contentSnapshots = cuttingPlanPages.snapshotContents(new SnapshotParameters());
            for (final WritableImage snapshot : contentSnapshots) {
                final Image cuttingPlan;
                try {
                    cuttingPlan = generatePDFImage(snapshot);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not export all cutting plans", ex);
                    // FIXME Inform user about non exported cutting plans
                    continue;
                }
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
                cuttingPlan.scaleToFit(pageSize.getWidth(), pageSize.getHeight());
                final float leftMargin = (pageSize.getWidth() - cuttingPlan.getImageScaledWidth()) / 2;
                cuttingPlan.setMarginLeft(Math.max(0, leftMargin));
                document.add(cuttingPlan);
            }

            try {
                Desktop.getDesktop()
                        .open(savePath);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Could not open exported cutting plan to user", ex);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE,
                    String.format("Could not open '%s' for writing", savePath.getAbsolutePath()), ex);
            final Alert writeAccessDeniedAlert = WoodPacker.getDialogGenerator()
                    .createErrorAlert(WoodPacker.getResource("writeAccessDenied", savePath.getAbsolutePath()));
            DialogGenerator.showAndWait(writeAccessDeniedAlert);
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private void exportPreview() {
        final Optional<File> savePath = PredefinedFileChooser.CUTTING_PLAN
                .askForSavePath(requiredPlanksView.getScene().getWindow());
        savePath.ifPresent(file -> new Thread(() -> {
            getScreenManager()
                    .showOverlay(WoodPacker.getResource("creatingCuttingPlanDocument"));
            try {
                generateCuttingPlanDocument(file);
            } catch (DialogCreationException ex) {
                LOGGER.log(Level.WARNING, "Could not show exception to user", ex);
            }
            getScreenManager()
                    .hideOverlay();
        }).start());
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
