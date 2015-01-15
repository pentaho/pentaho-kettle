/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.namedcluster.NamedClusterManager;
import org.pentaho.di.core.namedcluster.model.NamedCluster;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.namedcluster.dialog.NamedClusterDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer.TYPE;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoerCollection;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UINamedCluster;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UINamedClusters;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class NamedClustersController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!

  private XulTree namedClustersTable = null;

  protected BindingFactory bf = null;

  private boolean isRepReadOnly = true;

  private Binding bindButtonNew = null;

  private Binding bindButtonEdit = null;

  private Binding bindButtonRemove = null;

  private Shell shell = null;

  private UINamedClusters namedClustersList = new UINamedClusters();

  private NamedClusterDialog namedClusterDialog;

  private MainController mainController;

  protected ContextChangeVetoerCollection contextChangeVetoers;

  protected List<UINamedCluster> selectedNamedClusters;
  protected List<UINamedCluster> repositoryNamedClusters;

  public NamedClustersController() {
  }

  @Override
  public String getName() {
    return "namedClustersController";
  }

  public void init( Repository repository ) throws ControllerInitializationException {
    this.repository = repository;
  }

  private NamedClusterDialog getNamedClusterDialog() {
    if ( namedClusterDialog == null ) {
      namedClusterDialog = new NamedClusterDialog( shell );
    }
    return namedClusterDialog;
  }

  private void createBindings() {
    refreshNamedClustersList();
    namedClustersTable = (XulTree) document.getElementById( "named-clusters-table" );

    // Bind the named clusters table to a list of named clusters
    bf.setBindingType( Binding.Type.ONE_WAY );

    //CHECKSTYLE:LineLength:OFF
    try {
      bf.createBinding( namedClustersList, "children", namedClustersTable, "elements" ).fireSourceChanged();
      ( bindButtonNew = bf.createBinding( this, "repReadOnly", "named-clusters-new", "disabled" ) ).fireSourceChanged();
      ( bindButtonEdit = bf.createBinding( this, "repReadOnly", "named-clusters-edit", "disabled" ) ).fireSourceChanged();
      ( bindButtonRemove = bf.createBinding( this, "repReadOnly", "named-clusters-remove", "disabled" ) ).fireSourceChanged();

      if ( repository != null ) {
        bf.createBinding( namedClustersTable, "selectedItems", this, "selectedNamedClusters" );
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

  private void refreshNamedClustersList() {
    final List<UINamedCluster> tmpList = new ArrayList<UINamedCluster>();
    Runnable r = new Runnable() {
      public void run() {
        try {
          for ( NamedCluster namedCluster : NamedClusterManager.getInstance().list( repository.getMetaStore() ) ) {
            try {
              tmpList.add( UIObjectRegistry.getInstance().constructUINamedCluster( namedCluster, repository ) );
            } catch ( UIObjectCreationException uoe ) {
              tmpList.add( new UINamedCluster( namedCluster, repository ) );
            }
          }
        } catch ( Exception e ) {
          if ( mainController == null || !mainController.handleLostRepository( e ) ) {
            // convert to runtime exception so it bubbles up through the UI
            throw new RuntimeException( e );
          }
        }
      }
    };
    doWithBusyIndicator( r );
    namedClustersList.setChildren( tmpList );
  }

  public void createNamedCluster() {
    try {
      // user will have to select from list of templates
      // for now hard code to hadoop-cluster
      NamedCluster namedCluterTemplate = NamedClusterManager.getInstance().getClusterTemplate();
      namedCluterTemplate.initializeVariablesFrom( null );
      getNamedClusterDialog().setNamedCluster( namedCluterTemplate );

      String namedClusterName = getNamedClusterDialog().open();
      if ( namedClusterName != null && !namedClusterName.equals( "" ) ) {
        // See if this named cluster exists...
        NamedCluster namedCluster = NamedClusterManager.getInstance().read( namedClusterName, Spoon.getInstance().getMetaStore() );
        if ( namedCluster == null ) {
          NamedClusterManager.getInstance().create( getNamedClusterDialog().getNamedCluster(), Spoon.getInstance().getMetaStore() );
        } else {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedCluster.Create.AlreadyExists.Message" ) );
          mb.setText( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedCluster.Create.AlreadyExists.Title" ) );
          mb.open();
        }
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Create.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshNamedClustersList();
    }
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

  boolean compareNamedClusters( List<UINamedCluster> ro1, List<UINamedCluster> ro2 ) {
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

  public void editNamedCluster() {
    try {
      Collection<UINamedCluster> namedClusters = namedClustersTable.getSelectedItems();

      if ( namedClusters != null && !namedClusters.isEmpty() ) {
        // Grab the first item in the list & send it to the database dialog
        NamedCluster original = ( (UINamedCluster) namedClusters.toArray()[0] ).getNamedCluster();
        NamedCluster namedCluster = original.clone();

        // Make sure this NamedCluster already exists and store its id for updating
        if ( NamedClusterManager.getInstance().read( namedCluster.getName(), Spoon.getInstance().getMetaStore() ) == null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedCluster.Edit.DoesNotExists.Message" ) );
          mb
            .setText( BaseMessages.getString(
              PKG, "RepositoryExplorerDialog.NamedCluster.Edit.DoesNotExists.Title" ) );
          mb.open();
        } else {
          getNamedClusterDialog().setNamedCluster( namedCluster );
          String namedClusterName = getNamedClusterDialog().open();
          if ( namedClusterName != null && !namedClusterName.equals( "" ) ) {
            // delete original
            NamedClusterManager.getInstance().delete( original.getName(), Spoon.getInstance().getMetaStore() );
            NamedClusterManager.getInstance().create( namedCluster, Spoon.getInstance().getMetaStore() );
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.NamedCluster.Edit.NoItemSelected.Message" ) );
        mb
          .setText( BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.NamedCluster.Edit.NoItemSelected.Title" ) );
        mb.open();
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Edit.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Edit.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshNamedClustersList();
    }
  }

  public void removeNamedCluster() {
    try {
      Collection<UINamedCluster> namedClusters = namedClustersTable.getSelectedItems();

      if ( namedClusters != null && !namedClusters.isEmpty() ) {
        for ( Object obj : namedClusters ) {
          if ( obj != null && obj instanceof UINamedCluster ) {
            UINamedCluster uiNamedCluster= (UINamedCluster) obj;

            NamedCluster namedCluster = uiNamedCluster.getNamedCluster();

            // Make sure this named cluster already exists and store its id for updating
            if ( NamedClusterManager.getInstance().read( namedCluster.getName(), repository.getMetaStore() ) == null ) {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb
                .setMessage( BaseMessages.getString(
                  PKG, "RepositoryExplorerDialog.NamedCluster.Delete.DoesNotExists.Message", namedCluster.getName() ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Delete.DoesNotExists.Title" ) );
              mb.open();
            } else {
              NamedClusterManager.getInstance().delete( namedCluster.getName(), repository.getMetaStore() );
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.NamedCluster.Delete.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Delete.NoItemSelected.Title" ) );
        mb.open();
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Delete.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedCluster.Delete.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshNamedClustersList();
    }
  }

  public void setSelectedNamedClusters( List<UINamedCluster> namedClusters) {
    // SELECTION LOGIC
    if ( !compareNamedClusters( namedClusters, this.selectedNamedClusters) ) {
      List<TYPE> pollResults = pollContextChangeVetoResults();
      if ( !contains( TYPE.CANCEL, pollResults ) ) {
        this.selectedNamedClusters = namedClusters;
        setRepositoryNamedClusters( namedClusters );
      } else {
        namedClustersTable.setSelectedItems( this.selectedNamedClusters );
        return;
      }
    }

    // ENABLE BUTTONS LOGIC
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( namedClusters != null && namedClusters.size() > 0 ) {
      enableRemove = true;
      if ( namedClusters.size() == 1 ) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons( true, enableEdit, enableRemove );
  }

  public List<UINamedCluster> getRepositoryNamedClusters() {
    return repositoryNamedClusters;
  }

  public void setRepositoryNamedClusters( List<UINamedCluster> repositoryNamedClusters ) {
    this.repositoryNamedClusters = repositoryNamedClusters;
    firePropertyChange( "repositoryNamedClusters", null, repositoryNamedClusters );
  }

  public void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    XulButton bNew = (XulButton) document.getElementById( "named-clusters-new" );
    XulButton bEdit = (XulButton) document.getElementById( "named-clusters-edit" );
    XulButton bRemove = (XulButton) document.getElementById( "named-clusters-remove" );

    bNew.setDisabled( !enableNew );
    bEdit.setDisabled( !enableEdit );
    bRemove.setDisabled( !enableRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

}
