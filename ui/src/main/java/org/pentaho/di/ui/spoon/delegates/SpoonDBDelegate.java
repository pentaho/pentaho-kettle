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

package org.pentaho.di.ui.spoon.delegates;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.NotePadMeta;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.UndoInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.shared.DatabaseManagementInterface;
import org.pentaho.di.trans.HasDatabasesInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.steps.selectvalues.SelectMetadataChange;
import org.pentaho.di.trans.steps.selectvalues.SelectValuesMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.database.dialog.DatabaseExplorerDialog;
import org.pentaho.di.ui.core.database.dialog.SQLEditor;
import org.pentaho.di.ui.core.database.wizard.CreateDatabaseWizard;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.SQLStatementsDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.TreeUtil;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.dialog.GetJobSQLProgressDialog;
import org.pentaho.di.ui.spoon.dialog.GetSQLProgressDialog;
import org.pentaho.di.ui.spoon.tree.provider.DBConnectionFolderProvider;

public class SpoonDBDelegate extends SpoonDelegate {
  private static Class<?> PKG = Spoon.class; // for i18n purposes, needed by Translator2!!
  private DatabaseDialog databaseDialog;

  public SpoonDBDelegate( Spoon spoon ) {
    super( spoon );
  }

  public void sqlConnection( DatabaseMeta databaseMeta ) {
    SQLEditor sql =
      new SQLEditor( databaseMeta, spoon.getShell(), SWT.NONE, databaseMeta, DBCache.getInstance(), "" );
    sql.open();
  }

  public void editConnection( DatabaseManagementInterface dbManager, DatabaseMeta databaseMeta ) {
    String originalName = databaseMeta.getName();
    getDatabaseDialog().setDatabaseMeta( databaseMeta );
    try {
      getDatabaseDialog().setDatabases( dbManager.getAll() );
      String newname = getDatabaseDialog().open();
      if ( !Utils.isEmpty( newname ) ) { // null: CANCEL
        databaseMeta.setName( originalName );

        databaseMeta = getDatabaseDialog().getDatabaseMeta();
        databaseMeta.setName( newname.trim() );
        databaseMeta.setDisplayName( newname.trim() );
        if ( !newname.equals( originalName ) ) {
          dbManager.remove( originalName );
          spoon.refreshDbConnection( newname.trim() );
        }

        dbManager.add( databaseMeta );
        spoon.refreshDbConnection( originalName );
        refreshTree();
      }
      spoon.setShellText();
    } catch ( Exception e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message" ), new KettleException( e.getMessage(), e ) );
    }
  }

  private DatabaseDialog getDatabaseDialog() {
    if ( databaseDialog != null ) {
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog( spoon.getShell() );
    return databaseDialog;
  }

  public void dupeConnection( DatabaseMeta databaseMeta, DatabaseManagementInterface dbManager ) {
    try {
      List<DatabaseMeta> databaseMetas = dbManager.getAll();
      String originalName = databaseMeta.getName();
      Set<String> dbNames = getDatabaseNames( databaseMetas );

      //Clone the databaseMeta
      DatabaseMeta databaseMetaCopy = (DatabaseMeta) databaseMeta.clone();
      String newName = TreeUtil.findUniqueSuffix( originalName, dbNames );
      databaseMetaCopy.setName( newName );
      databaseMetaCopy.setDisplayName( newName );

      getDatabaseDialog().setDatabaseMeta( databaseMetaCopy );

      getDatabaseDialog().setDatabases( dbManager.getAll() );
      String selectedName = getDatabaseDialog().open();
      if ( !Utils.isEmpty( selectedName ) ) { // null: CANCEL
        selectedName = selectedName.trim();
        // check if the selectedName already exist
        if ( selectedName.equals( originalName )
            && databaseMeta.findDatabase( dbManager.getAll(), selectedName ) != null ) {
          databaseMetaCopy.setName( selectedName );
          DatabaseDialog.showDatabaseExistsDialog( spoon.getShell(), databaseMetaCopy );
          return;
        }
        databaseMetaCopy.setName( selectedName );
        databaseMetaCopy.setDisplayName( selectedName );
        dbManager.add( databaseMetaCopy );
        if ( !selectedName.equals( originalName ) ) {
          spoon.refreshDbConnection( selectedName );
        }
        refreshTree();
      }
    } catch ( Exception ex ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message" ), new KettleException( ex.getMessage(), ex ) );
    }

  }

  private Set<String> getDatabaseNames( List<DatabaseMeta> dbMetas ) {
    return dbMetas.stream().map( DatabaseMeta::getName ).collect( Collectors.toSet() );
  }

  public void clipConnection( DatabaseMeta databaseMeta ) {
    String xml = XMLHandler.getXMLHeader() + databaseMeta.getXML();
    GUIResource.getInstance().toClipboard( xml );
  }

  /**
   * Delete a database connection
   *
   * @param name
   *          The name of the database connection.
   */
  public void delConnection( DatabaseManagementInterface dbMgr, DatabaseMeta db ) {
    UndoInterface undoInterface = spoon.getActiveUndoInterface();
    //TODO UNDO . No clue what the int should be.
    //if ( undoInterface != null ) {
    //  spoon.addUndoDelete(
    //      undoInterface, new DatabaseMeta[] { (DatabaseMeta) db.clone() }, new int[] { pos } );
    //}
    try {
      dbMgr.remove( db );
    } catch ( Exception e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.UnexpectedError.Message" ), new KettleException( e.getMessage(), e ) );
    }
    DBCache.getInstance().clear( db.getName() ); // remove this from the cache as well.

    spoon.refreshDbConnection( db.getName() );
    refreshTree();
    spoon.setShellText();
  }

  /**
   * return a schema, table combination from the explorer
   *
   * @param databaseMeta
   * @param aLook
   * @return schema [0] and table [1]
   */
  public String[] exploreDB( DatabaseMeta databaseMeta, DatabaseManagementInterface dbManager, boolean aLook ) {
    try {
      List<DatabaseMeta> databases = null;
      List<DatabaseMeta> databaseMetas = dbManager.getAll();

      DatabaseExplorerDialog std =
        new DatabaseExplorerDialog( spoon.getShell(), SWT.NONE, databaseMeta, databases, aLook );
      std.open();
      return new String[] { std.getSchemaName(), std.getTableName() };

    } catch ( Exception ex ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message" ), new KettleException( ex.getMessage(), ex ) );
    }
    return new String[0];
  }

  public void clearDBCache( DatabaseMeta databaseMeta ) {
    if ( databaseMeta != null ) {
      DBCache.getInstance().clear( databaseMeta.getName() );
    } else {
      DBCache.getInstance().clear( null );
    }
  }

  public void moveToGlobal( DatabaseMeta databaseMeta, DatabaseManagementInterface dbManager ) throws KettleException {
    moveCopy( dbManager, DefaultBowl.getInstance().getManager( DatabaseManagementInterface.class ), databaseMeta, true );
  }

  public void moveToProject( DatabaseMeta databaseMeta, DatabaseManagementInterface dbManager ) throws KettleException {
    moveCopy( dbManager, spoon.getBowl().getManager( DatabaseManagementInterface.class ), databaseMeta, true );
  }

  public void copyToGlobal( DatabaseMeta databaseMeta, DatabaseManagementInterface dbManager ) throws KettleException {
    moveCopy( dbManager, DefaultBowl.getInstance().getManager( DatabaseManagementInterface.class ), databaseMeta, false );
  }

  public void copyToProject( DatabaseMeta databaseMeta, DatabaseManagementInterface dbManager ) throws KettleException {
    moveCopy( dbManager, spoon.getBowl().getManager( DatabaseManagementInterface.class ), databaseMeta, false );
  }

  private void moveCopy( DatabaseManagementInterface srcDbManager, DatabaseManagementInterface targetDbManager,
                        DatabaseMeta databaseMeta, boolean deleteFromSource ) {
    try {
      // Check if the connection already exist in target, prompt for override
      if ( targetDbManager.get( databaseMeta.getName() ) != null ) {
        if ( !spoon.overwritePrompt( BaseMessages.getString( PKG, "Spoon.Message.OverwriteConnectionYN", databaseMeta.getName() ),
          BaseMessages.getString( PKG, "Spoon.Message.OverwriteConnection.DontShowAnyMoreMessage" ), Props.STRING_ASK_ABOUT_REPLACING_DATABASES ) ) {

          return;
        }
      }
      // Adding the databaseMeta to target dbManager
      targetDbManager.add( databaseMeta );

      if ( deleteFromSource ) {
        srcDbManager.remove( databaseMeta );
      }
      refreshTree();
    } catch ( Exception ex ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message" ), new KettleException( ex.getMessage(), ex ) );
    }

  }
  public void getSQL() {
    TransMeta transMeta = spoon.getActiveTransformation();
    if ( transMeta != null ) {
      getTransSQL( transMeta );
    }
    JobMeta jobMeta = spoon.getActiveJob();
    if ( jobMeta != null ) {
      getJobSQL( jobMeta );
    }
  }

  /**
   * Get & show the SQL required to run the loaded transformation...
   *
   */
  public void getTransSQL( TransMeta transMeta ) {
    GetSQLProgressDialog pspd = new GetSQLProgressDialog( spoon.getShell(), transMeta );
    List<SQLStatement> stats = pspd.open();
    if ( stats != null ) {
      // null means error, but we already displayed the error

      if ( stats.size() > 0 ) {
        SQLStatementsDialog ssd =
          new SQLStatementsDialog( spoon.getShell(), Variables.getADefaultVariableSpace(), SWT.NONE, stats );
        String sn = ssd.open();

        if ( sn != null ) {
          StepMeta esi = transMeta.findStep( sn );
          if ( esi != null ) {
            spoon.delegates.steps.editStep( transMeta, esi );
          }
        }
      } else {
        MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.NoSQLNeedEexecuted.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.NoSQLNeedEexecuted.Title" ) ); // "SQL"
        mb.open();
      }
    }
  }

  /**
   * Get & show the SQL required to run the loaded job entry...
   *
   */
  public void getJobSQL( JobMeta jobMeta ) {
    GetJobSQLProgressDialog pspd = new GetJobSQLProgressDialog( spoon.getShell(), jobMeta, spoon.getRepository() );
    List<SQLStatement> stats = pspd.open();
    if ( stats != null ) {
      // null means error, but we already displayed the error

      if ( stats.size() > 0 ) {
        SQLStatementsDialog ssd = new SQLStatementsDialog( spoon.getShell(), jobMeta, SWT.NONE, stats );
        ssd.open();
      } else {
        MessageBox mb = new MessageBox( spoon.getShell(), SWT.OK | SWT.ICON_INFORMATION );
        mb.setMessage( BaseMessages.getString( PKG, "Spoon.Dialog.JobNoSQLNeedEexecuted.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "Spoon.Dialog.JobNoSQLNeedEexecuted.Title" ) );
        mb.open();
      }
    }
  }

  public boolean copyTable( DatabaseMeta sourceDBInfo, DatabaseMeta targetDBInfo, String tablename ) {
    try {
      //
      // Create a new transformation...
      //
      TransMeta meta = new TransMeta();
      meta.getDatabaseManagementInterface().add( sourceDBInfo );
      meta.getDatabaseManagementInterface().add( targetDBInfo );

      //
      // Add a note
      //
      String note =
        BaseMessages.getString( PKG, "Spoon.Message.Note.ReadInformationFromTableOnDB", tablename, sourceDBInfo
          .getDatabaseName() )
          + Const.CR; // "Reads information from table ["+tablename+"]
      // on database ["+sourceDBInfo+"]"
      note +=
        BaseMessages.getString( PKG, "Spoon.Message.Note.WriteInformationToTableOnDB", tablename, targetDBInfo
          .getDatabaseName() ); // "After that, it writes
      // the information to table
      // ["+tablename+"] on
      // database
      // ["+targetDBInfo+"]"
      NotePadMeta ni = new NotePadMeta( note, 150, 10, -1, -1 );
      meta.addNote( ni );

      //
      // create the source step...
      //
      String fromstepname = BaseMessages.getString( PKG, "Spoon.Message.Note.ReadFromTable", tablename ); // "read
      // from
      // ["+tablename+"]";
      TableInputMeta tii = new TableInputMeta();
      tii.setDatabaseMeta( sourceDBInfo );
      tii.setSQL( "SELECT * FROM " + tablename );

      PluginRegistry registry = PluginRegistry.getInstance();

      String fromstepid = registry.getPluginId( StepPluginType.class, tii );
      StepMeta fromstep = new StepMeta( fromstepid, fromstepname, tii );
      fromstep.setLocation( 150, 100 );
      fromstep.setDraw( true );
      fromstep.setDescription( BaseMessages.getString(
        PKG, "Spoon.Message.Note.ReadInformationFromTableOnDB", tablename, sourceDBInfo.getDatabaseName() ) );
      meta.addStep( fromstep );

      //
      // add logic to rename fields in case any of the field names contain
      // reserved words...
      // Use metadata logic in SelectValues, use SelectValueInfo...
      //
      Database sourceDB = new Database( loggingObject, sourceDBInfo );
      sourceDB.shareVariablesWith( meta );
      sourceDB.connect();
      try {
        // Get the fields for the input table...
        RowMetaInterface fields = sourceDB.getTableFields( tablename );

        // See if we need to deal with reserved words...
        int nrReserved = targetDBInfo.getNrReservedWords( fields );
        if ( nrReserved > 0 ) {
          SelectValuesMeta svi = new SelectValuesMeta();
          svi.allocate( 0, 0, nrReserved );
          int nr = 0;
          //CHECKSTYLE:Indentation:OFF
          for ( int i = 0; i < fields.size(); i++ ) {
            ValueMetaInterface v = fields.getValueMeta( i );
            if ( targetDBInfo.isReservedWord( v.getName() ) ) {
              if ( svi.getMeta()[nr] == null ) {
                svi.getMeta()[nr] = new SelectMetadataChange( svi );
              }
              svi.getMeta()[nr].setName( v.getName() );
              svi.getMeta()[nr].setRename( targetDBInfo.quoteField( v.getName() ) );
              nr++;
            }
          }

          String selstepname = BaseMessages.getString( PKG, "Spoon.Message.Note.HandleReservedWords" );
          String selstepid = registry.getPluginId( StepPluginType.class, svi );
          StepMeta selstep = new StepMeta( selstepid, selstepname, svi );
          selstep.setLocation( 350, 100 );
          selstep.setDraw( true );
          selstep.setDescription( BaseMessages.getString(
            PKG, "Spoon.Message.Note.RenamesReservedWords", targetDBInfo.getPluginId() ) ); //
          meta.addStep( selstep );

          TransHopMeta shi = new TransHopMeta( fromstep, selstep );
          meta.addTransHop( shi );
          fromstep = selstep;
        }

        //
        // Create the target step...
        //
        //
        // Add the TableOutputMeta step...
        //
        String tostepname = BaseMessages.getString( PKG, "Spoon.Message.Note.WriteToTable", tablename );
        TableOutputMeta toi = new TableOutputMeta();
        toi.setDatabaseMeta( targetDBInfo );
        toi.setTableName( tablename );
        toi.setCommitSize( 200 );
        toi.setTruncateTable( true );

        String tostepid = registry.getPluginId( StepPluginType.class, toi );
        StepMeta tostep = new StepMeta( tostepid, tostepname, toi );
        tostep.setLocation( 550, 100 );
        tostep.setDraw( true );
        tostep.setDescription( BaseMessages.getString(
          PKG, "Spoon.Message.Note.WriteInformationToTableOnDB2", tablename, targetDBInfo.getDatabaseName() ) );
        meta.addStep( tostep );

        //
        // Add a hop between the two steps...
        //
        TransHopMeta hi = new TransHopMeta( fromstep, tostep );
        meta.addTransHop( hi );

        // OK, if we're still here: overwrite the current transformation...
        // Set a name on this generated transformation
        //
        String name = "Copy table from [" + sourceDBInfo.getName() + "] to [" + targetDBInfo.getName() + "]";
        String transName = name;
        int nr = 1;
        if ( spoon.delegates.trans.getTransformation( transName ) != null ) {
          nr++;
          transName = name + " " + nr;
        }
        meta.setName( transName );
        spoon.delegates.trans.addTransGraph( meta );

        spoon.refreshGraph();
        refreshTree();
      } finally {
        sourceDB.disconnect();
      }
    } catch ( Exception e ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ), BaseMessages
          .getString( PKG, "Spoon.Dialog.UnexpectedError.Message" ), new KettleException( e.getMessage(), e ) );
      return false;
    }
    return true;
  }

  public void newConnection( ) {
    DatabaseMeta databaseMeta = new DatabaseMeta();
    getDatabaseDialog().setDatabaseMeta( databaseMeta );
    try {
      DatabaseManagementInterface databaseManagementInterface = spoon.getBowl().getManager( DatabaseManagementInterface.class );
      getDatabaseDialog().setDatabases( databaseManagementInterface.getAll() );
      String con_name = getDatabaseDialog().open();
      if ( !Utils.isEmpty( con_name ) ) {
        con_name = con_name.trim();
        databaseMeta.setName( con_name );
        databaseMeta.setDisplayName( con_name );
        databaseMeta = getDatabaseDialog().getDatabaseMeta();

        if ( databaseMeta.findDatabase( databaseManagementInterface.getAll(), con_name ) == null ) {
          databaseManagementInterface.add( databaseMeta );
          spoon.refreshDbConnection( con_name );
          refreshTree();
        } else {
          DatabaseDialog.showDatabaseExistsDialog( spoon.getShell(), databaseMeta );
        }
      }
    } catch ( KettleException exception ) {
      new ErrorDialog( spoon.getShell(),
                       BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingConnection.Title" ),
                       BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingConnection.Message", databaseMeta.getName() ), exception );
    }
  }

  public void newConnection( Optional<VariableSpace> varspace, DatabaseManagementInterface dbMgr ) {
    final DatabaseMeta databaseMeta = new DatabaseMeta();
    varspace.ifPresentOrElse( v -> databaseMeta.shareVariablesWith( v ),
      () -> databaseMeta.initializeVariablesFrom( null ) );

    getDatabaseDialog().setDatabaseMeta( databaseMeta );
    String con_name = getDatabaseDialog().open();
    if ( !Utils.isEmpty( con_name ) ) {
      con_name = con_name.trim();
      DatabaseMeta newDatabaseMeta = getDatabaseDialog().getDatabaseMeta();

      try {
        if ( dbMgr.get( con_name ) == null ) {
          dbMgr.add( newDatabaseMeta );
          // TODO UNDO. No clue what the int should be
          //spoon.addUndoNew( (UndoInterface) meta, new DatabaseMeta[]{(DatabaseMeta) newDatabaseMeta
          //        .clone()}, new int[]{hasDatabasesInterface.indexOfDatabase( newDatabaseMeta )} );
          spoon.refreshDbConnection( con_name );
          refreshTree();
        } else {
          DatabaseDialog.showDatabaseExistsDialog( spoon.getShell(), newDatabaseMeta );
        }
      } catch ( KettleException exception ) {
        new ErrorDialog( spoon.getShell(),
          BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingConnection.Title" ),
          BaseMessages.getString( PKG, "Spoon.Dialog.ErrorSavingConnection.Message", newDatabaseMeta.getName() ), exception );
      }
    }
  }

  public void createDatabaseWizard( ) {
    DatabaseManagementInterface dbManager = null;
    try {
      dbManager = spoon.getBowl().getManager( DatabaseManagementInterface.class );
      CreateDatabaseWizard cdw = new CreateDatabaseWizard();
      DatabaseMeta newDdatabaseMeta = cdw.createAndRunDatabaseWizard( spoon.getShell(), PropsUI.getInstance(), dbManager.getAll() );
      if ( newDdatabaseMeta != null ) { // finished
         dbManager.add( newDdatabaseMeta );
      }
      refreshTree();
    } catch ( Exception ex ) {
      new ErrorDialog(
        spoon.getShell(), BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedError.Title" ),
        BaseMessages.getString( PKG, "Spoon.Dialog.UnexpectedDbError.Message" ), new KettleException( ex.getMessage(), ex ) );
    }
  }
  private void refreshTree() {
    spoon.refreshTree( DBConnectionFolderProvider.STRING_CONNECTIONS );
  }
}
