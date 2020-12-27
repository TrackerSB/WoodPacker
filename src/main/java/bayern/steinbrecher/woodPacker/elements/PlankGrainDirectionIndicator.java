package bayern.steinbrecher.woodPacker.elements;

import bayern.steinbrecher.woodPacker.data.PlankGrainDirection;
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
public class PlankGrainDirectionIndicator extends Control {
    private static final Logger LOGGER = Logger.getLogger(PlankGrainDirectionIndicator.class.getName());
    // TODO Avoid duplication of properties in skin
    private final ReadOnlyObjectWrapper<PlankGrainDirection> value = new ReadOnlyObjectWrapper<>(null);

    public PlankGrainDirectionIndicator() {
        super();

        skinProperty()
                .addListener((obs, oldSkin, newSkin) -> {
                    value.unbind();
                    if (newSkin instanceof PlankGrainDirectionIndicatorSkin) {
                        PlankGrainDirectionIndicatorSkin castedSkin = (PlankGrainDirectionIndicatorSkin) newSkin;
                        value.bind(castedSkin.plankGrainDirectionProperty());
                    } else {
                        LOGGER.log(Level.WARNING,
                                String.format("Cannot determine value since the currently set skin is of type '%s' "
                                                + "(Expected '%s')",
                                        newSkin.getClass().getCanonicalName(),
                                        PlankGrainDirectionIndicatorSkin.class.getCanonicalName()
                                )
                        );
                    }
                });
    }

    @Override
    protected Skin<PlankGrainDirectionIndicator> createDefaultSkin() {
        return new PlankGrainDirectionIndicatorSkin(this);
    }

    public ReadOnlyObjectProperty<PlankGrainDirection> valueProperty() {
        return value.getReadOnlyProperty();
    }

    public PlankGrainDirection getValue() {
        return valueProperty().get();
    }
}
