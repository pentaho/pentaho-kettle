/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2019 by Hitachi Vantara : http://www.pentaho.com
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

import java.io.File;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.spoon.SpoonPluginManager;
import org.pentaho.di.ui.spoon.XulSpoonSettingsManager;
import org.pentaho.di.ui.xul.KettleXulLoader;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulRunner;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.DefaultSettingsManager;
import org.pentaho.ui.xul.swt.SwtXulRunner;

public class XulDatabaseExplorerDialog {

  private static final Class<?> PKG = XulDatabaseExplorerDialog.class;

  private Shell shell;
  private XulDomContainer container;
  private XulRunner runner;
  private XulDatabaseExplorerController controller;
  private DatabaseMeta databaseMeta;
  private List<DatabaseMeta> databases;
  private static final String XUL = "org/pentaho/di/ui/core/database/dialog/database_explorer.xul";
  private boolean look;
  private String schemaName;
  private String selectedTable;

  public XulDatabaseExplorerDialog( Shell aShell, DatabaseMeta aDatabaseMeta, List<DatabaseMeta> aDataBases,
    boolean aLook ) {
    this.shell = aShell;
    this.databaseMeta = aDatabaseMeta;
    this.databases = aDataBases;
    this.look = aLook;
  }

  public boolean open() {
    try {

      KettleXulLoader theLoader = new KettleXulLoader();
      theLoader.setSettingsManager( XulSpoonSettingsManager.getInstance() );
      theLoader.setSettingsManager( new DefaultSettingsManager( new File( Const.getKettleDirectory()
        + Const.FILE_SEPARATOR + "xulSettings.properties" ) ) );
      theLoader.setOuterContext( this.shell );

      this.container = theLoader.loadXul( XUL, new XulDatabaseExplorerResourceBundle() );

      XulDialog theExplorerDialog =
        (XulDialog) this.container.getDocumentRoot().getElementById( "databaseExplorerDialog" );

      SpoonPluginManager.getInstance().applyPluginsForContainer( "database_dialog", container );

      this.controller =
        new XulDatabaseExplorerController(
          this.shell, this.databaseMeta, this.databases, look );

      this.container.addEventHandler( this.controller );

      this.runner = new SwtXulRunner();
      this.runner.addContainer( this.container );

      this.runner.initialize();

      this.controller.setSelectedSchemaAndTable( schemaName, selectedTable );

      // show dialog if connection is success only.
      if ( controller.getActionStatus() == UiPostActionStatus.OK ) {
        theExplorerDialog.show();
      }

    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Error exploring database", e );
    }
    return this.controller.getSelectedTable() != null;
  }

  public void setSelectedSchemaAndTable( String aSchema, String aTable ) {
    schemaName = aSchema;
    selectedTable = aTable;
  }

  public String getSchemaName() {
    return ( this.controller != null ) ? this.controller.getSelectedSchema() : schemaName;
  }

  public String getTableName() {
    return ( this.controller != null ) ? this.controller.getSelectedTable() : selectedTable;
  }

  private static class XulDatabaseExplorerResourceBundle extends ResourceBundle {
    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( PKG, key );
    }
  }
}
