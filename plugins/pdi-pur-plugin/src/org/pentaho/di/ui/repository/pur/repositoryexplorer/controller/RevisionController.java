/*!
 * Copyright 2010 - 2015 Pentaho Corporation.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.ui.repository.pur.repositoryexplorer.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.ILockObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IRevisionObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectRevision;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIRepositoryObjectRevisions;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.RepositoryExplorerCallback;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.MainController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.components.XulTab;
import org.pentaho.ui.xul.containers.XulTabbox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.custom.DialogConstant;
import org.pentaho.ui.xul.util.XulDialogCallback;

public class RevisionController extends AbstractXulEventHandler implements IUISupportController, java.io.Serializable {

  private static final long serialVersionUID = 4072800373062217146L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final Class<?> PKG = IUIEEUser.class;

  private static final String OPEN_REVISION_BUTTON = "revision-open";
  private static final String RESTORE_REVISION_BUTTON = "revision-restore";

  protected XulTree folderTree;
  protected XulTree fileTable;
  protected XulTab historyTab;
  protected Repository repository;
  protected XulTree revisionTable;
  protected MainController mainController;
  protected BrowseController browseController;
  protected BindingFactory bf;
  protected XulTabbox filePropertiesTabbox;
  protected RepositoryExplorerCallback callback;
  protected UIRepositoryObjectRevisions revisions;
  protected Binding revisionBinding;
  protected boolean masterVersioningEnabled = true;
  protected boolean masterCommentsEnabled = true;

  private XulMessageBox messageBox;

  protected ResourceBundle messages = new ResourceBundle() {

    @Override
    public Enumeration<String> getKeys() {
      return null;
    }

    @Override
    protected Object handleGetObject( String key ) {
      return BaseMessages.getString( PKG, key );
    }

  };

  public RevisionController() {
    super();
    // TODO Auto-generated constructor stub
  }

  public void init( Repository repository ) throws ControllerInitializationException {
    try {
      this.repository = repository;
      mainController = (MainController) this.getXulDomContainer().getEventHandler( "mainController" ); //$NON-NLS-1$
      browseController = (BrowseController) this.getXulDomContainer().getEventHandler( "browseController" ); //$NON-NLS-1$
      bf = new DefaultBindingFactory();
      bf.setDocument( this.getXulDomContainer().getDocumentRoot() );

      messageBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
      createBindings();
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        throw new ControllerInitializationException( e );
      }
    }
  }

  private void createBindings() {
    filePropertiesTabbox = (XulTabbox) document.getElementById( "file-properties-tabs" ); //$NON-NLS-1$
    historyTab = (XulTab) document.getElementById( "history" ); //$NON-NLS-1$
    revisionTable = (XulTree) document.getElementById( "revision-table" ); //$NON-NLS-1$
    folderTree = (XulTree) document.getElementById( "folder-tree" ); //$NON-NLS-1$
    fileTable = (XulTree) document.getElementById( "file-table" ); //$NON-NLS-1$ 

    // Hide the history tab if versioning is off
    historyTab.setVisible( false );

    bf.setBindingType( Binding.Type.ONE_WAY );
    BindingConvertor<int[], Boolean> forButtons = new BindingConvertor<int[], Boolean>() {

      @Override
      public Boolean sourceToTarget( int[] value ) {
        return value != null && !( value.length <= 0 );
      }

      @Override
      public int[] targetToSource( Boolean value ) {
        return null;
      }
    };
    Binding openButtonBinding =
        bf.createBinding( revisionTable, "selectedRows", OPEN_REVISION_BUTTON, "!disabled", forButtons ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    Binding restoreButtonBinding =
        bf.createBinding( revisionTable, "selectedRows", RESTORE_REVISION_BUTTON, "!disabled", forButtons ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( folderTree, "selectedItems", this, "historyTabVisibility" ); //$NON-NLS-1$  //$NON-NLS-2$

    revisionBinding = bf.createBinding( this, "revisionObjects", revisionTable, "elements" ); //$NON-NLS-1$ //$NON-NLS-2$

    revisionBinding = bf.createBinding( browseController, "repositoryItems", this, "revisionObjects", //$NON-NLS-1$ //$NON-NLS-2$
        new BindingConvertor<List<UIRepositoryObject>, UIRepositoryObjectRevisions>() {

          private void disableButtons() {
            document.getElementById( OPEN_REVISION_BUTTON ).setDisabled( true );
            document.getElementById( RESTORE_REVISION_BUTTON ).setDisabled( true );
          }

          @Override
          public UIRepositoryObjectRevisions sourceToTarget( List<UIRepositoryObject> ro ) {
            if ( ro == null || ro.isEmpty() || !masterVersioningEnabled ) {
              return new UIRepositoryObjectRevisions();
            }

            if ( ro.get( 0 ) instanceof UIRepositoryDirectory ) {
              historyTab.setVisible( false );
              filePropertiesTabbox.setSelectedIndex( 0 );
              disableButtons();
              return null;
            }

            UIRepositoryObjectRevisions revisions = null;
            try {
              UIRepositoryContent rc = (UIRepositoryContent) ro.get( 0 );
              if ( rc instanceof IRevisionObject ) {
                Boolean versioningEnabled = ( (IRevisionObject) rc ).getVersioningEnabled();
                // Show/hide the Verison History tab
                historyTab.setVisible( versioningEnabled );
                if ( !versioningEnabled ) {
                  return new UIRepositoryObjectRevisions();
                }
                Boolean versionCommentEnabled = ( (IRevisionObject) rc ).getVersionCommentEnabled();

                // Show/Hide the version comment column
                setRevisionTableColumns( versionCommentEnabled );

                revisions = ( (IRevisionObject) rc ).getRevisions();

              } else {
                throw new IllegalStateException( BaseMessages.getString( PKG,
                    "RevisionsController.RevisionsNotSupported" ) ); //$NON-NLS-1$
              }
            } catch ( KettleException e ) {
              if ( mainController == null || !mainController.handleLostRepository( e ) ) {
                // convert to runtime exception so it bubbles up through the UI
                throw new RuntimeException( e );
              }
            }

            return revisions;
          }

          @Override
          public List<UIRepositoryObject> targetToSource( UIRepositoryObjectRevisions elements ) {
            return null;
          }
        } );

    try {
      openButtonBinding.fireSourceChanged();
      restoreButtonBinding.fireSourceChanged();
      revisionBinding.fireSourceChanged();
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        // convert to runtime exception so it bubbles up through the UI
        throw new RuntimeException( e );
      }
    }

  }

  public void setRevisionObjects( UIRepositoryObjectRevisions revisions ) {
    this.revisions = revisions;
    this.firePropertyChange( "revisionObjects", null, revisions );
  }

  public UIRepositoryObjectRevisions getRevisionObjects() {
    return revisions;
  }

  public String getName() {
    return "revisionController"; //$NON-NLS-1$
  }

  public <T> void setHistoryTabVisibility( Collection<T> items ) {
    historyTab.setVisible( false );
    filePropertiesTabbox.setSelectedIndex( 0 );
  }

  public RepositoryExplorerCallback getCallback() {
    return callback;
  }

  public void setCallback( RepositoryExplorerCallback callback ) {
    this.callback = callback;
  }

  public void openRevision() {
    Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
    UIRepositoryContent contentToOpen = content.iterator().next();

    Collection<UIRepositoryObjectRevision> revision = revisionTable.getSelectedItems();

    // TODO: Is it a requirement to allow opening multiple revisions?
    UIRepositoryObjectRevision revisionToOpen = revision.iterator().next();
    if ( mainController != null && mainController.getCallback() != null ) {
      try {
        if ( mainController.getCallback().open( contentToOpen, revisionToOpen.getName() ) ) {
          // TODO: fire request to close dialog
        }
      } catch ( Exception e ) {
        if ( mainController != null ) {
          mainController.handleLostRepository( e );
        }
      }
    }
  }

  public void restoreRevision() {

    try {
      final Collection<UIRepositoryContent> content = fileTable.getSelectedItems();
      final UIRepositoryContent contentToRestore = content.iterator().next();

      Collection<UIRepositoryObjectRevision> versions = revisionTable.getSelectedItems();
      final UIRepositoryObjectRevision versionToRestore = versions.iterator().next();

      XulPromptBox commitPrompt = promptCommitComment( document, messages, null );

      if ( contentToRestore instanceof ILockObject && ( (ILockObject) contentToRestore ).isLocked() ) {
        // Cannot restore revision of locked content
        messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
        messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
        messageBox.setMessage( BaseMessages.getString( PKG, "RevisionsController.RestoreLockedFileNotAllowed" ) ); //$NON-NLS-1$
        messageBox.open();
        return;
      }

      commitPrompt.addDialogCallback( new XulDialogCallback<String>() {
        public void onClose( XulComponent component, Status status, String value ) {

          if ( !status.equals( Status.CANCEL ) ) {
            try {
              if ( contentToRestore instanceof IRevisionObject ) {
                ( (IRevisionObject) contentToRestore ).restoreRevision( versionToRestore, value );
                List<UIRepositoryObject> objects = new ArrayList<UIRepositoryObject>();
                objects.add( contentToRestore );
                browseController.setRepositoryObjects( objects );
              } else {
                throw new IllegalStateException( BaseMessages.getString( PKG,
                    "RevisionsController.RevisionsNotSupported" ) ); //$NON-NLS-1$
              }
            } catch ( Exception e ) {
              if ( mainController == null || !mainController.handleLostRepository( e ) ) {
                // convert to runtime exception so it bubbles up through the UI
                throw new RuntimeException( e );
              }
            }
          }
        }

        public void onError( XulComponent component, Throwable err ) {
          if ( mainController == null || !mainController.handleLostRepository( err ) ) {
            throw new RuntimeException( err );
          }
        }
      } );

      commitPrompt.open();
    } catch ( Exception e ) {
      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
        throw new RuntimeException( new KettleException( e ) );
      }
    }
  }

  private XulPromptBox promptCommitComment( org.pentaho.ui.xul.dom.Document document, ResourceBundle messages,
      String defaultMessage ) throws XulException {
    XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$

    prompt.setTitle( BaseMessages.getString( PKG, "RepositoryExplorer.CommitTitle" ) ); //$NON-NLS-1$
    prompt.setButtons( new DialogConstant[] { DialogConstant.OK, DialogConstant.CANCEL } );

    prompt.setMessage( BaseMessages.getString( PKG, "RepositoryExplorer.CommitLabel" ) ); //$NON-NLS-1$
    prompt.setValue( defaultMessage == null
        ? BaseMessages.getString( PKG, "RepositoryExplorer.DefaultCommitMessage" ) : defaultMessage ); //$NON-NLS-1$
    return prompt;
  }

  private void setRevisionTableColumns( Boolean versionCommentEnabled ) {
    if ( versionCommentEnabled ) {
      revisionTable.getColumns().getColumn( 2 ).setHidden( false );
    } else {
      revisionTable.getColumns().getColumn( 2 ).setHidden( true );
    }
  }
}
