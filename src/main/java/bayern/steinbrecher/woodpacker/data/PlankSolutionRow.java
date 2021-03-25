package bayern.steinbrecher.woodpacker.data;

import javafx.geometry.Point2D;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankSolutionRow {
    private final Point2D startOffset;
    private final boolean addingHorizontally;
    private final int maxLength;
    private final int maxBreadth;
    private final int cuttingWidth;
    private final SortedSet<RequiredPlank> planks;
    private int currentLength;
    private int currentBreadth;

    /**
     * @param startOffset        The position in the base plank space of the left upper corner
     * @param addingHorizontally The direction in which {@link Plank}s are added. {@code true} iff adding in X
     *                           direction, {@code false} iff adding in Y direction.
     * @param maxLength          The maximum space in the direction in which {@link Plank}s are added
     * @param maxBreath          The maximum space orthogonal to the direction in which {@link Plank}s are added
     */
    public PlankSolutionRow(final Point2D startOffset, final boolean addingHorizontally, final int maxLength,
                            final int maxBreath, int cuttingWidth) {
        this.startOffset = startOffset;
        this.addingHorizontally = addingHorizontally;
        this.maxLength = maxLength;
        this.maxBreadth = maxBreath;
        this.cuttingWidth = cuttingWidth;

        // FIXME Is this additional sorting required?
        // If horizontal row sort by height descending; otherwise sort by width descending
        final Function<Plank, Integer> compareMethod = isAddingHorizontally() ? Plank::getHeight : Plank::getWidth;
        final Comparator<Plank> descendingBreadthComparator = (pA, pB) -> {
            final int diff = compareMethod.apply(pB) - compareMethod.apply(pA);
            /* NOTE The IDs of the planks have to be considered since otherwise the sorted set of planks could not
             * contain planks of the same size.
             */
            return (diff == 0) ? pA.getPlankId().compareTo(pB.getPlankId()) : diff;
        };
        this.planks = new TreeSet<>(descendingBreadthComparator);
    }

    public PlankSolutionRow(final PlankSolutionRow toCopy) {
        this(toCopy.getStartOffset(), toCopy.isAddingHorizontally(), toCopy.getMaxLength(), toCopy.getMaxBreadth(),
                toCopy.getCuttingWidth());
        currentBreadth = toCopy.getCurrentBreadth();
        currentLength = toCopy.getCurrentLength();
        planks.addAll(toCopy.getPlanks());
    }

    public Point2D getStartOffset() {
        return startOffset;
    }

    public boolean isAddingHorizontally() {
        return addingHorizontally;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getMaxBreadth() {
        return maxBreadth;
    }

    public Set<RequiredPlank> getPlanks() {
        return Collections.unmodifiableSet(planks);
    }

    public int getCurrentLength() {
        return currentLength;
    }

    public int getCurrentBreadth() {
        return currentBreadth;
    }

    public int getCuttingWidth() {
        return cuttingWidth;
    }

    /**
     * When cutting a plank one has to cut through the complete base plank. The area utilization describes how much of
     * the cut off plank consists of actually to be used planks.
     *
     * @return The area utilization in [0; 1]
     */
    public double getAreaUtilization() {
        final int cutOffArea = getCurrentBreadth() * getMaxLength();
        double areaUtilization;
        if (cutOffArea <= 0) {
            areaUtilization = 0d;
        } else {
            final int utilizedArea = planks.stream()
                    .mapToInt(RequiredPlank::getArea)
                    .sum();
            areaUtilization = ((double) utilizedArea) / cutOffArea;
        }
        return areaUtilization;
    }

    private int getPlankLength(final Plank plank) {
        return isAddingHorizontally() ? plank.getWidth() : plank.getHeight();
    }

    private int getPlankBreadth(final Plank plank) {
        return isAddingHorizontally() ? plank.getHeight() : plank.getWidth();
    }

    public Set<Integer> getBreadths() {
        return getPlanks()
                .stream()
                .map(this::getPlankBreadth)
                .collect(Collectors.toSet());
    }

    private boolean isRotatedAsRow(final Plank plank) {
        return getPlanks().isEmpty()
                || plank.matchesGrainDirection(getPlanks().iterator().next().getGrainDirection());
    }

    public boolean canContain(final Plank toBeAdded) {
        return (getCurrentLength() + getCuttingWidth() + getPlankLength(toBeAdded)) <= getMaxLength()
                && getPlankBreadth(toBeAdded) <= getMaxBreadth()
                && isRotatedAsRow(toBeAdded)
                && !getPlanks().contains(toBeAdded);
    }

    public boolean addPlank(final RequiredPlank plank) {
        final boolean containable = canContain(plank);
        if (containable) {
            if (!getPlanks().isEmpty()) {
                currentLength += getCuttingWidth();
            }
            currentLength = getCurrentLength() + getPlankLength(plank);
            currentBreadth = Math.max(getCurrentBreadth(), getPlankBreadth(plank));

            final boolean addedPlank = planks.add(plank);
            assert addedPlank : String.format(
                    "Plank '%s' was not added even though the row could contain it", plank.getPlankId());
        }
        return containable;
    }
}
