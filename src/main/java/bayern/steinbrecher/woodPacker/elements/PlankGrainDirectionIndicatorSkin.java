package bayern.steinbrecher.woodPacker.elements;

import bayern.steinbrecher.woodPacker.data.PlankGrainDirection;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankGrainDirectionIndicatorSkin extends SkinBase<PlankGrainDirectionIndicator> {

    private static final Map<PlankGrainDirection, String> GRAIN_DIRECTION_SYMBOL_FILE_NAMES = Map.of(
            PlankGrainDirection.IRRELEVANT, "plankGrainIrrelevant.png",
            PlankGrainDirection.HORIZONTAL, "plankGrainHorizontal.png",
            PlankGrainDirection.VERTICAL, "plankGrainVertical.png"
    );
    private final ReadOnlyObjectWrapper<PlankGrainDirection> plankGrainDirection
            = new ReadOnlyObjectWrapper<>(PlankGrainDirection.IRRELEVANT);

    protected PlankGrainDirectionIndicatorSkin(PlankGrainDirectionIndicator control) {
        super(control);

        Button indicatorButton = new Button();
        indicatorButton.setOnAction(aevt -> {
            plankGrainDirection.set(
                    switch (getPlankGrainDirection()) {
                        case HORIZONTAL -> PlankGrainDirection.IRRELEVANT;
                        case IRRELEVANT -> PlankGrainDirection.VERTICAL;
                        case VERTICAL -> PlankGrainDirection.HORIZONTAL;
                    }
            );
        });

        Consumer<PlankGrainDirection> updateIndicatorGraphic = direction -> {
            indicatorButton.setGraphic(generateImageView(direction));
        };
        plankGrainDirectionProperty()
                .addListener((obs, previousDirection, currentDirection) -> {
                    updateIndicatorGraphic.accept(currentDirection);
                });
        updateIndicatorGraphic.accept(getPlankGrainDirection()); // Ensure initial state

        getChildren()
                .add(indicatorButton);
    }

    public static ImageView generateImageView(PlankGrainDirection direction) {
        String imageName = GRAIN_DIRECTION_SYMBOL_FILE_NAMES.get(direction);
        URL symbolResource = PlankGrainDirectionIndicatorSkin.class
                .getResource(imageName);
        if (symbolResource == null) {
            throw new ExceptionInInitializerError(String.format("Cannot find symbol for '%s'", imageName));
        } else {
            return new ImageView(symbolResource.toExternalForm());
        }
    }

    public ReadOnlyObjectProperty<PlankGrainDirection> plankGrainDirectionProperty() {
        return plankGrainDirection.getReadOnlyProperty();
    }

    public PlankGrainDirection getPlankGrainDirection() {
        return plankGrainDirectionProperty().get();
    }
}
