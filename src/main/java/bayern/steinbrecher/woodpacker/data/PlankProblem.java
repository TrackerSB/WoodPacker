package bayern.steinbrecher.woodpacker.data;

import bayern.steinbrecher.javaUtility.SupplyingMap;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.geometry.Point2D;
import javafx.util.Pair;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class PlankProblem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * Sort {@link PlankSolutionRow} by descending area and ascending by the name of the pivot element ascending.
     */
    private static final Comparator<PlankVariationGroup> VARIATION_GROUP_SORTER = (rpA, rpB) -> {
        final RequiredPlank pivotA = rpA.getPivot();
        final RequiredPlank pivotB = rpB.getPivot();
        final int areaDifference = pivotB.getArea() - pivotA.getArea();
        if (areaDifference == 0) {
            return pivotA.getPlankId().compareTo(pivotB.getPlankId());
        }
        return areaDifference;
    };
    private static final long internalSerialVersion = 92870523745L;

    // Since internal serial version 1
    private transient /*final*/ ObservableMap<PlankSolutionCriterion, Double> criterionWeights;
    private transient /*final*/ SetProperty<RequiredPlank> requiredPlanks;
    private transient /*final*/ ObjectProperty<BasePlank> basePlank;
    private transient /*final*/ IntegerProperty basePlankOversize;
    /**
     * A list of cutting planks that fit on the given {@link BasePlank} and a list of the remaining planks that do not
     * fit onto the base plank.
     */
    private transient /*final*/ ReadOnlyObjectWrapper<Pair<Collection<CuttingPlan>, Set<RequiredPlank>>>
            proposedSolution;

    public PlankProblem() {
        initializeTransientMember();

        requiredPlanksProperty()
                .addListener((obs, oldList, currentList) -> {
                    if (currentList != null) {
                        currentList.addListener((InvalidationListener) observable -> updateSolution());
                    }
                });
        basePlankProperty()
                .addListener((obs, previousBasePlank, currentBasePlank) -> updateSolution());
        criterionWeightsProperty()
                .addListener((InvalidationListener) observable -> updateSolution());
        basePlankOversizeProperty()
                .addListener(observable -> updateSolution());
        updateSolution(); // Ensure initial state
    }

    private void initializeTransientMember() {
        criterionWeights = FXCollections.observableMap(new SupplyingMap<>(criterion -> 1d));
        requiredPlanks = new SimpleSetProperty<>(FXCollections.observableSet());
        basePlank = new SimpleObjectProperty<>(null);
        basePlankOversize = new SimpleIntegerProperty(0);
        proposedSolution = new ReadOnlyObjectWrapper<>(new Pair<>(List.of(), Set.of()));
    }

    private double determineCandidateQuality(final PlankSolutionRow candidate) {
        return criterionWeights.entrySet()
                .stream()
                .mapToDouble(criterion -> criterion.getKey().getRating(candidate, this) * criterion.getValue())
                .sum();
    }

    private PlankSolutionRow createCandidate(
            final boolean horizontal, final Point2D basePlankOffset, final Plank availablePlank,
            final Set<PlankVariationGroup> plankVariations) {
        assert !plankVariations.isEmpty() : "No planks left for creating a candidate";
        int maxLength;
        int maxBreadth;
        if (horizontal) {
            maxLength = availablePlank.getWidth();
            maxBreadth = availablePlank.getHeight();
        } else {
            maxLength = availablePlank.getHeight();
            maxBreadth = availablePlank.getWidth();
        }
        final PlankSolutionRow finalCandidate
                = new PlankSolutionRow(basePlankOffset, horizontal, maxLength, maxBreadth);
        double finalQuality = determineCandidateQuality(finalCandidate);
        for (final PlankVariationGroup group : plankVariations) {
            final Optional<Pair<RequiredPlank, Double>> optBestVariant = group.getVariations()
                    .stream()
                    .map(v -> {
                        final PlankSolutionRow currentVariant = new PlankSolutionRow(finalCandidate);
                        if (currentVariant.addPlank(v)) {
                            final double currentVariantQuality = determineCandidateQuality(currentVariant);
                            return new Pair<>(v, currentVariantQuality);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .max(Comparator.comparing(Pair::getValue));
            if (optBestVariant.isPresent()) {
                final Pair<RequiredPlank, Double> bestVariant = optBestVariant.get();
                if (bestVariant.getValue() > finalQuality) {
                    finalCandidate.addPlank(bestVariant.getKey());
                    finalQuality = bestVariant.getValue();
                }
            }
        }
        return finalCandidate;
    }

    private Optional<PlankSolutionRow> determineBestCandidate(
            SortedSet<PlankVariationGroup> unplacedPlanks, Map<Point2D, RemainingBasePlank> remainingPartitions) {
        return remainingPartitions.entrySet()
                .stream()
                .flatMap(entry -> {
                    final RemainingBasePlank remaining = entry.getValue();
                    List<PlankSolutionRow> candidates = new ArrayList<>();
                    if (remaining.isRestrictToVerticalCandidates() == null
                            || remaining.isRestrictToVerticalCandidates()) {
                        candidates.add(createCandidate(
                                false, entry.getKey(), remaining.getBasePlank(), unplacedPlanks));
                    }
                    if (remaining.isRestrictToVerticalCandidates() == null
                            || !remaining.isRestrictToVerticalCandidates()) {
                        candidates.add(createCandidate(
                                true, entry.getKey(), remaining.getBasePlank(), unplacedPlanks));
                    }
                    return candidates.stream();
                })
                .filter(c -> c.getPlanks().size() > 0)
                .map(c -> new Pair<>(c, determineCandidateQuality(c)))
                .max(Comparator.comparing(Pair::getValue))
                .map(Pair::getKey);
    }

    private void shrinkRemainingPartitions(Map<Point2D, RemainingBasePlank> remainingPartitions,
                                           PlankSolutionRow rowToAdded) {
        final Point2D selectedOffset = rowToAdded.getStartOffset();
        final RemainingBasePlank selectedRemaining = remainingPartitions.remove(selectedOffset);
        final BasePlank selectedPartition = selectedRemaining.getBasePlank();

        // Split partition containing the row to add into remaining partitions
        if (rowToAdded.addHorizontal()) {
            final Optional<BasePlank> remainingPartitionNotInRow
                    = selectedPartition.heightDecreased(rowToAdded.getCurrentBreadth());
            remainingPartitionNotInRow.ifPresent(
                    bp -> remainingPartitions.put(
                            selectedOffset.add(0, rowToAdded.getCurrentBreadth()),
                            new RemainingBasePlank(null, bp)));
            final Optional<BasePlank> remainingPartitionInRow = selectedPartition.heightDecreased(
                    remainingPartitionNotInRow.map(Plank::getHeight).orElse(0))
                    .flatMap(bp -> bp.widthDecreased(rowToAdded.getCurrentLength()));
            remainingPartitionInRow.ifPresent(
                    bp -> remainingPartitions.put(
                            selectedOffset.add(rowToAdded.getCurrentLength(), 0),
                            new RemainingBasePlank(true, bp)));
        } else {
            final Optional<BasePlank> remainingPartitionNotInRow
                    = selectedPartition.widthDecreased(rowToAdded.getCurrentBreadth());
            remainingPartitionNotInRow.ifPresent(
                    bp -> remainingPartitions.put(
                            selectedOffset.add(rowToAdded.getCurrentBreadth(), 0),
                            new RemainingBasePlank(null, bp)));
            final Optional<BasePlank> remainingPartitionInRow = selectedPartition.widthDecreased(
                    remainingPartitionNotInRow.map(Plank::getWidth).orElse(0))
                    .flatMap(bp -> bp.heightDecreased(rowToAdded.getCurrentLength()));
            remainingPartitionInRow.ifPresent(
                    bp -> remainingPartitions.put(
                            selectedOffset.add(0, rowToAdded.getCurrentLength()),
                            new RemainingBasePlank(false, bp)));
        }
    }

    private void updateSolution() {
        final Collection<CuttingPlan> cuttingPlans = new ArrayList<>();
        Set<RequiredPlank> ignoredPlanks;
        if (getBasePlank() == null
                // Do not place planks if all criteria are disabled, i.e. if they are all neither positive nor negative
                || criterionWeights.values().stream().allMatch(d -> d == 0)) {
            ignoredPlanks = requiredPlanks;
        } else {
            /* The following collection contains all not yet placed planks in all variations in which they are allowed.
             * In case the base plank as well as the required plank have a grain direction there is only one allowed
             * variation of the plank. If either the base plank or the required plank have no grain direction the
             * collection contains two versions of the plank (i.e. rotated and not rotated).
             */
            final SortedSet<PlankVariationGroup> unplacedPlanks = requiredPlanks.stream()
                    .peek(rp -> rp.setPlacedInSolution(false))
                    .map(rp -> new PlankVariationGroup(rp, getBasePlank()))
                    // Sort by area decreasing
                    .collect(Collectors.toCollection(() -> new TreeSet<>(VARIATION_GROUP_SORTER)));

            final Map<Point2D, RemainingBasePlank> remainingPartitions = new ConcurrentHashMap<>();
            final Collection<PlankSolutionRow> currentSolutionRows = new ArrayList<>();
            final AtomicBoolean potentialForMorePlacements = new AtomicBoolean(true);
            Runnable resetCurrentCuttingPlan = () -> {
                currentSolutionRows.clear();
                remainingPartitions.clear();
                final Optional<BasePlank> emptyBasePlank = getBasePlank().widthDecreased(2 * getBasePlankOversize())
                        .flatMap(bp -> bp.heightDecreased(2 * getBasePlankOversize()));
                if (emptyBasePlank.isPresent()) {
                    final Point2D initialOffset = new Point2D(getBasePlankOversize(), getBasePlankOversize());
                    remainingPartitions.put(initialOffset, new RemainingBasePlank(null, emptyBasePlank.get()));
                } else {
                    potentialForMorePlacements.set(false);
                }
            };
            resetCurrentCuttingPlan.run();

            while (!unplacedPlanks.isEmpty() && potentialForMorePlacements.get()) {
                final Optional<PlankSolutionRow> optBestCandidate
                        = determineBestCandidate(unplacedPlanks, remainingPartitions);
                if (optBestCandidate.isPresent()) {
                    final PlankSolutionRow bestCandidateRow = optBestCandidate.get();
                    shrinkRemainingPartitions(remainingPartitions, bestCandidateRow);

                    currentSolutionRows.add(bestCandidateRow);
                    bestCandidateRow.getPlanks()
                            .forEach(p -> unplacedPlanks.removeIf(up -> {
                                final RequiredPlank pivot = up.getPivot();
                                final boolean wasPlaced = pivot.equals(p);
                                if (wasPlaced) {
                                    pivot.setPlacedInSolution(true);
                                }
                                return wasPlaced;
                            }));
                } else {
                    // No further candidates can be found for the current set of remaining base plank partitions
                    if (currentSolutionRows.isEmpty()) { // If on an empty base plank there are no candidates available
                        potentialForMorePlacements.set(false);
                    } else {
                        cuttingPlans.add(new CuttingPlan(new ArrayList<>(currentSolutionRows), getBasePlank()));
                        resetCurrentCuttingPlan.run();
                    }
                }
            }
            if (!currentSolutionRows.isEmpty()) {
                cuttingPlans.add(new CuttingPlan(new ArrayList<>(currentSolutionRows), getBasePlank()));
            }

            ignoredPlanks = unplacedPlanks.stream()
                    .map(PlankVariationGroup::getPivot)
                    .collect(Collectors.toSet());
            ignoredPlanks.forEach(p -> p.setPlacedInSolution(false));
        }
        proposedSolution.set(new Pair<>(cuttingPlans, ignoredPlanks));
    }

    @SuppressWarnings("unchecked")
    @Serial
    private void readObject(final ObjectInputStream input) throws IOException, ClassNotFoundException {
        initializeTransientMember();
        final long inputSerialVersion = input.readLong();

        // Internal serial version 1
        if (inputSerialVersion >= 1) {
            criterionWeightsProperty()
                    .putAll((HashMap<PlankSolutionCriterion, Double>) input.readObject());
            requiredPlanksProperty()
                    .addAll((HashSet<RequiredPlank>) input.readObject());
            setBasePlank((BasePlank) input.readObject());
            setBasePlankOversize(input.readInt());
            updateSolution();
        }
    }

    @Serial
    private void writeObject(final ObjectOutputStream output) throws IOException {
        output.writeLong(internalSerialVersion);

        // Internal serial version 1
        output.writeObject(new HashMap<>(criterionWeightsProperty()));
        output.writeObject(new HashSet<>(getRequiredPlanks()));
        output.writeObject(getBasePlank());
        output.writeInt(getBasePlankOversize());
    }

    public ObservableMap<PlankSolutionCriterion, Double> criterionWeightsProperty() {
        // FIXME How to avoid that null values are set as weight
        return criterionWeights;
    }

    public double getCriterionWeight(final PlankSolutionCriterion criterion) {
        return criterionWeights.get(criterion);
    }

    public void setCriterionWeight(final PlankSolutionCriterion criterion, final double weight) {
        criterionWeights.put(criterion, weight);
    }

    public SetProperty<RequiredPlank> requiredPlanksProperty() {
        return requiredPlanks;
    }

    public ObservableSet<RequiredPlank> getRequiredPlanks() {
        return requiredPlanksProperty().get();
    }

    public void setRequiredPlanks(final ObservableSet<RequiredPlank> requiredPlanks) {
        requiredPlanksProperty().set(requiredPlanks);
    }

    public ObjectProperty<BasePlank> basePlankProperty() {
        return basePlank;
    }

    public BasePlank getBasePlank() {
        return basePlankProperty().get();
    }

    public void setBasePlank(final BasePlank basePlank) {
        basePlankProperty().set(basePlank);
    }

    public IntegerProperty basePlankOversizeProperty() {
        return basePlankOversize;
    }

    public int getBasePlankOversize() {
        return basePlankOversizeProperty().get();
    }

    public void setBasePlankOversize(final int basePlankOversize) {
        if (basePlankOversize < 0) {
            throw new IllegalArgumentException("The oversize has to be non-negative");
        }
        basePlankOversizeProperty().set(basePlankOversize);
    }

    public ReadOnlyObjectProperty<Pair<Collection<CuttingPlan>, Set<RequiredPlank>>> proposedSolutionProperty() {
        return proposedSolution.getReadOnlyProperty();
    }

    public Pair<Collection<CuttingPlan>, Set<RequiredPlank>> getProposedSolution() {
        return proposedSolutionProperty().get();
    }

    private static class RemainingBasePlank {
        /**
         * {@code true} --> Only vertical candidates are allowed.
         * {@code false} --> Only horizontal candidates are allowed.
         * {@code null} --> Both vertical and horizontal candidates are allowed.
         */
        private final Boolean restrictToVerticalCandidates;
        private final BasePlank basePlank;

        private RemainingBasePlank(final Boolean restrictToVerticalCandidates, final BasePlank basePlank) {
            this.restrictToVerticalCandidates = restrictToVerticalCandidates;
            this.basePlank = basePlank;
        }

        public Boolean isRestrictToVerticalCandidates() {
            return restrictToVerticalCandidates;
        }

        public BasePlank getBasePlank() {
            return basePlank;
        }
    }

    private static class PlankVariationGroup {
        private final RequiredPlank pivot;
        private final List<RequiredPlank> variations = new ArrayList<>();

        public PlankVariationGroup(final RequiredPlank pivot, final BasePlank basePlank) {
            this.pivot = pivot;

            /* A plank can be placed if either its grain direction the base planks grain direction is irrelevant or
             * the grain direction matches the base planks grain direction.
             */
            if (pivot.matchesGrainDirection(basePlank.getGrainDirection())) {
                variations.add(pivot);
            }
            final RequiredPlank rotatedPivot = pivot.rotated();
            if (rotatedPivot.matchesGrainDirection(basePlank.getGrainDirection())) {
                variations.add(pivot.rotated());
            }
        }

        @Override
        public boolean equals(final Object other) {
            boolean isEqual;
            if (this == other) {
                isEqual = true;
            } else if (other == null || getClass() != other.getClass()) {
                isEqual = false;
            } else {
                final PlankVariationGroup that = (PlankVariationGroup) other;
                isEqual = this.getPivot().equals(that.getPivot());
            }
            return isEqual;
        }

        @Override
        public int hashCode() {
            return getPivot().hashCode();
        }

        /**
         * @return The pivot element which yielded all variations. NOTE It is not necessarily the case that this pivot
         * is an allowed variation itself.
         */
        public RequiredPlank getPivot() {
            return pivot;
        }

        public List<RequiredPlank> getVariations() {
            return Collections.unmodifiableList(variations);
        }
    }
}
