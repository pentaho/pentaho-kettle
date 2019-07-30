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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.DBCache;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.DatabaseMetaInformation;
import org.pentaho.di.core.database.Schema;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LoggingObject;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransProfileFactory;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.dialog.EnterTextDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.dialog.PreviewRowsDialog;
import org.pentaho.di.ui.core.dialog.StepFieldsDialog;
import org.pentaho.di.ui.trans.dialog.TransPreviewProgressDialog;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.tags.SwtButton;
import org.pentaho.ui.xul.swt.tags.SwtDialog;
import org.pentaho.ui.xul.util.XulDialogCallback;

public class XulDatabaseExplorerController extends AbstractXulEventHandler implements IUiActionStatus {

  private static final Class<?> PKG = XulDatabaseExplorerController.class;

  private XulDatabaseExplorerModel model;
  private Binding databaseTreeBinding;
  private Binding selectedTableBinding;
  // private Binding selectedSchemaBinding;
  private XulTree databaseTree;
  private XulButton expandCollapseButton;
  private BindingFactory bf;
  protected Shell shell;
  private SwtDialog dbExplorerDialog;
  private DBCache dbcache;
  private List<DatabaseMeta> databases;
  private boolean isExpanded;
  private boolean isJustLook;

  private UiPostActionStatus status = UiPostActionStatus.NONE;

  private static final String DATABASE_IMAGE = "ui/images/folder_connection.svg";
  private static final String FOLDER_IMAGE = "ui/images/BOL.svg";
  private static final String SCHEMA_IMAGE = "ui/images/schema.svg";
  private static final String TABLE_IMAGE = "ui/images/table.svg";
  private static final String EXPAND_ALL_IMAGE = "ui/images/ExpandAll.svg";
  private static final String COLLAPSE_ALL_IMAGE = "ui/images/CollapseAll.svg";

  private static final String STRING_SCHEMAS = BaseMessages
    .getString( PKG, "DatabaseExplorerDialog.Schemas.Label" );
  private static final String STRING_TABLES = BaseMessages.getString( PKG, "DatabaseExplorerDialog.Tables.Label" );
  private static final String STRING_VIEWS = BaseMessages.getString( PKG, "DatabaseExplorerDialog.Views.Label" );
  private static final String STRING_SYNONYMS = BaseMessages.getString(
    PKG, "DatabaseExplorerDialog.Synonyms.Label" );

  public XulDatabaseExplorerController( Shell shell, DatabaseMeta databaseMeta, List<DatabaseMeta> databases,
    boolean aLook ) {
    this.model = new XulDatabaseExplorerModel( databaseMeta );
    this.shell = shell;
    this.bf = new DefaultBindingFactory();
    this.databases = databases;
    this.dbcache = DBCache.getInstance();
    this.isJustLook = aLook;
  }

  public void init() {

    SwtButton theAcceptButton = (SwtButton) this.document.getElementById( "databaseExplorerDialog_accept" );
    SwtButton theCancelButton = (SwtButton) this.document.getElementById( "databaseExplorerDialog_cancel" );
    if ( this.isJustLook ) {
      theAcceptButton.setVisible( false );
      theCancelButton.setLabel( BaseMessages.getString( getClass(), "DatabaseExplorer.Button.Ok" ) );
      theAcceptButton.setDisabled( false );

    } else {
      theAcceptButton.setLabel( BaseMessages.getString( getClass(), "DatabaseExplorer.Button.Ok" ) );
      theCancelButton.setLabel( BaseMessages.getString( getClass(), "DatabaseExplorer.Button.Cancel" ) );
      theAcceptButton.setDisabled( true );
    }

    this.dbExplorerDialog = (SwtDialog) this.document.getElementById( "databaseExplorerDialog" );

    createDatabaseNodes( shell );
    if ( this.status != UiPostActionStatus.OK ) {
      // something goes dramatically wrong!
      return;
    }

    this.bf.setDocument( super.document );
    this.bf.setBindingType( Type.ONE_WAY );

    this.expandCollapseButton = (XulButton) document.getElementById( "expandCollapseButton" );
    this.databaseTree = (XulTree) document.getElementById( "databaseTree" );
    this.databaseTreeBinding = bf.createBinding( this.model, "database", this.databaseTree, "elements" );

    bf.createBinding(
      model, "selectedNode", theAcceptButton, "disabled", new BindingConvertor<DatabaseExplorerNode, Boolean>() {

        @Override
        public Boolean sourceToTarget( DatabaseExplorerNode arg0 ) {
          return ( !isJustLook && ( arg0 == null || !arg0.isTable() ) );

        }

        @Override
        public DatabaseExplorerNode targetToSource( Boolean arg0 ) {
          // TODO Auto-generated method stub
          return null;
        }

      } );

    bf.setBindingType( Binding.Type.BI_DIRECTIONAL );
    this.bf.createBinding(
      this.databaseTree, "selectedItems", this.model, "selectedNode",
      new BindingConvertor<List<DatabaseExplorerNode>, DatabaseExplorerNode>() {

        @Override
        public DatabaseExplorerNode sourceToTarget( List<DatabaseExplorerNode> arg0 ) {
          if ( arg0 == null || arg0.size() == 0 ) {
            return null;
          }
          return arg0.get( 0 );
        }

        @Override
        public List<DatabaseExplorerNode> targetToSource( DatabaseExplorerNode arg0 ) {
          return Collections.singletonList( arg0 );
        }

      } );

    BindingConvertor<DatabaseExplorerNode, Boolean> isDisabledConvertor =
      new BindingConvertor<DatabaseExplorerNode, Boolean>() {
        public Boolean sourceToTarget( DatabaseExplorerNode value ) {
          return !( value != null && value.isTable() );
        }

        public DatabaseExplorerNode targetToSource( Boolean value ) {
          return null;
        }
      };
    bf.setBindingType( Binding.Type.ONE_WAY );
    this.bf.createBinding( this.databaseTree, "selectedItem", "buttonMenuPopUp", "disabled", isDisabledConvertor );
    this.bf.createBinding(
      this.databaseTree, "selectedItem", "buttonMenuPopUpImg", "disabled", isDisabledConvertor );
    this.bf.createBinding( this.databaseTree, "selectedItem", "action_popup", "disabled", isDisabledConvertor );
    fireBindings();
  }

  public void setSelectedSchemaAndTable( String aSchema, String aTable ) throws KettleDatabaseException {
    this.model.setSelectedNode( model.findBy( aSchema, aTable ) );
  }

  public String getSelectedTable() {
    return model.getTable();
  }

  public DatabaseMeta getDatabaseMeta() {
    return this.model.getDatabaseMeta();
  }

  public String getSelectedSchema() {
    return model.getSchema();
  }

  public void accept() {
    if ( this.model.getTable() != null ) {
      this.dbExplorerDialog.setVisible( false );
    }
  }

  public void cancel() {
    this.model.setSelectedNode( null );
    this.dbExplorerDialog.setVisible( false );
  }

  public void truncate() {
    if ( this.model.getTable() == null ) {
      return;
    }
    DatabaseMeta dm = this.model.getDatabaseMeta();
    String message = dm.getTruncateTableStatement( this.model.getSchema(), this.model.getTable() );
    if ( message == null ) {
      message = "Truncating tables is not supported by " + dm.getDatabaseInterface().getPluginName();
    }
    SQLEditor theSqlEditor =
      new SQLEditor( this.getDatabaseMeta(), this.dbExplorerDialog.getShell(), SWT.NONE, dm, this.dbcache, "-- "
        + message );
    theSqlEditor.open();
  }

  public void viewSql() {
    if ( this.model.getTable() == null ) {
      return;
    }
    SQLEditor theSqlEditor =
      new SQLEditor( this.getDatabaseMeta(), this.dbExplorerDialog.getShell(), SWT.NONE, this.model
        .getDatabaseMeta(), this.dbcache, "SELECT * FROM " + getSchemaAndTable( this.model ) );
    theSqlEditor.open();
  }

  public void showLayout() {

    DatabaseMeta databaseMeta = model.getDatabaseMeta();
    String schemaTable = databaseMeta.getQuotedSchemaTableCombination( model.getSchema(), model.getTable() );

    String theSql = databaseMeta.getSQLQueryFields( schemaTable );
    GetQueryFieldsProgressDialog theProgressDialog =
      new GetQueryFieldsProgressDialog( this.shell, databaseMeta, theSql );
    RowMetaInterface fields = theProgressDialog.open();

    StepFieldsDialog stepFieldsDialog =
        new StepFieldsDialog( this.dbExplorerDialog.getShell(), databaseMeta, SWT.NONE, schemaTable, fields );
    stepFieldsDialog.setShellText( BaseMessages.getString( PKG, "DatabaseExplorerDialog.TableLayout.ShellText" ) );
    stepFieldsDialog
      .setOriginText( BaseMessages.getString( PKG, "DatabaseExplorerDialog.TableLayout.OriginText" ) );
    stepFieldsDialog.setShowEditButton( false );
    stepFieldsDialog.open();
  }

  public void displayRowCount() {
    if ( this.model.getTable() == null ) {
      return;
    }
    try {
      GetTableSizeProgressDialog pd =
        new GetTableSizeProgressDialog(
          this.dbExplorerDialog.getShell(), this.model.getDatabaseMeta(), this.model.getTable(), model
            .getSchema() );
      Long theCount = pd.open();
      if ( theCount != null ) {
        XulMessageBox theMessageBox = (XulMessageBox) document.createElement( "messagebox" );
        theMessageBox.setModalParent( this.dbExplorerDialog.getShell() );
        theMessageBox.setTitle( BaseMessages.getString( PKG, "DatabaseExplorerDialog.TableSize.Title" ) );
        theMessageBox.setMessage( BaseMessages.getString(
          PKG, "DatabaseExplorerDialog.TableSize.Message", this.model.getTable(), theCount.toString() ) );
        theMessageBox.open();
      }
    } catch ( XulException e ) {
      LogChannel.GENERAL.logError( "Error displaying row count", e );
    }
  }

  private void fireBindings() {
    try {
      this.databaseTreeBinding.fireSourceChanged();
      if ( this.getSelectedTable() != null ) {
        this.selectedTableBinding.fireSourceChanged();
      }
      // if (this.getSelectedSchema() != null) {
      // this.selectedSchemaBinding.fireSourceChanged();
      // }
    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Error firing bindings in database explorer", e );
    }
  }

  public String getName() {
    return "dbexplorer";
  }

  public void preview( boolean askLimit ) {
    if ( model.getTable() == null ) {
      return;
    }
    try {
      PromptCallback theCallback = new PromptCallback();
      @SuppressWarnings( "unused" )
      boolean execute = true;
      int limit = 100;
      if ( askLimit ) {
        XulPromptBox thePromptBox = (XulPromptBox) this.document.createElement( "promptbox" );
        thePromptBox.setModalParent( this.dbExplorerDialog.getShell() );
        thePromptBox.setTitle( "Enter Max Rows" );
        thePromptBox.setMessage( "Max Rows:" );
        thePromptBox.addDialogCallback( theCallback );
        thePromptBox.open();
        execute = theCallback.getLimit() != -1;
        limit = theCallback.getLimit();
      }

      // if (execute) {
      // XulPreviewRowsDialog thePreviewRowsDialog = new XulPreviewRowsDialog(this.shell, SWT.NONE,
      // this.model.getDatabaseMeta(), this.model.getTable(), theCallback.getLimit());
      // thePreviewRowsDialog.open();
      // }

      GetPreviewTableProgressDialog pd =
        new GetPreviewTableProgressDialog( this.dbExplorerDialog.getShell(), this.model.getDatabaseMeta(), model
          .getSchema(), model.getTable(), limit );
      List<Object[]> rows = pd.open();
      if ( rows != null ) { // otherwise an already shown error...

        if ( rows.size() > 0 ) {
          PreviewRowsDialog prd =
            new PreviewRowsDialog(
              this.dbExplorerDialog.getShell(), this.model.getDatabaseMeta(), SWT.None, this.model.getTable(),
              pd.getRowMeta(), rows );
          prd.open();
        } else {
          MessageBox mb = new MessageBox( this.dbExplorerDialog.getShell(), SWT.ICON_INFORMATION | SWT.OK );
          mb.setMessage( BaseMessages.getString( PKG, "DatabaseExplorerDialog.NoRows.Message" ) );
          mb.setText( BaseMessages.getString( PKG, "DatabaseExplorerDialog.NoRows.Title" ) );
          mb.open();
        }
      }

    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Error previewing rows", e );
    }
  }

  public void refresh() {
    collapse();
    this.model.getDatabase().clear();
    createDatabaseNodes( this.dbExplorerDialog.getShell() );
    if ( this.status != UiPostActionStatus.OK ) {
      // something goes dramatically wrong!
      return;
    }
    fireBindings();
  }

  /**
   * 
   * @return true if all goes fine, false otherwise. This will signal to caller
   * that it may not attempt to show broken dialog
   */
  void createDatabaseNodes( Shell dialogsParent ) {
    this.status = UiPostActionStatus.NONE;
    Database theDatabase = new Database( null, this.model.getDatabaseMeta() );
    try {
      theDatabase.connect();
      GetDatabaseInfoProgressDialog gdipd =
        new GetDatabaseInfoProgressDialog( dialogsParent, this.model.getDatabaseMeta() );
      DatabaseMetaInformation dmi = gdipd.open();

      // Adds the main database node.
      DatabaseExplorerNode theDatabaseNode = new DatabaseExplorerNode();
      theDatabaseNode.setName( this.model.getDatabaseMeta().getName() );
      theDatabaseNode.setImage( DATABASE_IMAGE );
      this.model.getDatabase().add( theDatabaseNode );

      // Adds the Schema database node.
      DatabaseExplorerNode theSchemasNode = new DatabaseExplorerNode();
      theSchemasNode.setName( STRING_SCHEMAS );
      theSchemasNode.setImage( FOLDER_IMAGE );
      theDatabaseNode.add( theSchemasNode );

      // Adds the Tables database node.
      DatabaseExplorerNode theTablesNode = new DatabaseExplorerNode();
      theTablesNode.setName( STRING_TABLES );
      theTablesNode.setImage( FOLDER_IMAGE );
      theDatabaseNode.add( theTablesNode );

      // Adds the Views database node.
      DatabaseExplorerNode theViewsNode = new DatabaseExplorerNode();
      theViewsNode.setName( STRING_VIEWS );
      theViewsNode.setImage( FOLDER_IMAGE );
      theDatabaseNode.add( theViewsNode );

      // Adds the Synonyms database node.
      DatabaseExplorerNode theSynonymsNode = new DatabaseExplorerNode();
      theSynonymsNode.setName( STRING_SYNONYMS );
      theSynonymsNode.setImage( FOLDER_IMAGE );
      theDatabaseNode.add( theSynonymsNode );

      // Adds the database schemas.
      Schema[] schemas = dmi.getSchemas();
      if ( schemas != null ) {
        DatabaseExplorerNode theSchemaNode = null;
        for ( int i = 0; i < schemas.length; i++ ) {
          theSchemaNode = new DatabaseExplorerNode();
          theSchemaNode.setName( schemas[i].getSchemaName() );
          theSchemaNode.setImage( SCHEMA_IMAGE );
          theSchemaNode.setIsSchema( true );
          theSchemasNode.add( theSchemaNode );

          // Adds the database tables for the given schema.
          String[] theTableNames = schemas[i].getItems();
          if ( theTableNames != null ) {
            for ( int i2 = 0; i2 < theTableNames.length; i2++ ) {
              DatabaseExplorerNode theTableNode = new DatabaseExplorerNode();
              theTableNode.setIsTable( true );
              theTableNode.setSchema( schemas[i].getSchemaName() );
              theTableNode.setName( theTableNames[i2] );
              theTableNode.setImage( TABLE_IMAGE );
              theSchemaNode.add( theTableNode );
              theTableNode.setParent( theSchemaNode );
            }
          }
        }
      }

      // Adds the database tables.
      Map<String, Collection<String>> tableMap = dmi.getTableMap();
      List<String> tableKeys = new ArrayList<String>( tableMap.keySet() );
      Collections.sort( tableKeys );
      for ( String schema : tableKeys ) {
        List<String> tables = new ArrayList<String>( tableMap.get( schema ) );
        Collections.sort( tables );
        for ( String table : tables ) {
          DatabaseExplorerNode theTableNode = new DatabaseExplorerNode();
          theTableNode.setSchema( schema );
          theTableNode.setIsTable( true );
          theTableNode.setName( table );
          theTableNode.setImage( TABLE_IMAGE );
          theTablesNode.add( theTableNode );
        }
      }

      // Adds the database views.
      Map<String, Collection<String>> viewMap = dmi.getViewMap();
      if ( viewMap != null ) {
        List<String> viewKeys = new ArrayList<String>( viewMap.keySet() );
        Collections.sort( viewKeys );
        for ( String schema : viewKeys ) {
          List<String> views = new ArrayList<String>( viewMap.get( schema ) );
          Collections.sort( views );
          for ( String view : views ) {
            DatabaseExplorerNode theViewNode = new DatabaseExplorerNode();
            theViewNode.setIsTable( true );
            theViewNode.setName( view );
            theViewNode.setImage( TABLE_IMAGE );
            theViewsNode.add( theViewNode );
          }
        }
      }

      // Adds the Synonyms.
      Map<String, Collection<String>> synonymMap = dmi.getSynonymMap();
      if ( synonymMap != null ) {
        List<String> synonymKeys = new ArrayList<String>( synonymMap.keySet() );
        Collections.sort( synonymKeys );
        for ( String schema : synonymKeys ) {
          List<String> synonyms = new ArrayList<String>( synonymMap.get( schema ) );
          Collections.sort( synonyms );
          for ( String synonym : synonyms ) {
            DatabaseExplorerNode theSynonymNode = new DatabaseExplorerNode();
            theSynonymNode.setIsTable( true );
            theSynonymNode.setName( synonym );
            theSynonymNode.setImage( TABLE_IMAGE );
            theSynonymsNode.add( theSynonymNode );
          }
        }
      }

    } catch ( Exception e ) {
      // Something goes wrong?
      this.status = UiPostActionStatus.ERROR;
      theDatabase.disconnect();
      new ErrorDialog( dialogsParent, "Error", "Unexpected explorer error:", e );
      this.status = UiPostActionStatus.ERROR_DIALOG_SHOWN;
      return;
    } finally {
      if ( theDatabase != null ) {
        try {
          theDatabase.disconnect();
        } catch ( Exception ignored ) {
          // Can't do anything else here...
        }
        theDatabase = null;
      }
    }
    this.status = UiPostActionStatus.OK;
  }

  public void close() {
    this.dbExplorerDialog.setVisible( false );
  }

  public void expandCollapse() {
    if ( this.isExpanded ) {
      collapse();
    } else {
      expand();
    }
  }

  private void expand() {
    this.databaseTree.expandAll();
    this.isExpanded = true;
    this.expandCollapseButton.setImage( COLLAPSE_ALL_IMAGE );
  }

  private void collapse() {
    this.databaseTree.collapseAll();
    this.isExpanded = false;
    this.expandCollapseButton.setImage( EXPAND_ALL_IMAGE );
  }

  public void getDDL() {
    if ( model.getTable() == null ) {
      return;
    }
    Database db = new Database( null, this.model.getDatabaseMeta() );
    try {
      db.connect();
      String tableName = getSchemaAndTable( this.model );
      RowMetaInterface r = db.getTableFields( tableName );
      String sql = db.getCreateTableStatement( tableName, r, null, false, null, true );
      SQLEditor se =
        new SQLEditor( this.getDatabaseMeta(), this.dbExplorerDialog.getShell(), SWT.NONE, this.model
          .getDatabaseMeta(), this.dbcache, sql );
      se.open();
    } catch ( KettleDatabaseException dbe ) {
      new ErrorDialog(
        this.dbExplorerDialog.getShell(), BaseMessages.getString( PKG, "Dialog.Error.Header" ), BaseMessages
          .getString( PKG, "DatabaseExplorerDialog.Error.RetrieveLayout" ), dbe );
    } finally {
      db.disconnect();
    }
  }

  public void getDDLForOther() {

    if ( databases != null ) {
      try {

        // Now select the other connection...

        // Only take non-SAP ERP connections....
        List<DatabaseMeta> dbs = new ArrayList<DatabaseMeta>();
        for ( int i = 0; i < databases.size(); i++ ) {
          if ( ( ( databases.get( i ) ).getDatabaseInterface().isExplorable() ) ) {
            dbs.add( databases.get( i ) );
          }
        }

        String[] conn = new String[dbs.size()];
        for ( int i = 0; i < conn.length; i++ ) {
          conn[i] = ( dbs.get( i ) ).getName();
        }

        EnterSelectionDialog esd = new EnterSelectionDialog( this.dbExplorerDialog.getShell(), conn,
          BaseMessages.getString( PKG, "DatabaseExplorerDialog.TargetDatabase.Title" ),
          BaseMessages.getString( PKG, "DatabaseExplorerDialog.TargetDatabase.Message" ) );
        String target = esd.open();
        if ( target != null ) {
          DatabaseMeta targetdbi = DatabaseMeta.findDatabase( dbs, target );
          Database targetdb = new Database( null, targetdbi );
          try {
            targetdb.connect();
            String tableName = getSchemaAndTable( model );
            RowMetaInterface r = targetdb.getTableFields( tableName );

            String sql = targetdb.getCreateTableStatement( tableName, r, null, false, null, true );
            SQLEditor se =
              new SQLEditor( this.getDatabaseMeta(), this.dbExplorerDialog.getShell(), SWT.NONE, this.model
                .getDatabaseMeta(), this.dbcache, sql );
            se.open();
          } finally {
            targetdb.disconnect();
          }
        }
      } catch ( KettleDatabaseException dbe ) {
        new ErrorDialog(
          this.dbExplorerDialog.getShell(), BaseMessages.getString( PKG, "Dialog.Error.Header" ), BaseMessages
            .getString( PKG, "DatabaseExplorerDialog.Error.GenDDL" ), dbe );
      }
    } else {
      MessageBox mb = new MessageBox( this.dbExplorerDialog.getShell(), SWT.NONE | SWT.ICON_INFORMATION );
      mb.setMessage( BaseMessages.getString( PKG, "DatabaseExplorerDialog.NoConnectionsKnown.Message" ) );
      mb.setText( BaseMessages.getString( PKG, "DatabaseExplorerDialog.NoConnectionsKnown.Title" ) );
      mb.open();
    }
  }

  public void dataProfile() {
    if ( model.getTable() == null ) {
      return;
    }
    Shell dbShell = (Shell) dbExplorerDialog.getRootObject();
    try {
      TransProfileFactory profileFactory =
        new TransProfileFactory( this.model.getDatabaseMeta(), getSchemaAndTable( this.model ) );
      TransMeta transMeta = profileFactory.generateTransformation( new LoggingObject( model.getTable() ) );
      TransPreviewProgressDialog progressDialog =
        new TransPreviewProgressDialog(
          dbShell, transMeta, new String[] { TransProfileFactory.RESULT_STEP_NAME, }, new int[] { 25000, } );

      progressDialog.open();

      if ( !progressDialog.isCancelled() ) {
        Trans trans = progressDialog.getTrans();
        String loggingText = progressDialog.getLoggingText();

        if ( trans.getResult() != null && trans.getResult().getNrErrors() > 0 ) {
          EnterTextDialog etd =
            new EnterTextDialog(
              dbShell, BaseMessages.getString( PKG, "System.Dialog.PreviewError.Title" ), BaseMessages
                .getString( PKG, "System.Dialog.PreviewError.Message" ), loggingText, true );
          etd.setReadOnly();
          etd.open();
        }

        PreviewRowsDialog prd =
          new PreviewRowsDialog(
            dbShell, transMeta, SWT.NONE, TransProfileFactory.RESULT_STEP_NAME, progressDialog
              .getPreviewRowsMeta( TransProfileFactory.RESULT_STEP_NAME ), progressDialog
              .getPreviewRows( TransProfileFactory.RESULT_STEP_NAME ), loggingText );
        prd.open();

      }

    } catch ( Exception e ) {
      new ErrorDialog( this.dbExplorerDialog.getShell(),
        BaseMessages.getString( PKG, "DatabaseExplorerDialog.UnexpectedProfilingError.Title" ),
        BaseMessages.getString( PKG, "DatabaseExplorerDialog.UnexpectedProfilingError.Message" ), e );
    }

  }

  class PromptCallback implements XulDialogCallback<Object> {

    private int limit = -1;

    public void onClose( XulComponent aSender, Status aReturnCode, Object aRetVal ) {
      if ( aReturnCode == Status.ACCEPT ) {
        try {
          this.limit = Integer.parseInt( aRetVal.toString() );
        } catch ( NumberFormatException e ) {
          LogChannel.GENERAL.logError( "Error parsing string '" + aRetVal.toString() + "'", e );
        }
      }
    }

    public void onError( XulComponent aSenter, Throwable aThrowable ) {
    }

    public int getLimit() {
      return this.limit;
    }
  }

  private String getSchemaAndTable( XulDatabaseExplorerModel model ) {
    return getSchemaAndTable( model, model.getDatabaseMeta() );
  }

  private String getSchemaAndTable( XulDatabaseExplorerModel model, DatabaseMeta meta ) {
    if ( model.getSchema() != null ) {
      return meta.getQuotedSchemaTableCombination( model.getSchema(), model.getTable() );
    } else {
      return meta.getQuotedSchemaTableCombination( null, model.getTable() );
    }
  }

  @Override
  public UiPostActionStatus getActionStatus() {
    return status;
  }
}
