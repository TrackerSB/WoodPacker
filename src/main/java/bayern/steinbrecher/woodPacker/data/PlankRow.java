package bayern.steinbrecher.woodPacker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlankRow {
    private final double startY;
    private final double height;
    private final double maxWidth;
    private final List<Plank> planks = new ArrayList<>();
    private double currentWidth = 0;

    public PlankRow(double startY, double height, double maxWidth) {
        this.startY = startY;
        this.height = height;
        this.maxWidth = maxWidth;
    }

    public double getStartY() {
        return startY;
    }

    public double getHeight() {
        return height;
    }

    public double getMaxWidth() {
        return maxWidth;
    }

    public List<Plank> getPlanks() {
        return Collections.unmodifiableList(planks);
    }

    public boolean addPlank(Plank plank) {
        // FIXME Rotation based on the required grain is missing
        double rowWidthAfterAddition = getCurrentWidth() + plank.getWidth();
        if (plank.getHeight() > getHeight()
                || rowWidthAfterAddition > getMaxWidth()) {
            return false;
        } else {
            planks.add(plank);
            currentWidth = rowWidthAfterAddition;
            return true;
        }
    }

    public double getCurrentWidth() {
        return currentWidth;
    }
}
