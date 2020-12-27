package bayern.steinbrecher.woodPacker.elements;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.woodPacker.WoodPacker;
import bayern.steinbrecher.woodPacker.data.PlankGrainDirection;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankFieldSkin extends SkinBase<PlankField> {
    private final ReadOnlyIntegerWrapper plankWidth = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper plankHeight = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<PlankGrainDirection> grainDirection = new ReadOnlyObjectWrapper<>();

    protected PlankFieldSkin(PlankField control) {
        super(control);

        CheckedIntegerSpinner widthField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        widthField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString("width"));
        widthField.setEditable(true);
        plankWidth.bind(widthField.valueProperty());

        Label separator = new Label("\u2a09");

        CheckedIntegerSpinner heightField = new CheckedIntegerSpinner(1, Integer.MAX_VALUE, 1000, 1);
        heightField.setPromptText(WoodPacker.LANGUAGE_BUNDLE.getString("height"));
        heightField.setEditable(true);
        plankHeight.bind(heightField.valueProperty());

        PlankGrainDirectionIndicator indicator = new PlankGrainDirectionIndicator();
        grainDirection.bind(indicator.valueProperty());

        getChildren()
                .add(new HBox(widthField, separator, heightField, indicator));
    }

    public ReadOnlyIntegerProperty plankWidthProperty() {
        return plankWidth.getReadOnlyProperty();
    }

    public int getPlankWidth() {
        return plankWidthProperty().get();
    }

    public ReadOnlyIntegerProperty plankHeightProperty() {
        return plankHeight.getReadOnlyProperty();
    }

    public int getPlankHeight() {
        return plankHeightProperty().get();
    }

    public ReadOnlyObjectProperty<PlankGrainDirection> grainDirectionProperty() {
        return grainDirection.getReadOnlyProperty();
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirectionProperty().get();
    }
}
