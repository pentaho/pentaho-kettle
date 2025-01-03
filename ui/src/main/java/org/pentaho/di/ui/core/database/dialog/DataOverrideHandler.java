/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.core.database.dialog;

import java.util.ArrayList;
import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseTestResults;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.database.dialog.tags.ExtMenuList;
import org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.ShowMessageDialog;
import org.pentaho.di.ui.util.DialogUtils;
import org.pentaho.ui.database.event.DataHandler;
import org.pentaho.ui.xul.components.XulMenuList;
import org.pentaho.ui.xul.components.XulTextbox;
import org.pentaho.ui.xul.containers.XulTree;

public class DataOverrideHandler extends DataHandler {
  boolean cancelPressed = false;

  @Override
  public void onCancel() {
    super.onCancel();
    this.cancelPressed = true;
  }

  @Override
  public void onOK() {
    // check for duplicates before closing the dialog.
    if ( databases != null ) {
      DatabaseMeta database = new DatabaseMeta();
      this.getInfo( database );
      if ( ! Objects.equals( database.getName(), originalName ) ) {
        DialogUtils.removeMatchingObject( originalName, databases );
        if ( DialogUtils.objectWithTheSameNameExists( database, databases ) ) {
          DatabaseDialog.showDatabaseExistsDialog( getShell(), database );
          return;
        }
      }
    }
    super.onOK();
    this.cancelPressed = false;
  }

  @Override
  public Object getData() {
    if ( !cancelPressed ) {
      return super.getData();
    } else {
      return null;
    }
  }

  private static Class<?> PKG = DataOverrideHandler.class; // for i18n purposes, needed by Translator2!!

  private java.util.List<DatabaseMeta> databases;

  public DataOverrideHandler() {
  }

  public void explore() {

    Shell parent = getShell();

    DatabaseMeta dbinfo = new DatabaseMeta();
    getInfo( dbinfo );

    try {
      if ( dbinfo.getAccessType() != DatabaseMeta.TYPE_ACCESS_PLUGIN ) {
        DatabaseExplorerDialog ded = new DatabaseExplorerDialog( parent, SWT.NONE, dbinfo, databases, true );
        ded.open();
      } else {
        MessageBox mb = new MessageBox( parent, SWT.OK | SWT.ICON_INFORMATION );
        mb.setText( BaseMessages.getString( PKG, "DatabaseDialog.ExplorerNotImplemented.Title" ) );
        mb.setMessage( BaseMessages.getString( PKG, "DatabaseDialog.ExplorerNotImplemented.Message" ) );
        mb.open();
      }
    } catch ( Exception e ) {
      new ErrorDialog( parent, BaseMessages.getString( PKG, "DatabaseDialog.ErrorParameters.title" ), BaseMessages
        .getString( PKG, "DatabaseDialog.ErrorParameters.description" ), e );
    }
  }

  private Shell getShell() {
    Object obj = document.getRootElement().getManagedObject();
    Shell parent;
    if ( obj instanceof Shell ) {
      parent = (Shell) obj;
    } else {
      parent = ( (Composite) obj ).getShell();
    }
    if ( parent == null ) {
      throw new IllegalStateException( "Could not get Shell reference from Xul Dialog Tree." );
    }
    return parent;
  }

  public void showFeatureList() {

    Shell parent = getShell();

    DatabaseMeta dbinfo = new DatabaseMeta();
    getInfo( dbinfo );

    try {
      java.util.List<RowMetaAndData> buffer = dbinfo.getFeatureSummary();
      if ( buffer.size() > 0 ) {
        RowMetaInterface rowMeta = buffer.get( 0 ).getRowMeta();
        java.util.List<Object[]> rowData = new ArrayList<Object[]>();
        for ( RowMetaAndData row : buffer ) {
          rowData.add( row.getData() );
        }

        PreviewRowsDialog prd = new PreviewRowsDialog( parent, dbinfo, SWT.NONE, null, rowMeta, rowData );
        prd.setTitleMessage( BaseMessages.getString( PKG, "DatabaseDialog.FeatureList.title" ), BaseMessages
          .getString( PKG, "DatabaseDialog.FeatureList.title" ) );
        prd.open();
      }
    } catch ( Exception e ) {
      new ErrorDialog(
        parent, BaseMessages.getString( PKG, "DatabaseDialog.FeatureListError.title" ), BaseMessages.getString(
          PKG, "DatabaseDialog.FeatureListError.description" ), e );
    }
  }

  @Override
  protected void getControls() {

    super.getControls();

    XulTextbox[] boxes =
      new XulTextbox[] {
        hostNameBox, databaseNameBox, portNumberBox, userNameBox, passwordBox, customDriverClassBox,
        customUrlBox, dataTablespaceBox, indexTablespaceBox, poolSizeBox, maxPoolSizeBox, languageBox,
        systemNumberBox, clientBox, serverInstanceBox, warehouseBox };

    for ( int i = 0; i < boxes.length; i++ ) {
      XulTextbox xulTextbox = boxes[i];
      if ( ( xulTextbox != null ) && ( xulTextbox instanceof ExtTextbox ) ) {
        ExtTextbox ext = (ExtTextbox) xulTextbox;
        ext.setVariableSpace( databaseMeta );
      }
    }

    XulTree[] trees = new XulTree[] { poolParameterTree, clusterParameterTree, optionsParameterTree };

    for ( int i = 0; i < trees.length; i++ ) {
      XulTree xulTree = trees[i];
      if ( xulTree != null ) {
        xulTree.setData( databaseMeta );
      }
    }

    XulMenuList[] menus = new XulMenuList[] { namedClusterList };

    for ( int i = 0; i < menus.length; i++ ) {
      XulMenuList xulMenu = menus[i];
      if ( xulMenu != null && xulMenu instanceof ExtMenuList ) {
        ExtMenuList ext = (ExtMenuList) xulMenu;
        ext.setVariableSpace( databaseMeta );
      }
    }
  }

  public java.util.List<DatabaseMeta> getDatabases() {
    return databases;
  }

  public void setDatabases( java.util.List<DatabaseMeta> databases ) {
    this.databases = databases;
  }

  @Override
  protected void showMessage( String message, boolean scroll ) {
    Shell parent = getShell();

    ShowMessageDialog msgDialog =
      new ShowMessageDialog( parent, SWT.ICON_INFORMATION | SWT.OK, BaseMessages.getString(
        PKG, "DatabaseDialog.DatabaseConnectionTest.title" ), message, scroll );
    msgDialog.open();
  }

  @Override
  protected void showMessage( DatabaseTestResults databaseTestResults ) {
    String title = "";
    String message = "";

    if ( databaseTestResults.isSuccess() ) {
      title = BaseMessages.getString( PKG, "DatabaseDialog.DatabaseConnectionTestSuccess.title" );
      message = databaseTestResults.getMessage();

      if ( message.contains( Const.CR ) ) {
        message = message.substring( 0, message.indexOf( Const.CR ) ) + Const.CR + message.substring( message.indexOf( Const.CR ) );
        message = message.substring( 0, message.lastIndexOf( Const.CR ) );
      }

      ShowMessageDialog msgDialog = new ShowMessageDialog( getShell(), SWT.ICON_INFORMATION | SWT.OK, title, message, message.length() > 300 );
      msgDialog.setType( Const.SHOW_MESSAGE_DIALOG_DB_TEST_SUCCESS );
      msgDialog.open();
    } else {
      Exception exception = databaseTestResults.getException();

      title = BaseMessages.getString( PKG, "DatabaseDialog.DatabaseConnectionTest.title" );
      message = exception != null ? exception.getMessage() : "";

      if ( message.contains( Const.CR ) ) {
        message = message.trim().split( Const.CR )[0];
      }

      new ErrorDialog( getCenteredShell( getShell() ), title, message, exception );
    }
  }

  private Shell getCenteredShell( Shell shell ) {
    Rectangle shellBounds = shell.getBounds();
    Monitor monitor = shell.getDisplay().getPrimaryMonitor();

    if ( shell.getParent() != null ) {
      monitor = shell.getParent().getMonitor();
    }

    Rectangle monitorClientArea = monitor.getClientArea();

    int middleX = monitorClientArea.x + ( monitorClientArea.width - shellBounds.width ) / 2;
    int middleY = monitorClientArea.y + ( monitorClientArea.height - shellBounds.height ) / 2;

    shell.setLocation( middleX, middleY );

    return shell;
  }
}
