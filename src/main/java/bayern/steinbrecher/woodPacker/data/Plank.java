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
}
