package bayern.steinbrecher.woodpacker.elements;

import javafx.application.Platform;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Pagination;
import javafx.scene.image.WritableImage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnapshotPagination extends Pagination {
    private static final Logger LOGGER = Logger.getLogger(SnapshotPagination.class.getName());

    public List<WritableImage> snapshotContents(final SnapshotParameters params) {
        if (Platform.isFxApplicationThread()) {
            LOGGER.log(Level.WARNING, "This method should not be called on JavaFX Application Thread");
        }
        final int originalPageIndex = getCurrentPageIndex();

        List<WritableImage> images = new ArrayList<>();
        for (int i = 0; i < getPageCount(); i++) {
            AtomicBoolean tookScreenshot = new AtomicBoolean(false);
            final int nextIndexForSnapshot = i;
            Platform.runLater(() -> {
                setCurrentPageIndex(nextIndexForSnapshot);
                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Wait for animation
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.WARNING,
                                String.format("The snapshot for page %d may be inaccurate", nextIndexForSnapshot));
                    }
                    Platform.runLater(() -> {
                        images.add(snapshot(params, null));
                        tookScreenshot.set(true);
                    });
                }).start();
            });
            try {
                final FutureTask<Object> snapshotFinishTask = new FutureTask<>(() -> {
                    while (!tookScreenshot.get()) {
                        Thread.yield();
                    }
                    return null;
                });
                snapshotFinishTask.run();
                snapshotFinishTask.get(3, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException ex) {
                LOGGER.log(Level.WARNING,
                        String.format("Could not take snapshot for page %d", nextIndexForSnapshot), ex);
            }
        }

        // Restore previous shown page
        Platform.runLater(() -> setCurrentPageIndex(originalPageIndex));

        return images;
    }
}
