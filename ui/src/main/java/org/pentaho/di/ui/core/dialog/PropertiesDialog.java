/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.ui.core.dialog;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.Const;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.ui.util.HelpUtils;

import java.util.HashMap;
import java.util.Map;

public class PropertiesDialog extends Dialog {
  private static final Class<?> PKG = PropertiesDialog.class;

  private Shell shell;
  private TableView propertiesTable;

  private TransMeta transMeta;
  private Map<String, String> properties;
  private String helpUrl;
  private String helpTitle;
  private String helpHeader;

  public PropertiesDialog( Shell shell, TransMeta transMeta, Map<String, String> properties, String title ) {
    this( shell, transMeta, properties, title, null, null, null );
  }

  public PropertiesDialog( Shell shell, TransMeta transMeta, Map<String, String> properties, String title,
                           String helpUrl, String helpTitle, String helpHeader ) {
    super( shell, SWT.NONE );
    this.setText( title );
    this.transMeta = transMeta;
    this.properties = properties;
    this.helpUrl = helpUrl;
    this.helpTitle = helpTitle;
    this.helpHeader = helpHeader;
  }

  public Map<String, String> open() {
    PropsUI props = PropsUI.getInstance();
    Shell parent = getParent();
    Display display = parent.getDisplay();

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.RESIZE );
    props.setLook( shell );
    shell.setImage( GUIResource.getInstance().getImageSpoon() );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = Const.FORM_MARGIN;
    formLayout.marginHeight = Const.FORM_MARGIN;

    shell.setLayout( formLayout );
    shell.setText( getText() );

    if ( StringUtils.isNotEmpty( helpUrl ) ) {
      HelpUtils.createHelpButton( shell, helpTitle, helpUrl, helpHeader );
    }

    Button wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );
    wCancel.addListener( SWT.Selection, e -> close() );

    Button wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );
    wOK.addListener( SWT.Selection, e -> ok() );

    ColumnInfo[] columns = createColumns();

    propertiesTable = new TableView(
      transMeta,
      shell,
      SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
      columns,
      10,
      false,
      null,
      props,
      false
    );

    propertiesTable.setSortable( false );
    propertiesTable.getTable().addListener( SWT.Resize, event -> {
      Table table = (Table) event.widget;
      table.getColumn( 1 ).setWidth( 220 );
      table.getColumn( 2 ).setWidth( 220 );
    } );

    populateData();

    FormData fdData = new FormData();
    fdData.left = new FormAttachment( 0, 0 );
    fdData.top = new FormAttachment( 0, 0 );
    fdData.right = new FormAttachment( 100, 0 );
    fdData.bottom = new FormAttachment( wOK, 0 );
    fdData.width = 450;

    propertiesTable.setLayoutData( fdData );

    BaseStepDialog.setSize( shell );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }

    return properties;
  }

  protected ColumnInfo[] createColumns() {
    return new ColumnInfo[]{
      new ColumnInfo( BaseMessages.getString( "propertiesDialog.name" ), ColumnInfo.COLUMN_TYPE_TEXT ),
      new ColumnInfo( BaseMessages.getString( "propertiesDialog.value" ), ColumnInfo.COLUMN_TYPE_TEXT ) };
  }

  private void populateData() {
    if ( null == properties ) {
      return;
    }

    int rowIndex = 0;
    for ( Map.Entry<String, String> entry : properties.entrySet() ) {
      TableItem tableItem = propertiesTable.getTable().getItem( rowIndex++ );
      tableItem.setText( 1, entry.getKey() );
      tableItem.setText( 2, entry.getValue() );
    }
  }

  private Map<String, String> retrieveProperties() {
    Map<String, String> enteredProperties = new HashMap<>();
    for ( TableItem row : propertiesTable.getTable().getItems() ) {
      String config = row.getText( 1 );
      String value = row.getText( 2 );
      if ( !StringUtils.isBlank( config ) && !enteredProperties.containsKey( config ) ) {
        enteredProperties.put( config, value );
      }
    }

    return enteredProperties;
  }

  private void close() {
    properties = null;
    dispose();
  }

  private void ok() {
    properties = retrieveProperties();
    dispose();
  }

  public void dispose() {
    shell.dispose();
  }
}
