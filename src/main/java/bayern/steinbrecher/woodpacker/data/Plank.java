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
    private final String plankId;
    private final int width; // in mm
    private final int height; // in mm
    private final PlankGrainDirection grainDirection;
    private final String comment;

    public Plank(final String plankId, final int width, final int height, final PlankGrainDirection grainDirection) {
        this(plankId, width, height, grainDirection, "");
    }

    public Plank(final String plankId, final int width, final int height, final PlankGrainDirection grainDirection,
                 final String comment) {
        this.plankId = plankId;
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

    public String getPlankId() {
        return plankId;
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
    public boolean matchesGrainDirection(final PlankGrainDirection direction) {
        return direction == PlankGrainDirection.IRRELEVANT
                || getGrainDirection() == PlankGrainDirection.IRRELEVANT
                || getGrainDirection() == direction;
    }

    @Override
    public boolean equals(final Object other) {
        boolean isEqual;
        if (this == other) {
            isEqual = true;
        } else if (other == null || getClass() != other.getClass()) {
            isEqual = false;
        } else {
            final Plank plank = (Plank) other;
            isEqual = getPlankId().equals(plank.getPlankId());
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlankId());
    }

    @Override
    public int compareTo(final Plank other) {
        return Collator.getInstance()
                .compare(this.getPlankId(), other.getPlankId());
    }
}
