/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
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
import org.pentaho.di.ui.core.gui.GUIResource;

/**
 * A Widget that combines a Text widget with an "In Use" image to the left.
 * It's shown when there is content in the text field.
 * 
 * @author Matt
 * @since 25-FEB-2009
 */
public class InUseText extends Composite {
  private ControlDecoration inUseControlDecoration;

  private Text wText;

  public InUseText(Composite composite, int flags) {
    super(composite, SWT.NONE);

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 0;
    formLayout.marginHeight = 0;
    formLayout.marginTop = 0;
    formLayout.marginBottom = 0;

    this.setLayout(formLayout);

    // add a text field on it...
    wText = new Text(this, flags);

    inUseControlDecoration = new ControlDecoration(wText, SWT.CENTER | SWT.LEFT);
    Image image = GUIResource.getInstance().getImageInfoHop();
    inUseControlDecoration.setImage(image);
    inUseControlDecoration.setDescriptionText(Messages.getString("TextVar.tooltip.FieldIsInUse"));
    inUseControlDecoration.hide();
    
    // If the length of the string is longer than 0 : set the "in use" image...
    //
    wText.addModifyListener(new ModifyListener() {
		public void modifyText(ModifyEvent arg0) {
			if (!Const.isEmpty(wText.getText())) inUseControlDecoration.show();
			else inUseControlDecoration.hide();
		}
	});

    FormData fdText = new FormData();
    fdText.top = new FormAttachment(0, 0);
    fdText.left = new FormAttachment(0, image.getBounds().width);
    fdText.right = new FormAttachment(100, 0);
    wText.setLayoutData(fdText);
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
}