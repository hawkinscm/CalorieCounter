
package seahawk.caloriecounter.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.impl.NutritionFactsImpl;
import seahawk.caloriecounter.gui.common.CommonUtil;
import seahawk.caloriecounter.gui.common.LinkLabel;
import seahawk.xmlhandler.XmlException;
import seahawk.xmlhandler.XmlReader;
import seahawk.xmlhandler.XmlTag;

public final class FoodstuffsPanel extends JPanel implements Observer {
  private static final String NEW_MARKER = "-New-";

	private static final int SELECT_COLUMN   = 0;
  private static final int NAME_COLUMN     = 1;
	private static final int CATEGORY_COLUMN = 2;
	private static final int AMOUNT_COLUMN   = 3;
	private static final int UNIT_COLUMN     = 4;
  private static final int FACTS_START_COLUMN = 5;
	
  private CalorieCounterActionHandler actionHandler;
  private FoodStore foodStore;

	private DefaultTableModel foodstuffTableModel;
	private JTable foodstuffTable;
  private String selectedFoodNameBeforeEdit;
  private FoodSortType foodSortType;
  private boolean reverseSort;

	public FoodstuffsPanel(CalorieCounterActionHandler actionHandler, FoodStore foodStore, FoodSortType foodSortType, boolean reverseSort) {
    this.actionHandler = actionHandler;
    this.foodStore = foodStore;
    this.foodSortType = foodSortType;
    this.reverseSort = reverseSort;

    actionHandler.addObserver(this);

    this.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(10, 15, 10, 20);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    add(createAddRemovePanel(), c);

    c.gridx = 1;
    c.weightx = 1;
    JLabel spacingLabel = new JLabel(" ");
    add(spacingLabel, c);

    c.insets.left = 20;
    c.insets.top = 0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy++;
    c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    add(createFoodstuffTablePane(), c);
	}

  public FoodSortType getFoodSortType() {
    return foodSortType;
  }

  public boolean isReverseSort() {
    return reverseSort;
  }
	
	private JPanel createAddRemovePanel() {
		JPanel addRemovePanel = new JPanel(new FlowLayout());

		JButton addButton = new JButton("Add");
		addButton.setMnemonic(KeyEvent.VK_A);
		addButton.addActionListener(e -> addFoodstuff());
		addRemovePanel.add(addButton);

    JButton importButton = new JButton("Import");
    importButton.setMnemonic(KeyEvent.VK_I);
    importButton.addActionListener(e -> importFoodstuff());
    addRemovePanel.add(importButton);

		JButton copyButton = new JButton("Copy");
		copyButton.setMnemonic(KeyEvent.VK_C);
		copyButton.addActionListener(e -> copyFoodstuff());
		addRemovePanel.add(copyButton);
		
		JButton deleteButton = new JButton("Delete");
		deleteButton.setMnemonic(KeyEvent.VK_D);
		deleteButton.addActionListener(e -> deleteFoodstuffs());
		addRemovePanel.add(deleteButton);

    JButton renameCategoryButton = new JButton("Rename Category");
    renameCategoryButton.setMnemonic(KeyEvent.VK_R);
    renameCategoryButton.addActionListener(e -> renameCategory());
    addRemovePanel.add(renameCategoryButton);

		return addRemovePanel;
	}

	private JScrollPane createFoodstuffTablePane() {
    DefaultTableColumnModel foodstuffColumnModel = new DefaultTableColumnModel();
    foodstuffColumnModel.addColumn(CommonUtil.createColumn(SELECT_COLUMN, null, 16, null));
    foodstuffColumnModel.addColumn(CommonUtil.createColumn(NAME_COLUMN, "Name", 90, 370, null, null));
    foodstuffColumnModel.addColumn(CommonUtil.createColumn(CATEGORY_COLUMN, "Category", 60, 130, null, null));
    foodstuffColumnModel.addColumn(CommonUtil.createColumn(AMOUNT_COLUMN, "Amt", 35, CommonUtil.getRightAlignedCellRenderer()));
    foodstuffColumnModel.addColumn(CommonUtil.createColumn(UNIT_COLUMN, "Unit", 60, 130, null, null));

    int numColumns = FACTS_START_COLUMN;
    for (NutrientType type : NutrientType.values())
      foodstuffColumnModel.addColumn(CommonUtil.createColumn(numColumns++, type.getDisplayName(),
         getNutrientTypeColumnSize(type), CommonUtil.getRightAlignedCellRenderer()));

    foodstuffTableModel = new DefaultTableModel(0, numColumns) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return true;
      }

      @Override
      public void fireTableCellUpdated(int row, int column) {
        if (column == NAME_COLUMN)
          updateName(row);
        else if (column == CATEGORY_COLUMN)
          updateCategory(row);
        else if (column == AMOUNT_COLUMN)
          updateAmount(row);
        else if (column == UNIT_COLUMN)
          updateUnit(row);
        else if (column != SELECT_COLUMN)
          updateFact(row, column);
      }
    };

    foodstuffTable = new JTable(foodstuffTableModel, foodstuffColumnModel) {
      @Override
      public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        super.changeSelection(row, column, toggle, extend);

        if (column == CATEGORY_COLUMN || column == UNIT_COLUMN)
          CommonUtil.selectComboBox(this, row, column);
      }

      @Override
      public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);

        if (c instanceof JTextComponent) {
          ((JTextField) c).selectAll();
          selectedFoodNameBeforeEdit = ((JTextField) c).getText();
        }

        return c;
      }

      @Override
      public Class getColumnClass(int column) {
        if (column == SELECT_COLUMN)
          return Boolean.class;
        return super.getColumnClass(column);
      }
    };
    foodstuffTable.setRowSelectionAllowed(false);
    foodstuffTable.setSurrendersFocusOnKeystroke(true);
    foodstuffTable.getTableHeader().setReorderingAllowed(false);
    CommonUtil.correctSortedFoodTableKeyActions(foodstuffTable, 1, foodstuffTable.getColumnCount() - 1);

    foodstuffTable.getTableHeader().addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          int column = foodstuffTable.columnAtPoint(e.getPoint());
          if (column == NAME_COLUMN)
            setFoodSortType(FoodSortType.NAME);
          else if (column == CATEGORY_COLUMN)
            setFoodSortType(FoodSortType.FOODSTUFF_CATEGORY);
          else if (column == AMOUNT_COLUMN)
            setFoodSortType(FoodSortType.AMOUNT);
          else if (column == UNIT_COLUMN)
            setFoodSortType(FoodSortType.UNIT);
          else
            setFoodSortType(FoodSortType.getBy(NutrientType.getByIndex(column - FACTS_START_COLUMN)));

          refreshFoodstuffTable();
        }
      }

      public void mousePressed(MouseEvent e) {
      }

      public void mouseReleased(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseExited(MouseEvent e) {
      }
    });

    refreshFoodstuffTable();
    refreshCategoryComboBoxes();
    refreshMeasurementUnitComboBoxes();

    foodstuffTable.setPreferredScrollableViewportSize(foodstuffTable.getPreferredSize());
    return new JScrollPane(foodstuffTable,
       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

  private void setFoodSortType(FoodSortType newSortType) {
    //noinspection SimplifiableConditionalExpression
    reverseSort = (foodSortType == newSortType) ? !reverseSort : false;
    foodSortType = newSortType;
  }

  private int getNutrientTypeColumnSize(NutrientType type) {
    switch (type) {
      case CALORIES : return 40;
      case PROTEIN : return 50;
      case FAT_CALORIES : return 50;
      case SODIUM : return 70;
      default : return 60;
    }
  }

  private void refreshFoodstuffTable() {
    foodstuffTableModel.setRowCount(0);
    for (Foodstuff foodstuff : foodStore.getFoodstuffs(foodSortType, reverseSort))
      addFoodstuffRow(foodstuff);
  }

  private void addFoodstuffRow(Foodstuff foodstuff) {
    Object[] foodRow = new Object[foodstuffTableModel.getColumnCount()];
    foodRow[SELECT_COLUMN] = false;
    foodRow[NAME_COLUMN] = foodstuff.getName();
    foodRow[CATEGORY_COLUMN] = foodstuff.getCategory().toString();
    foodRow[AMOUNT_COLUMN] = foodstuff.getAmount().toString();
    foodRow[UNIT_COLUMN] = foodstuff.getUnit().toString();

    NutritionFacts facts = foodstuff.getFacts();
    int idx = FACTS_START_COLUMN;
    for (NutrientType type : NutrientType.values())
      foodRow[idx++] = type.getDisplayMeasure(facts.getAmount(type));

    foodstuffTableModel.addRow(foodRow);
  }

  private void refreshCategoryComboBoxes() {
    TableColumn column = foodstuffTable.getColumnModel().getColumn(CATEGORY_COLUMN);
    JComboBox<String> comboBox = new JComboBox<>();
    int count = 0;
    for (FoodstuffCategory category : FoodstuffCategory.values()) {
      if (count % 7 == 0)
        comboBox.addItem(NEW_MARKER); // default view of combo box is 8 rows; this will make sure new is always visible in drop-down list

      comboBox.addItem(category.toString());
      count++;
    }
    column.setCellEditor(new DefaultCellEditor(comboBox));
  }

  private void refreshMeasurementUnitComboBoxes() {
    TableColumn column = foodstuffTable.getColumnModel().getColumn(UNIT_COLUMN);
    JComboBox<String> comboBox = new JComboBox<>();
    comboBox.addItem(NEW_MARKER);
    int count = -(MeasurementUnit.getVolumeUnits().length + MeasurementUnit.getWeightUnits().length);
    for (MeasurementUnit unit : MeasurementUnit.values()) {
      if (count >= 0 && count % 7 == 0)
        comboBox.addItem(NEW_MARKER); // default view of combo box is 8 rows; this will make sure new is always visible in drop-down list (where appropriate)

      comboBox.addItem(unit.toString());
      count++;
    }
    column.setCellEditor(new DefaultCellEditor(comboBox));
  }
	
	private void addFoodstuff() {
		finishTableEditing();

    String foodName = getNewFoodName("");
    if (foodName == null)
      return;

    actionHandler.addFoodstuff(foodName, FoodstuffCategory.values().iterator().next());
    foodstuffTable.changeSelection(foodstuffTable.getSelectedRow(), NAME_COLUMN, false, false);
	}

  private String getNewFoodName(String defaultName) {
    String chosenFoodName = (String) JOptionPane.showInputDialog(this, "Food Name:", "New Foodstuff",
         JOptionPane.QUESTION_MESSAGE, null, null, defaultName);

    if (chosenFoodName == null)
      return null;

    if (chosenFoodName.trim().isEmpty()) {
      return getNewFoodName("");
    }

    if (Character.isLowerCase(chosenFoodName.charAt(0))) {
      String remainingFoodName = chosenFoodName.length() > 1 ? chosenFoodName.substring(1) : "";
      chosenFoodName = Character.toUpperCase(chosenFoodName.charAt(0)) + remainingFoodName;
    }

    if (foodStore.containsFood(chosenFoodName)) {
      JOptionPane.showMessageDialog(this, "A food with this name already exists.", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
      return getNewFoodName(chosenFoodName);
    }
    return chosenFoodName;
  }

  private void importFoodstuff() {
    finishTableEditing();


    NutritionFacts nutritionFacts = parsePage();
    if (nutritionFacts == null) {
      return;
    }

    String foodName = getNewFoodName("");
    if (foodName == null)
      return;

    actionHandler.addFoodstuff(foodName, FoodstuffCategory.values().iterator().next());
    Foodstuff food = foodStore.getFoodstuff(foodName);
    for (NutrientType type : NutrientType.values()) {
      actionHandler.setFoodFactAmount(food, type, nutritionFacts.getAmount(type));
    }
    foodstuffTable.changeSelection(foodstuffTable.getSelectedRow(), NAME_COLUMN, false, false);
  }

  private NutritionFacts parsePage() {
    JPanel requestPanel = new JPanel();
    requestPanel.add(new JLabel("http URL of nutrition facts: "));
    requestPanel.add(new LinkLabel("CalorieKing", "http://www.calorieking.com/"));
    requestPanel.add(new LinkLabel("nutritionix", "http://www.nutritionix.com/"));
    String nutritionFactsLink = (String) JOptionPane.showInputDialog(this, requestPanel, "Import Foodstuff",
       JOptionPane.QUESTION_MESSAGE, null, null, null);

    if (nutritionFactsLink == null) {
      return null;
    }

    try {
      URL pageUrl = new URL(nutritionFactsLink);
      HttpURLConnection connection = (HttpURLConnection) pageUrl.openConnection();
      connection.addRequestProperty("User-Agent", "Mozilla/4.0");
      try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
        if (nutritionFactsLink.contains("calorieking.com")) {
          return getNutritionFactsFromCalorieKingPage(reader);
        }
        else if (nutritionFactsLink.contains("nutritionix.com")) {
          return getNutritionFactsFromNutritionixPage(reader);
        }
        else {
          throw new IllegalArgumentException("Unsupported site.  Must use calorieking or nutritionix");
        }
      }
    }
    catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Unable to read nutrition data: " + e.getMessage(), "Calorie Counter", JOptionPane.ERROR_MESSAGE);
      e.printStackTrace();
      return null;
    }
  }

  // todo following code to separate classes
  private NutritionFacts getNutritionFactsFromCalorieKingPage(BufferedReader reader) throws IOException, XmlException {
    String tableHtml = "";
    boolean tableFound = false;
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      if (!tableFound && inputLine.contains("<table>")) {
        tableFound = true;
      }

      if (tableFound) {
        tableHtml += inputLine;
        if (inputLine.contains("</table>")) {
          break;
        }
      }
    }
    XmlTag rootTag = new XmlReader().parseXML(tableHtml);

    NutritionFactsImpl facts = new NutritionFactsImpl();
    List<NutrientType> unsetTypes = new ArrayList<>(Arrays.asList(NutrientType.values()));
    for (XmlTag rowTag : rootTag.getSubTag("table").getSubTags("tr")) {
      NutrientType type = parseNutrientType(rowTag);
      if (type != null) {
        PositiveDecimalNumber amount = parseAmount(rowTag, type);
        facts.setAmount(type, amount);
        unsetTypes.remove(type);
      }
    }
    if (unsetTypes.size() > 1) {
      JOptionPane.showMessageDialog(this, "Unable to find the following nutrition data: " + Arrays.toString(unsetTypes.toArray()), "Calorie Counter", JOptionPane.WARNING_MESSAGE);
    }
    return facts;
  }

  private NutrientType parseNutrientType(XmlTag rowTag) {
    String classType = rowTag.getAttributeValue("class");
    if (classType == null) {
      return null;
    }
    switch (classType) {
      case "energy" : return NutrientType.CALORIES;
      case "fat-calories" : return NutrientType.FAT_CALORIES;
      case "total-fat" : return NutrientType.TOTAL_FAT;
      case "sat-fat" : return NutrientType.SATURATED_FAT;
      case "sugars" : return NutrientType.SUGARS;
      case "cholesterol" : return NutrientType.CHOLESTEROL;
      case "sodium" : return NutrientType.SODIUM;
      case "total-carbs" : return NutrientType.CARBOHYDRATES;
      case "protein" : return NutrientType.PROTEIN;
      default : return null;
    }
  }

  private PositiveDecimalNumber parseAmount(XmlTag rowTag, NutrientType type) throws XmlException {
    for (XmlTag subElement : rowTag.getSubTags("td")) {
      String classType = subElement.getAttributeValue("class");
      if (type == NutrientType.CALORIES || type == NutrientType.FAT_CALORIES) {
        XmlTag subSubElement = subElement.getSubTag("span");
        return new PositiveDecimalNumber(subSubElement.getContent().replaceAll("[^0-9]", ""));
      }
      else if ("amount".equals(classType)) {
        String amountStr = subElement.getContent().trim();
        PositiveDecimalNumber amount = new PositiveDecimalNumber(amountStr.replaceAll("[^0-9\\.]", ""));
        if (type.matchesUnitAbbreviation(amountStr.replaceAll("^.*[0-9\\.]+", "").trim())) {
          return amount;
        }
        else {
          throw new IllegalStateException("Unable to read " + type.name() + " data: expected " + type.getDisplayMeasure(amount) + ", found " + amountStr);
        }
      }
    }
    throw new IllegalStateException("Unable to retrieve amount for " + type.name());
  }

  private NutritionFacts getNutritionFactsFromNutritionixPage(BufferedReader reader) throws IOException, XmlException {
    NutritionFactsImpl facts = new NutritionFactsImpl();
    List<NutrientType> unsetTypes = new ArrayList<>(Arrays.asList(NutrientType.values()));

    boolean nutritionDataFound = false;
    String inputLine;
    while ((inputLine = reader.readLine()) != null) {
      if (!nutritionDataFound && inputLine.trim().startsWith("//") && inputLine.trim().contains("values for the nutrition info")) {
        nutritionDataFound = true;
      }
      else if (nutritionDataFound) {
        if (!inputLine.contains(":")) {
          break;
        }
        String[] namedAmount = inputLine.split(":");
        NutrientType type = parseNutrientType(namedAmount[0]);
        if (type != null) {
          PositiveDecimalNumber amount = new PositiveDecimalNumber(namedAmount[1].replaceAll("[\\s,]", ""));
          facts.setAmount(type, amount);
          unsetTypes.remove(type);
        }
      }
    }

    if (unsetTypes.size() > 1) {
      JOptionPane.showMessageDialog(this, "Unable to find the following nutrition data: " + Arrays.toString(unsetTypes.toArray()), "Calorie Counter", JOptionPane.WARNING_MESSAGE);
    }
    return facts;
  }

  private NutrientType parseNutrientType(String name) {
    switch (name.replaceAll("[\\s']", "")) {
      case "valueCalories" : return NutrientType.CALORIES;
      case "valueFatCalories" : return NutrientType.FAT_CALORIES;
      case "valueTotalFat" : return NutrientType.TOTAL_FAT;
      case "valueSatFat" : return NutrientType.SATURATED_FAT;
      case "valueSugars" : return NutrientType.SUGARS;
      case "valueCholesterol" : return NutrientType.CHOLESTEROL;
      case "valueSodium" : return NutrientType.SODIUM;
      case "valueTotalCarb" : return NutrientType.CARBOHYDRATES;
      case "valueProteins" : return NutrientType.PROTEIN;
      default : return null;
    }
  }

	private void copyFoodstuff() {
		finishTableEditing();

    Foodstuff foodToCopy = null;
    for (int row = 0; row < foodstuffTableModel.getRowCount(); row++) {
      if (!(Boolean) foodstuffTable.getValueAt(row, SELECT_COLUMN))
        continue;

      if (foodToCopy != null) {
        String msg = "You can only copy one row at a time.  Uncheck all checkboxes but one before copying.";
        JOptionPane.showMessageDialog(this, msg, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
        return;
      }

      foodToCopy = foodStore.getFoodstuff((String) foodstuffTable.getValueAt(row, NAME_COLUMN));
    }
    if (foodToCopy == null) {
      return;
    }

    String newFoodName = getNewFoodName(foodToCopy.getName());
    if (newFoodName == null)
      return;

    actionHandler.copyFood(foodToCopy, newFoodName);
    foodstuffTable.changeSelection(foodstuffTable.getSelectedRow(), NAME_COLUMN, false, false);
	}
	
	private void deleteFoodstuffs() {
		finishTableEditing();

		List<String> namesOfFoodstuffsToDelete = new ArrayList<>();
		for (int row = 0; row < foodstuffTableModel.getRowCount(); row++) {
			if ((Boolean) foodstuffTable.getValueAt(row, SELECT_COLUMN))
				namesOfFoodstuffsToDelete.add((String) foodstuffTable.getValueAt(row, NAME_COLUMN));
		}
		if (namesOfFoodstuffsToDelete.isEmpty())
			return;

		String deleteMsg = "The " + namesOfFoodstuffsToDelete.size() + " selected row(s) will be deleted.";
		deleteMsg += "\nAre you sure you want to do this?";
		int option = JOptionPane.showConfirmDialog(this, deleteMsg, "Foodstuff removal", JOptionPane.YES_NO_CANCEL_OPTION);
		if (option != JOptionPane.YES_OPTION)
			return;

    List<String> namesOfUsedFoods = new ArrayList<>();
		for (String name : namesOfFoodstuffsToDelete) {
      if (!actionHandler.removeFood(foodStore.getFoodstuff(name)))
        namesOfUsedFoods.add(name);
    }

    if (!namesOfUsedFoods.isEmpty()) {
      String message = "<html>Unable to delete the following foods because they are used in meals or selected Daily Records:";
      for (String name : namesOfUsedFoods)
        message += " <br><b> " + name + "</b>";
      message += "</html>";
      JOptionPane.showMessageDialog(this, message, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
    }
	}
	
	private void renameCategory() {
		finishTableEditing();
		
    FoodstuffCategory[] categories = FoodstuffCategory.values().toArray(new FoodstuffCategory[FoodstuffCategory.values().size()]);
    FoodstuffCategory oldCategory = (FoodstuffCategory) JOptionPane.showInputDialog(this, "Change which Category?",
				"Rename Category", JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]);
		if (oldCategory == null)
			return;

		String newName = JOptionPane.showInputDialog(this, "New Category Name:", "Rename Category", JOptionPane.QUESTION_MESSAGE);
		if (newName == null || newName.trim().isEmpty())
			return;

    actionHandler.changeCategoryName(oldCategory, newName);
    refreshCategoryComboBoxes();
	}

  private void updateName(int row) {
    Foodstuff food = foodStore.getFoodstuff(selectedFoodNameBeforeEdit);
    String newFoodName = (String) foodstuffTable.getValueAt(row, NAME_COLUMN);
    if (!newFoodName.isEmpty() && Character.isLowerCase(newFoodName.charAt(0))) {
      String remainingFoodName = newFoodName.length() > 1 ? newFoodName.substring(1) : "";
      foodstuffTable.setValueAt(Character.toUpperCase(newFoodName.charAt(0)) + remainingFoodName, row, NAME_COLUMN);
      return;
    }

    if (food.getName().equals(newFoodName))
      return;

    if (newFoodName.trim().equals("")) {
      foodstuffTable.setValueAt(food.getName(), row, NAME_COLUMN);
      return;
    }

    if (foodStore.containsFood(newFoodName)) {
      JOptionPane.showMessageDialog(this, "A food with this name already exists.", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
      foodstuffTable.setValueAt(food.getName(), row, NAME_COLUMN);
      foodstuffTable.grabFocus();
      return;
    }

    actionHandler.changeFoodName(food, newFoodName);
  }
	
	private void updateCategory(int row) {
		Foodstuff food = foodStore.getFoodstuff((String) foodstuffTable.getValueAt(row, NAME_COLUMN));
		FoodstuffCategory oldCategory = food.getCategory();
		String newCategoryName = (String) foodstuffTable.getValueAt(row, CATEGORY_COLUMN);

    if (newCategoryName.equals(oldCategory.toString())) {
      return;
    }

    if (newCategoryName.equals(NEW_MARKER)) {
      newCategoryName = JOptionPane.showInputDialog(this, "New Category Name:", "New Category", JOptionPane.QUESTION_MESSAGE);
      if (newCategoryName == null) {
        foodstuffTable.setValueAt(food.getCategory().toString(), row, CATEGORY_COLUMN);
        return;
      }
    }

    int preCategorySize = FoodstuffCategory.values().size();
    FoodstuffCategory newCategory = FoodstuffCategory.findOrCreate(newCategoryName);
    int postCategorySize = FoodstuffCategory.values().size();

    if (oldCategory == newCategory) {
      foodstuffTable.setValueAt(food.getCategory().toString(), row, CATEGORY_COLUMN);
      return;
    }

		actionHandler.setFoodstuffCategory(food, newCategory);

    if (postCategorySize > preCategorySize)
      refreshCategoryComboBoxes();
	}
	
	private void updateAmount(int row) {
    Foodstuff food = foodStore.getFoodstuff((String) foodstuffTable.getValueAt(row, NAME_COLUMN));
		String amountValue = (String) foodstuffTable.getValueAt(row, AMOUNT_COLUMN);

    if (food.getAmount().toString().equals(amountValue)) {
      return;
    }

    try {
      if (!actionHandler.setFoodAmount(food, new PositiveDecimalNumber(amountValue)))
        foodstuffTable.setValueAt(food.getAmount().toString(), row, AMOUNT_COLUMN);
    }
    catch (IllegalArgumentException e) {
      // just set value back to what it was
      foodstuffTable.setValueAt(food.getAmount().toString(), row, AMOUNT_COLUMN);
    }

	}

	private void updateUnit(int row) {
    Foodstuff food = foodStore.getFoodstuff((String) foodstuffTable.getValueAt(row, NAME_COLUMN));
    MeasurementUnit oldUnit = food.getUnit();
    String newUnitName = (String) foodstuffTable.getValueAt(row, UNIT_COLUMN);

    if (newUnitName.equals(oldUnit.toString())) {
      return;
    }

    if (newUnitName.equals(NEW_MARKER)) {
      newUnitName = JOptionPane.showInputDialog(this, "New Unit Name:", "New Unit", JOptionPane.QUESTION_MESSAGE);
      if (newUnitName == null) {
        foodstuffTable.setValueAt(food.getUnit().toString(), row, UNIT_COLUMN);
        return;
      }
    }

    int preUnitSize = MeasurementUnit.values().size();
    MeasurementUnit newUnit = MeasurementUnit.findOrCreate(newUnitName);
    int postUnitSize = MeasurementUnit.values().size();

    if (oldUnit == newUnit) {
      foodstuffTable.setValueAt(food.getUnit().toString(), row, UNIT_COLUMN);
      return;
    }

    actionHandler.setFoodUnit(food, newUnit);

    if (postUnitSize > preUnitSize)
      refreshMeasurementUnitComboBoxes();
	}
	
	private void updateFact(int row, int column) {
    Foodstuff food = foodStore.getFoodstuff((String) foodstuffTable.getValueAt(row, NAME_COLUMN));
    NutrientType type = NutrientType.getByIndex(column - FACTS_START_COLUMN);
    String amountValue = (String) foodstuffTable.getValueAt(row, column);

    if (amountValue.equals(type.getDisplayMeasure(food.getFacts().getAmount(type)))) {
      return;
    }

    amountValue = amountValue.replaceFirst("m?g", "");
    try {
      if (!actionHandler.setFoodFactAmount(food, type, new PositiveDecimalNumber(amountValue)))
        foodstuffTable.setValueAt(type.getDisplayMeasure(food.getFacts().getAmount(type)), row, column);
    }
    catch (IllegalArgumentException e) {
      // just set value back to what it was
      foodstuffTable.setValueAt(type.getDisplayMeasure(food.getFacts().getAmount(type)), row, column);
    }
	}
	
	private void finishTableEditing() {
		if (foodstuffTable.isEditing())
      foodstuffTable.getCellEditor().stopCellEditing();
	}

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ChangeNotification) {
      ChangeNotification changeNotification = (ChangeNotification) arg;
      if (changeNotification.isFoodstuffChange()) {
        refreshFoodstuffTable();

        Food food = changeNotification.getFoodstuff();
        if (food != null) {
          int newRow = 0;
          for (int currentRow = 0; currentRow < foodstuffTable.getRowCount(); currentRow++) {
            if (foodstuffTable.getValueAt(currentRow, NAME_COLUMN).equals(food.getName())) {
              newRow = currentRow;
              break;
            }
          }
          int columnToSelect = foodstuffTable.getSelectedColumn();
          if (columnToSelect < 0)
            columnToSelect = NAME_COLUMN;
          foodstuffTable.grabFocus();
          foodstuffTable.changeSelection(newRow, columnToSelect, false, false);
        }
      }
    }
  }
}


