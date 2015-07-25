package seahawk.caloriecounter.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import seahawk.caloriecounter.domain.api.DailyRecordHistory;
import seahawk.caloriecounter.domain.api.NutrientType;
import seahawk.caloriecounter.domain.api.NutritionFacts;
import seahawk.caloriecounter.domain.api.date.Date;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;

public class DailyRecordHistoryChartDialog extends JDialog {
  private DailyRecordHistory dailyRecordHistory;
  private JFreeChart chart;
  private TimeSeriesCollection dataCollection;

  private NutrientType nutrientType = NutrientType.CALORIES;
  private int calenderRangeField = Calendar.DATE;
  private int calendarRangeAmount = 14;

  public DailyRecordHistoryChartDialog(JFrame parent, DailyRecordHistory dailyRecordHistory) {
    super(parent, "Daily History Chart");
    this.dailyRecordHistory = dailyRecordHistory;

    setJMenuBar(createChartMenuBar());

    dataCollection = new TimeSeriesCollection();
    chart = ChartFactory.createXYLineChart("", "Day (Year Month Day)", "Amount", dataCollection, PlotOrientation.VERTICAL, false, true, false);
    chart.setBackgroundPaint(Color.white);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(Color.lightGray);
    plot.setDomainGridlinePaint(Color.white);
    plot.setRangeGridlinePaint(Color.white);
    plot.setRenderer(new XYLineAndShapeRenderer(true, true));
    StandardXYToolTipGenerator standardXYToolTipGenerator = new StandardXYToolTipGenerator(
       "<html>{1}<br>{2}</html>",
       DateFormat.getDateInstance(),
       NumberFormat.getNumberInstance());
    plot.getRenderer().setBaseToolTipGenerator(standardXYToolTipGenerator);
    DateAxis dateAxis = new DateAxis();
    dateAxis.setDateFormatOverride(DateFormat.getDateInstance());
    plot.setDomainAxis(dateAxis);

    ChartPanel chartPanel = new ChartPanel(chart);
    getContentPane().add(chartPanel);
  }

  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      refreshData();
      setLocationRelativeTo(getParent());
    }
    super.setVisible(visible);
  }

  private void refreshData() {
    TimeSeries dailyFactSeries = new TimeSeries(nutrientType.name());

    if (calendarRangeAmount == -1) {
      for (Date date : dailyRecordHistory.getRecordedDates()) {
        NutritionFacts dailyFacts = dailyRecordHistory.getDailyFacts(date);
        dailyFactSeries.add(new Day(date.getDay(), date.getMonth(), date.getYear()), dailyFacts.getAmount(nutrientType).getValue());
      }
    }
    else {
      Calendar current = Calendar.getInstance();
      Calendar earliestDate = Calendar.getInstance();
      earliestDate.add(calenderRangeField, -1 * calendarRangeAmount);
      while (current.after(earliestDate)) {
        Date date = new Date(current);
        NutritionFacts dailyFacts = dailyRecordHistory.getDailyFacts(date);
        if (dailyFacts != null) {
          dailyFactSeries.add(new Day(date.getDay(), date.getMonth(), date.getYear()), dailyFacts.getAmount(nutrientType).getValue());
        }

        current.add(Calendar.DATE, -1);
      }
    }

    dataCollection.removeAllSeries();
    dataCollection.addSeries(dailyFactSeries);

    chart.setTitle(nutrientType.name().replace('_', ' '));

    pack();
  }

  private JMenuBar createChartMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu nutrientMenu = new JMenu("Nutrient");
    ButtonGroup nutrientGroup = new ButtonGroup();
    for (final NutrientType type : NutrientType.values()) {
      JRadioButtonMenuItem nutrientMenuItem = new JRadioButtonMenuItem(type.name().replace('_', ' '));
      if (type == NutrientType.CALORIES) {
        nutrientMenuItem.setSelected(true);
      }
      nutrientMenuItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          nutrientType = type;
          refreshData();
        }
      });
      nutrientMenu.add(nutrientMenuItem);
      nutrientGroup.add(nutrientMenuItem);
    }
    menuBar.add(nutrientMenu);

    JMenu timespanMenuItem = new JMenu("Timespan");
    ButtonGroup timespanGroup = new ButtonGroup();

    String[] timespanNames = new String[] {"1 Week", "2 Weeks", "1 Month", "3 Months", "6 Months", "1 Year", "All"};
    final int[] timespanCalendarField = new int[] {Calendar.DATE, Calendar.DATE, Calendar.MONTH, Calendar.MONTH, Calendar.MONTH, Calendar.YEAR, -1};
    final int[] timespanCalendarRange = new int[] {7, 14, 1, 3, 6, 1, -1};
    for (int index = 0; index < timespanNames.length; index++) {
      JMenuItem menuItem = new JRadioButtonMenuItem(timespanNames[index]);
      if (index == 1) {
        menuItem.setSelected(true);
      }
      final int currentIndex = index;
      menuItem.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          calenderRangeField = timespanCalendarField[currentIndex];
          calendarRangeAmount = timespanCalendarRange[currentIndex];
          refreshData();
        }
      });
      timespanMenuItem.add(menuItem);
      timespanGroup.add(menuItem);
    }

    menuBar.add(timespanMenuItem);

    return menuBar;
  }


}
