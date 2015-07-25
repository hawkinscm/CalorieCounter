
package seahawk.caloriecounter.gui;

import seahawk.caloriecounter.domain.api.*;

import java.awt.*;
import java.awt.event.*;
import java.util.Observable;
import java.util.Observer;
import javax.swing.*;

public final class MasterFrame extends JFrame implements Observer {
  private CalorieCounterActionHandler actionHandler;

  private DailyRecordHistoryChartDialog chartDialog;

  private JMenuItem undoMenuItem;
  private JMenuItem redoMenuItem;

  private FoodstuffsPanel foodstuffsPanel;
  private MealsPanel mealsPanel;

	public MasterFrame(CalorieCounterActionHandler actionHandler, FoodStore foodStore, DailyRecordStore dailyRecordStore, DisplayConfig config) {
		super("Calorie Counter");

    this.actionHandler = actionHandler;
    this.actionHandler.addObserver(this);

    if (config == null) {
      Dimension screenSize = getToolkit().getScreenSize();
      final int START_MENU_HEIGHT = 30;
      config = new DisplayConfig(screenSize.width, screenSize.height - START_MENU_HEIGHT, 0, 0,
         FoodSortType.FOODSTUFF_CATEGORY.getPersistenceValue(), false, FoodSortType.NAME.getPersistenceValue(), false);
    }

		setSize(config.getSizeWidth(), config.getSizeHeight());
		setLocation(config.getLocationX(), config.getLocationY());

		getContentPane().setLayout(new GridBagLayout());

		createMenus();
    chartDialog = new DailyRecordHistoryChartDialog(this, dailyRecordStore.getHistory());
		createTabbedPanePanel(foodStore, dailyRecordStore, config);
	}

  public DisplayConfig getDisplayConfig() {
    return new DisplayConfig(getSize().width, getSize().height, getLocation().x, getLocation().y,
       foodstuffsPanel.getFoodSortType().getPersistenceValue(), foodstuffsPanel.isReverseSort(),
       mealsPanel.getMealSortType().getPersistenceValue(), mealsPanel.isReverseSort());
  }
	
	private void createMenus() {
    JMenuBar menuBar = new JMenuBar();
		menuBar.add(createFileMenu());
    menuBar.add(createEditMenu());
    menuBar.add(createHistoryMenu());
		setJMenuBar(menuBar);
	}

  private JMenu createFileMenu() {
    JMenu fileMenu = new JMenu("File");
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem saveMenuItem = new JMenuItem("Save", KeyEvent.VK_S);
    saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
    saveMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          actionHandler.saveAllChangedData();
        }
        catch (Exception ex) {
          JOptionPane.showMessageDialog(MasterFrame.this, ex.getMessage(), "Calorie Counter", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    fileMenu.add(saveMenuItem);

    JMenuItem exitMenuItem = new JMenuItem("Exit", KeyEvent.VK_X);
    exitMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionHandler.exit();
      }
    });
    fileMenu.add(exitMenuItem);

    return fileMenu;
  }

  private JMenu createEditMenu() {
    JMenu editMenu = new JMenu("Edit");
    editMenu.setMnemonic(KeyEvent.VK_E);

    undoMenuItem = new JMenuItem("Undo", KeyEvent.VK_U);
    undoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
    undoMenuItem.setEnabled(false);
    undoMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionHandler.undo();
      }
    });
    editMenu.add(undoMenuItem);

    redoMenuItem = new JMenuItem("Redo", KeyEvent.VK_R);
    redoMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
    redoMenuItem.setEnabled(false);
    redoMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        actionHandler.redo();
      }
    });
    editMenu.add(redoMenuItem);

    return editMenu;
  }

  private JMenu createHistoryMenu() {
    JMenu historyMenu = new JMenu("History");
    historyMenu.setMnemonic(KeyEvent.VK_H);

    JMenuItem chartMenuItem = new JMenuItem("Chart", KeyEvent.VK_C);
    chartMenuItem.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        chartDialog.setVisible(true);
      }
    });
    historyMenu.add(chartMenuItem);

    return historyMenu;
  }

  private void createTabbedPanePanel(FoodStore foodStore, DailyRecordStore dailyRecordStore, DisplayConfig config) {
    JTabbedPane tabbedPane = new JTabbedPane();
		setContentPane(tabbedPane);

    FoodSortType foodstuffsSortType = FoodSortType.getByPersistenceValue(config.getFoodstuffsTableSortPersistenceValue());
    foodstuffsPanel = new FoodstuffsPanel(actionHandler, foodStore, foodstuffsSortType, config.isReverseFoodstuffsTableSort());
    FoodSortType mealsSortType = FoodSortType.getByPersistenceValue(config.getMealsTableSortPersistenceValue());
    mealsPanel = new MealsPanel(actionHandler, foodStore, mealsSortType, config.isReverseMealsTableSort());
    MealPanel mealPanel = new MealPanel(actionHandler, foodStore);
		DailyRecordPanel dailyRecordPanel = new DailyRecordPanel(actionHandler, foodStore, dailyRecordStore);
		
		tabbedPane.add("Foodstuffs", foodstuffsPanel);
		tabbedPane.add("Meals", mealsPanel);
    tabbedPane.add("Meal", mealPanel);
		tabbedPane.add("Daily Record", dailyRecordPanel);

    tabbedPane.setSelectedComponent(dailyRecordPanel);
	}

  @Override
  public void update(Observable o, Object arg) {
    if (arg instanceof ChangeNotification) {
      String undoDescription = actionHandler.getUndoChangeDescription();
      boolean undoAvailable = undoDescription != null;
      undoMenuItem.setEnabled(undoAvailable);
      undoMenuItem.setText(undoAvailable ? "Undo " + undoDescription : "Undo");

      String redoDescription = actionHandler.getRedoChangeDescription();
      boolean redoAvailable = redoDescription != null;
      redoMenuItem.setEnabled(redoAvailable);
      redoMenuItem.setText(redoAvailable ? "Redo " + redoDescription : "Redo");
    }
  }
}
