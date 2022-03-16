package bayern.steinbrecher.woodpacker.elements;

import bayern.steinbrecher.checkedElements.CheckableControlBase;
import bayern.steinbrecher.checkedElements.CheckedControl;
import bayern.steinbrecher.checkedElements.report.ReportEntry;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

/**
 * @author Stefan Huber
 * @since 0.1
 */
public class EdgeBandSelector extends Control implements CheckedControl {
    private CheckedControl ccBase = new CheckableControlBase<>(this);
    private final BooleanProperty leftSelected = new SimpleBooleanProperty();
    private final BooleanProperty upperSelected = new SimpleBooleanProperty();
    private final BooleanProperty rightSelected = new SimpleBooleanProperty();
    private final BooleanProperty lowerSelected = new SimpleBooleanProperty();
    private final IntegerProperty thickness = new SimpleIntegerProperty(1);

    public EdgeBandSelector() {
        super();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EdgeBandSelectorSkin(this);
    }

    @Override
    public ReadOnlyBooleanProperty validProperty() {
        return ccBase.validProperty();
    }

    @Override
    public boolean addValidityConstraint(ObservableBooleanValue constraint) {
        return ccBase.addValidityConstraint(constraint);
    }

    @Override
    public ObservableList<ReportEntry> getReports() {
        return ccBase.getReports();
    }

    @Override
    public boolean addReport(ReportEntry report) {
        return ccBase.addReport(report);
    }

    @Override
    public ReadOnlyBooleanProperty checkedProperty() {
        return ccBase.checkedProperty();
    }

    public BooleanProperty leftSelectedProperty() {
        return leftSelected;
    }

    public boolean isLeftSelected() {
        return leftSelectedProperty().get();
    }

    public void setLeftSelected(boolean selected) {
        leftSelectedProperty().set(selected);
    }

    public BooleanProperty upperSelectedProperty() {
        return upperSelected;
    }

    public boolean isUpperSelected() {
        return upperSelectedProperty().get();
    }

    public void setUpperSelected(boolean selected) {
        upperSelectedProperty().set(selected);
    }

    public BooleanProperty rightSelectedProperty() {
        return rightSelected;
    }

    public boolean isRightSelected() {
        return rightSelectedProperty().get();
    }

    public void setRightSelected(boolean selected) {
        rightSelectedProperty().set(selected);
    }

    public BooleanProperty lowerSelectedProperty() {
        return lowerSelected;
    }

    public boolean isLowerSelected() {
        return lowerSelectedProperty().get();
    }

    public void setLowerSelected(boolean selected) {
        lowerSelectedProperty().set(selected);
    }

    public IntegerProperty thicknessProperty() {
        return thickness;
    }

    public int getThickness() {
        return thicknessProperty().get();
    }

    public void setThickness(int thickness) {
        thicknessProperty().set(thickness);
    }
}
