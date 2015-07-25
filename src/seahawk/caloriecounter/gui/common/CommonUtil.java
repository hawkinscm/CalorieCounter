package seahawk.caloriecounter.gui.common;

import seahawk.caloriecounter.domain.api.NutrientType;
import seahawk.caloriecounter.domain.api.log.SimpleLogger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator.Attribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public class CommonUtil {
  public static void selectComboBox(JTable table, int row, int column) {
    if (table.isEditing())
      return;

    Point startingPoint = MouseInfo.getPointerInfo().getLocation();

    int tablex = table.getLocationOnScreen().x;
    int tabley = table.getLocationOnScreen().y;

    int x = table.getCellRect(row, column, true).x + tablex;
    int y = table.getCellRect(row, column, true).y + tabley;

    try {
      Robot robot = new Robot();
      robot.mouseMove(x+1, y+1);
      robot.mousePress(InputEvent.BUTTON1_MASK);
      robot.mouseRelease(InputEvent.BUTTON1_MASK);
      robot.mouseMove(startingPoint.x, startingPoint.y);
    }
    catch (Exception e) {
      SimpleLogger.error(e);
    }
  }

  public static TableColumn createColumn(int index, String name, Integer fixedWidth, TableCellRenderer renderer) {
    TableColumn column = new TableColumn(index);
    if (name != null) column.setHeaderValue(name);
    if (fixedWidth != null) {
      column.setMinWidth(fixedWidth);
      column.setPreferredWidth(fixedWidth);
      column.setMaxWidth(fixedWidth);
    }
    if (renderer != null) column.setCellRenderer(renderer);
    return column;
  }

  public static TableColumn createColumn(int index, String name, Integer min, Integer pref, Integer max, TableCellRenderer renderer) {
    TableColumn column = new TableColumn(index);
    column.setHeaderValue(name);
    if (min != null) column.setMinWidth(min);
    if (pref != null) column.setPreferredWidth(pref);
    if (max != null) column.setMaxWidth(max);
    if (renderer != null) column.setCellRenderer(renderer);
    return column;
  }

	public static TableCellRenderer getHyperLinkCellRenderer() {
    return new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, column);

        Map<Attribute, Object> fontStyle;
        fontStyle = new HashMap<>();
        fontStyle.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        fontStyle.put(TextAttribute.FOREGROUND, Color.BLUE);
        c.setFont(Font.getFont(fontStyle));
        setHorizontalAlignment(CENTER);
        return c;
      }
    };
	}
	
	public static DefaultTableCellRenderer getGrayedOutCellRenderer(int textAlignment) {
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, selected, focused, row, column);
        c.setForeground(Color.GRAY);
        return c;
      }
    };
    renderer.setHorizontalAlignment(textAlignment);
    return renderer;
	}

  public static DefaultTableCellRenderer getRightAlignedCellRenderer() {
    DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
    renderer.setHorizontalAlignment(JLabel.RIGHT);
    return renderer;
  }
	
	public static void addHyperLinkMotionListener(final JTable table, final int hyperLinkColumn) {
		MouseMotionListener hyperLinkMotionListener;
		hyperLinkMotionListener = new MouseMotionListener() {
			public void mouseDragged(MouseEvent e) {}
		
			public void mouseMoved(MouseEvent e) {
				Point mouse = e.getPoint();
				int column = table.columnAtPoint(mouse);
			
				if (column == hyperLinkColumn) {
					int row = table.rowAtPoint(e.getPoint());
					Rectangle cell = table.getCellRect(row, column, true);
					int linkStartx = cell.x + 5;
					int linkEndx = cell.x + cell.width - 5;
					int linkStarty = cell.y + 3;
					int linkEndy = cell.y + cell.height - 5;
				
					if (mouse.x >= linkStartx && mouse.x <= linkEndx &&
					   mouse.y >= linkStarty && mouse.y <= linkEndy) {
						if (table.getCursor().getType() != Cursor.HAND_CURSOR)
							table.setCursor(new Cursor(Cursor.HAND_CURSOR));
						return;
					}
				}
			
				if (table.getCursor().getType() != Cursor.DEFAULT_CURSOR)
					table.setCursor(null);
			}
		};
		table.addMouseMotionListener(hyperLinkMotionListener);
	}

  public static void correctSortedFoodTableKeyActions(final JTable foodTable, final int minimumTabColumnIndex, final int maximumTabColumnIndex) {
    Action foodTableTab = new AbstractAction("FoodTableTab") {
      public void actionPerformed(ActionEvent evt) {
        if (foodTable.isEditing())
          foodTable.getCellEditor().stopCellEditing();

        int rowToSelect = foodTable.getSelectedRow();
        int columnToSelect = foodTable.getSelectedColumn() + 1;
        if (columnToSelect > maximumTabColumnIndex) {
          columnToSelect = minimumTabColumnIndex;
          rowToSelect++;
          if (rowToSelect >= foodTable.getRowCount())
            rowToSelect = 0;
        }
        foodTable.changeSelection(rowToSelect, columnToSelect, false, false);
      }
    };
    foodTable.getActionMap().put("FoodTableTab", foodTableTab);
    foodTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "FoodTableTab");

    Action foodTableBackTab = new AbstractAction("FoodTableBackTab") {
      public void actionPerformed(ActionEvent evt) {
        if (foodTable.isEditing())
          foodTable.getCellEditor().stopCellEditing();

        int rowToSelect = foodTable.getSelectedRow();
        int columnToSelect = foodTable.getSelectedColumn() - 1;
        if (columnToSelect < minimumTabColumnIndex) {
          columnToSelect = maximumTabColumnIndex;
          rowToSelect--;
          if (rowToSelect < 0)
            rowToSelect = foodTable.getRowCount() - 1;
        }
        foodTable.changeSelection(rowToSelect, columnToSelect, false, false);
      }
    };
    foodTable.getActionMap().put("FoodTableBackTab", foodTableBackTab);
    foodTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "FoodTableBackTab");

    Action foodTableUp = new AbstractAction("FoodTableUp") {
      public void actionPerformed(ActionEvent evt) {
        if (foodTable.isEditing())
          foodTable.getCellEditor().stopCellEditing();

        if (foodTable.getSelectedRow() > 0)
          foodTable.changeSelection(foodTable.getSelectedRow() - 1, foodTable.getSelectedColumn(), false, false);
      }
    };
    foodTable.getActionMap().put("FoodTableUp", foodTableUp);
    foodTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "FoodTableUp");

    Action foodTableDown = new AbstractAction("FoodTableDown") {
      public void actionPerformed(ActionEvent evt) {
        if (foodTable.isEditing())
          foodTable.getCellEditor().stopCellEditing();

        if (foodTable.getSelectedRow() < foodTable.getRowCount() - 1)
          foodTable.changeSelection(foodTable.getSelectedRow() + 1, foodTable.getSelectedColumn(), false, false);
      }
    };
    foodTable.getActionMap().put("FoodTableDown", foodTableDown);
    foodTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "FoodTableDown");

    Action foodTableLeft = new AbstractAction("FoodTableLeft") {
      public void actionPerformed(ActionEvent evt) {
        if (foodTable.isEditing())
          foodTable.getCellEditor().stopCellEditing();

        if (foodTable.getSelectedColumn() > 0)
          foodTable.changeSelection(foodTable.getSelectedRow(), foodTable.getSelectedColumn() - 1, false, false);
      }
    };
    foodTable.getActionMap().put("FoodTableLeft", foodTableLeft);
    foodTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "FoodTableLeft");

    Action foodTableRight = new AbstractAction("FoodTableRight") {
      public void actionPerformed(ActionEvent evt) {
        if (foodTable.isEditing())
          foodTable.getCellEditor().stopCellEditing();

        if (foodTable.getSelectedColumn() < foodTable.getColumnCount() - 1)
          foodTable.changeSelection(foodTable.getSelectedRow(), foodTable.getSelectedColumn() + 1, false, false);
      }
    };
    foodTable.getActionMap().put("FoodTableRight", foodTableRight);
    foodTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "FoodTableRight");
  }

  public static int getNutrientTypeColumnSize(NutrientType type) {
    switch (type) {
      case CALORIES : return 40;
      case PROTEIN : return 55;
      case FAT_CALORIES : return 45;
      case SATURATED_FAT : return 55;
      case CHOLESTEROL : return 70;
      case SODIUM : return 75;
      default : return 60;
    }
  }
}
