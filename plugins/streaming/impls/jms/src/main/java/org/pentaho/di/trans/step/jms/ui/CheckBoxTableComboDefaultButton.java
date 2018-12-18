/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.step.jms.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.jms.context.JmsProvider;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.widget.CheckBoxTableCombo;

import java.util.Map;
import java.util.function.Consumer;

public class CheckBoxTableComboDefaultButton extends CheckBoxTableCombo {

  private Button wUseDefaultCheckBox;
  private JmsProvider.ConnectionType selectedConnectionType;

  public CheckBoxTableComboDefaultButton( Composite parentComposite, PropsUI props, ModifyListener lsMod,
                                          TransMeta transMeta, Map<String, String> dataMap, String buttonName,
                                          String tableName, String columnOneName, String columnTwoName,
                                          boolean isEnabled, boolean useDefaultContext,
                                          Consumer<JmsProvider.ConnectionType> toggleVisibilityCallback,
                                          String connectionType ) {
    super( parentComposite, props, lsMod, transMeta, dataMap, buttonName, tableName, columnOneName, columnTwoName,
      isEnabled );

    wUseDefaultCheckBox = new Button( parentComposite, SWT.CHECK );
    wUseDefaultCheckBox.setText( "Use system default SSL context" );
    FormData fdCheckBox = new FormData();
    fdCheckBox.left = new FormAttachment( wCheckBox, 10 );
    wUseDefaultCheckBox.setLayoutData( fdCheckBox );
    wUseDefaultCheckBox.setSelection( useDefaultContext );
    wUseDefaultCheckBox.setEnabled( isEnabled );
    selectedConnectionType = JmsProvider.ConnectionType.valueOf( connectionType );

    wCheckBox.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        boolean selection = ( (Button) selectionEvent.getSource() ).getSelection();
        wUseDefaultCheckBox.setEnabled( selection );
      }

      @Override public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        boolean selection = ( (Button) selectionEvent.getSource() ).getSelection();
        wUseDefaultCheckBox.setEnabled( selection );
      }
    } );

    wUseDefaultCheckBox.addSelectionListener( new SelectionListener() {
      @Override public void widgetSelected( SelectionEvent selectionEvent ) {
        toggleVisibilityCallback.accept( selectedConnectionType );
        lsMod.modifyText( null );
      }

      @Override public void widgetDefaultSelected( SelectionEvent selectionEvent ) {
        toggleVisibilityCallback.accept( selectedConnectionType );
        lsMod.modifyText( null );
      }
    } );
  }

  public boolean getUseDefaultSslContext() {
    return this.wUseDefaultCheckBox.getSelection();
  }

  /**
   * Method should be used to set the value of the currently selected connection type on the connection tab.
   *
   * @param type Selected connection type on the connection tab.
   */
  public void setSelectedConnectionType( JmsProvider.ConnectionType type ) {
    selectedConnectionType = type;
  }

  public JmsProvider.ConnectionType getSelectedConnectionType() {
    return selectedConnectionType;
  }
}
