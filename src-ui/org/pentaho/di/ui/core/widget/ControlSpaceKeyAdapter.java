package org.pentaho.di.ui.core.widget;

import java.util.Arrays;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

public class ControlSpaceKeyAdapter extends KeyAdapter {

  private static final PropsUI props = PropsUI.getInstance();

  private GetCaretPositionInterface getCaretPositionInterface;

  private InsertTextInterface insertTextInterface;

  private VariableSpace variables;

  private Text textField;

  public ControlSpaceKeyAdapter(final VariableSpace space, final Text textField) {
    this(space, textField, null, null);
  }

  public ControlSpaceKeyAdapter(VariableSpace space, final Text textField,
      final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface) {
    
    this.variables = space;
    this.textField = textField;
    this.getCaretPositionInterface = getCaretPositionInterface;
    this.insertTextInterface = insertTextInterface;
    
  }

  public void keyPressed(KeyEvent e) {
    // CTRL-<SPACE> --> Insert a variable
    if (e.character == ' ' && ((e.stateMask & SWT.CONTROL) != 0) && ((e.stateMask & SWT.ALT) == 0)) {
      e.doit = false;

      final int position;
      if (getCaretPositionInterface != null)
        position = getCaretPositionInterface.getCaretPosition();
      else
        position = -1;

      // Drop down a list of variables...
      //
      Rectangle bounds = textField.getBounds();
      Point location = GUIResource.calculateControlPosition(textField);

      final Shell shell = new Shell(textField.getShell(), SWT.NONE);
      shell.setSize(bounds.width, 200);
      shell.setLocation(location.x, location.y + bounds.height);
      shell.setLayout(new FillLayout());
      final List list = new List(shell, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
      props.setLook(list);
      list.setItems(getVariableNames(variables));
      final DefaultToolTip toolTip = new DefaultToolTip(list, ToolTip.RECREATE, true);
      toolTip.setImage(GUIResource.getInstance().getImageSpoon());
      toolTip.setHideOnMouseDown(true);
      toolTip.setRespectMonitorBounds(true);
      toolTip.setRespectDisplayBounds(true);
      toolTip.setPopupDelay(350);

      list.addSelectionListener(new SelectionAdapter() {
        // Enter or double-click: picks the variable
        //
        public synchronized void widgetDefaultSelected(SelectionEvent e) {
          applyChanges(shell, list, textField, position, insertTextInterface);
        }

        // Select a variable name: display the value in a tool tip
        //
        public void widgetSelected(SelectionEvent event) {
          if (list.getSelectionCount() <= 0)
            return;
          String name = list.getSelection()[0];
          String value = variables.getVariable(name);
          Rectangle shellBounds = shell.getBounds();
          String message = Messages.getString("TextVar.VariableValue.Message", name, value);
          if (name.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
            message += Messages.getString("TextVar.InternalVariable.Message");
          toolTip.setText(message);
          toolTip.hide();
          toolTip.show(new Point(shellBounds.width, 0));
        }
      });

      list.addKeyListener(new KeyAdapter() {

        public synchronized void keyPressed(KeyEvent e) {
          if (e.keyCode == SWT.CR && ((e.keyCode & SWT.CONTROL) == 0) && ((e.keyCode & SWT.SHIFT) == 0)) {
            applyChanges(shell, list, textField, position, insertTextInterface);
          }
        }

      });
      
      list.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent event) {
          shell.dispose();
        }
      });
      
      shell.open();
    }
    ;
  }

  private static final void applyChanges(Shell shell, List list, Text textField, int position,
      InsertTextInterface insertTextInterface) {
    String extra = "${" + list.getSelection()[0] + "}";
    if (insertTextInterface != null) {
      insertTextInterface.insertText(extra, position);
    } else {
      if (textField.isDisposed())
        return;

      if (list.getSelectionCount() <= 0)
        return;
      textField.insert(extra);
    }
    if (!shell.isDisposed())
      shell.dispose();
  }

  public static final String[] getVariableNames(VariableSpace space) {
    String variableNames[] = space.listVariables();
      Arrays.sort(variableNames);
      
      // repeat a few entries at the top, for convenience...
      //
      String[] array = new String[variableNames.length+2];
      int index=0;
      array[index++]= Const.INTERNAL_VARIABLE_TRANSFORMATION_FILENAME_DIRECTORY;
      array[index++]= Const.INTERNAL_VARIABLE_JOB_FILENAME_DIRECTORY;
      
      for (String name : variableNames) array[index++] = name;
      
      return array;
  }

  public void setVariables(VariableSpace vars){
    variables = vars;
  }
}
