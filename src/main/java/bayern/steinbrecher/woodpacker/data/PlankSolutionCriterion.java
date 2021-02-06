package bayern.steinbrecher.woodpacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public enum PlankSolutionCriterion {
    BREATH_DIFFERENCES("breadthDifferences") {
        /**
         * The less the breadths of the planks differs the better. Having exactly one breadth is perfect.
         */
        @Override
        public double getRating(final PlankSolutionRow solutionRow, final PlankProblem plankProblem) {
            final int numBreadths = solutionRow.getBreadths()
                    .size();
            return (numBreadths > 0) ? (1d / numBreadths) : 0d; // NOPMD - Parenthesis clarify structure
        }
    },
    NUM_PLANKS("numPlanks") {
        /**
         * The more planks in a row the better.
         */
        @Override
        public double getRating(final PlankSolutionRow solutionRow, final PlankProblem plankProblem) {
            return ((double) solutionRow.getPlanks().size())
                    / plankProblem.getRequiredPlanks().size();
        }
    },
    ROW_SPACE_WASTE("rowSpaceWaste") {
        /**
         * The less space a row wastes the better.
         */
        @Override
        public double getRating(final PlankSolutionRow solutionRow, final PlankProblem plankProblem) {
            final int usedArea = solutionRow.getUsedArea();
            double rating;
            if (usedArea <= 0) {
                rating = 0d;
            } else {
                rating = ((double) solutionRow.getPlanks()
                        .stream()
                        .mapToInt(RequiredPlank::getArea)
                        .sum()) / usedArea;
            }
            return rating;
        }
    };
    private final String resourceKey;

    PlankSolutionCriterion(final String resourceKey) {
        this.resourceKey = resourceKey;
    }

    /**
     * Rate the given row without considering any weight.
     *
     * @param solutionRow  The {@link PlankSolutionRow} to rate.
     * @param plankProblem The {@link PlankProblem} the {@link PlankSolutionRow} to rate belongs to.
     * @return The returned values area normalized to [0; 1]. The higher the returned value the better the criterion is
     * fulfilled.
     */
    public abstract double getRating(final PlankSolutionRow solutionRow, final PlankProblem plankProblem);

    public String getResourceKey() {
        return resourceKey;
    }
}
