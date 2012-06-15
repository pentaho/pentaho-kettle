/*
 * ******************************************************************************
 * Pentaho Big Data
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */

package org.pentaho.di.ui.job.entries.oozie.xul;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.dom.Element;
import org.pentaho.ui.xul.swt.tags.SwtCheckbox;

/**
 * User: RFellows
 * Date: 6/15/12
 */
public class BindableSwtCheckbox extends SwtCheckbox {

  public BindableSwtCheckbox(Element self, XulComponent parent, XulDomContainer container, String tagName) {
    super(self, parent, container, tagName);
  }

  @Override
  protected Button createNewButton(Composite parent) {
    final Button button = new Button(parent, SWT.CHECK);
    button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        changeSupport.firePropertyChange("checked", !button.getSelection(), button.getSelection());
      }
    });
    return button;
  }

  @Override
  public void setChecked(boolean checked) {
    boolean prev = isChecked();
    if ((!button.isDisposed()) && (button != null)) button.setSelection(checked);
    changeSupport.firePropertyChange("checked", null, checked);
  }

}
