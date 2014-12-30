/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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
import org.pentaho.di.core.namedconfig.NamedConfigurationManager;
import org.pentaho.di.core.namedconfig.model.NamedConfiguration;
import org.pentaho.di.core.namedconfig.model.NamedConfigurationType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.namedconfig.dialog.NamedConfigurationDialog;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoer.TYPE;
import org.pentaho.di.ui.repository.repositoryexplorer.ContextChangeVetoerCollection;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorer;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UINamedConfiguration;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UINamedConfigurations;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectCreationException;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIObjectRegistry;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.swt.tags.SwtDialog;

public class NamedConfigurationsController extends LazilyInitializedController implements IUISupportController {

  private static Class<?> PKG = RepositoryExplorer.class; // for i18n purposes, needed by Translator2!!

  private XulTree namedConfigurationsTable = null;

  protected BindingFactory bf = null;

  private boolean isRepReadOnly = true;

  private Binding bindButtonNew = null;

  private Binding bindButtonEdit = null;

  private Binding bindButtonRemove = null;

  private Shell shell = null;

  private UINamedConfigurations namedConfigurationsList = new UINamedConfigurations();

  private NamedConfigurationDialog namedConfigurationDialog;

  private MainController mainController;

  protected ContextChangeVetoerCollection contextChangeVetoers;

  protected List<UINamedConfiguration> selectedNamedConfigurations;
  protected List<UINamedConfiguration> repositoryNamedConfigurations;

  public NamedConfigurationsController() {
  }

  @Override
  public String getName() {
    return "namedConfigurationsController";
  }

  public void init( Repository repository ) throws ControllerInitializationException {
    this.repository = repository;
  }

  private NamedConfigurationDialog getNamedConfigurationDialog() {
    if ( namedConfigurationDialog == null ) {
      namedConfigurationDialog = new NamedConfigurationDialog( shell );
    }
    return namedConfigurationDialog;
  }

  private void createBindings() {
    refreshNamedConfigurationList();
    namedConfigurationsTable = (XulTree) document.getElementById( "named-configurations-table" );

    // Bind the named configuration table to a list of named configurations
    bf.setBindingType( Binding.Type.ONE_WAY );

    //CHECKSTYLE:LineLength:OFF
    try {
      bf.createBinding( namedConfigurationsList, "children", namedConfigurationsTable, "elements" ).fireSourceChanged();
      ( bindButtonNew = bf.createBinding( this, "repReadOnly", "named-configurations-new", "disabled" ) ).fireSourceChanged();
      ( bindButtonEdit = bf.createBinding( this, "repReadOnly", "named-configurations-edit", "disabled" ) ).fireSourceChanged();
      ( bindButtonRemove = bf.createBinding( this, "repReadOnly", "named-configurations-remove", "disabled" ) ).fireSourceChanged();

      if ( repository != null ) {
        bf.createBinding( namedConfigurationsTable, "selectedItems", this, "selectedNamedConfigurations" );
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

  private void refreshNamedConfigurationList() {
    final List<UINamedConfiguration> tmpList = new ArrayList<UINamedConfiguration>();
    Runnable r = new Runnable() {
      public void run() {
        try {
          for ( NamedConfiguration namedConfiguration : NamedConfigurationManager.getInstance().list( repository.getMetaStore() ) ) {
            try {
              tmpList.add( UIObjectRegistry.getInstance().constructUINamedConfiguration( namedConfiguration, repository ) );
            } catch ( UIObjectCreationException uoe ) {
              tmpList.add( new UINamedConfiguration( namedConfiguration, repository ) );
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
    namedConfigurationsList.setChildren( tmpList );
  }

  public void createNamedConfiguration() {
    try {
      // user will have to select from list of templates
      // for now hard code to hadoop-cluster
      NamedConfiguration namedConfigurationTemplate = NamedConfigurationManager.getInstance().getConfigurationTemplates( 
          NamedConfigurationType.HADOOP_CLUSTER.getType() ).get( 0 );
      namedConfigurationTemplate.initializeVariablesFrom( null );
      getNamedConfigurationDialog().setNamedConfiguration( namedConfigurationTemplate );

      String namedConfigurationName = getNamedConfigurationDialog().open();
      if ( namedConfigurationName != null && !namedConfigurationName.equals( "" ) ) {
        // See if this named configuration exists...
        NamedConfiguration namedConfiguration = NamedConfigurationManager.getInstance().read( namedConfigurationName, repository.getMetaStore() );
        if ( namedConfiguration == null ) {
          NamedConfigurationManager.getInstance().create( getNamedConfigurationDialog().getNamedConfiguration(), repository.getMetaStore() );
        } else {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedConfiguration.Create.AlreadyExists.Message" ) );
          mb.setText( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedConfiguration.Create.AlreadyExists.Title" ) );
          mb.open();
        }
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Create.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Create.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshNamedConfigurationList();
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

  boolean compareNamedConfigurations( List<UINamedConfiguration> ro1, List<UINamedConfiguration> ro2 ) {
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

  public void editNamedConfiguration() {
    try {
      Collection<UINamedConfiguration> namedConfigurations = namedConfigurationsTable.getSelectedItems();

      if ( namedConfigurations != null && !namedConfigurations.isEmpty() ) {
        // Grab the first item in the list & send it to the database dialog
        NamedConfiguration original = ( (UINamedConfiguration) namedConfigurations.toArray()[0] ).getNamedConfiguration();
        NamedConfiguration namedConfiguration = original.clone();

        // Make sure this NamedConfiguration already exists and store its id for updating
        if ( NamedConfigurationManager.getInstance().read( namedConfiguration.getName(), repository.getMetaStore() ) == null ) {
          MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
          mb.setMessage( BaseMessages.getString(
            PKG, "RepositoryExplorerDialog.NamedConfiguration.Edit.DoesNotExists.Message" ) );
          mb
            .setText( BaseMessages.getString(
              PKG, "RepositoryExplorerDialog.NamedConfiguration.Edit.DoesNotExists.Title" ) );
          mb.open();
        } else {
          getNamedConfigurationDialog().setNamedConfiguration( namedConfiguration );
          String namedConfigurationName = getNamedConfigurationDialog().open();
          if ( namedConfigurationName != null && !namedConfigurationName.equals( "" ) ) {
            // delete original
            NamedConfigurationManager.getInstance().delete( original.getName(), repository.getMetaStore() );
            NamedConfigurationManager.getInstance().create( namedConfiguration, repository.getMetaStore() );
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.NamedConfiguration.Edit.NoItemSelected.Message" ) );
        mb
          .setText( BaseMessages
            .getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Edit.NoItemSelected.Title" ) );
        mb.open();
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Edit.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Edit.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshNamedConfigurationList();
    }
  }

  public void removeNamedConfiguration() {
    try {
      Collection<UINamedConfiguration> namedConfigurations = namedConfigurationsTable.getSelectedItems();

      if ( namedConfigurations != null && !namedConfigurations.isEmpty() ) {
        for ( Object obj : namedConfigurations ) {
          if ( obj != null && obj instanceof UINamedConfiguration ) {
            UINamedConfiguration uiNamedConfiguration = (UINamedConfiguration) obj;

            NamedConfiguration namedConfiguration = uiNamedConfiguration.getNamedConfiguration();

            // Make sure this named configuration already exists and store its id for updating
            if ( NamedConfigurationManager.getInstance().read( namedConfiguration.getName(), repository.getMetaStore() ) == null ) {
              MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
              mb
                .setMessage( BaseMessages.getString(
                  PKG, "RepositoryExplorerDialog.NamedConfiguration.Delete.DoesNotExists.Message", namedConfiguration
                    .getName() ) );
              mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Delete.DoesNotExists.Title" ) );
              mb.open();
            } else {
              NamedConfigurationManager.getInstance().delete( namedConfiguration.getName(), repository.getMetaStore() );
            }
          }
        }
      } else {
        MessageBox mb = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
        mb.setMessage( BaseMessages.getString(
          PKG, "RepositoryExplorerDialog.NamedConfiguration.Delete.NoItemSelected.Message" ) );
        mb.setText( BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Delete.NoItemSelected.Title" ) );
        mb.open();
      }
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        new ErrorDialog( shell,
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Delete.UnexpectedError.Title" ),
          BaseMessages.getString( PKG, "RepositoryExplorerDialog.NamedConfiguration.Delete.UnexpectedError.Message" ), e );
      }
    } finally {
      refreshNamedConfigurationList();
    }
  }

  public void setSelectedNamedConfigurations( List<UINamedConfiguration> namedConfigurations ) {
    // SELECTION LOGIC
    if ( !compareNamedConfigurations( namedConfigurations, this.selectedNamedConfigurations ) ) {
      List<TYPE> pollResults = pollContextChangeVetoResults();
      if ( !contains( TYPE.CANCEL, pollResults ) ) {
        this.selectedNamedConfigurations = namedConfigurations;
        setRepositoryNamedConfigurations( namedConfigurations );
      } else {
        namedConfigurationsTable.setSelectedItems( this.selectedNamedConfigurations );
        return;
      }
    }

    // ENABLE BUTTONS LOGIC
    boolean enableEdit = false;
    boolean enableRemove = false;
    if ( namedConfigurations != null && namedConfigurations.size() > 0 ) {
      enableRemove = true;
      if ( namedConfigurations.size() == 1 ) {
        enableEdit = true;
      }
    }
    // Convenience - Leave 'new' enabled, modify 'edit' and 'remove'
    enableButtons( true, enableEdit, enableRemove );
  }

  public List<UINamedConfiguration> getRepositoryNamedConfigurations() {
    return repositoryNamedConfigurations;
  }

  public void setRepositoryNamedConfigurations( List<UINamedConfiguration> repositoryNamedConfigurations ) {
    this.repositoryNamedConfigurations = repositoryNamedConfigurations;
    firePropertyChange( "repositoryNamedConfigurations", null, repositoryNamedConfigurations );
  }

  public void enableButtons( boolean enableNew, boolean enableEdit, boolean enableRemove ) {
    XulButton bNew = (XulButton) document.getElementById( "named-configurations-new" );
    XulButton bEdit = (XulButton) document.getElementById( "named-configurations-edit" );
    XulButton bRemove = (XulButton) document.getElementById( "named-configurations-remove" );

    bNew.setDisabled( !enableNew );
    bEdit.setDisabled( !enableEdit );
    bRemove.setDisabled( !enableRemove );
  }

  public void tabClicked() {
    lazyInit();
  }

}
