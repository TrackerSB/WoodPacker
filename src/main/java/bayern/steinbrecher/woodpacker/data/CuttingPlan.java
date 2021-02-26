package bayern.steinbrecher.woodpacker.data;

import java.util.Collection;
import java.util.Collections;

public class CuttingPlan {
    private final Collection<PlankSolutionRow> rows;

    public CuttingPlan(Collection<PlankSolutionRow> rows) {
        this.rows = Collections.unmodifiableCollection(rows);
    }

    public Collection<PlankSolutionRow> getRows() {
        return rows;
    }
}
