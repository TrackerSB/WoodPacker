package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.woodpacker.WoodPacker;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankFieldSkin extends SkinBase<PlankField> {
    protected PlankFieldSkin(PlankField control) {
        super(control);

        CheckedIntegerSpinner widthField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        widthField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString("width"));
        widthField.setEditable(true);
        control.plankWidthProperty()
                .bind(widthField.valueProperty());
        control.plankWidthProperty()
                .addListener((ob, oldWidth, newWidth) -> {
                    widthField.getEditor()
                            .setText(String.valueOf(newWidth));
                });

        Label separator = new Label("\u2a09");

        CheckedIntegerSpinner heightField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
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

        getChildren()
                .add(new HBox(widthField, separator, heightField, indicator));
    }
}
