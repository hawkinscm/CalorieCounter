package seahawk.caloriecounter.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.gui.common.CommonUtil;

public final class MealsPanel extends JPanel implements Observer {
  private static final String NEW_MARKER = "-New-";

  private static final int SELECT_COLUMN    = 0;
  private static final int NAME_COLUMN      = 1;
  private static final int AMOUNT_COLUMN    = 2;
  private static final int UNIT_COLUMN      = 3;
	
  private CalorieCounterActionHandler actionHandler;
  private FoodStore foodStore;

  private DefaultTableModel mealTableModel;
  private JTable mealTable;
  private String selectedMealNameBeforeEdit;
  private FoodSortType mealSortType;
  private boolean reverseSort;

	public MealsPanel(CalorieCounterActionHandler actionHandler, FoodStore foodStore, FoodSortType mealSortType, boolean reverseSort) {
    this.actionHandler = actionHandler;
    this.foodStore = foodStore;
    this.mealSortType = mealSortType;
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
    add(createMealTablePane(), c);
	}

  public FoodSortType getMealSortType() {
    return mealSortType;
  }

  public boolean isReverseSort() {
    return reverseSort;
  }

  private JPanel createAddRemovePanel() {
    JPanel addRemovePanel = new JPanel(new FlowLayout());

    JButton addButton = new JButton("Add");
    addButton.setMnemonic(KeyEvent.VK_A);
    addButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        addMeal();
      }
    });
    addRemovePanel.add(addButton);

    JButton copyButton = new JButton("Copy");
    copyButton.setMnemonic(KeyEvent.VK_C);
    copyButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        copyMeal();
      }
    });
    addRemovePanel.add(copyButton);

    JButton deleteButton = new JButton("Delete");
    deleteButton.setMnemonic(KeyEvent.VK_D);
    deleteButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        deleteMeals();
      }
    });
    addRemovePanel.add(deleteButton);

    return addRemovePanel;
  }
	
	private JScrollPane createMealTablePane() {
    DefaultTableColumnModel mealColumnModel = new DefaultTableColumnModel();
    mealColumnModel.addColumn(CommonUtil.createColumn(SELECT_COLUMN, null, 16, null));
    mealColumnModel.addColumn(CommonUtil.createColumn(NAME_COLUMN, "Name", 80, 370, null, null));
    mealColumnModel.addColumn(CommonUtil.createColumn(AMOUNT_COLUMN, "Amt", 35, CommonUtil.getRightAlignedCellRenderer()));
    mealColumnModel.addColumn(CommonUtil.createColumn(UNIT_COLUMN, "Unit", 60, 130, null, null));

    int numColumns = UNIT_COLUMN + 1;
    for (NutrientType type : NutrientType.values())
      mealColumnModel.addColumn(CommonUtil.createColumn(numColumns++, type.getDisplayName(),
         CommonUtil.getNutrientTypeColumnSize(type), CommonUtil.getGrayedOutCellRenderer(JLabel.RIGHT)));

    mealTableModel =  new DefaultTableModel(0, numColumns) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return column == SELECT_COLUMN || column == NAME_COLUMN || column == AMOUNT_COLUMN || column == UNIT_COLUMN;
      }

      @Override
      public void fireTableCellUpdated(int row, int column) {
        if (column == NAME_COLUMN)
          updateName(row);
        else if (column == AMOUNT_COLUMN)
          updateAmount(row);
        else if (column == UNIT_COLUMN)
          updateUnit(row);
      }
    };

    mealTable = new JTable(mealTableModel, mealColumnModel) {
      @Override
      public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        super.changeSelection(row, column, toggle, extend);

        if (column == UNIT_COLUMN)
          CommonUtil.selectComboBox(this, row, column);
      }

      @Override
      public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);

        if (c instanceof JTextComponent) {
          ((JTextField) c).selectAll();
          selectedMealNameBeforeEdit = ((JTextField) c).getText();
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
    mealTable.setRowSelectionAllowed(false);
    mealTable.setSurrendersFocusOnKeystroke(true);
    mealTable.getTableHeader().setReorderingAllowed(false);
    CommonUtil.correctSortedFoodTableKeyActions(mealTable, 1, UNIT_COLUMN);

    mealTable.getTableHeader().addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          int column = mealTable.columnAtPoint(e.getPoint());
          if (column == NAME_COLUMN)
            setMealSortType(FoodSortType.NAME);
          else if (column == AMOUNT_COLUMN)
            setMealSortType(FoodSortType.AMOUNT);
          else if (column == UNIT_COLUMN)
            setMealSortType(FoodSortType.UNIT);
          else
            setMealSortType(FoodSortType.getBy(NutrientType.getByIndex(column - (UNIT_COLUMN + 1))));

          refreshMealTable();
        }
      }
      public void mousePressed(MouseEvent e) {}
      public void mouseReleased(MouseEvent e) {}
      public void mouseEntered(MouseEvent e) {}
      public void mouseExited(MouseEvent e) {}
    });

    refreshMealTable();
    refreshMeasurementUnitComboBoxes();

    mealTable.setPreferredScrollableViewportSize(mealTable.getPreferredSize());
    return new JScrollPane(mealTable,
       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

  private void setMealSortType(FoodSortType newSortType) {
    //noinspection SimplifiableConditionalExpression
    reverseSort = (mealSortType == newSortType) ? !reverseSort : false;
    mealSortType = newSortType;
  }

  private void refreshMealTable() {
    mealTableModel.setRowCount(0);
    for (Meal meal : foodStore.getMeals(mealSortType, reverseSort))
      addMealRow(meal);
  }

  private void addMealRow(Meal meal) {
    Object[] foodRow = new Object[mealTableModel.getColumnCount()];
    foodRow[SELECT_COLUMN] = false;
    foodRow[NAME_COLUMN] = meal.getName();
    foodRow[AMOUNT_COLUMN] = meal.getAmount().toString();
    foodRow[UNIT_COLUMN] = meal.getUnit().toString();

    NutritionFacts facts = meal.getFacts();
    int idx = UNIT_COLUMN + 1;
    for (NutrientType type : NutrientType.values())
      foodRow[idx++] = type.getDisplayMeasure(facts.getAmount(type));

    mealTableModel.addRow(foodRow);
  }

  private void refreshMeasurementUnitComboBoxes() {
    TableColumn column = mealTable.getColumnModel().getColumn(UNIT_COLUMN);
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
		
	private void addMeal() {
		finishTableEditing();

    String mealName = getNewMealName("");
    if (mealName == null)
      return;

    actionHandler.addMeal(mealName);
    mealTable.changeSelection(mealTable.getSelectedRow(), NAME_COLUMN, false, false);
	}

  private String getNewMealName(String defaultName) {
    String chosenMealName = (String) JOptionPane.showInputDialog(this, "Meal Name:", "New Meal",
       JOptionPane.QUESTION_MESSAGE, null, null, defaultName);

    if (chosenMealName == null)
      return null;

    if (chosenMealName.trim().isEmpty()) {
      return getNewMealName("");
    }

    if (Character.isLowerCase(chosenMealName.charAt(0))) {
      String remainingFoodName = chosenMealName.length() > 1 ? chosenMealName.substring(1) : "";
      chosenMealName = Character.toUpperCase(chosenMealName.charAt(0)) + remainingFoodName;
    }

    if (foodStore.containsFood(chosenMealName)) {
      JOptionPane.showMessageDialog(this, "A food with this name already exists.", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
      return getNewMealName(chosenMealName);
    }
    return chosenMealName;
  }

  private void copyMeal() {
    finishTableEditing();

    Meal mealToCopy = null;
    for (int row = 0; row < mealTableModel.getRowCount(); row++) {
      if (!(Boolean) mealTable.getValueAt(row, SELECT_COLUMN))
        continue;

      if (mealToCopy != null) {
        String msg = "You can only copy one row at a time.  Uncheck all checkboxes but one before copying.";
        JOptionPane.showMessageDialog(this, msg, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
        return;
      }

      mealToCopy = foodStore.getMeal((String) mealTable.getValueAt(row, NAME_COLUMN));
    }
    if (mealToCopy == null) {
      return;
    }

    String newMealName = getNewMealName(mealToCopy.getName());
    if (newMealName == null)
      return;

    actionHandler.copyFood(mealToCopy, newMealName);
    mealTable.changeSelection(mealTable.getSelectedRow(), NAME_COLUMN, false, false);
  }

	private void deleteMeals() {
    finishTableEditing();

    List<String> namesOfMealsToDelete = new ArrayList<>();
    for (int row = 0; row < mealTableModel.getRowCount(); row++) {
      if ((Boolean) mealTable.getValueAt(row, SELECT_COLUMN))
        namesOfMealsToDelete.add((String) mealTable.getValueAt(row, NAME_COLUMN));
    }
    if (namesOfMealsToDelete.isEmpty())
      return;

    String deleteMsg = "The " + namesOfMealsToDelete.size() + " selected row(s) will be deleted.";
    deleteMsg += "\nAre you sure you want to do this?";
    int option = JOptionPane.showConfirmDialog(this, deleteMsg, "Meal removal", JOptionPane.YES_NO_CANCEL_OPTION);
    if (option != JOptionPane.YES_OPTION)
      return;

    List<String> namesOfUsedMeals = new ArrayList<>();
    for (String name : namesOfMealsToDelete) {
      if (!actionHandler.removeFood(foodStore.getMeal(name)))
        namesOfUsedMeals.add(name);
    }

    if (!namesOfUsedMeals.isEmpty()) {
      String message = "<html>Unable to delete the following meals because they are used in other meals or in selected Daily Records:";
      for (String name : namesOfUsedMeals)
        message += " <br><b> " + name + "</b>";
      message += "</html>";
      JOptionPane.showMessageDialog(this, message, "Calorie Counter", JOptionPane.ERROR_MESSAGE);
    }
	}
	
	private void updateName(int row) {
    Meal meal = foodStore.getMeal(selectedMealNameBeforeEdit);
    String newMealName = (String) mealTable.getValueAt(row, NAME_COLUMN);
    if (!newMealName.isEmpty() && Character.isLowerCase(newMealName.charAt(0))) {
      String remainingFoodName = newMealName.length() > 1 ? newMealName.substring(1) : "";
      mealTable.setValueAt(Character.toUpperCase(newMealName.charAt(0)) + remainingFoodName, row, NAME_COLUMN);
      return;
    }

    if (meal.getName().equals(newMealName))
      return;

    if (newMealName.trim().equals("")) {
      mealTable.setValueAt(meal.getName(), row, NAME_COLUMN);
      return;
    }

    if (foodStore.containsFood(newMealName)) {
      JOptionPane.showMessageDialog(this, "A food with this name already exists.", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
      mealTable.setValueAt(meal.getName(), row, NAME_COLUMN);
      return;
    }

    actionHandler.changeFoodName(meal, newMealName);
	}
	
	private void updateAmount(int row) {
    Meal meal = foodStore.getMeal((String) mealTable.getValueAt(row, NAME_COLUMN));
    String amountValue = (String) mealTable.getValueAt(row, AMOUNT_COLUMN);

    if (meal.getAmount().toString().equals(amountValue))
      return;

    try {
      if (!actionHandler.setFoodAmount(meal, new PositiveDecimalNumber(amountValue)))
        mealTable.setValueAt(meal.getAmount().toString(), row, AMOUNT_COLUMN);
    }
    catch (IllegalArgumentException e) {
      // just set meal amount back to what it was
      mealTable.setValueAt(meal.getAmount().toString(), row, AMOUNT_COLUMN);
    }
	}

	private void updateUnit(int row) {
    Meal meal = foodStore.getMeal((String) mealTable.getValueAt(row, NAME_COLUMN));
    MeasurementUnit oldUnit = meal.getUnit();
    String newUnitName = (String) mealTable.getValueAt(row, UNIT_COLUMN);

    if (newUnitName.equals(oldUnit.toString()))
      return;

    if (newUnitName.equals(NEW_MARKER)) {
      newUnitName = JOptionPane.showInputDialog(this, "New Unit Name:", "New Unit", JOptionPane.QUESTION_MESSAGE);
      if (newUnitName == null) {
        mealTable.setValueAt(meal.getUnit().toString(), row, UNIT_COLUMN);
        return;
      }
    }

    int preUnitSize = MeasurementUnit.values().size();
    MeasurementUnit newUnit = MeasurementUnit.findOrCreate(newUnitName);
    int postUnitSize = MeasurementUnit.values().size();

    if (oldUnit == newUnit) {
      mealTable.setValueAt(meal.getUnit().toString(), row, UNIT_COLUMN);
      return;
    }

    actionHandler.setFoodUnit(meal, newUnit);

    if (postUnitSize > preUnitSize)
      refreshMeasurementUnitComboBoxes();
	}

  private void finishTableEditing() {
		if (mealTable.isEditing())
			mealTable.getCellEditor().stopCellEditing();
	}

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ChangeNotification) {
      ChangeNotification changeNotification = (ChangeNotification) arg;
      if (changeNotification.isFoodstuffChange()) {
        if (changeNotification.isEdit()) {
          refreshMealTable();
          refreshMeasurementUnitComboBoxes();
        }
      }
      else if (changeNotification.isMealChange()) {
        refreshMealTable();
        if (changeNotification.isEdit()) {
          refreshMeasurementUnitComboBoxes();
        }

        Meal meal = changeNotification.getMeal();
        if (meal != null) {
          int newRow = 0;
          for (int currentRow = 0; currentRow < mealTable.getRowCount(); currentRow++) {
            if (mealTable.getValueAt(currentRow, NAME_COLUMN).equals(meal.getName())) {
              newRow = currentRow;
              break;
            }
          }
          int columnToSelect = mealTable.getSelectedColumn();
          if (columnToSelect < 0)
            columnToSelect = NAME_COLUMN;
          mealTable.grabFocus();
          mealTable.changeSelection(newRow, columnToSelect, false, false);
        }
      }
    }
  }
}


