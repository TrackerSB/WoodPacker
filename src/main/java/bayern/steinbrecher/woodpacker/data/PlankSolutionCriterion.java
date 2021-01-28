package bayern.steinbrecher.woodpacker.data;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public enum PlankSolutionCriterion {
    /**
     * The less the breadths of the planks differs the better.
     */
    BREATH_DIFFERENCES("breadthDifferences") {
        @Override
        public double getRating(PlankSolutionRow solutionRow) {
            return 1d / solutionRow.getBreadths().size();
        }
    },
    /**
     * The less space a row wastes the better.
     */
    ROW_SPACE_WASTE("rowSpaceWaste") {
        @Override
        public double getRating(PlankSolutionRow solutionRow) {
            return ((double) solutionRow.getCurrentLength()) / solutionRow.getMaxLength();
        }
    };
    private final String resourceKey;

    PlankSolutionCriterion(String resourceKey) {
        this.resourceKey = resourceKey;
    }

    /**
     * Rate the given row without considering any weight.
     *
     * @param solutionRow The {@link PlankSolutionRow} to rate.
     * @return The higher the returned value the better the criterion is fulfilled.
     */
    public abstract double getRating(PlankSolutionRow solutionRow);

    public String getResourceKey() {
        return resourceKey;
    }
}
