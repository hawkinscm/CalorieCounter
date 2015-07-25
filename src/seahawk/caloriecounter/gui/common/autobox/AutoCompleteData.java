package seahawk.caloriecounter.gui.common.autobox;

import java.util.*;

class AutoCompleteData {
  private List<String> allData;
  private List<String> allDataInLowercase;
  private List<String> filtered;

  void setAllData(Set<String> data) {
    allData = new ArrayList<>(data.size());
    allDataInLowercase = new ArrayList<>(data.size());
    for (Object datum : data) {
      allData.add(datum.toString());
      allDataInLowercase.add(datum.toString().toLowerCase());
    }

    filtered = allData;
  }

  List<String> getFiltered() {
    if (filtered == null)
      filtered = allData;

    return filtered;
  }

  void setFilter(String filter) {
    if (filter == null || filter.isEmpty()) {
      filtered = allData;
    }
    else {
      filtered = new ArrayList<>();
      filter = filter.toLowerCase();
      for (int index = 0; index < allDataInLowercase.size(); index++) {
        if (allDataInLowercase.get(index).contains(filter)) {
          filtered.add(allData.get(index));
        }
      }
    }
  }
}
