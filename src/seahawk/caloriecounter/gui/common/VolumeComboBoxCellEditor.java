package seahawk.caloriecounter.gui.common;

import seahawk.caloriecounter.domain.api.MeasurementUnit;

import javax.swing.*;

public class VolumeComboBoxCellEditor extends DefaultCellEditor {
  private static final JComboBox<MeasurementUnit> comboBox = new JComboBox<>(MeasurementUnit.getVolumeUnits());

  public VolumeComboBoxCellEditor() {
    super(comboBox);
  }
}
