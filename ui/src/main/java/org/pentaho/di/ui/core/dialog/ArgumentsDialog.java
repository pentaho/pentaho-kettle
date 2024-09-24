/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2020 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.ExecutionConfiguration;
import org.pentaho.di.base.AbstractMeta;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.util.HelpUtils;

public class ArgumentsDialog extends Dialog {

  private static Class<?> PKG = ArgumentsDialog.class; // for i18n purposes, needed by Translator2!!
  private TableView wArguments;
  private ExecutionConfiguration configuration;
  private Shell shell;
  private Display display;
  private PropsUI props;

  /**
   * Create the composite.
   * 
   * @param parent
   * @param style
   */
  public ArgumentsDialog( final Shell parent, ExecutionConfiguration configuration, AbstractMeta abstractMeta ) {
    super( parent );
    this.configuration = configuration;

    display = parent.getDisplay();
    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.APPLICATION_MODAL );
    props = PropsUI.getInstance();
    props.setLook( shell );
    shell.setImage( parent.getImage() );
    shell.setLayout( new FormLayout() );
    shell.setText( BaseMessages.getString( PKG, "ArgumentsDialog.Arguments.Label" ) );

    ColumnInfo[] cArguments =
    { new ColumnInfo( BaseMessages.getString( PKG, "ArgumentsDialog.ArgumentsColumn.Argument" ),
            ColumnInfo.COLUMN_TYPE_TEXT, false, true, 180 ), // Argument name
      new ColumnInfo( BaseMessages.getString( PKG, "ArgumentsDialog.ArgumentsColumn.Value" ),
            ColumnInfo.COLUMN_TYPE_TEXT, false, false, 172 ), // Actual value
    };

    int nrArguments = configuration.getArguments() != null ? configuration.getArguments().size() : 0;

    wArguments =
        new TableView( abstractMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, cArguments, nrArguments, false,
            null, props, false );

    FormData fd_argumentsTable = new FormData();
    fd_argumentsTable.top = new FormAttachment( 0, 15 );
    fd_argumentsTable.left = new FormAttachment( 0, 15 );
    fd_argumentsTable.bottom = new FormAttachment( 0, 221 );
    fd_argumentsTable.right = new FormAttachment( 100, -15 );
    wArguments.setLayoutData( fd_argumentsTable );

    Label separator = new Label( shell, SWT.SEPARATOR | SWT.HORIZONTAL );
    FormData fd_separator = new FormData();
    fd_separator.top = new FormAttachment( wArguments, 15 );
    fd_separator.right = new FormAttachment( wArguments, 0, SWT.RIGHT );
    fd_separator.left = new FormAttachment( 0, 15 );
    separator.setLayoutData( fd_separator );

    Button cancelButton = new Button( shell, SWT.NONE );
    cancelButton.setText( "Cancel" );
    FormData fd_cancelButton = new FormData();
    fd_cancelButton.top = new FormAttachment( separator, 15 );
    fd_cancelButton.right = new FormAttachment( wArguments, 0, SWT.RIGHT );
    cancelButton.setLayoutData( fd_cancelButton );
    cancelButton.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        dispose();
      }
    } );

    Button okButton = new Button( shell, SWT.NONE );
    okButton.setText( "OK" );
    FormData fd_okButton = new FormData();
    fd_okButton.top = new FormAttachment( cancelButton, 0, SWT.TOP );
    fd_okButton.right = new FormAttachment( cancelButton, -5 );
    fd_okButton.bottom = new FormAttachment( 100, -15 );
    okButton.setLayoutData( fd_okButton );
    okButton.addSelectionListener( new SelectionAdapter() {
      public void widgetSelected( SelectionEvent e ) {
        ok();
      }
    } );

    Button btnHelp = new Button( shell, SWT.NONE );
    btnHelp.setImage( GUIResource.getInstance().getImageHelpWeb() );
    btnHelp.setText( BaseMessages.getString( PKG, "System.Button.Help" ) );
    btnHelp.setToolTipText( BaseMessages.getString( PKG, "System.Tooltip.Help" ) );
    FormData fd_btnHelp = new FormData();
    fd_btnHelp.bottom = new FormAttachment( 100, -15 );
    fd_btnHelp.left = new FormAttachment( 0, 15 );
    btnHelp.setLayoutData( fd_btnHelp );
    btnHelp.addSelectionListener( new SelectionAdapter() {
      @Override
      public void widgetSelected( SelectionEvent evt ) {
        String docUrl = Const.getDocUrl( BaseMessages.getString( Spoon.class, "Spoon.ArgumentsDialog.Help" ) );
        String docTitle = BaseMessages.getString( PKG, "ArgumentsDialog.docTitle" );
        String docHeader = BaseMessages.getString( PKG, "ArgumentsDialog.docHeader" );
        HelpUtils.openHelpDialog( parent.getShell(), docTitle, docUrl, docHeader );
      }
    } );

    shell.pack();
    getArgumentsData();
    shell.open();

    Rectangle shellBounds = getParent().getBounds();
    Point dialogSize = shell.getSize();

    shell.setLocation( shellBounds.x + ( shellBounds.width - dialogSize.x ) / 2, shellBounds.y + ( shellBounds.height
        - dialogSize.y ) / 2 );

    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
  }

  @Override
  protected void checkSubclass() {
  }

  private void getArgumentsData() {
    wArguments.clearAll( false );

    List<String> argumentNames = new ArrayList<String>( configuration.getArguments().keySet() );
    Collections.sort( argumentNames );

    for ( int i = 0; i < argumentNames.size(); i++ ) {
      String argumentName = argumentNames.get( i );
      String argumentValue = configuration.getArguments().get( argumentName );

      TableItem tableItem = new TableItem( wArguments.table, SWT.NONE );
      tableItem.setText( 1, Const.NVL( argumentName, "" ) );
      tableItem.setText( 2, Const.NVL( argumentValue, "" ) );
    }
    wArguments.removeEmptyRows();
    wArguments.setRowNums();
    wArguments.optWidth( true );
  }

  private void getInfoArguments() {
    Map<String, String> map = new HashMap<String, String>();
    int nrNonEmptyArguments = wArguments.nrNonEmpty();
    for ( int i = 0; i < nrNonEmptyArguments; i++ ) {
      TableItem tableItem = wArguments.getNonEmpty( i );
      String varName = tableItem.getText( 1 );
      String varValue = tableItem.getText( 2 );

      if ( !Utils.isEmpty( varName ) ) {
        map.put( varName, varValue );
      }
    }
    configuration.setArguments( map );
  }

  protected void ok() {
    if ( Const.isOSX() ) {
      // OSX bug workaround.
      //
      wArguments.applyOSXChanges();
    }
    getInfoArguments();
    dispose();
  }

  private void dispose() {
    props.setScreen( new WindowProperty( shell ) );
    shell.dispose();
  }
}
