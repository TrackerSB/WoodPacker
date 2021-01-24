package bayern.steinbrecher.woodpacker.utility;

import bayern.steinbrecher.javaUtility.CompareUtility;
import bayern.steinbrecher.woodpacker.data.BasePlank;
import bayern.steinbrecher.woodpacker.data.Plank;
import bayern.steinbrecher.woodpacker.data.PlankSolutionRow;
import bayern.steinbrecher.woodpacker.elements.ScaledCanvas;
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

/**
 * A utility for generating drawing actions that can be used with {@link ScaledCanvas}.
 *
 * @author Stefan Huber
 * @since 0.1
 */
public final class DrawActionGenerator {
    // Base planks drawing action configuration
    private static final int minNumGrainIndicationSteps = 5;
    private static final int maxNumGrainIndicationSteps = 50;
    private static final int preferredGrainIndicationStepSize = 10;

    // Required planks drawing action configuration
    /**
     * The percentage of the width which a label of a required plank is allowed to take.
     */
    private static final double MAX_LABEL_WIDTH_FACTOR = 0.75;

    private DrawActionGenerator() {
        throw new UnsupportedOperationException("Construction of instances is prohibited");
    }

    public static Consumer<GraphicsContext> forBasePlank(BasePlank basePlank) {
        if (basePlank == null) {
            return gc -> {
            };
        }

        List<Line> grainLines = switch (basePlank.getGrainDirection()) {
            case HORIZONTAL -> {
                final int numSteps = CompareUtility.clamp(
                        basePlank.getHeight() / preferredGrainIndicationStepSize,
                        minNumGrainIndicationSteps, maxNumGrainIndicationSteps);
                final double stepSize = basePlank.getHeight() / (double) numSteps;
                List<Line> lines = new ArrayList<>();
                for (double yPos = stepSize; yPos < basePlank.getHeight(); yPos += stepSize) {
                    lines.add(new Line(0, yPos, basePlank.getWidth(), yPos));
                }
                yield lines;
            }
            case VERTICAL -> {
                final int numSteps = CompareUtility.clamp(
                        basePlank.getWidth() / preferredGrainIndicationStepSize,
                        minNumGrainIndicationSteps, maxNumGrainIndicationSteps);
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
                        basePlank.getWidth() / preferredGrainIndicationStepSize,
                        minNumGrainIndicationSteps, maxNumGrainIndicationSteps);
                final double stepSize = basePlank.getWidth() / (double) numSteps;
                List<Line> lines = new ArrayList<>();
                for (double xPos = -topBottomXDelta + stepSize; xPos < basePlank.getWidth(); xPos += stepSize) {
                    lines.add(new Line(xPos, basePlank.getHeight(), xPos + topBottomXDelta, 0));
                }
                yield lines;
            }
        };

        return gc -> {
            gc.setFill(Color.BURLYWOOD);
            gc.fillRect(0, 0, basePlank.getWidth(), basePlank.getHeight());
            gc.setStroke(Color.GRAY);
            for (Line grainLine : grainLines) {
                gc.strokeLine(grainLine.getStartX(), grainLine.getStartY(),
                        grainLine.getEndX(), grainLine.getEndY());
            }
        };
    }

    public static Consumer<GraphicsContext> forRequiredPlanks(
            BasePlank basePlank, Iterable<PlankSolutionRow> placedPlankRows) {
        if (placedPlankRows == null) {
            return gc -> {
            };
        }

        return gc -> {
            gc.setTextAlign(TextAlignment.CENTER);
            for (PlankSolutionRow row : placedPlankRows) {
                Point2D rowToBasePlankOffset = row.getStartOffset();
                double plankToRowXOffset = 0;
                double plankToRowYOffset = 0;
                for (Plank plank : row.getPlanks()) {
                    double plankXPos = rowToBasePlankOffset.getX() + plankToRowXOffset;
                    double plankYPos = rowToBasePlankOffset.getY() + plankToRowYOffset;
                    gc.beginPath();
                    gc.rect(plankXPos, plankYPos, plank.getWidth(), plank.getHeight());
                    gc.setStroke(Color.BLACK);
                    gc.stroke();
                    gc.setFill(Color.BURLYWOOD);
                    gc.fill();

                    gc.setTextBaseline(VPos.CENTER);
                    gc.setFill(Color.BLACK);
                    double fontSize = basePlank.getHeight() / 20d;
                    gc.setFont(Font.font(fontSize));
                    double textXOffset = plankXPos + (plank.getWidth() / 2d);
                    double textYOffset = plankYPos + (plank.getHeight() / 2d);
                    double maxTextLength = MAX_LABEL_WIDTH_FACTOR * Math.max(plank.getWidth(), plank.getHeight());
                    if (plank.getHeight() > plank.getWidth()) {
                        gc.translate(textXOffset, textYOffset);
                        gc.rotate(-90);
                        gc.fillText(plank.getId(), 0, 0, maxTextLength);
                        gc.rotate(90);
                        gc.translate(-textXOffset, -textYOffset);
                    } else {
                        gc.fillText(plank.getId(), textXOffset, textYOffset, maxTextLength);
                    }

                    if (row.addHorizontal()) {
                        plankToRowXOffset += plank.getWidth();
                    } else {
                        plankToRowYOffset += plank.getHeight();
                    }
                }
                gc.setStroke(Color.RED);
                double rowWidth = row.addHorizontal() ? row.getCurrentLength() : row.getBreadth();
                double rowHeight = row.addHorizontal() ? row.getBreadth() : row.getCurrentLength();
                gc.strokeRect(rowToBasePlankOffset.getX(), rowToBasePlankOffset.getY(), rowWidth, rowHeight);
            }
        };
    }
}
