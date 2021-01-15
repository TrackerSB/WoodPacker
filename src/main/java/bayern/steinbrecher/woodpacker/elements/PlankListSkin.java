package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.report.ReportEntry;
import bayern.steinbrecher.checkedElements.report.ReportType;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Optional;

public class PlankListSkin extends SkinBase<PlankList> {
    private static final double ID_BADGE_MIN_WIDTH = 50;
    private static final double ID_BADGE_PADDING = 5;
    private static final Font ID_BADGE_FONT = Font.font(15);

    private final BooleanProperty basePlankNameAlreadyExists = new SimpleBooleanProperty();

    private Node generateItemGraphic(Plank item) {
        Text idText = new Text(item.getId());
        idText.setFill(Color.WHITE);
        idText.setFont(ID_BADGE_FONT);
        double backgroundWidth = Math.max(idText.getBoundsInLocal().getWidth(), ID_BADGE_MIN_WIDTH)
                + (2 * ID_BADGE_PADDING);
        Rectangle idBackground = new Rectangle(backgroundWidth, 2 * ID_BADGE_FONT.getSize(), Color.BLACK);
        idBackground.setArcWidth(idBackground.getHeight() / 2);
        idBackground.setArcHeight(idBackground.getHeight() / 2);
        StackPane idBadge = new StackPane(idBackground, idText);
        ImageView grainDirectionIcon
                = PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection());
        HBox content = new HBox(idBadge, grainDirectionIcon);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(10);
        return content;
    }

    private Node createPlankView(PlankList control) {
        ListView<Plank> planksView = new ListView<>();
        // Sync control --> planksView
        ChangeListener<ObservableSet<Plank>> onPlanksChanged = (obs, previousSet, currentSet) -> {
            ObservableList<Plank> plankList = FXCollections.observableArrayList();
            plankList.addAll(currentSet);
            planksView.setItems(plankList);
            currentSet.addListener((SetChangeListener<? super Plank>) change -> {
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

        planksView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Plank item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText("");
                    setGraphic(null);
                    setContextMenu(null);
                } else {
                    setText(item.toString());
                    setGraphic(generateItemGraphic(item));
                    ImageView deletePlankItemGraphic
                            = new ImageView(getClass().getResource("trash.png").toExternalForm());
                    deletePlankItemGraphic.setFitHeight(20);
                    deletePlankItemGraphic.setPreserveRatio(true);
                    MenuItem deletePlankItem
                            = new MenuItem(WoodPacker.LANGUAGE_BUNDLE.getString("delete"), deletePlankItemGraphic);
                    deletePlankItem.setOnAction(evt -> control.getPlanks().remove(item));
                    setContextMenu(new ContextMenu(deletePlankItem));
                }
            }
        });
        planksView.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);

        // Sync visually selected plank <--> selected plank in model
        planksView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, previousItem, currentItem) -> control.setSelectedPlank(currentItem));
        ChangeListener<Optional<Plank>> onModelSelectedPlankChanged = (obs, previouslySelected, currentlySelected) -> {
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

        ScrollPane scrollablePlanksView = new ScrollPane(planksView);
        scrollablePlanksView.setFitToWidth(true);
        scrollablePlanksView.setFitToHeight(true);
        VBox.setVgrow(scrollablePlanksView, Priority.ALWAYS);

        return scrollablePlanksView;
    }

    private void updateBasePlankNameAlreadyExists(PlankList control, String idForNewPlank) {
        basePlankNameAlreadyExists.set(
                control.getPlanks()
                        .stream()
                        .anyMatch(plank -> plank.getId().equals(idForNewPlank))
        );
    }

    private PlankField createNewPlankField(PlankList control) {
        PlankField newPlankField = new PlankField();

        // Add report checking whether a new base plank can be added with the currently specified data
        newPlankField.plankIdProperty()
                .addListener(
                        (observable, previousId, currentId) -> updateBasePlankNameAlreadyExists(control, currentId));
        ChangeListener<ObservableSet<Plank>> onItemsPropertyUpdate = (obs, previousItemList, currentItemList) -> {
            updateBasePlankNameAlreadyExists(control, newPlankField.getPlankId());
            currentItemList.addListener((InvalidationListener) obss
                    -> updateBasePlankNameAlreadyExists(control, newPlankField.getPlankId()));
        };
        control.planksProperty()
                .addListener(onItemsPropertyUpdate);
        onItemsPropertyUpdate.changed(null, null, control.getPlanks()); // Ensure initial state
        newPlankField.addReport(
                new ReportEntry("basePlankNameAlreadyExists", ReportType.ERROR, basePlankNameAlreadyExists));
        return newPlankField;
    }

    protected PlankListSkin(PlankList control) {
        super(control);

        Node plankView = createPlankView(control);
        PlankField newPlankField = createNewPlankField(control);

        ImageView addPlankGraphic = new ImageView(getClass().getResource("add.png").toExternalForm());
        addPlankGraphic.setFitHeight(20);
        addPlankGraphic.setPreserveRatio(true);
        Button addPlankButton = new Button(WoodPacker.LANGUAGE_BUNDLE.getString("add"), addPlankGraphic);
        newPlankField.validProperty()
                .addListener((obs, wasValid, isValid) -> addPlankButton.setDisable(!isValid));
        addPlankButton.setOnAction(aevt -> {
            if (newPlankField.isValid()) {
                control.getPlanks()
                        .add(newPlankField.createPlank(PlankMaterial.UNDEFINED)); // FIXME Specify correct material
                newPlankField.setPlankId("");
            }
        });
        ButtonBar.setButtonData(addPlankButton, ButtonData.APPLY);

        ImageView clearAllPlanksGraphic = new ImageView(getClass().getResource("clearedClipboard.png")
                .toExternalForm());
        clearAllPlanksGraphic.setFitHeight(20);
        clearAllPlanksGraphic.setPreserveRatio(true);
        Button clearAllPlanksButton
                = new Button(WoodPacker.LANGUAGE_BUNDLE.getString("clearAll"), clearAllPlanksGraphic);
        clearAllPlanksButton.setOnAction(aevt -> control.getPlanks().clear());

        ChangeListener<Boolean> onPlankListEmptyChanged = (obs, wasEmpty, isEmpty) -> {
            clearAllPlanksButton.setDisable(isEmpty);
        };
        control.planksProperty()
                .emptyProperty()
                .addListener(onPlankListEmptyChanged);
        onPlankListEmptyChanged.changed(null, null, control.getPlanks().isEmpty());

        ButtonBar actionsBar = new ButtonBar();
        actionsBar.getButtons()
                .addAll(addPlankButton, clearAllPlanksButton);

        VBox content = new VBox(plankView, newPlankField, actionsBar);
        content.setSpacing(5);
        content.setAlignment(Pos.TOP_LEFT);
        getChildren()
                .add(content);
    }
}
