package bayern.steinbrecher.woodpacker.elements;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Pagination;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.PaginationSkin;
import javafx.scene.image.WritableImage;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class SnapshotPagination extends Pagination {
    private static final Logger LOGGER = Logger.getLogger(SnapshotPagination.class.getName());
    private static final Field CURRENT_PAGE_CONTENT_FIELD;

    static {
        try {
            CURRENT_PAGE_CONTENT_FIELD = PaginationSkin.class
                    .getDeclaredField("currentStackPane");
        } catch (NoSuchFieldException ex) {
            throw new ExceptionInInitializerError(ex);
        }
        CURRENT_PAGE_CONTENT_FIELD.setAccessible(true);
    }

    private Node determineSnapshotCandidate() {
        /* NOTE 2021-03-07: The skin of the pagination has three children. One contains the pagination
         * controls and the other two contain the current page as well as the next page which is
         * populated for implementing the pagination animation. Which one of the two contains the
         * current content alternates. Thus reflective access to the skin is required for determining
         * the current child holding the content.
         */
        Node snapshotCandidate;
        final Skin<?> skin = getSkin();
        if (skin instanceof PaginationSkin) {
            final PaginationSkin paginationSkin = (PaginationSkin) skin;
            try {
                final Object currentPageObject = CURRENT_PAGE_CONTENT_FIELD.get(paginationSkin);
                if (currentPageObject instanceof Parent) {
                    final Parent currentPage = (Parent) currentPageObject;
                    final ObservableList<Node> currentPageChildren
                            = currentPage.getChildrenUnmodifiable();
                    if (currentPageChildren.size() == 1) { // NOPMD - The component to screenshot was ascertainable
                        snapshotCandidate = currentPageChildren.get(0);
                    } else {
                        LOGGER.log(Level.INFO, "It's unclear from which page content child to "
                                + "take a snapshot of. Taking a snapshot of the complete page.");
                        snapshotCandidate = currentPage;
                    }
                } else {
                    LOGGER.log(Level.WARNING,
                            "The current implementation of the pagination skin is unsupported");
                    snapshotCandidate = this;
                }
            } catch (IllegalAccessException ex) {
                LOGGER.log(Level.WARNING, "Could not access inner content of current page", ex);
                snapshotCandidate = this;
            }
        } else {
            LOGGER.log(Level.WARNING,
                    String.format("The skin class %s is unsupported.", skin.getClass().getName()));
            snapshotCandidate = this;
        }
        return snapshotCandidate;
    }

    public List<WritableImage> snapshotContents(final SnapshotParameters params) {
        if (Platform.isFxApplicationThread()) {
            LOGGER.log(Level.WARNING, "This method should not be called on JavaFX Application Thread");
        }
        final int originalPageIndex = getCurrentPageIndex();

        final List<WritableImage> images = new ArrayList<>();
        final AtomicBoolean tookScreenshot = new AtomicBoolean();
        for (int i = 0; i < getPageCount(); i++) {
            tookScreenshot.set(false);
            final int nextIndexForSnapshot = i;
            Platform.runLater(() -> {
                setCurrentPageIndex(nextIndexForSnapshot);
                new Thread(() -> {
                    try {
                        Thread.sleep(500); // Wait for animation of pagination
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.WARNING,
                                String.format("The snapshot for page %d may be inaccurate", nextIndexForSnapshot));
                    }
                    Platform.runLater(() -> {
                        final Node snapshotCandidate = determineSnapshotCandidate();
                        WritableImage image;
                        if (snapshotCandidate instanceof ScaledCanvas) {
                            image = ((ScaledCanvas) snapshotCandidate)
                                    .snapshotDrawingArea();
                        } else {
                            image = snapshotCandidate.snapshot(params, null);
                        }
                        images.add(image);
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
                LOGGER.log(Level.WARNING, String.format(
                        "It's unclear whether a snapshot for page %d was generated", nextIndexForSnapshot), ex);
            }
        }

        // Restore previous shown page
        Platform.runLater(() -> setCurrentPageIndex(originalPageIndex));

        return images;
    }
}
