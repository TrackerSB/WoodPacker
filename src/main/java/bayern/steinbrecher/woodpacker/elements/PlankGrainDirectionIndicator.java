package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankGrainDirectionIndicator extends Control {
    private final ReadOnlyObjectWrapper<PlankGrainDirection> value
            = new ReadOnlyObjectWrapper<>(PlankGrainDirection.IRRELEVANT);
    private final ReadOnlyBooleanWrapper inAutoMode = new ReadOnlyBooleanWrapper();
    private final PlankField<?> autoConnection;

    /**
     * @param autoConnection Iff not {@code null} then the automatic grain direction determination is connected to the
     *                       given {@link PlankField}.
     */
    public PlankGrainDirectionIndicator(final PlankField<?> autoConnection) {
        super();
        this.autoConnection = autoConnection;
        if (autoConnection != null) {
            autoConnection.grainDirectionProperty()
                    .bindBidirectional(value);
        }
        enableAutoMode();
    }

    @Override
    protected Skin<PlankGrainDirectionIndicator> createDefaultSkin() {
        return new PlankGrainDirectionIndicatorSkin(this, autoConnection);
    }

    public ReadOnlyBooleanProperty inAutoModeProperty() {
        return inAutoMode;
    }

    public boolean isInAutoMode() {
        return inAutoModeProperty()
                .get();
    }

    /* NOTE 2022-05-05: It has to be ensured to not bypass the corresponding setter (i.e. to update the auto mode
     * property)
     */
    public ReadOnlyObjectProperty<PlankGrainDirection> valueProperty() {
        return value.getReadOnlyProperty();
    }

    public PlankGrainDirection getValue() {
        return valueProperty().get();
    }

    public void setValue(final PlankGrainDirection direction) {
        inAutoMode.set(false);
        value.set(direction);
    }

    // WARNING 2022-05-03: Only skins of this element are supposed to call this method
    void enableAutoMode() {
        inAutoMode.setValue(autoConnection != null);
    }

    // WARNING 2022-05-03: Only skins of this element are supposed to call this method
    void setAutoValue(final PlankGrainDirection direction) {
        assert isInAutoMode() : "This method must only be used if the indicator is in auto mode";
        value.set(direction);
    }
}
