package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.woodpacker.WoodPacker;
import bayern.steinbrecher.woodpacker.data.EdgeBand;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Line;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class EdgeBandSelectorSkin extends SkinBase<EdgeBandSelector> {
    private static final int EDGE_BAND_LENGTH = 30;

    private ToggleButton createEdgeBandButton(final EdgeBand edgeBand) {
        var edgeBandSelectedButton = new ToggleButton();
        var edgeBandLine = switch (edgeBand) {
            case LEFT, RIGHT -> new Line(0, 0, 0, EDGE_BAND_LENGTH);
            case UPPER, LOWER -> new Line(0, 0, EDGE_BAND_LENGTH, 0);
        };
        edgeBandSelectedButton.setGraphic(edgeBandLine);
        GridPane.setHalignment(edgeBandSelectedButton, HPos.CENTER);
        GridPane.setValignment(edgeBandSelectedButton, VPos.CENTER);
        return edgeBandSelectedButton;
    }

    private CheckedIntegerSpinner createThicknessSpinner(final EdgeBandSelector control) {
        CheckedIntegerSpinner thicknessSpinner = new CheckedIntegerSpinner();
        thicknessSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1, 1));
        thicknessSpinner.setPromptText(WoodPacker.getResource("thickness"));
        thicknessSpinner.setEditable(true);
        thicknessSpinner.getEditor()
                .setText("");
        thicknessSpinner.setMaxWidth(2 * EDGE_BAND_LENGTH);

        ChangeListener<Integer> thicknessChangedListener = (obs, previousThickness, currentThickness) -> {
            control.setThickness(currentThickness);
        };
        thicknessSpinner.valueProperty()
                .addListener(thicknessChangedListener);
        // Ensure initial state
        thicknessChangedListener.changed(null, null, thicknessSpinner.getValue());
        control.thicknessProperty()
                .addListener((obs, previousThickness, currentThickness) -> {
                    thicknessSpinner.getEditor()
                            .setText(String.valueOf(currentThickness));
                });

        GridPane.setHalignment(thicknessSpinner, HPos.CENTER);
        GridPane.setValignment(thicknessSpinner, VPos.CENTER);
        return thicknessSpinner;
    }

    protected EdgeBandSelectorSkin(EdgeBandSelector control) {
        super(control);

        ToggleButton leftButton = createEdgeBandButton(EdgeBand.LEFT);
        ToggleButton upperButton = createEdgeBandButton(EdgeBand.UPPER);
        ToggleButton rightButton = createEdgeBandButton(EdgeBand.RIGHT);
        ToggleButton lowerButton = createEdgeBandButton(EdgeBand.LOWER);

        BooleanBinding anyEdgeSelected = leftButton.selectedProperty()
                .or(upperButton.selectedProperty())
                .or(rightButton.selectedProperty())
                .or(lowerButton.selectedProperty());

        CheckedIntegerSpinner thicknessSpinner = createThicknessSpinner(control);
        thicknessSpinner.checkedProperty().bind(anyEdgeSelected);

        var holder = new GridPane();
        holder.add(upperButton, 1, 0);
        holder.addRow(1, leftButton, thicknessSpinner, rightButton);
        holder.add(lowerButton, 1, 2);
        getChildren()
                .add(holder);
        control.addValidityConstraint(anyEdgeSelected.not().or(thicknessSpinner.validProperty()));
    }
}
