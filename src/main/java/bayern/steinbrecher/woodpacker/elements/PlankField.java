package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankGrainDirection;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankField extends Control {
    private final IntegerProperty plankWidth = new SimpleIntegerProperty();
    private final IntegerProperty plankHeight = new SimpleIntegerProperty();
    private final ObjectProperty<PlankGrainDirection> grainDirection = new SimpleObjectProperty<>();

    public PlankField() {
        super();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankFieldSkin(this);
    }

    public IntegerProperty plankWidthProperty() {
        return plankWidth;
    }

    public int getPlankWidth() {
        return plankWidthProperty().get();
    }

    public void setPlankWidth(int plankWidth) {
        plankWidthProperty().set(plankWidth);
    }

    public IntegerProperty plankHeightProperty() {
        return plankHeight;
    }

    public int getPlankHeight() {
        return plankHeightProperty().get();
    }

    public void setPlankHeight(int plankHeight) {
        plankHeightProperty().set(plankHeight);
    }

    public ObjectProperty<PlankGrainDirection> grainDirectionProperty() {
        return grainDirection;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirectionProperty().get();
    }

    public void setGrainDirection(PlankGrainDirection direction) {
        grainDirectionProperty().set(direction);
    }

    public Plank createPlank() {
        return new Plank(getPlankWidth(), getPlankHeight(), getGrainDirection());
    }
}
