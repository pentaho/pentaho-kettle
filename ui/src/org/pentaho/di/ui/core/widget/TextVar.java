/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.ui.core.widget;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;

/**
 * A Widget that combines a Text widget with a Variable button that will insert an Environment variable.
 * The tool tip of the text widget shows the content of the Text widget with expanded variables.
 * 
 * @author Matt
 * @since 17-may-2006
 */
public class TextVar extends Composite {
  private static Class<?> PKG = TextVar.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private String toolTipText;

  // private static final PropsUI props = PropsUI.getInstance();

  private ControlDecoration controlDecoration;

  private GetCaretPositionInterface getCaretPositionInterface;

  private InsertTextInterface insertTextInterface;
  
  private ControlSpaceKeyAdapter controlSpaceKeyAdapter;

  private VariableSpace variables;

  private Text wText;

  public TextVar(VariableSpace space, Composite composite, int flags) {
    this(space, composite, flags, null, null, null);
  }

  public TextVar(VariableSpace space, Composite composite, int flags, String toolTipText) {
    this(space, composite, flags, toolTipText, null, null);
  }

  public TextVar(VariableSpace space, Composite composite, int flags,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface) {
    this(space, composite, flags, null, getCaretPositionInterface, insertTextInterface);
  }

  public TextVar(VariableSpace space, Composite composite, int flags, String toolTipText,
      GetCaretPositionInterface getCaretPositionInterface, InsertTextInterface insertTextInterface) {
    super(composite, SWT.NONE);
    this.toolTipText = toolTipText;
    this.getCaretPositionInterface = getCaretPositionInterface;
    this.insertTextInterface = insertTextInterface;
    this.variables = space;

    PropsUI.getInstance().setLook(this);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;

    this.setLayout(formLayout);

    // add a text field on it...
    wText = new Text(this, flags);

    controlDecoration = new ControlDecoration(wText, SWT.TOP | SWT.RIGHT);
    Image image = GUIResource.getInstance().getImageVariable();
    controlDecoration.setImage(image);
    controlDecoration.setDescriptionText(BaseMessages.getString(PKG, "TextVar.tooltip.InsertVariable"));
    PropsUI.getInstance().setLook(controlDecoration.getControl());

    wText.addModifyListener(getModifyListenerTooltipText(wText));
  
    controlSpaceKeyAdapter = new ControlSpaceKeyAdapter(variables, wText,getCaretPositionInterface, insertTextInterface);
    wText.addKeyListener(controlSpaceKeyAdapter);

    FormData fdText = new FormData();
    fdText.top = new FormAttachment(0, 0);
    fdText.left = new FormAttachment(0, 0);
    fdText.right = new FormAttachment(100, -image.getBounds().width);
    wText.setLayoutData(fdText);
  }

  /**
   * @return the getCaretPositionInterface
   */
  public GetCaretPositionInterface getGetCaretPositionInterface() {
    return getCaretPositionInterface;
  }

  /**
   * @param getCaretPositionInterface the getCaretPositionInterface to set
   */
  public void setGetCaretPositionInterface(GetCaretPositionInterface getCaretPositionInterface) {
    this.getCaretPositionInterface = getCaretPositionInterface;
  }

  /**
   * @return the insertTextInterface
   */
  public InsertTextInterface getInsertTextInterface() {
    return insertTextInterface;
  }

  /**
   * @param insertTextInterface the insertTextInterface to set
   */
  public void setInsertTextInterface(InsertTextInterface insertTextInterface) {
    this.insertTextInterface = insertTextInterface;
  }

  private ModifyListener getModifyListenerTooltipText(final Text textField) {
    return new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        if (textField.getEchoChar() == '\0') // Can't show passwords ;-)
        {
          String tip = textField.getText();
          if (!Const.isEmpty(tip) && !Const.isEmpty(toolTipText)) {
            tip += Const.CR + Const.CR + toolTipText;
          }

          if (Const.isEmpty(tip)) {
            tip = toolTipText;
          }
          textField.setToolTipText(variables.environmentSubstitute(tip));
        }
      }
    };
  }

  /**
   * @return the text in the Text widget   
   */
  public String getText() {
    return wText.getText();
  }

  /**
   * @param text the text in the Text widget to set.
   */
  public void setText(String text) {
    wText.setText(text);
  }

  public Text getTextWidget() {
    return wText;
  }

  /**
   * Add a modify listener to the text widget
   * @param modifyListener
   */
  public void addModifyListener(ModifyListener modifyListener) {
    wText.addModifyListener(modifyListener);
  }

  public void addSelectionListener(SelectionAdapter lsDef) {
    wText.addSelectionListener(lsDef);
  }

  public void addKeyListener(KeyListener lsKey) {
    wText.addKeyListener(lsKey);
  }

  public void addFocusListener(FocusListener lsFocus) {
    wText.addFocusListener(lsFocus);
  }

  public void setEchoChar(char c) {
    wText.setEchoChar(c);
  }

  public void setEnabled(boolean flag) {
    wText.setEnabled(flag);
  }

  public boolean setFocus() {
    return wText.setFocus();
  }

  public void addTraverseListener(TraverseListener tl) {
    wText.addTraverseListener(tl);
  }

  public void setToolTipText(String toolTipText) {
    this.toolTipText = toolTipText;
    wText.setToolTipText(toolTipText);
  }

  public void setEditable(boolean editable) {
    wText.setEditable(editable);
  }

  public void setSelection(int i) {
    wText.setSelection(i);
  }

  public void selectAll() {
    wText.selectAll();
  }

  public void showSelection() {
    wText.showSelection();
  }

  public void setVariables(VariableSpace vars) {
    variables = vars;
    controlSpaceKeyAdapter.setVariables(variables);
  }

}