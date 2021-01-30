package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.CheckedComboBox;
import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.checkedElements.textfields.CheckedTextField;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import bayern.steinbrecher.woodpacker.data.PlankMaterial;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

import java.util.function.BiConsumer;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankFieldSkin<T extends Plank> extends SkinBase<PlankField<T>> {
    private final BooleanProperty indicatorChangedByUser = new SimpleBooleanProperty(false);

    private Node createPlankIdField(final PlankField<T> control) {
        final CheckedTextField plankIdField = new CheckedTextField();
        plankIdField.setPromptText(WoodPacker.getResource("identifier"));
        plankIdField.textProperty()
                .bindBidirectional(control.plankIdProperty());
        control.addValidityConstraint(plankIdField.validProperty());

        final String externalIconPath = getClass()
                .getResource("bookmark.png")
                .toExternalForm();
        final ImageView idIcon = new ImageView(externalIconPath);
        idIcon.setPreserveRatio(true);
        idIcon.fitHeightProperty()
                .bind(plankIdField.heightProperty());
        return new HBox(idIcon, plankIdField);
    }

    private Node createGrainIndicator(final PlankField<T> control) {
        final PlankGrainDirectionIndicator indicator = new PlankGrainDirectionIndicator();
        control.grainDirectionProperty()
                .bindBidirectional(indicator.valueProperty());

        final Rectangle autoStateBackground = new Rectangle();
        autoStateBackground.setFill(Color.rgb(255, 255, 255, 0.5));
        autoStateBackground.widthProperty()
                .bind(indicator.widthProperty());
        autoStateBackground.heightProperty()
                .bind(indicator.heightProperty());
        autoStateBackground.visibleProperty()
                .bind(indicatorChangedByUser.not());

        final Text autoStateText = new Text("A");
        autoStateText.visibleProperty()
                .bind(indicatorChangedByUser.not());

        final StackPane indicatorNode = new StackPane(indicator, autoStateBackground, autoStateText);
        indicatorNode.setOnMouseClicked(mevt -> {
            indicatorChangedByUser.set(true);
            indicator.fireEvent(mevt); // FIXME This event does not reach the underlying button
        });

        return indicatorNode;
    }

    /**
     * @param forWidth {@code true} for width field; {@code false} for height field
     */
    private Node createLengthField(final PlankField<T> control, final boolean forWidth) {
        final CheckedIntegerSpinner lengthField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        // @formatter: off
        // widthField.setStyle("-fx-background-image: url('/bayern/steinbrecher/woodpacker/elements/plankWidth.png');"); // FIXME Icon not showing up
        // heightField.setStyle("-fx-background-image: url('/bayern/steinbrecher/woodpacker/elements/plankHeight.png');"); // FIXME Icon not showing up
        // @formatter: on
        lengthField.setPromptText(WoodPacker.getResource(forWidth ? "width" : "height"));
        lengthField.setEditable(true);
        final IntegerProperty lengthProperty = forWidth ? control.plankWidthProperty() : control.plankHeightProperty();
        lengthProperty.bind(lengthField.valueProperty());
        lengthProperty.addListener((ob, oldLength, newLength)
                -> lengthField.getEditor().setText(String.valueOf(newLength)));
        control.addValidityConstraint(lengthField.validProperty());

        final String externalIconPath = getClass()
                .getResource(forWidth ? "plankWidth.png" : "plankHeight.png")
                .toExternalForm();
        final ImageView lengthIcon = new ImageView(externalIconPath);
        lengthIcon.setPreserveRatio(true);
        lengthIcon.fitHeightProperty()
                .bind(lengthField.heightProperty());

        final HBox content = new HBox(lengthIcon, lengthField);
        content.setAlignment(Pos.CENTER_LEFT);
        return content;
    }

    private Node createCommentField(final PlankField<T> control) {
        final TextField commentField = new TextField();
        commentField.setPromptText(WoodPacker.getResource("description"));
        control.commentProperty()
                .bindBidirectional(commentField.textProperty());

        final String externalIconPath = getClass()
                .getResource("notepad.png")
                .toExternalForm();
        final ImageView commentIcon = new ImageView(externalIconPath);
        commentIcon.setPreserveRatio(true);
        commentIcon.fitHeightProperty()
                .bind(commentField.heightProperty());
        final HBox content = new HBox(commentIcon, commentField);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(5);
        return content;
    }

    private Node createMaterialSelection(final PlankField<T> control) {
        final CheckedComboBox<PlankMaterial> materialSelection
                = new CheckedComboBox<>(FXCollections.observableArrayList(PlankMaterial.values()));
        materialSelection.setEditable(false);
        materialSelection.getSelectionModel().select(PlankMaterial.UNDEFINED); // Ensure initial state
        materialSelection.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, previouslySelected, currentlySelected)
                        -> control.setSelectedMaterial(currentlySelected));
        control.selectedMaterialProperty()
                .addListener((obs, previouslySelected, currentlySelected)
                        -> materialSelection.getSelectionModel().select(currentlySelected));
        return materialSelection;
    }

    protected PlankFieldSkin(final PlankField<T> control, final Class<T> genericRuntimeType) {
        super(control);

        final Node plankIdField = createPlankIdField(control);

        final BiConsumer<Integer, Integer> autoUpdateIndicator = (plankWidth, plankHeight) -> {
            if (!indicatorChangedByUser.get()) {
                control.setGrainDirection(
                        (plankHeight > plankWidth)
                                ? PlankGrainDirection.VERTICAL
                                : PlankGrainDirection.HORIZONTAL);
            }
        };

        final Node widthField = createLengthField(control, true);
        control.plankWidthProperty()
                .addListener((ob, oldWidth, newWidth)
                        -> autoUpdateIndicator.accept(newWidth.intValue(), control.getPlankHeight()));

        final Label separator = new Label("\u2a09");

        final Node heightField = createLengthField(control, false);
        control.plankHeightProperty()
                .addListener((ob, oldHeight, newHeight)
                        -> autoUpdateIndicator.accept(control.getPlankWidth(), newHeight.intValue()));

        final HBox sizeRow = new HBox(widthField, separator, heightField);
        sizeRow.setAlignment(Pos.CENTER_LEFT);

        final HBox propertyRow = new HBox();
        propertyRow.setAlignment(Pos.CENTER_LEFT);
        propertyRow.setSpacing(5);
        final Node commentField = createCommentField(control);
        propertyRow.getChildren()
                .add(commentField);
        final Node indicatorNode = createGrainIndicator(control);
        propertyRow.getChildren()
                .add(indicatorNode);
        if (BasePlank.class.isAssignableFrom(genericRuntimeType)) {
            final Node materialSelection = createMaterialSelection(control);
            propertyRow.getChildren()
                    .add(materialSelection);
        }

        final VBox content = new VBox(plankIdField, sizeRow, propertyRow);
        content.setAlignment(Pos.TOP_LEFT);
        content.setSpacing(5);

        getChildren()
                .add(content);
    }
}
