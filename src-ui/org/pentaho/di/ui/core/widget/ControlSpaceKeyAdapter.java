/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
 */
package org.pentaho.di.ui.core.widget;

import java.util.Arrays;

import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

public class ControlSpaceKeyAdapter extends KeyAdapter {

  private static Class<?> PKG = ControlSpaceKeyAdapter.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private static final PropsUI props = PropsUI.getInstance();

  private GetCaretPositionInterface getCaretPositionInterface;

  private InsertTextInterface insertTextInterface;

  private VariableSpace variables;

  private Control control;

  /**
   * @param space
   * @param control a Text or CCombo box object
   */
  public ControlSpaceKeyAdapter(final VariableSpace space, final Control control) {
    this(space, control, null, null);
  }

  /**
   * 
   * @param space
   * @param control a Text or CCombo box object
   * @param getCaretPositionInterface
   * @param insertTextInterface
   */
  public ControlSpaceKeyAdapter(VariableSpace space, final Control control,
      final GetCaretPositionInterface getCaretPositionInterface, final InsertTextInterface insertTextInterface) {
    
    this.variables = space;
    this.control = control;
    this.getCaretPositionInterface = getCaretPositionInterface;
    this.insertTextInterface = insertTextInterface;
    
  }
  
  /**
   *  PDI-1284
   *  in chinese window, Ctrl-SPACE is reversed by system for input chinese character.
   *  use Ctrl-ALT-SPACE instead.
   * @param e
   * @return
   */
  private boolean isHotKey(KeyEvent e)
  {
	  if (System.getProperty("user.language").equals("zh")) 
		  return e.character == ' ' && ((e.stateMask & SWT.CONTROL) != 0) && ((e.stateMask & SWT.ALT) != 0);
	  // Detect Command-space on Mac OS X to fix PDI-4319
	  else if (System.getProperty("os.name").startsWith("Mac OS X"))
		  return e.character == ' ' && ((e.stateMask & SWT.MOD1) != 0) && ((e.stateMask & SWT.ALT) == 0);
	  else
		  return e.character == ' ' && ((e.stateMask & SWT.CONTROL) != 0) && ((e.stateMask & SWT.ALT) == 0);
  }

  public void keyPressed(KeyEvent e) {
    // CTRL-<SPACE> --> Insert a variable
    if (isHotKey(e)) {
      e.doit = false;

      // textField.setData(TRUE) indicates we have transitioned from the textbox to list mode...
      // This will be set to false when the list selection has been processed
      // and the list is being disposed of. 
      control.setData(Boolean.TRUE);
      
      final int position;
      if (getCaretPositionInterface != null)
        position = getCaretPositionInterface.getCaretPosition();
      else
        position = -1;

      // Drop down a list of variables...
      //
      Rectangle bounds = control.getBounds();
      Point location = GUIResource.calculateControlPosition(control);

      final Shell shell = new Shell(control.getShell(), SWT.NONE);
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
          applyChanges(shell, list, control, position, insertTextInterface);
        }

        // Select a variable name: display the value in a tool tip
        //
        public void widgetSelected(SelectionEvent event) {
          if (list.getSelectionCount() <= 0)
            return;
          String name = list.getSelection()[0];
          String value = variables.getVariable(name);
          Rectangle shellBounds = shell.getBounds();
          String message = BaseMessages.getString(PKG, "TextVar.VariableValue.Message", name, value);
          if (name.startsWith(Const.INTERNAL_VARIABLE_PREFIX))
            message += BaseMessages.getString(PKG, "TextVar.InternalVariable.Message");
          toolTip.setText(message);
          toolTip.hide();
          toolTip.show(new Point(shellBounds.width, 0));
        }
      });

      list.addKeyListener(new KeyAdapter() {

        public synchronized void keyPressed(KeyEvent e) {
          if (e.keyCode == SWT.CR && ((e.keyCode & SWT.CONTROL) == 0) && ((e.keyCode & SWT.SHIFT) == 0)) {
            applyChanges(shell, list, control, position, insertTextInterface);
          }
        }

      });
      
      list.addFocusListener(new FocusAdapter() {
        public void focusLost(FocusEvent event) {
          shell.dispose();
          if (!control.isDisposed()) control.setData(Boolean.FALSE);
        }
      });
      
      shell.open();
    }
    ;
  }

  private static final void applyChanges(Shell shell, List list, Control control, int position,
      InsertTextInterface insertTextInterface) {
    String extra = "${" + list.getSelection()[0] + "}";
    if (insertTextInterface != null) {
      insertTextInterface.insertText(extra, position);
    } else {
      if (control.isDisposed())
        return;

      if (list.getSelectionCount() <= 0)
        return;
      if (control instanceof Text) {
    	  ((Text)control).insert(extra);
      } else if (control instanceof CCombo) {
    	  CCombo combo = (CCombo)control;
    	  combo.setText(extra); // We can't know the location of the cursor yet. All we can do is overwrite.
      } 
    }
    if (!shell.isDisposed())
      shell.dispose();
    if (!control.isDisposed()) control.setData(Boolean.FALSE);
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
