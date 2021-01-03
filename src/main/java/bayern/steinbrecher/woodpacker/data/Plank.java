package bayern.steinbrecher.woodpacker.data;

import java.io.Serializable;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class Plank implements Serializable {
    private final String id;
    private final int width; // in mm
    private final int height; // in mm
    private final PlankGrainDirection grainDirection;
    private final PlankMaterial material;

    public Plank(String id, int width, int height, PlankGrainDirection grainDirection, PlankMaterial material) {
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
        return new Plank(id, getHeight(), getWidth(), rotatedGrainDirection, material);
    }

    @Override
    public String toString() {
        return String.format("\"%s\": %d [mm] x %d [mm] (%s)", getId(), getWidth(), getHeight(), getMaterial());
    }
}
