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
    private final int oversize;

    public CuttingPlan(final Collection<PlankSolutionRow> rows, BasePlank basePlank, int oversize) {
        this.rows = Collections.unmodifiableCollection(rows);
        this.basePlank = basePlank;
        this.oversize = oversize;
    }

    public Collection<PlankSolutionRow> getRows() {
        return rows;
    }

    public BasePlank getBasePlank() {
        return basePlank;
    }

    public int getOversize() {
        return oversize;
    }
}
