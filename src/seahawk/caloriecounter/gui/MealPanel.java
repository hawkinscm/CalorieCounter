
package seahawk.caloriecounter.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.gui.common.CommonUtil;
import seahawk.caloriecounter.gui.common.DataResizingTable;
import seahawk.caloriecounter.gui.common.VolumeComboBoxCellEditor;
import seahawk.caloriecounter.gui.common.WeightComboBoxCellEditor;
import seahawk.caloriecounter.gui.common.autobox.AutoCompleteComboBox;

public final class MealPanel extends JPanel implements Observer {
  private static final PositiveDecimalNumber ZERO = new PositiveDecimalNumber(0);
  private static final String NEW_MARKER = "-New-";

	private static final int I_REMOVE_COLUMN     = 0;
	private static final int I_INGREDIENT_COLUMN = 1;
	private static final int I_AMOUNT_COLUMN     = 2;
	private static final int I_UNIT_COLUMN       = 3;
	
  private CalorieCounterActionHandler actionHandler;
  private FoodStore foodStore;
	private Meal meal;

  private AutoCompleteComboBox mealSelectionComboBox;
  private boolean populatingMealSelectionComboBox = false;
  private JTextField amountTextField;
  private JComboBox<String> measurementUnitComboBox;
	private DataResizingTable totalMealFactsTable;

	private DefaultTableModel ingredientTableModel;
	private JTable ingredientTable;

  private DefaultTableModel foodTableModel;
  private AutoCompleteComboBox foodSearchComboBox;
  private JComboBox<String> categoryViewComboBox;

	public MealPanel(CalorieCounterActionHandler actionHandler, FoodStore foodStore) {
    this.actionHandler = actionHandler;
    this.foodStore = foodStore;
    this.meal = null;

    actionHandler.addObserver(this);

    this.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(20, 20, 20, 20);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
    add(createMealSummaryPanel(), c);

    c.gridx = 1;
    c.weightx = 1;
    JLabel spacingLabel = new JLabel(" ");
    add(spacingLabel, c);

    c.ipady = 0;
    c.insets.top = 0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy++;
    c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    add(createIngredientTablePane(), c);

    c.gridy++;
    add(createFoodTablePanel(), c);

    refreshMealSelectionComboBox();
	}

  private JPanel createMealSummaryPanel() {
    JPanel panel = new JPanel(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 0, 5, 5);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.NONE;
    c.gridx = 0;
    c.gridy = 0;
    c.gridwidth = 3;

    mealSelectionComboBox = new AutoCompleteComboBox(new HashSet<String>());
    Dimension foodSearchComboBoxSize = new Dimension(300, mealSelectionComboBox.getPreferredSize().height);
    mealSelectionComboBox.setMinimumSize(foodSearchComboBoxSize);
    mealSelectionComboBox.setPreferredSize(foodSearchComboBoxSize);
    mealSelectionComboBox.setMaximumSize(foodSearchComboBoxSize);
    mealSelectionComboBox.getEditor().getEditorComponent().addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
        mealSelectionComboBox.getEditor().selectAll();
      }
      public void focusLost(FocusEvent e) {}
    });
    mealSelectionComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          Meal selectedMeal = foodStore.getMeal((String) e.getItem());
          if (selectedMeal != null && selectedMeal.equals(meal))
            return;

          if (selectedMeal == null) {
            mealSelectionComboBox.setSelectedItem((meal == null) ? null : meal.getName());
          }
          else {
            meal = selectedMeal;
            amountTextField.setText(meal.getAmount().toString());
            measurementUnitComboBox.setSelectedItem(meal.getUnit().toString());
            refreshIngredientTable();
          }
        }
      }
    });
    panel.add(mealSelectionComboBox, c);

    c.insets.left = 0;
    c.insets.bottom = 0;
    c.gridwidth = 1;
    c.gridy++;
    panel.add(new JLabel("Amount:"), c);

    c.insets.left = 5;
    c.ipadx = 35;
    c.gridx++;
    amountTextField = new JTextField();
    amountTextField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateAmount();
      }
    });
    amountTextField.addFocusListener(new FocusListener() {
      public void focusGained(FocusEvent e) {
      }

      public void focusLost(FocusEvent e) {
        updateAmount();
      }
    });
    panel.add(amountTextField, c);

    c.ipadx = 0;
    c.gridx++;
    measurementUnitComboBox = new JComboBox<>();
    measurementUnitComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          if (!populatingMealSelectionComboBox) {
            updateUnit();
          }
        }
      }
    });
    panel.add(measurementUnitComboBox, c);
    refreshMeasurementUnitComboBox();

    c.gridheight = 2;
    c.insets.right = 0;
    c.ipady = 28;
    c.ipadx = 500;
    c.gridx++;
    c.gridy--;
    c.weightx = 1;
    DefaultTableColumnModel totalMealFactsColumnModel = new DefaultTableColumnModel();
    String[] totalMealFactValues = new String[NutrientType.values().length];
    for (NutrientType nutrientType : NutrientType.values()) {
      TableColumn column = new TableColumn(nutrientType.ordinal());
      column.setHeaderValue(nutrientType.getDisplayName());
      DefaultTableCellRenderer cellRenderer = CommonUtil.getGrayedOutCellRenderer(JLabel.RIGHT);
      column.setCellRenderer(cellRenderer);
      totalMealFactsColumnModel.addColumn(column);
    }

    DefaultTableModel totalMealFactsTableModel = new DefaultTableModel(0, totalMealFactsColumnModel.getColumnCount()) {
      @Override
      public boolean isCellEditable(int row, int col) {
        return false;
      }
    };
    totalMealFactsTableModel.addRow(totalMealFactValues);

    totalMealFactsTable = new DataResizingTable(totalMealFactsTableModel, totalMealFactsColumnModel);
    totalMealFactsTable.setColumnSelectionAllowed(false);
    totalMealFactsTable.setCellSelectionEnabled(false);
    totalMealFactsTable.setFocusable(false);
    refreshTotalMealFacts();

    JScrollPane pane = totalMealFactsTable.surroundWithScrollPane(false);
    pane.setBorder(BorderFactory.createEmptyBorder());
    panel.add(pane, c);

    panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 60));
    return panel;
  }

  private void refreshMealSelectionComboBox() {
    mealSelectionComboBox.setData(foodStore.getMealNames());
  }

  private void refreshMeasurementUnitComboBox() {
    populatingMealSelectionComboBox = true;
    measurementUnitComboBox.removeAllItems();
    measurementUnitComboBox.addItem(NEW_MARKER);
    int count = -(MeasurementUnit.getVolumeUnits().length + MeasurementUnit.getWeightUnits().length);
    for (MeasurementUnit unit : MeasurementUnit.values()) {
      if (count >= 0 && count % 7 == 0)
        measurementUnitComboBox.addItem(NEW_MARKER); // default view of combo box is 8 rows; this will make sure new is always visible in drop-down list (where appropriate)

      measurementUnitComboBox.addItem(unit.toString());
      count++;
    }
    measurementUnitComboBox.setSelectedItem((meal == null) ? null : meal.getUnit().toString());
    populatingMealSelectionComboBox = false;
  }

  private void refreshTotalMealFacts() {
    NutritionFacts facts = (meal == null) ? null : meal.getFacts();
    for (NutrientType nutrientType : NutrientType.values()) {
      PositiveDecimalNumber amount = (facts == null) ? ZERO : facts.getAmount(nutrientType);
      totalMealFactsTable.getModel().setValueAt(nutrientType.getDisplayMeasure(amount), 0, nutrientType.ordinal());
    }
    totalMealFactsTable.refresh();
  }

  private void updateAmount() {
    String amountValue = amountTextField.getText();

    if (meal == null || meal.getAmount().toString().equals(amountValue))
      return;

    try {
      actionHandler.setFoodAmount(meal, new PositiveDecimalNumber(amountValue));
    }
    catch (IllegalArgumentException e) {
      // ignore - just set meal amount back to what it was (done below)
    }

    amountTextField.setText(meal.getAmount().toString());
  }

  private void updateUnit() {
    if (meal == null)
      return;

    MeasurementUnit oldUnit = meal.getUnit();
    String newUnitName = (String) measurementUnitComboBox.getSelectedItem();

    if (newUnitName.equals(oldUnit.toString()))
      return;

    if (newUnitName.equals(NEW_MARKER)) {
      newUnitName = JOptionPane.showInputDialog(this, "New Unit Name:", "New Unit", JOptionPane.QUESTION_MESSAGE);
      if (newUnitName == null) {
        measurementUnitComboBox.setSelectedItem(oldUnit.toString());
        return;
      }
    }

    int preUnitSize = MeasurementUnit.values().size();
    MeasurementUnit newUnit = MeasurementUnit.findOrCreate(newUnitName);
    int postUnitSize = MeasurementUnit.values().size();

    if (oldUnit == newUnit) {
      measurementUnitComboBox.setSelectedItem(oldUnit.toString());
      return;
    }

    actionHandler.setFoodUnit(meal, newUnit);

    if (postUnitSize > preUnitSize)
      refreshMeasurementUnitComboBox();

    measurementUnitComboBox.setSelectedItem(newUnit.toString());
  }

  private JScrollPane createIngredientTablePane() {
    DefaultTableColumnModel ingredientColumnModel = new DefaultTableColumnModel();
    ingredientColumnModel.addColumn(CommonUtil.createColumn(I_REMOVE_COLUMN, "Remove", 60, CommonUtil.getHyperLinkCellRenderer()));
    ingredientColumnModel.addColumn(CommonUtil.createColumn(I_INGREDIENT_COLUMN, "Name", 80, 370, null, CommonUtil.getGrayedOutCellRenderer(JLabel.LEFT)));
    ingredientColumnModel.addColumn(CommonUtil.createColumn(I_AMOUNT_COLUMN, "Amt", 35, CommonUtil.getRightAlignedCellRenderer()));
    ingredientColumnModel.addColumn(CommonUtil.createColumn(I_UNIT_COLUMN, "Unit", 60, 130, null, null));

    int numColumns = 4;
    for (NutrientType type : NutrientType.values()) {
      ingredientColumnModel.addColumn(CommonUtil.createColumn(numColumns++, type.getDisplayName(),
         CommonUtil.getNutrientTypeColumnSize(type), CommonUtil.getGrayedOutCellRenderer(JLabel.RIGHT)));
    }

    ingredientTableModel = new DefaultTableModel(0, numColumns) {
      @Override
      public boolean isCellEditable(int row, int column) {
        if (column == I_AMOUNT_COLUMN)
          return true;
        else if (column == I_UNIT_COLUMN) {
          MeasurementUnit unit = (MeasurementUnit) getValueAt(row, column);
          return unit.getConvertibleUnits().size() > 1;
        }
        return false;
      }

      @Override
      public void fireTableCellUpdated(int row, int column) {
        if (column == I_AMOUNT_COLUMN)
          updateIngredientAmount(row);
        else if (column == I_UNIT_COLUMN)
          updateIngredientUnit(row);
      }
    };

    ingredientTable = new JTable(ingredientTableModel, ingredientColumnModel) {
      @Override
      public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);

        if (c instanceof JTextComponent)
          ((JTextField) c).selectAll();
        return c;
      }

      @Override
      public TableCellEditor getCellEditor(int row, int column) {
        if (column == I_UNIT_COLUMN) {
          MeasurementUnit unit = (MeasurementUnit) getValueAt(row, column);
          if (unit.isWeightUnit())
            return new WeightComboBoxCellEditor();
          else if (unit.isVolumeUnit())
            return new VolumeComboBoxCellEditor();
        }
        return super.getCellEditor(row, column);
      }
    };
    ingredientTable.setRowSelectionAllowed(false);
    CommonUtil.addHyperLinkMotionListener(ingredientTable, I_REMOVE_COLUMN);

    MouseListener hyperLinkListener = new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          Point mousePoint = e.getPoint();
          if (ingredientTable.columnAtPoint(mousePoint) == I_REMOVE_COLUMN)
            removeIngredient(ingredientTable.rowAtPoint(mousePoint));
        }
      }
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {
        if (getCursor().getType() != Cursor.DEFAULT_CURSOR)
          setCursor(null);
      }
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
    };
    ingredientTable.addMouseListener(hyperLinkListener);

    refreshIngredientTable();

    ingredientTable.setPreferredScrollableViewportSize(ingredientTable.getPreferredSize());
    return new JScrollPane(ingredientTable,
       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  private void refreshIngredientTable() {
    ingredientTableModel.setRowCount(0);
    if (meal != null) {
      for (Ingredient ingredient : meal.getIngredients()) {
        addIngredientRow(ingredient);
      }
      refreshTotalMealFacts();
    }
  }

  private void addIngredientRow(Ingredient ingredient) {
    Object[] ingredientRow = new Object[ingredientTableModel.getColumnCount()];
    ingredientRow[I_REMOVE_COLUMN] = "Remove";
    ingredientRow[I_INGREDIENT_COLUMN] = ingredient;
    ingredientRow[I_AMOUNT_COLUMN] = ingredient.getAmount().toString();
    ingredientRow[I_UNIT_COLUMN] = ingredient.getUnit();

    NutritionFacts facts = ingredient.getFacts();
    int idx = 4;
    for (NutrientType type : NutrientType.values())
      ingredientRow[idx++] = type.getDisplayMeasure(facts.getAmount(type));

    ingredientTableModel.addRow(ingredientRow);
  }

  private void updateIngredientAmount(int row) {
    Ingredient ingredient = (Ingredient) ingredientTableModel.getValueAt(row, I_INGREDIENT_COLUMN);
    String amountValue = (String) ingredientTableModel.getValueAt(row, I_AMOUNT_COLUMN);

    if (ingredient.getAmount().toString().equals(amountValue))
      return;

    try {
      actionHandler.setMealIngredientAmount(meal, ingredient, new PositiveDecimalNumber(amountValue));
      refreshTotalMealFacts();
      ingredientTable.setValueAt(ingredient.getAmount().toString(), row, I_AMOUNT_COLUMN);
      ingredientTable.changeSelection(row, I_UNIT_COLUMN, false, false);
    }
    catch (IllegalArgumentException e) {
      ingredientTable.setValueAt(ingredient.getAmount().toString(), row, I_AMOUNT_COLUMN);
    }
  }

  private void updateIngredientUnit(int row) {
    Ingredient ingredient = (Ingredient) ingredientTableModel.getValueAt(row, I_INGREDIENT_COLUMN);
    MeasurementUnit newUnit = (MeasurementUnit) ingredientTable.getValueAt(row, I_UNIT_COLUMN);

    actionHandler.setMealIngredientUnit(meal, ingredient, newUnit);
    refreshTotalMealFacts();
  }

  private void removeIngredient(int row) {
    finishTableEditing();

    Ingredient ingredient = (Ingredient) ingredientTableModel.getValueAt(row, I_INGREDIENT_COLUMN);
    ingredientTableModel.removeRow(row);
    actionHandler.removeMealIngredient(meal, ingredient);

    refreshTotalMealFacts();
  }

  private void finishTableEditing() {
    if (ingredientTable.isEditing())
      ingredientTable.getCellEditor().stopCellEditing();
  }

  private JPanel createFoodTablePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    JScrollPane foodTablePane = createFoodTablePane();

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(0, 0, 0, 0);
    c.anchor = GridBagConstraints.NORTHEAST;
    c.fill = GridBagConstraints.NONE;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = .1;
    c.ipadx = 80;

    categoryViewComboBox = new JComboBox<>();
    categoryViewComboBox.addItem("All");
    categoryViewComboBox.addItem("Meal");
    for (FoodstuffCategory category : FoodstuffCategory.values())
      categoryViewComboBox.addItem(category.toString());
    categoryViewComboBox.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Category View"));
    categoryViewComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED)
          refreshFoodTable(e.getItem().toString());
      }
    });
    categoryViewComboBox.setSelectedItem(null);
    categoryViewComboBox.setSelectedIndex(0);
    panel.add(categoryViewComboBox, c);

    foodSearchComboBox = new AutoCompleteComboBox(foodStore.getFoodNames());
    Dimension foodSearchComboBoxSize = new Dimension(340, foodSearchComboBox.getPreferredSize().height);
    foodSearchComboBox.setMinimumSize(foodSearchComboBoxSize);
    foodSearchComboBox.setPreferredSize(foodSearchComboBoxSize);
    foodSearchComboBox.setMaximumSize(foodSearchComboBoxSize);
    foodSearchComboBox.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          Food food = foodStore.getFood(e.getItem().toString());
          if (meal != null && food != null) {
            if (actionHandler.addIngredientToMeal(meal, food)) {
              refreshTotalMealFacts();
              foodSearchComboBox.getModel().setSelectedItem(null);
            }
            else {
              String message = "Unable to add meal: Adding the \"" + food.getName() + "\" meal to this meal would create a looping meal.";
              JOptionPane.showMessageDialog(MealPanel.this, message, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
    });

    c.ipadx = 0;
    c.anchor = GridBagConstraints.NORTHWEST;
    c.fill = GridBagConstraints.BOTH;
    c.insets.right = 20;
    c.gridy++;
    c.weighty = 1;
    JPanel foodSearchPanel = new JPanel();
    foodSearchPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK), "Food Name - Search & Add"));
    foodSearchPanel.setMinimumSize(new Dimension(355, 56));
    foodSearchPanel.setPreferredSize(new Dimension(355, 56));
    foodSearchPanel.setMaximumSize(new Dimension(355, 56));
    foodSearchPanel.add(foodSearchComboBox);
    panel.add(foodSearchPanel, c);

    c.weightx = 1;
    c.insets.top = 8;
    c.insets.right = 0;
    c.gridx++;
    c.gridy = 0;
    c.gridheight = 2;
    panel.add(foodTablePane, c);

    return panel;
  }

  private JScrollPane createFoodTablePane() {
    final int addColumn = 0;
    final int categoryColumn = 1;
    final int nameColumn = 2;

    DefaultTableColumnModel foodColumnModel = new DefaultTableColumnModel();
    foodColumnModel.addColumn(CommonUtil.createColumn(addColumn, "Add", 30, CommonUtil.getHyperLinkCellRenderer()));
    foodColumnModel.addColumn(CommonUtil.createColumn(categoryColumn, "Category", 80, 150, null, null));
    foodColumnModel.addColumn(CommonUtil.createColumn(nameColumn, "Name", 80, 370, null, null));

    foodTableModel = new DefaultTableModel(0, 3) {
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };

    final JTable foodTable = new JTable(foodTableModel, foodColumnModel);
    foodTable.setRowSelectionAllowed(false);
    CommonUtil.addHyperLinkMotionListener(foodTable, addColumn);

    MouseListener hyperLinkListener = new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          Point mousePoint = e.getPoint();
          if (foodTable.columnAtPoint(mousePoint) == addColumn && meal != null) {
            Food food = foodStore.getFood(foodTableModel.getValueAt(foodTable.rowAtPoint(mousePoint), nameColumn).toString());
            if (actionHandler.addIngredientToMeal(meal, food)) {
              refreshTotalMealFacts();
            }
            else {
              String message = "Unable to add meal: Adding the \"" + food.getName() + "\" meal to this meal would create a looping meal.";
              JOptionPane.showMessageDialog(MealPanel.this, message, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
      }
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {
        if (getCursor().getType() != Cursor.DEFAULT_CURSOR)
          setCursor(null);
      }
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
    };
    foodTable.addMouseListener(hyperLinkListener);

    foodTable.setPreferredScrollableViewportSize(foodTable.getPreferredSize());
    return new JScrollPane(foodTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  }

  private void refreshFoodSearchComboBox() {
    foodSearchComboBox.setData(foodStore.getFoodNames());
  }

  private void refreshCategoryComboBox() {
    int previousSize = categoryViewComboBox.getItemCount();
    String previousSelection = (String) categoryViewComboBox.getSelectedItem();
    int previousSelectionIndex = categoryViewComboBox.getSelectedIndex();

    categoryViewComboBox.removeAllItems();
    categoryViewComboBox.addItem("All");
    categoryViewComboBox.addItem("Meal");
    for (FoodstuffCategory category : FoodstuffCategory.values())
      categoryViewComboBox.addItem(category.toString());

    if (previousSize == categoryViewComboBox.getItemCount())
      categoryViewComboBox.setSelectedIndex(previousSelectionIndex);
    else
      categoryViewComboBox.setSelectedItem(previousSelection);
  }

  private void refreshFoodTable(String category) {
    foodTableModel.setRowCount(0);
    for (String foodName : foodStore.getFoodNames()) {
      Food food = foodStore.getFood(foodName);
      if (isFoodInCategory(category, food)) {
        String[] foodRow = new String[3];
        foodRow[0] = "Add";
        foodRow[1] = food.isMeal() ? "Meal" : ((Foodstuff) food).getCategory().toString();
        foodRow[2] = food.getName();
        foodTableModel.addRow(foodRow);
      }
    }
  }

  private boolean isFoodInCategory(String category, Food food) {
    switch (category) {
      case "All":
        return true;
      case "Meal":
        return food.isMeal();
      default:
        return (food instanceof Foodstuff) && category.equals(((Foodstuff) food).getCategory().toString());
    }
  }

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ChangeNotification) {
      ChangeNotification changeNotification = (ChangeNotification) arg;
      if (changeNotification.isFoodstuffChange()) {
        if (changeNotification.isAdd() || changeNotification.isRemove()) {
          refreshFoodSearchComboBox();
          refreshFoodTable((String) categoryViewComboBox.getSelectedItem());
        }
        else {
          refreshIngredientTable();
          refreshCategoryComboBox();
          refreshMeasurementUnitComboBox();
        }
      }
      else if (changeNotification.isMealChange()) {
        Meal changedMeal = changeNotification.getMeal();
        if (changeNotification.isRemove())
          changedMeal = (meal == changedMeal) ? null : meal;

        if (changeNotification.isAdd() || changeNotification.isRemove()) {
          refreshMealSelectionComboBox();
          refreshFoodSearchComboBox();
          refreshFoodTable((String) categoryViewComboBox.getSelectedItem());
        }
        else {
          refreshIngredientTable();
          refreshMeasurementUnitComboBox();
        }

        if (changedMeal != null) {
          mealSelectionComboBox.setSelectedItem(null);
          mealSelectionComboBox.setSelectedItem(changedMeal.getName());
        }
      }
    }
  }
}


