package bayern.steinbrecher.woodPacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class Plank {
    private final int height; // in mm
    private final int width; // in mm
    private final PlankGrainDirection grainDirection;

    public Plank(int height, int width, PlankGrainDirection grainDirection) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height has to be positive");
        }
        this.height = height;
        if (width <= 0) {
            throw new IllegalArgumentException("Width has to be positive");
        }
        this.width = width;
        this.grainDirection = grainDirection;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirection;
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
        return new Plank(getWidth(), getHeight(), rotatedGrainDirection);
    }
}
