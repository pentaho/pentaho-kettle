/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2021 by Hitachi Vantara : http://www.pentaho.com
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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.RepositoryOperation;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.RepositorySecurityUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.database.DatabaseConnectionDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.containers.XulTree;

public class XulDatabaseDialog {

  private static Class<?> PKG = XulDatabaseDialog.class; // for i18n purposes, needed by Translator2!!

  private DatabaseMeta databaseMeta;

  /**
   * The original objectId of the databaseMeta before it was edited, possibly null.
   */
  private ObjectId databaseMetaObjectId;

  protected Shell shell;

  private Shell parentShell;

  private String databaseName;

  private java.util.List<DatabaseMeta> databases;

  private boolean modalDialog;

  DataOverrideHandler dataHandler = null;

  private LogChannelInterface log;

  private static final String EVENT_ID = "dataHandler";

  private static final String MESSAGES = "org.pentaho.di.ui.core.database.dialog.messages.messages";

  private static final String DIALOG_FRAGMENT_FILE = "/feature_override.xul";

  private static final String FRAGMENT_ID = "test-button-box";

  private static final String EXTENDED_WIDGET_CLASSNAME = "org.pentaho.di.ui.core.database.dialog.tags.ExtTextbox";

  private static final String PMD_WIDGET_CLASSNAME = "org.pentaho.ui.xul.swt.tags.SwtTextbox";

  private static final String EXTENDED_WIDGET_ID = "VARIABLETEXTBOX";

  private static final String EXTENDED_MENULIST_WIDGET_ID = "VARIABLEMENULIST";

  private static final String PMD_MENULIST_WIDGET_CLASSNAME = "org.pentaho.ui.xul.jface.tags.JfaceMenuList";

  private static final String EXTENDED_MENULIST_WIDGET_CLASSNAME = "org.pentaho.di.ui.core.database.dialog.tags.ExtMenuList";

  private DatabaseConnectionDialog databaseDialogInstance;

  private XulDialog xulDialogComponent;

  public XulDatabaseDialog( Shell parent ) {

    parentShell = parent;
    databases = null;

    log = new LogChannel( "XulDatabaseDialog" );
  }

  /**
   * Opens the XUL database dialog
   *
   * @return databaseName (or NULL on error or cancel) TODO: Fix deprecation warning in v3.2 by using the new dialog
   */
  public String open() {
    createDialog();

    try {

      // PDI-5088 clear previous options selections since we are re-using the dialog
      XulTree tree =
        (XulTree) xulDialogComponent.getDocument().getRootElement().getElementById( "options-parameter-tree" );
      tree.getRootChildren().removeAll();

      dataHandler.setData( databaseMeta );
      xulDialogComponent.show(); // Attention: onload: loadConnectionData() is called here the second time, see above
                                 // for first time
      // caught with a HACK in DataHandler.loadConnectionData()

      databaseMeta = (DatabaseMeta) dataHandler.getData();

      // keep the original id
      if ( databaseMeta != null ) {
        databaseMeta.setObjectId( databaseMetaObjectId );
        databaseName = Utils.isEmpty( databaseMeta.getName() ) ? null : databaseMeta.getName();
      } else {
        databaseName = null;
      }

    } catch ( Exception e ) {
      new ErrorDialog( parentShell, BaseMessages.getString( PKG, "XulDatabaseDialog.Error.Title" ), BaseMessages
        .getString( PKG, "XulDatabaseDialog.Error.Dialog" ), e );
      return null;
    }
    return databaseName;
  }

  @SuppressWarnings( "deprecation" )
  private void createDialog() {
    XulDomContainer container = null;
    try {
      databaseDialogInstance = new DatabaseConnectionDialog();
      if ( ( (Shell) this.parentShell ).getText().contains( "Metadata Editor" ) ) {
        databaseDialogInstance.registerClass( EXTENDED_WIDGET_ID, PMD_WIDGET_CLASSNAME );
        databaseDialogInstance.registerClass( EXTENDED_MENULIST_WIDGET_ID, PMD_MENULIST_WIDGET_CLASSNAME );
      } else {
        databaseDialogInstance.registerClass( EXTENDED_WIDGET_ID, EXTENDED_WIDGET_CLASSNAME );
        databaseDialogInstance.registerClass( EXTENDED_MENULIST_WIDGET_ID, EXTENDED_MENULIST_WIDGET_CLASSNAME );
      }
      /*
       * Attention: onload: loadConnectionData() is called here the first time, see below for second time
       */
      container = databaseDialogInstance.getSwtInstance( new KettleXulLoader(), parentShell );

      container.addEventHandler( EVENT_ID, DataOverrideHandler.class.getName() );

      SpoonPluginManager.getInstance().applyPluginsForContainer( "connection_dialog", container );

      dataHandler = (DataOverrideHandler) container.getEventHandler( EVENT_ID );
      if ( databaseMeta != null ) {
        dataHandler.setData( databaseMeta );
      }
      dataHandler.setDatabases( databases );
      dataHandler.getControls();

    } catch ( XulException e ) {
      new ErrorDialog( parentShell, BaseMessages.getString( PKG, "XulDatabaseDialog.Error.Title" ), BaseMessages
        .getString( PKG, "XulDatabaseDialog.Error.HandleXul" ), e );
      return;
    }

    try {
      // Inject the button panel that contains the "Feature List" and "Explore" buttons

      XulComponent boxElement = container.getDocumentRoot().getElementById( FRAGMENT_ID );
      XulComponent parentElement = boxElement.getParent();

      ResourceBundle res = null;
      try {
        res = GlobalMessages.getBundle( MESSAGES );
      } catch ( MissingResourceException e ) {
        log.logError(
          BaseMessages.getString( PKG, "XulDatabaseDialog.Error.ResourcesNotFound.Title" ), e.getMessage(), e );
      }

      XulDomContainer fragmentContainer = null;
      String pkg = getClass().getPackage().getName().replace( '.', '/' );

      // Kludge: paths of execution do not account for a null resourcebundle gracefully, need
      // to check for it here.
      if ( res != null ) {
        fragmentContainer = container.loadFragment( pkg.concat( DIALOG_FRAGMENT_FILE ), res );
      } else {
        fragmentContainer = container.loadFragment( pkg.concat( DIALOG_FRAGMENT_FILE ) );
      }

      XulComponent newBox = fragmentContainer.getDocumentRoot().getFirstChild();
      parentElement.replaceChild( boxElement, newBox );

    } catch ( Exception e ) {
      new ErrorDialog( parentShell, BaseMessages.getString( PKG, "XulDatabaseDialog.Error.Title" ), BaseMessages
        .getString( PKG, "XulDatabaseDialog.Error.HandleXul" ), e );
      return;
    }

    try {
      xulDialogComponent = (XulDialog) container.getDocumentRoot().getRootElement();

      parentShell.addDisposeListener( new DisposeListener() {

        public void widgetDisposed( DisposeEvent arg0 ) {
          xulDialogComponent.hide();
        }

      } );

    } catch ( Exception e ) {
      new ErrorDialog( parentShell, BaseMessages.getString( PKG, "XulDatabaseDialog.Error.Title" ), BaseMessages
        .getString( PKG, "XulDatabaseDialog.Error.Dialog" ), e );
      return;
    }
  }

  public void setDatabaseMeta( DatabaseMeta dbMeta ) {
    if ( ( Spoon.getInstance() != null )
      && RepositorySecurityUI.verifyOperations( parentShell, Spoon.getInstance().getRepository(), RepositoryOperation.MODIFY_DATABASE ) ) {
      return;
    }

    databaseMeta = dbMeta;
    if ( dbMeta != null ) {
      databaseMetaObjectId = databaseMeta.getObjectId();
      databaseName = databaseMeta.getDisplayName();
    }
  }

  public DatabaseMeta getDatabaseMeta() {
    return databaseMeta;
  }

  public void setDatabases( java.util.List<DatabaseMeta> databases ) {
    this.databases = databases;
  }

  /**
   * @return the modalDialog
   */
  public boolean isModalDialog() {
    return modalDialog;
  }

  /**
   * @param modalDialog
   *          the modalDialog to set
   */
  public void setModalDialog( boolean modalDialog ) {
    this.modalDialog = modalDialog;
  }

}
