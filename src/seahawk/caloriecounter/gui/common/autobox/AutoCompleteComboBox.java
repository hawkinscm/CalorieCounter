package seahawk.caloriecounter.gui.common.autobox;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Set;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public class AutoCompleteComboBox extends JComboBox<String> {
  private boolean internalProcessing;
  private String currentFilter;
  
  public AutoCompleteComboBox(Set<String> data) {
    super(new AutoCompleteModel(data));
    ((JTextComponent) getEditor().getEditorComponent()).setDocument(new AutoCompleteDocument());

    internalProcessing = false;
    currentFilter = null;

    setEditable(true);
    putClientProperty("JComboBox.isTableCellEditor", Boolean.TRUE);
    setFilter(null);
    setSelectedItem(null);
  }

  public void setData(Set<String> data) {
    getModel().setData(data);
  }

  private void setFilter(String newFilter) {
    if (newFilter != null && newFilter.trim().isEmpty())
      newFilter = null;

    if ((currentFilter == null && newFilter == null) || (newFilter != null && newFilter.equals(currentFilter))) {
      return;
    }
    currentFilter = newFilter;

    internalProcessing = true;
    getModel().setFilter(newFilter);
    internalProcessing = false;

    if (newFilter != null) {
      setPopupVisible(false);
      if (getModel().getSize() > 0) {
        setPopupVisible(true);
      }
    }
  }

  public void clearSelection() {
    int itemLength = getEditor().getItem().toString().length();
    ((JTextComponent) getEditor().getEditorComponent()).setSelectionStart(itemLength);
    ((JTextComponent) getEditor().getEditorComponent()).setSelectionEnd(itemLength);
  }

  @Override
  public AutoCompleteModel getModel() {
    return (AutoCompleteModel) super.getModel();
  }

  private class AutoCompleteDocument extends PlainDocument {
    boolean arrowKeyPressed = false;

    public AutoCompleteDocument() {
      getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {

        @Override
        public void keyPressed(KeyEvent e) {
          int key = e.getKeyCode();
          if (key == KeyEvent.VK_UP || key == KeyEvent.VK_DOWN) {
            arrowKeyPressed = true;
          }
        }
      });
    }

    void updateModel() throws BadLocationException {
      String filter = getText(0, getLength());
      setFilter(filter);
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
      if (internalProcessing)
        return;

      super.remove(offs, len);
      if (arrowKeyPressed) {
        arrowKeyPressed = false;
      }
      else {
        updateModel();
      }
      clearSelection();
    }

    @Override
    public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
      if (internalProcessing)
        return;

      super.insertString(offs, str, a);

      String text = getText(0, getLength());
      if (arrowKeyPressed) {
        arrowKeyPressed = false;
      }
      else if (!text.equals(getModel().getSelectedItem())){
        updateModel();
      }

      clearSelection();
    }
  }
}