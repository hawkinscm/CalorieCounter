
package seahawk.caloriecounter.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.text.*;

import net.sourceforge.jdatepicker.impl.JDatePickerDate;
import net.sourceforge.jdatepicker.impl.JDatePickerImpl;
import seahawk.caloriecounter.domain.api.*;
import seahawk.caloriecounter.domain.api.date.Date;
import seahawk.caloriecounter.domain.api.log.SimpleLogger;
import seahawk.caloriecounter.gui.common.VolumeComboBoxCellEditor;
import seahawk.caloriecounter.gui.common.WeightComboBoxCellEditor;
import seahawk.caloriecounter.gui.common.autobox.AutoCompleteComboBox;
import seahawk.caloriecounter.gui.common.CommonUtil;
import seahawk.caloriecounter.gui.common.DataResizingTable;
import seahawk.xmlhandler.XmlException;

public final class DailyRecordPanel extends JPanel implements Observer {
	private static final int EF_REMOVE_COLUMN = 0;
	private static final int EF_INGREDIENT_COLUMN = 1;
	private static final int EF_AMOUNT_COLUMN = 2;
	private static final int EF_UNIT_COLUMN = 3;

  private CalorieCounterActionHandler actionHandler;
  private FoodStore foodStore;
  private DailyRecordStore dailyRecordStore;
	private DailyRecord dailyRecord;

  private JDatePickerImpl datePicker;
  private DataResizingTable totalDailyFactsTable;
	private DefaultTableModel dailyRecordEatenFoodTableModel;
	private JTable dailyRecordEatenFoodTable;
	private DefaultTableModel foodTableModel;
  private AutoCompleteComboBox foodSearchComboBox;
  private JComboBox<String> categoryViewComboBox;

	public DailyRecordPanel(CalorieCounterActionHandler actionHandler, FoodStore foodStore, DailyRecordStore dailyRecordStore) {
    this.actionHandler = actionHandler;
    this.foodStore = foodStore;
    this.dailyRecordStore = dailyRecordStore;
    try {
      this.dailyRecord = actionHandler.loadOrCreateDailyRecord(new Date(Calendar.getInstance()));
    }
    catch (XmlException ex) {
      SimpleLogger.error("Unable to load daily record (" + new Date(Calendar.getInstance()).hashCode() + "). ", ex);
      JOptionPane.showMessageDialog(this, "Corrupted Daily Record File!  Loading Empty.", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
      this.dailyRecord = actionHandler.addDailyRecord(new Date(Calendar.getInstance()));
    }

    actionHandler.addObserver(this);
			
		this.setLayout(new GridBagLayout());

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(20, 20, 20, 20);
    c.anchor = GridBagConstraints.WEST;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.gridx = 0;
    c.gridy = 0;
		add(createDaySummaryPanel(), c);

    c.insets.bottom = 17;
    c.gridx = 1;
    c.weightx = 1;
    JLabel spacingLabel = new JLabel(" ");
    add(spacingLabel, c);

    c.insets.top = 0;
    c.gridx = 0;
    c.gridwidth = 2;
    c.gridy++;
    c.weighty = 1;
    c.fill = GridBagConstraints.BOTH;
    add(createEatenFoodTablePane(), c);

    c.gridy++;
		add(createFoodTablePanel(), c);
	}
	
	private JPanel createDaySummaryPanel() {
    JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 20);
    c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridx = 0;
		c.gridy = 0;
    c.ipadx = 240;

    final List<JDatePickerDate> dailyRecordDates = new ArrayList<>();
    for (Date dailyRecordDate : dailyRecordStore.getDates()) {
      dailyRecordDates.add(convertToJPickerDate(dailyRecordDate));
    }

		datePicker = new JDatePickerImpl(convertToJPickerDate(dailyRecord.getDate()), dailyRecordDates);
    datePicker.getDateDisplayTextField().setForeground(Color.BLUE);
    datePicker.getDateDisplayTextField().setBackground(Color.LIGHT_GRAY);
    datePicker.getDateDisplayTextField().setBorder(null);
    datePicker.getDateDisplayTextField().setHorizontalAlignment(SwingConstants.CENTER);
    datePicker.getModel().setDate(dailyRecord.getDate().getYear(), dailyRecord.getDate().getMonth() - 1, dailyRecord.getDate().getDay());
    datePicker.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int year = datePicker.getModel().getYear();
        int month = datePicker.getModel().getMonth() + 1;
        int day = datePicker.getModel().getDay();
        Date date = new Date(String.valueOf((year * 10000) + (month * 100) + day));
        try {
          setDailyRecord(actionHandler.loadOrCreateDailyRecord(date));
        }
        catch (XmlException ex) {
          SimpleLogger.error("Unable to load daily record (" + date.hashCode() + "). ", ex);
          JOptionPane.showMessageDialog(DailyRecordPanel.this, "Corrupted Daily Record File!  Loading Empty.", "Calorie Counter", JOptionPane.ERROR_MESSAGE);
          setDailyRecord(actionHandler.addDailyRecord(date));
        }

        dailyRecordDates.clear();
        for (Date dailyRecordDate : dailyRecordStore.getDates()) {
          dailyRecordDates.add(convertToJPickerDate(dailyRecordDate));
        }
      }
    });
		panel.add(datePicker, c);

    c.insets.top = 0;
    c.insets.right = 0;
    c.ipady = 28;
    c.ipadx = 500;
    c.gridx++;
    c.weightx = 1;
    DefaultTableColumnModel totalDailyFactsColumnModel = new DefaultTableColumnModel();
    String[] totalDailyFactValues = new String[NutrientType.values().length];
    for (NutrientType nutrientType : NutrientType.values()) {
      TableColumn column = new TableColumn(nutrientType.ordinal());
      column.setHeaderValue(nutrientType.getDisplayName());
      DefaultTableCellRenderer cellRenderer = CommonUtil.getGrayedOutCellRenderer(JLabel.RIGHT);
      column.setCellRenderer(cellRenderer);
      totalDailyFactsColumnModel.addColumn(column);
    }

    DefaultTableModel totalDailyFactsTableModel = new DefaultTableModel(0, totalDailyFactsColumnModel.getColumnCount()) {
      @Override
      public boolean isCellEditable(int row, int col) {
        return false;
      }
    };
    totalDailyFactsTableModel.addRow(totalDailyFactValues);

    totalDailyFactsTable = new DataResizingTable(totalDailyFactsTableModel, totalDailyFactsColumnModel);
    totalDailyFactsTable.setColumnSelectionAllowed(false);
    totalDailyFactsTable.setCellSelectionEnabled(false);
    totalDailyFactsTable.setFocusable(false);
    refreshDailyRecordFacts();

    JScrollPane pane = totalDailyFactsTable.surroundWithScrollPane(false);
    pane.setBorder(BorderFactory.createEmptyBorder());
    panel.add(pane, c);

    panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 40));
    return panel;
	}

  private void refreshDailyRecordFacts() {
    NutritionFacts facts = dailyRecord.getFacts();
    for (NutrientType nutrientType : NutrientType.values())
      totalDailyFactsTable.getModel().setValueAt(nutrientType.getDisplayMeasure(facts.getAmount(nutrientType)), 0, nutrientType.ordinal());
    totalDailyFactsTable.refresh();
  }

  private JDatePickerDate convertToJPickerDate(Date date) {
    return new JDatePickerDate(date.getYear(), date.getMonth() - 1, date.getDay());
  }

  public void setDailyRecord(DailyRecord dailyRecord) {
    this.dailyRecord = dailyRecord;
    refreshDailyRecordEatenFoodTable();
  }

	private JScrollPane createEatenFoodTablePane() {
    DefaultTableColumnModel ingredientColumnModel = new DefaultTableColumnModel();
    ingredientColumnModel.addColumn(CommonUtil.createColumn(EF_REMOVE_COLUMN, "Remove", 60, CommonUtil.getHyperLinkCellRenderer()));
    ingredientColumnModel.addColumn(CommonUtil.createColumn(EF_INGREDIENT_COLUMN, "Name", 80, 370, null, CommonUtil.getGrayedOutCellRenderer(JLabel.LEFT)));
    ingredientColumnModel.addColumn(CommonUtil.createColumn(EF_AMOUNT_COLUMN, "Amt", 35, CommonUtil.getRightAlignedCellRenderer()));
    ingredientColumnModel.addColumn(CommonUtil.createColumn(EF_UNIT_COLUMN, "Unit", 60, 130, null, null));

    int numColumns = 4;
		for (NutrientType type : NutrientType.values()) {
      ingredientColumnModel.addColumn(CommonUtil.createColumn(numColumns++, type.getDisplayName(),
         CommonUtil.getNutrientTypeColumnSize(type), CommonUtil.getGrayedOutCellRenderer(JLabel.RIGHT)));
    }

		dailyRecordEatenFoodTableModel = new DefaultTableModel(0, numColumns) {
      @Override
			public boolean isCellEditable(int row, int column) {
				if (column == EF_AMOUNT_COLUMN)
					return true;
				else if (column == EF_UNIT_COLUMN) {
          MeasurementUnit unit = (MeasurementUnit) getValueAt(row, column);
					return unit.getConvertibleUnits().size() > 1;
				}
				return false;
			}

      @Override
			public void fireTableCellUpdated(int row, int column) {
				if (column == EF_AMOUNT_COLUMN)
					updateEatenFoodAmount(row);
				else if (column == EF_UNIT_COLUMN)
					updateEatenFoodUnit(row);
			}
		};
		
		dailyRecordEatenFoodTable = new JTable(dailyRecordEatenFoodTableModel, ingredientColumnModel) {
      @Override
			public Component prepareEditor(TableCellEditor editor, int row, int column) {
				Component c = super.prepareEditor(editor, row, column);

				if (c instanceof JTextComponent)
          ((JTextField) c).selectAll();
				return c;
			}

      @Override
      public TableCellEditor getCellEditor(int row, int column) {
        if (column == EF_UNIT_COLUMN) {
          MeasurementUnit unit = (MeasurementUnit) getValueAt(row, column);
          if (unit.isWeightUnit())
            return new WeightComboBoxCellEditor();
          else if (unit.isVolumeUnit())
            return new VolumeComboBoxCellEditor();
        }
        return super.getCellEditor(row, column);
      }
		};
		dailyRecordEatenFoodTable.setRowSelectionAllowed(false);
		CommonUtil.addHyperLinkMotionListener(dailyRecordEatenFoodTable, EF_REMOVE_COLUMN);
				
		MouseListener hyperLinkListener = new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1) {
					Point mousePoint = e.getPoint();
					if (dailyRecordEatenFoodTable.columnAtPoint(mousePoint) == EF_REMOVE_COLUMN)
						removeEatenFood(dailyRecordEatenFoodTable.rowAtPoint(mousePoint));
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
		dailyRecordEatenFoodTable.addMouseListener(hyperLinkListener);
    refreshDailyRecordEatenFoodTable();

    dailyRecordEatenFoodTable.setPreferredScrollableViewportSize(dailyRecordEatenFoodTable.getPreferredSize());
		return new JScrollPane(dailyRecordEatenFoodTable,
       ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	}

  private void refreshDailyRecordEatenFoodTable() {
    dailyRecordEatenFoodTableModel.setRowCount(0);
    for (Ingredient ingredient : dailyRecord.getEatenFoods()) {
      addDailyRecordEatenFoodRow(ingredient);
    }
    refreshDailyRecordFacts();
  }

  private void addDailyRecordEatenFoodRow(Ingredient eatenFood) {
    Object[] ingredientRow = new Object[dailyRecordEatenFoodTableModel.getColumnCount()];
    ingredientRow[EF_REMOVE_COLUMN] = "Remove";
    ingredientRow[EF_INGREDIENT_COLUMN] = eatenFood;
    ingredientRow[EF_AMOUNT_COLUMN] = eatenFood.getAmount().toString();
    ingredientRow[EF_UNIT_COLUMN] = eatenFood.getUnit();

    NutritionFacts facts = eatenFood.getFacts();
    int idx = 4;
    for (NutrientType type : NutrientType.values())
      ingredientRow[idx++] = type.getDisplayMeasure(facts.getAmount(type));

    dailyRecordEatenFoodTableModel.addRow(ingredientRow);
  }

  private void updateEatenFoodAmount(int row) {
    Ingredient ingredient = (Ingredient) dailyRecordEatenFoodTableModel.getValueAt(row, EF_INGREDIENT_COLUMN);
    String amountValue = (String) dailyRecordEatenFoodTable.getValueAt(row, EF_AMOUNT_COLUMN);

    if (ingredient.getAmount().toString().equals(amountValue))
      return;

    try {
      actionHandler.setDailyRecordEatenFoodAmount(dailyRecord, ingredient, new PositiveDecimalNumber(amountValue));
      refreshDailyRecordFacts();
    }
    catch (IllegalArgumentException e) {
      dailyRecordEatenFoodTable.setValueAt(ingredient.getAmount().toString(), row, EF_AMOUNT_COLUMN);
    }
  }

  private void updateEatenFoodUnit(int row) {
    Ingredient ingredient = (Ingredient) dailyRecordEatenFoodTableModel.getValueAt(row, EF_INGREDIENT_COLUMN);
    MeasurementUnit newUnit = (MeasurementUnit) dailyRecordEatenFoodTable.getValueAt(row, EF_UNIT_COLUMN);

    actionHandler.setDailyRecordEatenFoodUnit(dailyRecord, ingredient, newUnit);
    refreshDailyRecordFacts();
  }

  private void removeEatenFood(int row) {
    finishTableEditing();

    Ingredient ingredient = (Ingredient) dailyRecordEatenFoodTableModel.getValueAt(row, EF_INGREDIENT_COLUMN);
    dailyRecordEatenFoodTableModel.removeRow(row);
    actionHandler.removeDailyRecordEatenFood(dailyRecord, ingredient);

    refreshDailyRecordFacts();
  }

  private void finishTableEditing() {
    if (dailyRecordEatenFoodTable.isEditing())
      dailyRecordEatenFoodTable.getCellEditor().stopCellEditing();
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
          if (food != null) {
            actionHandler.addDailyRecordEatenFood(dailyRecord, food);
            refreshDailyRecordFacts();
            foodSearchComboBox.getModel().setSelectedItem(null);
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
          if (foodTable.columnAtPoint(mousePoint) == addColumn) {
            Food food = foodStore.getFood(foodTableModel.getValueAt(foodTable.rowAtPoint(mousePoint), nameColumn).toString());
            actionHandler.addDailyRecordEatenFood(dailyRecord, food);
            refreshDailyRecordFacts();
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
          refreshDailyRecordEatenFoodTable();
          refreshCategoryComboBox();
        }
      }
      else if (changeNotification.isMealChange()) {
        if (changeNotification.isAdd() || changeNotification.isRemove()) {
          refreshFoodSearchComboBox();
          refreshFoodTable((String) categoryViewComboBox.getSelectedItem());
        }
        else {
          refreshDailyRecordEatenFoodTable();
        }
      }
      else if (changeNotification.isDailyRecordChange()) {
        setDailyRecord(changeNotification.getDailyRecord());
      }
    }
  }
}