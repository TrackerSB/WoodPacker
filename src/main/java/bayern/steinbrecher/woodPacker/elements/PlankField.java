package bayern.steinbrecher.woodPacker.elements;

import bayern.steinbrecher.woodPacker.data.Plank;
import bayern.steinbrecher.woodPacker.data.PlankGrainDirection;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankField extends Control {
    private static final Logger LOGGER = Logger.getLogger(PlankField.class.getName());
    // TODO Avoid duplication of properties in skin
    private final ReadOnlyIntegerWrapper plankWidth = new ReadOnlyIntegerWrapper();
    private final ReadOnlyIntegerWrapper plankHeight = new ReadOnlyIntegerWrapper();
    private final ReadOnlyObjectWrapper<PlankGrainDirection> grainDirection = new ReadOnlyObjectWrapper<>();

    public PlankField() {
        super();

        skinProperty()
                .addListener((obs, oldSkin, newSkin) -> {
                    plankWidth.unbind();
                    plankHeight.unbind();
                    grainDirection.unbind();
                    if (newSkin instanceof PlankFieldSkin) {
                        PlankFieldSkin castedSkin = (PlankFieldSkin) newSkin;
                        plankWidth.bind(castedSkin.plankWidthProperty());
                        plankHeight.bind(castedSkin.plankHeightProperty());
                        grainDirection.bind(castedSkin.grainDirectionProperty());
                    } else {
                        LOGGER.log(Level.WARNING,
                                String.format("Cannot determine values since the currently set skin is of type '%s' "
                                                + "(Expected '%s')",
                                        newSkin.getClass().getCanonicalName(),
                                        PlankFieldSkin.class.getCanonicalName()
                                )
                        );
                    }
                });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PlankFieldSkin(this);
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

    public Plank createPlank() {
        return new Plank(getPlankWidth(), getPlankHeight(), getGrainDirection());
    }
}
