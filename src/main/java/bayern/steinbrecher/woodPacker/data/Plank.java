package bayern.steinbrecher.woodPacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class Plank {
    private double height;
    private double width;
    private PlankGrainDirection grainDirection;

    public Plank(double height, double width, PlankGrainDirection grainDirection) {
        setHeight(height);
        setWidth(width);
        this.grainDirection = grainDirection;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        if (height <= 0) {
            throw new IllegalArgumentException("Height has to be positive");
        }
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
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
