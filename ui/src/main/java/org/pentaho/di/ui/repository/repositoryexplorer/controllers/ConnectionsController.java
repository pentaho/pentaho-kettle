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

package org.pentaho.di.ui.repository.repositoryexplorer.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.core.database.dialog.DatabaseDialog;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer.TYPE;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoerCollection;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnection;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIDatabaseConnections;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class ConnectionsController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!

  private XulTree connectionsTable = null;

  protected BindingFactory bf = null;

  private boolean isRepReadOnly = true;

  private Binding bindButtonNew = null;

  private Binding bindButtonEdit = null;

  private Binding bindButtonRemove = null;

  private Shell shell = null;

  private UIDatabaseConnections dbConnectionList = new UIDatabaseConnections();

  private DatabaseDialog databaseDialog;

  private MainController mainController;

  protected ContextChangeVetoerCollection contextChangeVetoers;

  protected List<UIDatabaseConnection> selectedConnections;
  protected List<UIDatabaseConnection> repositoryConnections;

  public ConnectionsController() {
  }

  @Override
  public String getName() {
    return "connectionsController";
  }

  @Override
  public void init( Repository repository ) throws ControllerInitializationException {
    this.repository = repository;
  }

  // package-local visibility for testing purposes
  DatabaseDialog getDatabaseDialog() {
    if ( databaseDialog != null ) {
      return databaseDialog;
    }
    databaseDialog = new DatabaseDialog( shell );
    return databaseDialog;
  }

  private void createBindings() {
    refreshConnectionList();
    connectionsTable = (XulTree) document.getElementById( "connections-table" );

    // Bind the connection table to a list of connections
    bf.setBindingType( Binding.Type.ONE_WAY );

    //CHECKSTYLE:LineLength:OFF
    try {
      bf.createBinding( dbConnectionList, "children", connectionsTable, "elements" ).fireSourceChanged();
      ( bindButtonNew = bf.createBinding( this, "repReadOnly", "connections-new", "disabled" ) ).fireSourceChanged();
      ( bindButtonEdit = bf.createBinding( this, "repReadOnly", "connections-edit", "disabled" ) ).fireSourceChanged();
      ( bindButtonRemove = bf.createBinding( this, "repReadOnly", "connections-remove", "disabled" ) ).fireSourceChanged();

      if ( repository != null ) {
        bf.createBinding( connectionsTable, "selectedItems", this, "selectedConnections" );
      }
    } catch ( Exception ex ) {
      if ( mainController == null || !mainController.handleLostRepository( ex ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( ex );
      }
    }
  }

  @Override
  protected boolean doLazyInit() {
    try {
      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" );
    } catch ( XulException e ) {
      return false;
    }

    try {
      setRepReadOnly( this.repository.getRepositoryMeta().getRepositoryCapabilities().isReadOnly() );

      // Load the SWT Shell from the explorer dialog
      shell = ( (SwtDialog) document.getElementById( "repository-explorer-dialog" ) ).getShell();
      bf = new DefaultBindingFactory();
      bf.setDocument( this.getXulDomContainer().getDocumentRoot() );

      if ( bf != null ) {
        createBindings();
      }
      enableButtons( true, false, false );

      return true;
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        return false;
      }
    }

    return false;
  }

  public Repository getRepository() {
    return repository;
  }

  public void setRepReadOnly( boolean isRepReadOnly ) {
    try {
      if ( this.isRepReadOnly != isRepReadOnly ) {
        this.isRepReadOnly = isRepReadOnly;

        if ( initialized ) {
          bindButtonNew.fireSourceChanged();
          bindButtonEdit.fireSourceChanged();
          bindButtonRemove.fireSourceChanged();
        }
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( e );
      }
    }
  }

  public boolean isRepReadOnly() {
    return isRepReadOnly;
  }

  private UIDatabaseConnection createUIConnection( DatabaseMeta dbMeta ) {
    UIDatabaseConnection conn = null;
    try {
      conn = UIObjectRegistry.getInstance().constructUIDatabaseConnection( dbMeta, repository );
    } catch ( UIObjectCreationException uoe ) {
      conn = new UIDatabaseConnection( dbMeta, repository );
    }
    return conn;
  }

  // package-local visibility for testing purposes
  void refreshConnectionList() {
    final List<UIDatabaseConnection> tmpList = new ArrayList<>();
    Runnable r = () -> {
      try {
        if ( repository instanceof RepositoryExtended ) {
          List<DatabaseMeta> databaseMetas = ((RepositoryExtended) repository).getConnections( false );
          databaseMetas.forEach( dbMeta -> {
            UIDatabaseConnection conn = createUIConnection( dbMeta );
            if ( conn != null ) {
              tmpList.add( conn );
            }
          } );
        } else {
          ObjectId[] dbIdList = repository.getDatabaseIDs( false );
          for ( ObjectId dbId : dbIdList ) {
            DatabaseMeta dbMeta = repository.loadDatabaseMeta( dbId, null );
            RepositoryElementMetaInterface repoMeta =
              repository.getObjectInformation( dbId, RepositoryObjectType.DATABASE );
            UIDatabaseConnection conn = createUIConnection( dbMeta );
            if ( conn != null ) {
              conn.setRepositoryElementMetaInterface( repoMeta );
              tmpList.add( conn );
            }
          }
        }
      } catch ( KettleException e ) {
        if ( mainController == null || !mainController.handleLostRepository( e ) ) {
          // convert to runtime exception so it bubbles up through the UI
          throw new RuntimeException( e );
        }
      }
    };
    doWithBusyIndicator( r );
    dbConnectionList.setChildren( tmpList );
  }

  public void createConnection() {
    try {
      DatabaseMeta databaseMeta = new DatabaseMeta();
      databaseMeta.initializeVariablesFrom( null );
      getDatabaseDialog().setDatabaseMeta( databaseMeta );

      String dbName = getDatabaseDialog().open();
      if ( dbName != null ) {
        dbName = dbName.trim();
        databaseMeta.setName( dbName );
        databaseMeta.setDisplayName( dbName );
        getDatabaseDialog().setDatabaseMeta( databaseMeta );

        if ( !dbName.isEmpty() ) {
          // See if this user connection exists...
          ObjectId idDatabase = repository.getDatabaseID( dbName );
          if ( idDatabase == null ) {
            repository.insertLogEntry( BaseMessages.getString(
              PKG, "ConnectionsController.Message.CreatingDatabase", getDatabaseDialog()
                .getDatabaseMeta().getName() ) );
            repository.save( getDatabaseDialog().getDatabaseMeta(), Const.VERSION_COMMENT_INITIAL_VERSION, null );
            reloadLoadedJobsAndTransformations();
          } else {
            showAlreadyExistsMessage();
          }
        }
      }
      // We should be able to tell the difference between a cancel and an empty database name
      //
      // else {
      // MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
      // mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Message"));
      // mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Title"));
      // mb.open();
      // }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshConnectionList();
    }
  }

  private boolean reloadLoadedJobsAndTransformations() {
    if ( mainController != null && mainController.getSharedObjectSyncUtil() != null ) {
      mainController.getSharedObjectSyncUtil().reloadJobRepositoryObjects( false );
      mainController.getSharedObjectSyncUtil().reloadTransformationRepositoryObjects( false );
      return true;
    }
    return false;
  }

  // package-local visibility for testing purposes
  void showAlreadyExistsMessage() {
    MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
    mb.setMessage( BaseMessages.getString(
      PKG, "RepositoryExplorerDialog.Connection.Create.AlreadyExists.Message" ) );
    mb.setText( BaseMessages.getString(
      PKG, "RepositoryExplorerDialog.Connection.Create.AlreadyExists.Title" ) );
    mb.open();
  }

  /**
   * Fire all current {@link ContextChangeVetoer}. Every one who has added their self as a vetoer has a change to vote
   * on what should happen.
   */
  List<TYPE> pollContextChangeVetoResults() {
    if ( contextChangeVetoers != null ) {
      return contextChangeVetoers.fireContextChange();
    } else {
      List<TYPE> returnValue = new ArrayList<TYPE>();
      returnValue.add( TYPE.NO_OP );
      return returnValue;
    }
  }

  public void addContextChangeVetoer( ContextChangeVetoer listener ) {
    if ( contextChangeVetoers == null ) {
      contextChangeVetoers = new ContextChangeVetoerCollection();
    }
    contextChangeVetoers.add( listener );
  }

  public void removeContextChangeVetoer( ContextChangeVetoer listener ) {
    if ( contextChangeVetoers != null ) {
      contextChangeVetoers.remove( listener );
    }
  }

  private boolean contains( TYPE type, List<TYPE> typeList ) {
    for ( TYPE t : typeList ) {
      if ( t.equals( type ) ) {
        return true;
      }
    }
    return false;
  }

  boolean compareConnections( List<UIDatabaseConnection> ro1, List<UIDatabaseConnection> ro2 ) {
    if ( ro1 != null && ro2 != null ) {
      if ( ro1.size() != ro2.size() ) {
        return false;
      }
      for ( int i = 0; i < ro1.size(); i++ ) {
        if ( ro1.get( i ) != null && ro2.get( i ) != null ) {
          if ( !ro1.get( i ).getName().equals( ro2.get( i ).getName() ) ) {
            return false;
          }
        }
      }
    } else {
      return false;
    }
    return true;
  }

  public void editConnection() {
    try {
      Collection<UIDatabaseConnection> connections = connectionsTable.getSelectedItems();

      if ( connections != null && !connections.isEmpty() ) {
        // Grab the first item in the list & send it to the database dialog
        DatabaseMeta databaseMeta = ( (UIDatabaseConnection) connections.toArray()[0] ).getDatabaseMeta();

        // Make sure this connection already exists and store its id for updating
        ObjectId idDatabase = repository.getDatabaseID( databaseMeta.getName() );
        if ( idDatabase == null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Connection.Edit.DoesNotExists.Message" ) );
          mb.setText( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.Connection.Edit.DoesNotExists.Title" ) );
          mb.open();
        } else {
          getDatabaseDialog().setDatabaseMeta( databaseMeta );
          String dbName = getDatabaseDialog().open();
          if ( dbName != null ) {
            dbName = dbName.trim();
            databaseMeta.setName( dbName );
            databaseMeta.setDisplayName( dbName );
            if ( !dbName.isEmpty() ) {
              ObjectId idRenamed = repository.getDatabaseID( dbName );
              if ( idRenamed == null || idRenamed.equals( idDatabase ) ) {
                // renaming to non-existing name or updating the current
                repository.insertLogEntry( BaseMessages.getString(
                  PKG, "ConnectionsController.Message.UpdatingDatabase", databaseMeta.getName() ) );
                repository.save( databaseMeta, Const.VERSION_COMMENT_EDIT_VERSION, null );
                reloadLoadedJobsAndTransformations();
              } else {
                // trying to rename to an existing name - show error dialog
                showAlreadyExistsMessage();
              }
            }
          }
          // We should be able to tell the difference between a cancel and an empty database name
          //
          // else {
          // MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
          // mb.setMessage(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Message"));
          // mb.setText(BaseMessages.getString(PKG, "RepositoryExplorerDialog.Connection.Edit.MissingName.Title"));
          // mb.open();
          // }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Message" ) );
        mb
          .setText( BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Edit.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshConnectionList();
    }
  }

  public void removeConnection() {
    try {
      Collection<UIDatabaseConnection> connections = connectionsTable.getSelectedItems();

      if ( connections != null && !connections.isEmpty() ) {
        for ( Object obj : connections ) {
          if ( obj != null && obj instanceof UIDatabaseConnection ) {
            UIDatabaseConnection connection = (UIDatabaseConnection) obj;

            DatabaseMeta databaseMeta = connection.getDatabaseMeta();

            // Make sure this connection already exists and store its id for updating
            ObjectId idDatabase = repository.getDatabaseID( databaseMeta.getName() );
            if ( idDatabase == null ) {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb
                .setMessage( BaseMessages.getString(
                  PKG, "RepositoryExplorerDialog.Connection.Delete.DoesNotExists.Message", databaseMeta
                    .getName() ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Delete.Title" ) );
              mb.open();
            } else {
              repository.deleteDatabaseMeta( databaseMeta.getName() );
              reloadLoadedJobsAndTransformations();
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.Connection.Edit.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Delete.Title" ) );
        mb.open();
      }
    } catch ( KettleException e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.Connection.Remove.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshConnectionList();
    }
  }

  public void setSelectedConnections( List<UIDatabaseConnection> connections ) {
    // SELECTION LOGIC
    if ( !compareConnections( connections, this.selectedConnections ) ) {
      List<TYPE> pollResults = pollContextChangeVetoResults();
      if ( !contains( TYPE.CANCEL, pollResults ) ) {
        this.selectedConnections = connections;
        setRepositoryConnections( connections );
      } else {
        connectionsTable.setSelectedItems( this.selectedConnections );
        return;
      }
    }

    // ENABLE BUTTONS LOGIC
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( connections != null && connections.size() > 0 ) {
      enableRemove = true;
      if ( connections.size() == 1 ) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons( true, enableEdit, enableRemove );
  }

  public List<UIDatabaseConnection> getRepositoryConnections() {
    return repositoryConnections;
  }

  public void setRepositoryConnections( List<UIDatabaseConnection> connections ) {
    this.repositoryConnections = connections;
    firePropertyChange( "repositoryConnections", null, connections );
  }

  public void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    XulButton bNew = (XulButton) document.getElementById( "connections-new" );
    XulButton bEdit = (XulButton) document.getElementById( "connections-edit" );
    XulButton bRemove = (XulButton) document.getElementById( "connections-remove" );

    bNew.setDisabled( !enableNew );
    bEdit.setDisabled( !enableEdit );
    bRemove.setDisabled( !enableRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

}
