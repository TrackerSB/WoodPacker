package bayern.steinbrecher.woodpacker.data;

import java.io.Serial;
import java.io.Serializable;
import java.text.Collator;
import java.util.Objects;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public abstract class Plank implements Comparable<Plank>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final int width; // in mm
    private final int height; // in mm
    private final PlankGrainDirection grainDirection;
    private final String comment;

    public Plank(String id, int width, int height, PlankGrainDirection grainDirection) {
        this(id, width, height, grainDirection, "");
    }

    public Plank(String id, int width, int height, PlankGrainDirection grainDirection, String comment) {
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

    @Override
    public int compareTo(Plank other) {
        return Collator.getInstance()
                .compare(this.getId(), other.getId());
    }
}
