package bayern.steinbrecher.woodpacker.data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class Plank implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final int width; // in mm
    private final int height; // in mm
    private final PlankGrainDirection grainDirection;
    private final PlankMaterial material;
    private final String comment;

    public Plank(String id, int width, int height, PlankGrainDirection grainDirection, PlankMaterial material) {
        this(id, width, height, grainDirection, material, "");
    }

    public Plank(String id, int width, int height, PlankGrainDirection grainDirection, PlankMaterial material,
                 String comment) {
        this.id = id;
        if (width <= 0) {
            throw new IllegalArgumentException("Width has to be positive");
        }
        this.width = width;
        if (height <= 0) {
            throw new IllegalArgumentException("Height has to be positive");
        }
        this.height = height;
        this.grainDirection = grainDirection;
        this.material = material;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirection;
    }

    public PlankMaterial getMaterial() {
        return material;
    }

    public String getComment() {
        return comment;
    }

    /**
     * @param direction The direction of the plank this plank might be cut out of.
     * @return {@code true} iff this planks grain direction is compatible with direction of a plank from which this
     * plank might be cut out of.
     */
    public boolean matchesGrainDirection(PlankGrainDirection direction) {
        return direction == PlankGrainDirection.IRRELEVANT
                || getGrainDirection() == PlankGrainDirection.IRRELEVANT
                || getGrainDirection() == direction;
    }

    public Plank rotated() {
        PlankGrainDirection rotatedGrainDirection = switch (getGrainDirection()) {
            case HORIZONTAL -> PlankGrainDirection.VERTICAL;
            case VERTICAL -> PlankGrainDirection.HORIZONTAL;
            case IRRELEVANT -> PlankGrainDirection.IRRELEVANT;
        };
        return new Plank(id, getHeight(), getWidth(), rotatedGrainDirection, getMaterial(), getComment());
    }

    public Optional<Plank> heightDecreased(int decreaseBy) {
        if (decreaseBy > getHeight()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot decrease planks height by %d since its height is %d", decreaseBy, getHeight()));
        }
        if (decreaseBy == getHeight()) {
            return Optional.empty();
        }
        return Optional.of(
                new Plank(getId(), getWidth(), getHeight() - decreaseBy, getGrainDirection(), getMaterial(),
                        getComment()));
    }

    public Optional<Plank> widthDecreased(int decreaseBy) {
        if (decreaseBy > getWidth()) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot decrease planks width by %d since its height is %d", decreaseBy, getHeight()));
        }
        if (decreaseBy == getWidth()) {
            return Optional.empty();
        }
        return Optional.of(
                new Plank(getId(), getWidth() - decreaseBy, getHeight(), getGrainDirection(), getMaterial(),
                        getComment()));
    }

    @Override
    public String toString() {
        if (getComment() == null || getComment().isBlank()) {
            return String.format("\"%s\": %d [mm] x %d [mm] (%s)", getId(), getWidth(), getHeight(), getMaterial());
        } else {
            return String.format("\"%s\": %d [mm] x %d [mm] (%s) - %s",
                    getId(), getWidth(), getHeight(), getMaterial(), getComment());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Plank plank = (Plank) o;
        return getId().equals(plank.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
