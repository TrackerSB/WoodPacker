package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.javaUtility.CompareUtility;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.CuttingPlan;
import bayern.steinbrecher.woodpacker.data.EdgeBand;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.data.RequiredPlank;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
import bayern.steinbrecher.woodpacker.internal.CompileSettings;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility for generating drawing actions that can be used with {@link ScaledCanvas}. The colors used by any draw
 * action as well as the order of drawing components also consider which components of any drawing should be visible
 * when converting the final image to black and white (e.g. for purpose of printing).
 *
 * @author Stefan Huber
 * @since 0.1
 */
public final class DrawActionGenerator {
    private static final Logger LOGGER = Logger.getLogger(DrawActionGenerator.class.getName());

    // Base planks drawing action configuration
    private static final int MIN_NUM_GRAIN_INDICATION_STEPS = 10;
    private static final int MAX_NUM_GRAIN_INDICATION_STEPS = 50;
    private static final int PREFERRED_GRAIN_INDICATION_STEP_SIZE = 30;

    // Required planks drawing action configuration
    /**
     * The percentage of the width which a label of a required plank is allowed to take.
     */
    private static final double MAX_LABEL_SIZE_FACTOR = 0.75;
    private static final double EDGE_BAND_INSET_FACTOR = 0.04;

    private DrawActionGenerator() {
        throw new UnsupportedOperationException("Construction of instances is prohibited");
    }

    public static Consumer<GraphicsContext> forBasePlank(final BasePlank basePlank) {
        return forBasePlank(basePlank, 0);
    }

    public static Consumer<GraphicsContext> forBasePlank(final BasePlank basePlank, final int oversize) {
        Consumer<GraphicsContext> drawingActions;
        if (basePlank == null) {
            drawingActions = gc -> {
            };
        } else {
            final List<Line> grainLines = switch (basePlank.getGrainDirection()) {
                case HORIZONTAL -> {
                    final int numSteps = CompareUtility.clamp(
                            basePlank.getHeight() / PREFERRED_GRAIN_INDICATION_STEP_SIZE,
                            MIN_NUM_GRAIN_INDICATION_STEPS, MAX_NUM_GRAIN_INDICATION_STEPS);
                    final double stepSize = basePlank.getHeight() / (double) numSteps;
                    List<Line> lines = new ArrayList<>();
                    for (double yPos = stepSize; yPos < basePlank.getHeight(); yPos += stepSize) {
                        lines.add(new Line(0, yPos, basePlank.getWidth(), yPos));
                    }
                    yield lines;
                }
                case VERTICAL -> {
                    final int numSteps = CompareUtility.clamp(
                            basePlank.getWidth() / PREFERRED_GRAIN_INDICATION_STEP_SIZE,
                            MIN_NUM_GRAIN_INDICATION_STEPS, MAX_NUM_GRAIN_INDICATION_STEPS);
                    final double stepSize = basePlank.getWidth() / (double) numSteps;
                    List<Line> lines = new ArrayList<>();
                    for (double xPos = stepSize; xPos < basePlank.getWidth(); xPos += stepSize) {
                        lines.add(new Line(xPos, 0, xPos, basePlank.getHeight()));
                    }
                    yield lines;
                }
                case IRRELEVANT -> {
                    final double topBottomXDelta = basePlank.getHeight() / Math.tan(Math.toRadians(45));
                    final int numSteps = CompareUtility.clamp(
                            basePlank.getWidth() / PREFERRED_GRAIN_INDICATION_STEP_SIZE,
                            MIN_NUM_GRAIN_INDICATION_STEPS, MAX_NUM_GRAIN_INDICATION_STEPS);
                    final double stepSize = basePlank.getWidth() / (double) numSteps;
                    List<Line> lines = new ArrayList<>();
                    for (double xPos = -topBottomXDelta + stepSize; xPos < basePlank.getWidth(); xPos += stepSize) {
                        lines.add(new Line(xPos, basePlank.getHeight(), xPos + topBottomXDelta, 0));
                    }
                    yield lines;
                }
            };

            final int maxDimension = Math.max(basePlank.getHeight(), basePlank.getWidth());

            drawingActions = gc -> {
                gc.beginPath();
                gc.rect(0, 0, basePlank.getWidth(), basePlank.getHeight());
                gc.setFill(Color.BURLYWOOD);
                gc.fill();
                gc.setLineWidth(Math.max(1, maxDimension / 500d));
                gc.setStroke(Color.BLACK);
                gc.stroke();
                gc.closePath();

                for (final Line grainLine : grainLines) {
                    gc.strokeLine(grainLine.getStartX(), grainLine.getStartY(),
                            grainLine.getEndX(), grainLine.getEndY());
                }

                gc.beginPath();
                // The oversize border consists of four bars, i.e. an upper, right, lower and left bar
                gc.rect(0, 0, basePlank.getWidth(), oversize);
                gc.rect(basePlank.getWidth() - oversize, 0, basePlank.getWidth(), basePlank.getHeight());
                gc.rect(0, basePlank.getHeight() - oversize, basePlank.getWidth(), basePlank.getHeight());
                gc.rect(0, 0, oversize, basePlank.getHeight());
                gc.setFill(Color.gray(0.5, 0.5));
                gc.fill();
                gc.closePath();
            };
        }
        return drawingActions;
    }

    public static Consumer<GraphicsContext> forCuttingPlan(final CuttingPlan cuttingPlan) {
        Consumer<GraphicsContext> drawingActions;
        if (cuttingPlan.getBasePlank() == null) {
            drawingActions = gc -> {
            };
        } else {
            drawingActions = gc -> {
                forBasePlank(cuttingPlan.getBasePlank(), cuttingPlan.getOversize())
                        .accept(gc);

                gc.setTextAlign(TextAlignment.CENTER);
                for (final PlankSolutionRow row : cuttingPlan.getRows()) {
                    final Point2D rowToBasePlankOffset = row.getStartOffset();
                    double plankToRowXOffset = 0;
                    double plankToRowYOffset = 0;
                    for (final RequiredPlank plank : row.getPlanks()) {
                        // Draw plank shape
                        final double plankXPos = rowToBasePlankOffset.getX() + plankToRowXOffset;
                        final double plankYPos = rowToBasePlankOffset.getY() + plankToRowYOffset;

                        gc.beginPath();
                        gc.rect(plankXPos, plankYPos, plank.getWidth(), plank.getHeight());
                        gc.setFill(Color.BURLYWOOD);
                        gc.fill();
                        // NOTE 2021-03-08: Stroke width is set by #forBasePlank(...)
                        gc.setStroke(Color.BLACK);
                        gc.stroke();
                        gc.closePath();

                        // Indicate edge bands
                        gc.setStroke(Color.GRAY);
                        final double edgeBandInset = Math.min(plank.getHeight(), plank.getWidth())
                                * EDGE_BAND_INSET_FACTOR;
                        final double minXPos = plankXPos + edgeBandInset;
                        final double maxXPos = plankXPos + plank.getWidth() - edgeBandInset;
                        final double minYPos = plankYPos + edgeBandInset;
                        final double maxYPos = plankYPos + plank.getHeight() - edgeBandInset;
                        for (final EdgeBand edgeBand : plank.getEdgeBands()) {
                            switch (edgeBand) {
                                case LEFT -> gc.strokeLine(minXPos, minYPos, minXPos, maxYPos);
                                case UPPER -> gc.strokeLine(minXPos, minYPos, maxXPos, minYPos);
                                case RIGHT -> gc.strokeLine(maxXPos, minYPos, maxXPos, maxYPos);
                                case LOWER -> gc.strokeLine(minXPos, maxYPos, maxXPos, maxYPos);
                                default -> LOGGER.log(Level.WARNING,
                                        String.format("Drawing edge band of type %s is not supported", edgeBand));
                            }
                        }
                        gc.setStroke(Color.BLACK);

                        final boolean drawLabelVertical = plank.getHeight() > plank.getWidth();
                        // Size in text direction
                        final double availableLength = drawLabelVertical ? plank.getHeight() : plank.getWidth();
                        // Size orthogonal to text direction
                        final double availableHeight = drawLabelVertical ? plank.getWidth() : plank.getHeight();

                        // Draw plank label
                        gc.setTextBaseline(VPos.CENTER);
                        gc.setFill(Color.BLACK);
                        final double maxLabelLength = MAX_LABEL_SIZE_FACTOR * availableLength;
                        final double maxLabelHeight = MAX_LABEL_SIZE_FACTOR * availableHeight;
                        final double labelFontSize = Math.min(
                                cuttingPlan.getBasePlank().getHeight() / 15d,
                                maxLabelHeight);
                        gc.setFont(Font.font(labelFontSize));
                        final double labelXOffset = plankXPos + (plank.getWidth() / 2d);
                        final double labelYOffset = plankYPos + (plank.getHeight() / 2d);
                        if (drawLabelVertical) {
                            gc.translate(labelXOffset, labelYOffset);
                            gc.rotate(-90);
                            gc.fillText(plank.getPlankId(), 0, 0, maxLabelLength);
                            gc.rotate(90);
                            gc.translate(-labelXOffset, -labelYOffset);
                        } else {
                            gc.fillText(plank.getPlankId(), labelXOffset, labelYOffset, maxLabelLength);
                        }

                        // Draw dimensioning labels
                        final double dimensioningLabelFontSize
                                = Math.min(availableLength - maxLabelLength, availableHeight - maxLabelHeight) / 2;
                        gc.setFont(Font.font(dimensioningLabelFontSize));
                        gc.setTextBaseline(VPos.TOP);
                        gc.fillText(String.valueOf(plank.getWidth()),
                                plankXPos + plank.getWidth() / 2d,
                                plankYPos + edgeBandInset,
                                plank.getWidth());
                        gc.rotate(-90);
                        gc.fillText(String.valueOf(plank.getHeight()),
                                -plankYPos - plank.getHeight() / 2d,
                                plankXPos + edgeBandInset,
                                plank.getHeight());
                        gc.rotate(90);

                        // Move offset to next plank in solution row
                        if (row.isAddingHorizontally()) {
                            plankToRowXOffset += plank.getWidth() + row.getCuttingWidth();
                        } else {
                            plankToRowYOffset += plank.getHeight() + row.getCuttingWidth();
                        }
                    }

                    if (CompileSettings.isGraphicalDebugEnabled()) {
                        gc.save();
                        gc.setLineDashes(0);
                        gc.setStroke(Color.BLUE);
                        final double rowWidth = row.isAddingHorizontally()
                                ? row.getCurrentLength()
                                : row.getCurrentBreadth();
                        final double rowHeight = row.isAddingHorizontally()
                                ? row.getCurrentBreadth()
                                : row.getCurrentLength();
                        gc.strokeRect(rowToBasePlankOffset.getX(), rowToBasePlankOffset.getY(), rowWidth, rowHeight);

                        gc.setLineDashes(20, 20);
                        gc.setStroke(Color.RED);
                        final double rowFullWidth = row.isAddingHorizontally()
                                ? row.getMaxLength()
                                : row.getCurrentBreadth();
                        final double rowFullHeight = row.isAddingHorizontally()
                                ? row.getCurrentBreadth() : row.getMaxLength();
                        gc.strokeRect(rowToBasePlankOffset.getX(), rowToBasePlankOffset.getY(),
                                rowFullWidth, rowFullHeight);
                        gc.restore();
                    }
                }
            };
        }
        return drawingActions;
    }

    public static Consumer<GraphicsContext> forPlaceholder(final ScaledCanvas canvas, final String message) {
        return gc -> {
            gc.setFill(Color.GRAY);
            gc.fillRect(0, 0, canvas.getTheoreticalWidth(), canvas.getTheoreticalHeight());
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font(canvas.getTheoreticalHeight() / 10));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.fillText(message,
                    canvas.getTheoreticalWidth() / 2,
                    canvas.getTheoreticalHeight() / 2,
                    canvas.getTheoreticalWidth());
        };
    }
}
