package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.woodpacker.WoodPacker;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.HBox;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankFieldSkin extends SkinBase<PlankField> {
    protected PlankFieldSkin(PlankField control) {
        super(control);

        CheckedIntegerSpinner widthField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        // widthField.setStyle("-fx-background-image: url('/bayern/steinbrecher/woodpacker/elements/plankWidth.png');"); // FIXME Icon not showing up
        widthField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString("width"));
        widthField.setEditable(true);
        control.plankWidthProperty()
                .bind(widthField.valueProperty());
        control.plankWidthProperty()
                .addListener((ob, oldWidth, newWidth) -> {
                    widthField.getEditor()
                            .setText(String.valueOf(newWidth));
                });

        CheckedIntegerSpinner heightField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        // heightField.setStyle("-fx-background-image: url('/bayern/steinbrecher/woodpacker/elements/plankHeight.png');"); // FIXME Icon not showing up
        heightField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString("height"));
        heightField.setEditable(true);
        control.plankHeightProperty()
                .bind(heightField.valueProperty());
        control.plankHeightProperty()
                .addListener((ob, oldHeight, newHeight) -> {
                    heightField.getEditor()
                            .setText(String.valueOf(newHeight));
                });

        PlankGrainDirectionIndicator indicator = new PlankGrainDirectionIndicator();
        control.grainDirectionProperty()
                .bindBidirectional(indicator.valueProperty());

        Label separator = new Label("\u2a09");

        ImageView widthIcon = new ImageView(getClass().getResource("plankWidth.png").toExternalForm());
        widthIcon.setPreserveRatio(true);
        widthIcon.fitHeightProperty()
                .bind(widthField.heightProperty());

        ImageView heightIcon = new ImageView(getClass().getResource("plankHeight.png").toExternalForm());
        heightIcon.setPreserveRatio(true);
        heightIcon.fitHeightProperty()
                .bind(heightField.heightProperty());

        HBox contentRow = new HBox(widthIcon, widthField, separator, heightIcon, heightField, indicator);
        contentRow.setAlignment(Pos.CENTER_LEFT);
        contentRow.setSpacing(5);
        getChildren()
                .add(contentRow);
    }
}
