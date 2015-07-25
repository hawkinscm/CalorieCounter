package seahawk.caloriecounter.gui.common.autobox;

import javax.swing.*;
import java.util.Set;

public class AutoCompleteModel extends AbstractListModel<String> implements ComboBoxModel<String> {
  private String selected;
  private AutoCompleteData data = new AutoCompleteData();

  public AutoCompleteModel(Set<String> data) {
    setData(data);
  }

  public void setData(Set<String> data) {
    this.data.setAllData(data);
  }

  public void setFilter(String filter) {
    int size1 = getSize();
    data.setFilter(filter);

    int size2 = getSize();

    if (size1 < size2) {
      fireIntervalAdded(this, size1, size2 - 1);
      fireContentsChanged(this, 0, size1 - 1);
    }
    else if (size1 > size2) {
      fireIntervalRemoved(this, size2, size1 - 1);
      fireContentsChanged(this, 0, size2 - 1);
    }
  }

  @Override
  public String getSelectedItem() {
    return selected;
  }

  @Override
  public void setSelectedItem(Object anObject) {
    if ((selected != null && !selected.equals(anObject)) || selected == null && anObject != null) {
      selected = (String) anObject;
      fireContentsChanged(this, -1, -1);
    }
  }

  @Override
  public int getSize() {
    return data.getFiltered().size();
  }

  @Override
  public String getElementAt(int index) {
    return data.getFiltered().get(index);
  }
}
