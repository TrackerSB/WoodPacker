package bayern.steinbrecher.woodpacker.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlankRow {
    private final double startY;
    private final double height;
    private final double maxWidth;
    private final PlankGrainDirection grainDirection;
    private final List<Plank> planks = new ArrayList<>();
    private double currentWidth = 0;

    public PlankRow(double startY, double height, double maxWidth, PlankGrainDirection grainDirection) {
        this.startY = startY;
        this.height = height;
        this.maxWidth = maxWidth;
        this.grainDirection = grainDirection;
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

    public PlankGrainDirection getGrainDirection() {
        return grainDirection;
    }

    public List<Plank> getPlanks() {
        return Collections.unmodifiableList(planks);
    }

    public boolean addPlank(Plank plank) {
        double rowWidthAfterAddition = getCurrentWidth() + plank.getWidth();
        if (plank.getHeight() > getHeight()
                || rowWidthAfterAddition > getMaxWidth()
                || !plank.matchesGrainDirection(getGrainDirection())) {
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
