package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
public class PlankFieldSkin extends SkinBase<PlankField> {
    private final BooleanProperty indicatorChangedByUser = new SimpleBooleanProperty(false);

    private Node createGrainIndicator(PlankField control) {
        PlankGrainDirectionIndicator indicator = new PlankGrainDirectionIndicator();
        control.grainDirectionProperty()
                .bindBidirectional(indicator.valueProperty());

        Rectangle autoStateBackground = new Rectangle();
        autoStateBackground.setFill(Color.rgb(255, 255, 255, 0.5));
        autoStateBackground.widthProperty()
                .bind(indicator.widthProperty());
        autoStateBackground.heightProperty()
                .bind(indicator.heightProperty());
        autoStateBackground.visibleProperty()
                .bind(indicatorChangedByUser.not());

        Text autoStateText = new Text("A");
        autoStateText.visibleProperty()
                .bind(indicatorChangedByUser.not());

        StackPane indicatorNode = new StackPane(indicator, autoStateBackground, autoStateText);
        indicatorNode.setOnMouseClicked(mevt -> {
            indicatorChangedByUser.set(true);
            indicator.fireEvent(mevt); // FIXME This event does not reach the underlying button
        });

        return indicatorNode;
    }

    /**
     * @param forWidth {@code true} for width field; {@code false} for height field
     */
    private Node createLengthField(PlankField control, boolean forWidth) {
        CheckedIntegerSpinner lengthField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        // @formatter: off
        // widthField.setStyle("-fx-background-image: url('/bayern/steinbrecher/woodpacker/elements/plankWidth.png');"); // FIXME Icon not showing up
        // heightField.setStyle("-fx-background-image: url('/bayern/steinbrecher/woodpacker/elements/plankHeight.png');"); // FIXME Icon not showing up
        // @formatter: on
        lengthField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString(forWidth ? "width" : "height"));
        lengthField.setEditable(true);
        IntegerProperty lengthProperty = forWidth ? control.plankWidthProperty() : control.plankHeightProperty();
        lengthProperty.bind(lengthField.valueProperty());
        lengthProperty.addListener((ob, oldLength, newLength) -> {
            lengthField.getEditor()
                    .setText(String.valueOf(newLength));
        });
        control.addValidityConstraint(lengthField.validProperty());

        String externalIconPath = getClass()
                .getResource(forWidth ? "plankWidth.png" : "plankHeight.png")
                .toExternalForm();
        ImageView lengthIcon = new ImageView(externalIconPath);
        lengthIcon.setPreserveRatio(true);
        lengthIcon.fitHeightProperty()
                .bind(lengthField.heightProperty());

        HBox content = new HBox(lengthIcon, lengthField);
        content.setAlignment(Pos.CENTER_LEFT);
        return content;
    }

    private Node createCommentField(PlankField control) {
        TextField commentField = new TextField();
        commentField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString("description"));
        control.commentProperty()
                .bindBidirectional(commentField.textProperty());

        String externalIconPath = getClass()
                .getResource("notepad.png")
                .toExternalForm();
        ImageView commentIcon = new ImageView(externalIconPath);
        commentIcon.setPreserveRatio(true);
        commentIcon.fitHeightProperty()
                .bind(commentField.heightProperty());
        HBox content = new HBox(commentIcon, commentField);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(5);
        return content;
    }

    protected PlankFieldSkin(PlankField control) {
        super(control);

        BiConsumer<Integer, Integer> autoUpdateIndicator = (plankWidth, plankHeight) -> {
            if (!indicatorChangedByUser.get()) {
                control.setGrainDirection(
                        (plankHeight > plankWidth)
                                ? PlankGrainDirection.VERTICAL
                                : PlankGrainDirection.HORIZONTAL);
            }
        };

        Node widthField = createLengthField(control, true);
        control.plankWidthProperty()
                .addListener((ob, oldWidth, newWidth) -> {
                    autoUpdateIndicator.accept(newWidth.intValue(), control.getPlankHeight());
                });

        Label separator = new Label("\u2a09");

        Node heightField = createLengthField(control, false);
        control.plankHeightProperty()
                .addListener((ob, oldHeight, newHeight) -> {
                    autoUpdateIndicator.accept(control.getPlankWidth(), newHeight.intValue());
                });

        Node commentField = createCommentField(control);

        Node indicatorNode = createGrainIndicator(control);

        HBox sizeRow = new HBox(widthField, separator, heightField);
        sizeRow.setAlignment(Pos.CENTER_LEFT);

        HBox propertyRow = new HBox(commentField, indicatorNode);
        propertyRow.setAlignment(Pos.CENTER_LEFT);
        propertyRow.setSpacing(5);

        VBox content = new VBox(sizeRow, propertyRow);
        content.setAlignment(Pos.TOP_LEFT);
        content.setSpacing(5);

        getChildren()
                .add(content);
    }
}
