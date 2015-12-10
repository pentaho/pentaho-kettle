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
package org.pentaho.di.ui.repository.pur.controller;

import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.repository.pur.UnifiedRepositoryLockService;
import org.pentaho.di.repository.pur.model.RepositoryLock;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.repository.pur.PurRepositoryDialog;
import org.pentaho.di.ui.repository.pur.services.IAbsSecurityProvider;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.spoon.job.JobGraph;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulDomContainer;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.swt.custom.DialogConstant;
import org.pentaho.ui.xul.util.XulDialogCallback;

public class SpoonLockController extends AbstractXulEventHandler implements java.io.Serializable {

  private static final long serialVersionUID = -8466323408581425803L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final Class<?> PKG = PurRepositoryDialog.class;

  private ILockService service;

  private EngineMetaInterface workingMeta = null;

  private BindingFactory bindingFactory = null;

  private boolean tabBound = false;

  private boolean isCreateAllowed = false;

  private boolean isLockingAllowed = false;

  private Shell shell;

  private static Log log = LogFactory.getLog( SpoonLockController.class );

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

  public String getName() {
    return "spoonLockController"; //$NON-NLS-1$
  }

  public void lockContent() throws Exception {
    try {
      if ( workingMeta != null && workingMeta.getObjectId() != null
          && supportsLocking( Spoon.getInstance().getRepository() ) ) {
        // Bind the tab icon if it is not already bound (cannot be done in init because TransGraph must exist to create
        // the tab)
        // Look in the SpoonTransformationDelegate for details on the TabItem creation
        if ( !tabBound ) {
          bindingFactory
              .createBinding(
                  this,
                  "activeMetaUnlocked", Spoon.getInstance().delegates.tabs.findTabMapEntry( workingMeta ).getTabItem(), "image", new BindingConvertor<Boolean, Image>() { //$NON-NLS-1$ //$NON-NLS-2$
                    @Override
                    public Image sourceToTarget( Boolean activeMetaUnlocked ) {
                      if ( activeMetaUnlocked ) {
                        if ( workingMeta instanceof TransMeta ) {
                          return GUIResource.getInstance().getImageTransGraph();
                        } else if ( workingMeta instanceof JobMeta ) {
                          return GUIResource.getInstance().getImageJobGraph();
                        }
                      } else {
                        return GUIResource.getInstance().getImageLocked();
                      }
                      return null;
                    }

                    @Override
                    public Boolean targetToSource( Image arg0 ) {
                      return false;
                    }
                  } );
          tabBound = true;
        }

        // Decide whether to lock or unlock the object
        if ( fetchRepositoryLock( workingMeta ) == null ) {
          // Lock the object (it currently is NOT locked)

          XulPromptBox lockNotePrompt = promptLockMessage( document, messages, null );
          lockNotePrompt.addDialogCallback( new XulDialogCallback<String>() {
            public void onClose( XulComponent component, Status status, String value ) {

              if ( !status.equals( Status.CANCEL ) ) {
                try {
                  if ( workingMeta instanceof TransMeta ) {
                    getService( Spoon.getInstance().getRepository() ).lockTransformation( workingMeta.getObjectId(),
                        value );
                  } else if ( workingMeta instanceof JobMeta ) {
                    getService( Spoon.getInstance().getRepository() ).lockJob( workingMeta.getObjectId(), value );
                  }

                  // Execute binding. Notify listeners that the object is now locked
                  firePropertyChange( "activeMetaUnlocked", true, false ); //$NON-NLS-1$ //$NON-NLS-2$

                  // this keeps the menu item and the state in sync
                  // could a binding be used instead?
                  XulDomContainer container = getXulDomContainer();
                  XulMenuitem lockMenuItem =
                      (XulMenuitem) container.getDocumentRoot().getElementById( "lock-context-lock" ); //$NON-NLS-1$
                  lockMenuItem.setSelected( true );
                } catch ( Exception e ) {
                  // convert to runtime exception so it bubbles up through the UI
                  throw new RuntimeException( e );
                }
              } else {
                // this keeps the menu item and the state in sync
                // could a binding be used instead?
                XulDomContainer container = getXulDomContainer();
                XulMenuitem lockMenuItem =
                    (XulMenuitem) container.getDocumentRoot().getElementById( "lock-context-lock" ); //$NON-NLS-1$
                lockMenuItem.setSelected( false );
              }
            }

            public void onError( XulComponent component, Throwable err ) {
              throw new RuntimeException( err );
            }
          } );

          lockNotePrompt.open();
        } else {
          // Unlock the object (it currently IS locked)
          if ( workingMeta instanceof TransMeta ) {
            getService( Spoon.getInstance().getRepository() ).unlockTransformation( workingMeta.getObjectId() );
          } else if ( workingMeta instanceof JobMeta ) {
            getService( Spoon.getInstance().getRepository() ).unlockJob( workingMeta.getObjectId() );
          }
          // Execute binding. Notify listeners that the object is now unlocked
          firePropertyChange( "activeMetaUnlocked", false, true ); //$NON-NLS-1$ //$NON-NLS-2$
        }

      } else if ( workingMeta != null && workingMeta.getObjectId() == null
          && supportsLocking( Spoon.getInstance().getRepository() ) ) {
        XulDomContainer container = getXulDomContainer();
        XulMenuitem lockMenuItem = (XulMenuitem) container.getDocumentRoot().getElementById( "lock-context-lock" ); //$NON-NLS-1$
        lockMenuItem.setSelected( false );

        XulMessageBox msgBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
        msgBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
        msgBox.setMessage( BaseMessages.getString( PKG, "LockController.SaveBeforeLock" ) );//$NON-NLS-1$
        msgBox.setModalParent( shell );

        msgBox.open();
      } else {
        XulDomContainer container = getXulDomContainer();
        XulMenuitem lockMenuItem = (XulMenuitem) container.getDocumentRoot().getElementById( "lock-context-lock" ); //$NON-NLS-1$
        lockMenuItem.setSelected( false );

        XulMessageBox msgBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
        msgBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
        msgBox.setMessage( BaseMessages.getString( PKG, "LockController.NoLockingSupport" ) );//$NON-NLS-1$
        msgBox.setModalParent( shell );
        msgBox.open();
      }
    } catch ( Throwable th ) {
      log.error( BaseMessages.getString( PKG, "LockController.NoLockingSupport" ), th );//$NON-NLS-1$
      new ErrorDialog(
          ( (Spoon) SpoonFactory.getInstance() ).getShell(),
          BaseMessages.getString( PKG, "Dialog.Error" ), BaseMessages.getString( PKG, "LockController.NoLockingSupport" ), th ); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  public void viewLockNote() throws Exception {
    if ( workingMeta != null && supportsLocking( Spoon.getInstance().getRepository() ) ) {
      try {
        RepositoryLock repoLock = fetchRepositoryLock( workingMeta );
        if ( repoLock != null ) {
          XulMessageBox msgBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
          msgBox.setTitle( BaseMessages.getString( PKG, "PurRepository.LockNote.Title" ) ); //$NON-NLS-1$
          msgBox.setMessage( repoLock.getMessage() );
          msgBox.setModalParent( shell );

          msgBox.open();
        }
      } catch ( Throwable th ) {
        log.error( BaseMessages.getString( PKG, "LockController.NoLockingSupport" ), th );//$NON-NLS-1$
        new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(), BaseMessages
            .getString( PKG, "Dialog.Error" ), BaseMessages.getString( PKG, "LockController.NoLockingSupport" ), th ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } else {
      XulMessageBox msgBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
      msgBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
      msgBox.setMessage( BaseMessages.getString( PKG, "LockController.NoLockingSupport" ) );//$NON-NLS-1$
      msgBox.setModalParent( shell );
      msgBox.open();
    }
  }

  @Override
  public void setXulDomContainer( XulDomContainer xulDomContainer ) {
    super.setXulDomContainer( xulDomContainer );
    init();
  }

  public boolean isActiveMetaUnlocked() {
    try {
      if ( fetchRepositoryLock( workingMeta ) != null ) {
        return false;
      } else {
        return true;
      }
    } catch ( KettleException e ) {
      throw new RuntimeException( e );
    }
  }

  protected void init() {
    try {
      if ( ( Spoon.getInstance().getRepository() != null )
          && ( Spoon.getInstance().getRepository().hasService( IAbsSecurityProvider.class ) ) ) {
        IAbsSecurityProvider securityService =
            (IAbsSecurityProvider) Spoon.getInstance().getRepository().getService( IAbsSecurityProvider.class );

        setCreateAllowed( allowedActionsContains( securityService, IAbsSecurityProvider.CREATE_CONTENT_ACTION ) );
      }

      shell = ( ( (Spoon) SpoonFactory.getInstance() ).getShell() );
      XulDomContainer container = getXulDomContainer();

      bindingFactory = new DefaultBindingFactory();
      bindingFactory.setDocument( container.getDocumentRoot() );

      bindingFactory.setBindingType( Type.ONE_WAY );

      bindingFactory.createBinding( this, "activeMetaUnlocked", "lock-context-locknotes", "disabled" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
      bindingFactory.createBinding( this, "lockingNotAllowed", "lock-context-lock", "disabled" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

      // Get trans* object to gain access to the *Meta object to determine if we are initially locked or not
      // Try transformation
      if ( container.getEventHandlers().containsKey( "transgraph" ) ) { //$NON-NLS-1$
        workingMeta = ( (TransGraph) container.getEventHandler( "transgraph" ) ).getMeta(); //$NON-NLS-1$
      } else if ( container.getEventHandlers().containsKey( "jobgraph" ) ) { //$NON-NLS-1$
        workingMeta = ( (JobGraph) container.getEventHandler( "jobgraph" ) ).getMeta(); //$NON-NLS-1$
      }

      RepositoryLock repoLock = fetchRepositoryLock( workingMeta );
      if ( repoLock != null ) {
        XulMenuitem lockMenuItem = (XulMenuitem) container.getDocumentRoot().getElementById( "lock-context-lock" ); //$NON-NLS-1$
        lockMenuItem.setSelected( true );
        // Permit locking/unlocking if the user owns the lock
        if ( Spoon.getInstance().getRepository() instanceof PurRepository ) {
          setLockingAllowed( new UnifiedRepositoryLockService( ( (PurRepository) Spoon.getInstance().getRepository() )
              .getPur() ).canUnlockFileById( workingMeta.getObjectId() ) );
        } else {
          setLockingAllowed( repoLock.getLogin().equalsIgnoreCase(
              Spoon.getInstance().getRepository().getUserInfo().getLogin() ) );
        }
      } else {
        setLockingAllowed( true );
      }

      firePropertyChange( "activeMetaUnlocked", null, repoLock == null ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    } catch ( Exception e ) {
      log.error( BaseMessages.getString( PKG, "LockController.NoLockingSupport" ), e );//$NON-NLS-1$
      new ErrorDialog( ( (Spoon) SpoonFactory.getInstance() ).getShell(),
          BaseMessages.getString( PKG, "Dialog.Error" ), e.getMessage(), e ); //$NON-NLS-1$

    }
  }

  public boolean isCreateAllowed() {
    return isCreateAllowed;
  }

  public void setCreateAllowed( boolean isCreateAllowed ) {
    this.isCreateAllowed = isCreateAllowed;
    this.firePropertyChange( "createAllowed", null, isCreateAllowed ); //$NON-NLS-1$
  }

  public boolean isLockingAllowed() {
    return isLockingAllowed;
  }

  public boolean isLockingNotAllowed() {
    return !isLockingAllowed;
  }

  public void setLockingAllowed( boolean isLockingAllowed ) {
    this.isLockingAllowed = isLockingAllowed;
    this.firePropertyChange( "lockingNotAllowed", null, !isLockingAllowed ); //$NON-NLS-1$
  }

  private boolean allowedActionsContains( IAbsSecurityProvider service, String action ) throws KettleException {
    List<String> allowedActions = service.getAllowedActions( IAbsSecurityProvider.NAMESPACE );
    for ( String actionName : allowedActions ) {
      if ( action != null && action.equals( actionName ) ) {
        return true;
      }
    }
    return false;
  }

  protected RepositoryLock fetchRepositoryLock( EngineMetaInterface meta ) throws KettleException {
    RepositoryLock result = null;
    if ( meta != null ) {
      if ( meta.getObjectId() != null ) {
        if ( meta instanceof TransMeta ) {
          result = getService( Spoon.getInstance().getRepository() ).getTransformationLock( meta.getObjectId() );
        } else if ( meta instanceof JobMeta ) {
          result = getService( Spoon.getInstance().getRepository() ).getJobLock( meta.getObjectId() );
        }
      }
    }
    return result;
  }

  private XulPromptBox promptLockMessage( org.pentaho.ui.xul.dom.Document document, ResourceBundle messages,
      String defaultMessage ) throws XulException {
    XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$
    prompt.setModalParent( shell );

    prompt.setTitle( BaseMessages.getString( PKG, "RepositoryExplorer.LockMessage.Title" ) );//$NON-NLS-1$
    prompt.setButtons( new DialogConstant[] { DialogConstant.OK, DialogConstant.CANCEL } );

    prompt.setMessage( BaseMessages.getString( PKG, "RepositoryExplorer.LockMessage.Label" ) );//$NON-NLS-1$
    prompt.setValue( defaultMessage == null
        ? BaseMessages.getString( PKG, "RepositoryExplorer.DefaultLockMessage" ) : defaultMessage ); //$NON-NLS-1$
    return prompt;
  }

  private ILockService getService( Repository repository ) throws KettleException {
    if ( service == null ) {
      if ( repository != null && repository.hasService( ILockService.class ) ) {
        return (ILockService) repository.getService( ILockService.class );
      } else {
        throw new IllegalStateException();
      }
    } else {
      return service;
    }
  }

  private boolean supportsLocking( Repository repository ) throws KettleException {
    return repository.hasService( ILockService.class );
  }

}
