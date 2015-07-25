package seahawk.caloriecounter.gui.common;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;

public class DataResizingTable extends JTable {

  public DataResizingTable(TableModel model, TableColumnModel columnModel) {
    super(model, columnModel);

    setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
  }

  public JScrollPane surroundWithScrollPane(boolean allowVerticalScroll) {
    return new JScrollPane(this,
       allowVerticalScroll ? ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED : ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
       ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  }

  public void refresh() {
    for (int column = 0; column < getColumnCount(); column++) {
      TableColumn tableColumn = getColumnModel().getColumn(column);

      TableCellRenderer renderer = tableColumn.getHeaderRenderer();
      if (renderer == null)
        renderer = getTableHeader().getDefaultRenderer();

      Component component = renderer.getTableCellRendererComponent(this, tableColumn.getHeaderValue(), false, false, -1, column);
      int preferredWidth = component.getPreferredSize().width + 6;
      int maxWidth = tableColumn.getMaxWidth();

      for (int row = 0; row < getRowCount(); row++) {
        TableCellRenderer cellRenderer = getCellRenderer(row, column);
        Component c = prepareRenderer(cellRenderer, row, column);
        int width = c.getPreferredSize().width + getIntercellSpacing().width + 2;
        preferredWidth = Math.max(preferredWidth, width);

        //  We've exceeded the maximum width, no need to check other rows
        if (preferredWidth >= maxWidth) {
          preferredWidth = maxWidth;
          break;
        }
      }

      tableColumn.setPreferredWidth(preferredWidth);
    }
    setPreferredScrollableViewportSize(getPreferredSize());
  }
}
