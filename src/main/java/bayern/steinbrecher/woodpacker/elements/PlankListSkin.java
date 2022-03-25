package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogFactory;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.When;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlankListSkin<T extends Plank> extends SkinBase<PlankList<T>> {
    private static final Logger LOGGER = Logger.getLogger(PlankListSkin.class.getName());
    private static final double ID_BADGE_MIN_WIDTH = 50;
    private static final double ID_BADGE_PADDING = 5;
    private static final Font ID_BADGE_FONT = Font.font(15);

    private final BooleanProperty plankNameAlreadyExists = new SimpleBooleanProperty();

    private Node generateItemGraphic(final Plank item) {
        final Text idText = new Text(item.getPlankId());
        idText.setFill(Color.WHITE);
        idText.setFont(ID_BADGE_FONT);
        final double backgroundWidth = Math.max(idText.getBoundsInLocal().getWidth(), ID_BADGE_MIN_WIDTH)
                + (2 * ID_BADGE_PADDING);
        final Rectangle idBackground = new Rectangle(backgroundWidth, 2 * ID_BADGE_FONT.getSize(), Color.BLACK);
        idBackground.setArcWidth(idBackground.getHeight() / 2);
        idBackground.setArcHeight(idBackground.getHeight() / 2);
        final StackPane idBadge = new StackPane(idBackground, idText);
        final ImageView grainDirectionIcon
                = PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection());
        final HBox content = new HBox(idBadge, grainDirectionIcon);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        return content;
    }

    private void setupSyncPlanksView(
            final ListView<T> planksView, final PlankList<T> control, final TextField searchField) {
        final ChangeListener<ObservableSet<T>> onPlanksChanged = (obs, previousSet, currentSet) -> {
            final FilteredList<T> filterableItems = new FilteredList<>(FXCollections.observableArrayList(currentSet));
            searchField.textProperty()
                    .addListener((obss, previousSearchText, currentSearchText) -> {
                        String lowerCaseSearchText = currentSearchText
                                .toLowerCase(Locale.ROOT);
                        final T currentSelectedItem = planksView.getSelectionModel()
                                .getSelectedItem();
                        filterableItems.setPredicate(
                                plank -> {
                                    String lowerCaseId = plank.getPlankId()
                                            .toLowerCase(Locale.ROOT);
                                    String lowerCaseComment = plank.getComment()
                                            .toLowerCase(Locale.ROOT);
                                    return lowerCaseId.contains(lowerCaseSearchText)
                                            || lowerCaseComment.contains(lowerCaseSearchText);
                                });

                        // Ensure that previously selected item is still selected after the visibility of items changed
                        planksView.getSelectionModel()
                                .select(currentSelectedItem);
                    });
            planksView.setItems(filterableItems);
            currentSet.addListener((SetChangeListener<T>) change -> {
                // FIXME Add should FOLLOW remove
                if (change.wasAdded() && !planksView.getItems().contains(change.getElementAdded())) {
                    planksView.getItems()
                            .add(change.getElementAdded());
                }
                if (change.wasRemoved()) {
                    planksView.getItems()
                            .remove(change.getElementRemoved());
                }
            });
        };
        control.planksProperty()
                .addListener(onPlanksChanged);
        onPlanksChanged.changed(null, null, control.getPlanks()); // Ensure initial state
    }

    private void syncSelectedPlank(final PlankList<T> control, final ListView<T> planksView) {
        planksView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, previousItem, currentItem) -> control.setSelectedPlank(currentItem));
        final ChangeListener<Optional<T>> onModelSelectedPlankChanged =
                (obs, previouslySelected, currentlySelected) -> {
                    /* NOTE 2021-01-15: The underlying ListView does not guarantee the uniqueness of planks. If there is
                     * some bug which results in having multiple identical planks in the ListView then trying to select
                     * the "selected plank" in the model in the ListView may change the "selected plank" in the model
                     * once more due to its ambiguity. In this case an infinite recursion in the sync could be the
                     * result.
                     */
                    if (currentlySelected.isPresent()) {
                        if (!currentlySelected.get().equals(planksView.getSelectionModel().getSelectedItem())) {
                            planksView.getSelectionModel()
                                    .select(currentlySelected.get());
                        }
                    } else {
                        planksView.getSelectionModel()
                                .clearSelection();
                    }
                };
        control.selectedPlankProperty()
                .addListener(onModelSelectedPlankChanged);
        // Ensure initial state
        onModelSelectedPlankChanged.changed(null, null, control.getSelectedPlank());
    }

    private MenuItem createPlankViewContextItem(
            final String iconResourcePath, final String descriptionResourceKey,
            final EventHandler<ActionEvent> handler) {
        final ImageView itemGraphic = new ImageView(getClass().getResource(iconResourcePath).toExternalForm());
        itemGraphic.setFitHeight(20);
        itemGraphic.setPreserveRatio(true);
        final MenuItem menuItem = new MenuItem(WoodPacker.getResource(descriptionResourceKey), itemGraphic);
        menuItem.setOnAction(handler);
        return menuItem;
    }

    private Node createPlankView(
            final PlankList<T> control, final PlankField<T> plankField, final TextField searchField) {
        final ListView<T> planksView = new ListView<>();

        setupSyncPlanksView(planksView, control, searchField);

        planksView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(final T item, final boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("");
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    setText(item.toString());
                    if (item instanceof RequiredPlank requiredPlank) {
                        textFillProperty()
                                .bind(new When(requiredPlank.placedInSolutionProperty())
                                        .then(Color.BLACK)
                                        .otherwise(Color.RED));
                    } else {
                        textFillProperty()
                                .unbind();
                        setTextFill(Color.BLACK);
                    }

                    setGraphic(generateItemGraphic(item));

                    final Runnable setupEditing = () -> {
                        // WARN 2022-03-25: Has to be set before changing height and width
                        plankField.setInAutoGrainDirectionMode(false);

                        plankField.setPlankId(item.getPlankId());
                        plankField.setComment(item.getComment());
                        plankField.setPlankHeight(item.getHeight());
                        plankField.setPlankWidth(item.getWidth());
                        plankField.setGrainDirection(item.getGrainDirection());
                        if (item instanceof BasePlank basePlank) {
                            plankField.setSelectedMaterial(basePlank.getMaterial());
                        }
                        if (item instanceof RequiredPlank requiredPlank) {
                            plankField.setEdgeBands(requiredPlank.getEdgeBands());
                            plankField.setEdgeBandThickness(requiredPlank.getEdgeBandThickness());
                        }
                    };

                    final MenuItem editPlankItem = createPlankViewContextItem(
                            "edit.png", "edit", evt -> setupEditing.run());
                    final MenuItem deletePlankItem = createPlankViewContextItem(
                            "trash.png", "delete", evt -> control.getPlanks().remove(item));
                    setContextMenu(new ContextMenu(editPlankItem, deletePlankItem));

                    setOnMouseClicked(evt -> {
                        if (evt.getButton() == MouseButton.PRIMARY
                                && evt.getClickCount() >= 2) { // NOPMD - Filter for double clicks
                            setupEditing.run();
                        }
                    });
                }
            }
        });
        planksView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);
        planksView.setPlaceholder(new Label(WoodPacker.getResource("noPlanksAdded")));

        syncSelectedPlank(control, planksView);

        final ScrollPane scrollablePlanksView = new ScrollPane(planksView);
        scrollablePlanksView.setFitToWidth(true);
        scrollablePlanksView.setFitToHeight(true);
        VBox.setVgrow(scrollablePlanksView, Priority.ALWAYS);

        return scrollablePlanksView;
    }

    private void updatePlankNameAlreadyExists(final PlankList<?> control, final String idForNewPlank) {
        plankNameAlreadyExists.set(
                control.getPlanks()
                        .stream()
                        .anyMatch(plank -> plank.getPlankId().equals(idForNewPlank))
        );
    }

    private PlankField<T> createNewPlankField(final PlankList<T> control, final Class<T> genericRuntimeType) {
        final PlankField<T> newPlankField = new PlankField<>(genericRuntimeType);

        // Add report checking whether a new base plank can be added with the currently specified data
        newPlankField.plankIdProperty()
                .addListener(
                        (observable, previousId, currentId) -> updatePlankNameAlreadyExists(control, currentId));
        final ChangeListener<ObservableSet<T>> onItemsPropertyUpdate = (obs, previousItemList, currentItemList) -> {
            updatePlankNameAlreadyExists(control, newPlankField.getPlankId());
            currentItemList.addListener((InvalidationListener) obss
                    -> updatePlankNameAlreadyExists(control, newPlankField.getPlankId()));
        };
        control.planksProperty()
                .addListener(onItemsPropertyUpdate);
        onItemsPropertyUpdate.changed(null, null, control.getPlanks()); // Ensure initial state
        return newPlankField;
    }

    private Node createUpdatePlankViewControl(final PlankList<T> control, final PlankField<T> plankField) {
        final Image addPlankImage = new Image(getClass().getResource("add.png").toExternalForm());
        final String addPlankLabel = WoodPacker.getResource("add");
        final Image replacePlankImage = new Image(getClass().getResource("replace.png").toExternalForm());
        final String replacePlankLabel = WoodPacker.getResource("replace");

        final ImageView updatePlankViewGraphic = new ImageView();
        updatePlankViewGraphic.imageProperty()
                .bind(new When(plankNameAlreadyExists).then(replacePlankImage).otherwise(addPlankImage));
        updatePlankViewGraphic.setFitHeight(20);
        updatePlankViewGraphic.setPreserveRatio(true);
        final Button updatePlankViewButton = new Button("<not set>", updatePlankViewGraphic);
        updatePlankViewButton.textProperty()
                .bind(new When(plankNameAlreadyExists).then(replacePlankLabel).otherwise(addPlankLabel));
        updatePlankViewButton.setOnAction(aevt -> {
            if (plankField.isValid()) {
                final ObservableSet<T> currentPlanks = control.getPlanks();
                final T newPlank = plankField.createPlank();
                currentPlanks.removeIf(i -> Objects.equals(i.getPlankId(), newPlank.getPlankId()));
                currentPlanks.add(newPlank);
                plankField.reset();
            }
        });
        ButtonBar.setButtonData(updatePlankViewButton, ButtonData.APPLY);

        final ChangeListener<Boolean> onValidityChanged = (obs, wasValid, isValid) -> updatePlankViewButton.setDisable(!isValid);
        plankField.validProperty()
                .addListener(onValidityChanged);
        onValidityChanged.changed(null, null, plankField.isValid()); // Ensure initial state

        return updatePlankViewButton;
    }

    protected PlankListSkin(final PlankList<T> control, final Class<T> genericRuntimeType) {
        super(control);

        final TextField searchField = new TextField();
        searchField.setPromptText(WoodPacker.getResource("searchFor"));
        final PlankField<T> newPlankField = createNewPlankField(control, genericRuntimeType);
        final Node plankView = createPlankView(control, newPlankField, searchField);

        final Node updatePlankViewButton = createUpdatePlankViewControl(control, newPlankField);

        final ImageView clearAllPlanksGraphic = new ImageView(
                getClass().getResource("clearedClipboard.png").toExternalForm());
        clearAllPlanksGraphic.setFitHeight(20);
        clearAllPlanksGraphic.setPreserveRatio(true);
        final Button clearAllPlanksButton
                = new Button(WoodPacker.getResource("clearAll"), clearAllPlanksGraphic);
        clearAllPlanksButton.setOnAction(aevt -> {
            boolean clearAllConfirmed;
            try {
                final Alert confirmClearAllAlert = WoodPacker.getDialogFactory()
                        .createInteractiveAlert(AlertType.WARNING, "confirmClearAll", ButtonType.YES, ButtonType.NO);
                final Optional<ButtonType> pressedButton = DialogFactory.showAndWait(confirmClearAllAlert);
                clearAllConfirmed = pressedButton.isPresent()
                        && pressedButton.get() == ButtonType.YES;
            } catch (DialogCreationException ex) {
                LOGGER.log(Level.WARNING, "Could not ask user for delete confirmation. Assume deletion confirmed.", ex);
                clearAllConfirmed = true;
            }
            if (clearAllConfirmed) {
                control.getPlanks().clear();
            }
        });

        final ChangeListener<Boolean> onPlankListEmptyChanged
                = (obs, wasEmpty, isEmpty) -> clearAllPlanksButton.setDisable(isEmpty);
        control.planksProperty()
                .emptyProperty()
                .addListener(onPlankListEmptyChanged);
        onPlankListEmptyChanged.changed(null, null, control.getPlanks().isEmpty());

        final ButtonBar actionsBar = new ButtonBar();
        actionsBar.getButtons()
                .addAll(updatePlankViewButton, clearAllPlanksButton);

        final VBox content = new VBox(searchField, plankView, newPlankField, actionsBar);
        content.setSpacing(5);
        content.setAlignment(Pos.TOP_LEFT);
        getChildren()
                .add(content);
    }
}
