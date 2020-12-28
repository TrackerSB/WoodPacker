package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankGrainDirectionIndicator extends Control {
    private final ObjectProperty<PlankGrainDirection> value
            = new SimpleObjectProperty<>(PlankGrainDirection.IRRELEVANT);

    public PlankGrainDirectionIndicator() {
        super();
    }

    @Override
    protected Skin<PlankGrainDirectionIndicator> createDefaultSkin() {
        return new PlankGrainDirectionIndicatorSkin(this);
    }

    public ObjectProperty<PlankGrainDirection> valueProperty() {
        return value;
    }

    public PlankGrainDirection getValue() {
        return valueProperty().get();
    }

    public void setValue(PlankGrainDirection direction) {
        valueProperty().set(direction);
    }
}
