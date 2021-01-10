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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PlankListSkin extends SkinBase<PlankList> {
    private final BooleanProperty basePlankNameAlreadyExists = new SimpleBooleanProperty();

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
                    ImageView grainDirectionIcon
                            = PlankGrainDirectionIndicatorSkin.generateImageView(item.getGrainDirection());
                    // FIXME Generate graphic showing grain direction and ID
                    // setGraphic(new HBox(createIdCircle(item.getId()), grainDirectionIcon));
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
        planksView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, previousItem, currentItem) -> control.setSelectedPlank(currentItem));

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

    protected PlankListSkin(PlankList control) {
        super(control);

        Node plankView = createPlankView(control);
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

        Button addPlankButton = new Button(WoodPacker.LANGUAGE_BUNDLE.getString("add"));
        newPlankField.validProperty()
                .addListener((obs, wasValid, isValid) -> addPlankButton.setDisable(!isValid));
        addPlankButton.setOnAction(aevt -> {
            if (newPlankField.isValid()) {
                control.getPlanks()
                        .add(newPlankField.createPlank(PlankMaterial.UNDEFINED)); // FIXME Specify correct material
                newPlankField.setPlankId("");
            }
        });
        ButtonBar actionsBar = new ButtonBar();
        actionsBar.getButtons()
                .addAll(addPlankButton);

        VBox content = new VBox(plankView, newPlankField, actionsBar);
        content.setSpacing(5);
        content.setAlignment(Pos.TOP_LEFT);
        getChildren()
                .add(content);
    }
}
