package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
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
    private final BooleanProperty valueUserDefined = new SimpleBooleanProperty(false);
    private final ReadOnlyBooleanWrapper inAutoMode = new ReadOnlyBooleanWrapper();
    private final PlankField<?> autoConnection;

    /**
     * @param autoConnection Iff not {@code null} then the automatic grain direction determination is connected to the
     *                       given {@link PlankField}.
     */
    public PlankGrainDirectionIndicator(final PlankField<?> autoConnection) {
        this.autoConnection = autoConnection;
        if (autoConnection == null) {
            inAutoMode.set(false);
        } else {
            inAutoMode.bind(valueUserDefinedProperty().not());
        }
    }

    @Override
    protected Skin<PlankGrainDirectionIndicator> createDefaultSkin() {
        return new PlankGrainDirectionIndicatorSkin(this);
    }

    public BooleanProperty valueUserDefinedProperty() {
        return valueUserDefined;
    }

    public boolean isValueUserDefined() {
        return valueUserDefinedProperty()
                .get();
    }

    public void setValueUserDefined(final boolean valueUserDefined) {
        valueUserDefinedProperty()
                .set(valueUserDefined);
    }

    public ReadOnlyBooleanProperty inAutoModeProperty() {
        return inAutoMode;
    }

    public boolean isInAutoMode() {
        return inAutoModeProperty()
                .get();
    }

    public ObjectProperty<PlankGrainDirection> valueProperty() {
        return value;
    }

    public PlankGrainDirection getValue() {
        return valueProperty().get();
    }

    public void setValue(final PlankGrainDirection direction) {
        valueProperty().set(direction);
    }

    public PlankField<?> getAutoConnection() {
        return autoConnection;
    }
}
