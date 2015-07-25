package seahawk.caloriecounter.gui.common;

import seahawk.caloriecounter.domain.api.MeasurementUnit;

import javax.swing.*;

public class WeightComboBoxCellEditor extends DefaultCellEditor {
  private static final JComboBox<MeasurementUnit> comboBox = new JComboBox<>(MeasurementUnit.getWeightUnits());

  public WeightComboBoxCellEditor() {
    super(comboBox);
  }
}
