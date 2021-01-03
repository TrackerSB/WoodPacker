package bayern.steinbrecher.woodpacker.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankSolutionRow {
    private final double startY;
    private final double height;
    private final double maxWidth;
    private final PlankGrainDirection grainDirection;
    private final Set<Plank> planks = new HashSet<>();
    private double currentWidth = 0;

    public PlankSolutionRow(double startY, double height, double maxWidth, PlankGrainDirection grainDirection) {
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

    public Set<Plank> getPlanks() {
        return Collections.unmodifiableSet(planks);
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
