package bayern.steinbrecher.woodPacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class Plank {
    private int height; // in mm
    private int width; // in mm
    private PlankGrainDirection grainDirection;

    public Plank(int height, int width, PlankGrainDirection grainDirection) {
        setHeight(height);
        setWidth(width);
        this.grainDirection = grainDirection;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height has to be positive");
        }
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        if (width <= 0) {
            throw new IllegalArgumentException("Width has to be positive");
        }
        this.width = width;
    }

    public PlankGrainDirection getGrainDirection() {
        return grainDirection;
    }

    public void setGrainDirection(PlankGrainDirection grainDirection) {
        this.grainDirection = grainDirection;
    }
}
