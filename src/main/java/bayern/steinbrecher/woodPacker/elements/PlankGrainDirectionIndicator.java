package bayern.steinbrecher.woodPacker.elements;

import bayern.steinbrecher.woodPacker.data.PlankGrainDirection;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankGrainDirectionIndicator extends Control {
    public PlankGrainDirectionIndicator() {
        super();
    }

    @Override
    protected Skin<PlankGrainDirectionIndicator> createDefaultSkin() {
        return new PlankGrainDirectionIndicatorSkin(this);
    }

    private PlankGrainDirectionIndicatorSkin getCastedSkin() {
        Skin<?> uncastedSkin = getSkin();
        if (uncastedSkin instanceof PlankGrainDirectionIndicatorSkin) {
            return (PlankGrainDirectionIndicatorSkin) uncastedSkin;
        } else {
            throw new IllegalStateException(
                    String.format("The currently set skin is not of type '%s'",
                            uncastedSkin.getClass().getCanonicalName()));
        }
    }

    public ReadOnlyObjectProperty<PlankGrainDirection> valueProperty() {
        return getCastedSkin().plankGrainDirectionProperty();
    }

    public PlankGrainDirection getValue() {
        return getCastedSkin().getPlankGrainDirection();
    }
}
