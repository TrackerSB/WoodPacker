package bayern.steinbrecher.woodpacker.data;

import bayern.steinbrecher.woodpacker.WoodPacker;

import java.io.Serial;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlank extends Plank {
    @Serial
    private static final long serialVersionUID = 1L;
    private final PlankMaterial material;

    public BasePlank(final String plankId, final int width, final int height, final PlankGrainDirection grainDirection,
                     final PlankMaterial material) {
        super(plankId, width, height, grainDirection);
        this.material = material;
    }

    public BasePlank(final String plankId, final int width, final int height, final PlankGrainDirection grainDirection,
                     final PlankMaterial material, final String comment) {
        super(plankId, width, height, grainDirection, comment);
        this.material = material;
    }

    public Optional<BasePlank> heightDecreased(final int decreaseBy) {
        if (decreaseBy > getHeight()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot decrease planks height by %d since its height is %d", decreaseBy, getHeight()));
        }
        Optional<BasePlank> resizedPlank;
        if (decreaseBy == getHeight()) {
            resizedPlank = Optional.empty();
        } else {
            resizedPlank = Optional.of(
                    new BasePlank(getPlankId(), getWidth(), getHeight() - decreaseBy, getGrainDirection(),
                            getMaterial(), getComment()));
        }
        return resizedPlank;
    }

    public Optional<BasePlank> widthDecreased(final int decreaseBy) {
        if (decreaseBy > getWidth()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot decrease planks width by %d since its height is %d", decreaseBy, getHeight()));
        }
        Optional<BasePlank> resizedPlank;
        if (decreaseBy == getWidth()) {
            resizedPlank = Optional.empty();
        } else {
            resizedPlank = Optional.of(
                    new BasePlank(getPlankId(), getWidth() - decreaseBy, getHeight(), getGrainDirection(),
                            getMaterial(), getComment()));
        }
        return resizedPlank;
    }

    @Override
    public String toString() {
        final String localizedMaterial = WoodPacker.getResource(getMaterial().getResourceKey());
        String plankDescription;
        if (getComment() == null || getComment().isBlank()) {
            plankDescription = String.format("\"%s\": %d [mm] x %d [mm] (%s)", getPlankId(), getWidth(), getHeight(),
                    localizedMaterial);
        } else {
            plankDescription = String.format("\"%s\": %d [mm] x %d [mm] (%s)\n%s",
                    getPlankId(), getWidth(), getHeight(), localizedMaterial, getComment());
        }
        return plankDescription;
    }

    public PlankMaterial getMaterial() {
        return material;
    }
}
