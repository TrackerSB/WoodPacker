package bayern.steinbrecher.woodpacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public enum PlankSolutionCriterion {
    BREATH_DIFFERENCES("breadthDifferences") {
        /**
         * The less the breadths of the planks differs the better. Having exactly one breadth is perfect.
         * @return Values in [0; 1]
         */
        @Override
        public double getRating(final PlankSolutionRow solutionRow) {
            final int numBreadths = solutionRow.getBreadths()
                    .size();
            return (numBreadths > 0) ? (1d / numBreadths) : 0;
        }
    },
    NUM_PLANKS("numPlanks"){
        /**
         * The more planks in a row the better.
         * @return Values in [0; Inf)
         */
        @Override
        public double getRating(PlankSolutionRow solutionRow) {
            return solutionRow.getPlanks()
                    .size();
        }
    },
    ROW_SPACE_WASTE("rowSpaceWaste") {
        /**
         * The less space a row wastes the better.
         * @return Values in [0; 1]
         */
        @Override
        public double getRating(final PlankSolutionRow solutionRow) {
            return ((double) solutionRow.getCurrentLength()) / solutionRow.getMaxLength();
        }
    };
    private final String resourceKey;

    PlankSolutionCriterion(final String resourceKey) {
        this.resourceKey = resourceKey;
    }

    /**
     * Rate the given row without considering any weight.
     *
     * @param solutionRow The {@link PlankSolutionRow} to rate.
     * @return The higher the returned value the better the criterion is fulfilled.
     */
    public abstract double getRating(final PlankSolutionRow solutionRow);

    public String getResourceKey() {
        return resourceKey;
    }
}
