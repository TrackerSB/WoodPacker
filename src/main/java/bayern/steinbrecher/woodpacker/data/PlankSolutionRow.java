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
    private final boolean addHorizontal;
    private final int maxLength;
    private final int breadth;
    private final SortedSet<RequiredPlank> planks;
    private int currentLength;

    /**
     * @param startOffset   The position in the base plank space of the left upper corner
     * @param addHorizontal The direction in which {@link Plank}s are added. {@code true} iff adding in X direction,
     *                      {@code false} iff adding in Y direction.
     * @param maxLength     The maximum space in the direction in which {@link Plank}s are added
     * @param breadth       The maximum space orthogonal to the direction in which {@link Plank}s are added
     */
    public PlankSolutionRow(final Point2D startOffset, final boolean addHorizontal, final int maxLength,
                            final int breadth) {
        this.startOffset = startOffset;
        this.addHorizontal = addHorizontal;
        this.maxLength = maxLength;
        this.breadth = breadth;

        // If horizontal row sort by height descending; otherwise sort by width descending
        final Function<Plank, Integer> compareMethod = addHorizontal() ? Plank::getHeight : Plank::getWidth;
        final Comparator<Plank> descendingBreadthComparator = (pA, pB) -> {
            final int diff = compareMethod.apply(pB) - compareMethod.apply(pA);
            /* NOTE The IDs of the planks have to be considered since otherwise the sorted set of planks could not
             * contain planks of the same size.
             */
            return (diff == 0) ? pA.getId().compareTo(pB.getId()) : diff;
        };
        this.planks = new TreeSet<>(descendingBreadthComparator);
    }

    public Point2D getStartOffset() {
        return startOffset;
    }

    public boolean addHorizontal() {
        return addHorizontal;
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getBreadth() {
        return breadth;
    }

    public Set<RequiredPlank> getPlanks() {
        return Collections.unmodifiableSet(planks);
    }

    public int getCurrentLength() {
        return currentLength;
    }

    private int getPlankLength(final Plank plank) {
        return addHorizontal() ? plank.getWidth() : plank.getHeight();
    }

    private int getPlankBreadth(final Plank plank) {
        return addHorizontal() ? plank.getHeight() : plank.getWidth();
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

    public boolean canContain(final PlankSolutionRow toBeAdded) {
        return (getCurrentLength() + toBeAdded.getCurrentLength()) < getMaxLength()
                && toBeAdded.getBreadth() <= getBreadth()
                && toBeAdded.getPlanks().stream().allMatch(p -> isRotatedAsRow(p) && !getPlanks().contains(p));
    }

    public boolean canContain(final Plank toBeAdded) {
        return (getCurrentLength() + getPlankLength(toBeAdded)) <= getMaxLength()
                && getPlankBreadth(toBeAdded) <= getBreadth()
                && isRotatedAsRow(toBeAdded)
                && !getPlanks().contains(toBeAdded);
    }

    public boolean addPlank(final RequiredPlank plank) {
        final boolean containable = canContain(plank);
        if (containable) {
            final boolean addedPlank = planks.add(plank);
            assert addedPlank : String.format(
                    "Plank '%s' was not added even though the row could contain it", plank.getId());
            currentLength = getCurrentLength() + getPlankLength(plank);
        }
        return containable;
    }
}
