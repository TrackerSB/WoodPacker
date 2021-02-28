package bayern.steinbrecher.woodpacker.data;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class CuttingPlan {
    private final Collection<PlankSolutionRow> rows;

    public CuttingPlan(final Collection<PlankSolutionRow> rows) {
        this.rows = Collections.unmodifiableCollection(rows);
    }

    public Collection<PlankSolutionRow> getRows() {
        return rows;
    }
}
