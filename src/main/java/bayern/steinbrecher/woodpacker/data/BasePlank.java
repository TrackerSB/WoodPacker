package bayern.steinbrecher.woodpacker.data;

import bayern.steinbrecher.woodpacker.WoodPacker;

import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class BasePlank extends Plank {
    private final PlankMaterial material;

    public BasePlank(final String id, final int width, final int height, final PlankGrainDirection grainDirection,
                     final PlankMaterial material) {
        super(id, width, height, grainDirection);
        this.material = material;
    }

    public BasePlank(final String id, final int width, final int height, final PlankGrainDirection grainDirection,
                     final PlankMaterial material, final String comment) {
        super(id, width, height, grainDirection, comment);
        this.material = material;
    }

    public Optional<BasePlank> heightDecreased( final int decreaseBy) {
        if (decreaseBy > getHeight()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot decrease planks height by %d since its height is %d", decreaseBy, getHeight()));
        }
        if (decreaseBy == getHeight()) {
            return Optional.empty();
        }
        return Optional.of(
                new BasePlank(getId(), getWidth(), getHeight() - decreaseBy, getGrainDirection(), getMaterial(),
                        getComment()));
    }

    public Optional<BasePlank> widthDecreased(final int decreaseBy) {
        if (decreaseBy > getWidth()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot decrease planks width by %d since its height is %d", decreaseBy, getHeight()));
        }
        if (decreaseBy == getWidth()) {
            return Optional.empty();
        }
        return Optional.of(
                new BasePlank(getId(), getWidth() - decreaseBy, getHeight(), getGrainDirection(), getMaterial(),
                        getComment()));
    }

    @Override
    public String toString() {
        final String localizedMaterial = WoodPacker.getResource(getMaterial().getResourceKey());
        if (getComment() == null || getComment().isBlank()) {
            return String.format("\"%s\": %d [mm] x %d [mm] (%s)", getId(), getWidth(), getHeight(), localizedMaterial);
        } else {
            return String.format("\"%s\": %d [mm] x %d [mm] (%s)\n%s",
                    getId(), getWidth(), getHeight(), localizedMaterial, getComment());
        }
    }

    public PlankMaterial getMaterial() {
        return material;
    }
}
