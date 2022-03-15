package bayern.steinbrecher.woodpacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public enum PlankSolutionCriterion {
    /* NOTE 2022-03-15: If you see a typo in the enum name, you're right. Due to the requirement of downwards
     * compatibility of the serialization, this issue may never be fixed.
     */
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
         * The less space a row wastes when being cut off the better.
         */
        @Override
        public double getRating(final PlankSolutionRow solutionRow, final PlankProblem plankProblem) {
            return solutionRow.getAreaUtilization();
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
