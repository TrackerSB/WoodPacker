package bayern.steinbrecher.woodpacker.data;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class CuttingPlan {
    private final Collection<PlankSolutionRow> rows;
    private final BasePlank basePlank;

    public CuttingPlan(final Collection<PlankSolutionRow> rows, BasePlank basePlank) {
        this.rows = Collections.unmodifiableCollection(rows);
        this.basePlank = basePlank;
    }

    public Collection<PlankSolutionRow> getRows() {
        return rows;
    }

    public BasePlank getBasePlank() {
        return basePlank;
    }
}
