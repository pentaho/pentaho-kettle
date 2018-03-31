/*!
 * Copyright 2010 - 2017 Hitachi Vantara.  All rights reserved.
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

import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.pur.PurRepository;
import org.pentaho.di.repository.pur.PurRepositoryMeta;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.ILockObject;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.services.ILockService;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.IUISupportController;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryContent;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjects;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding.Type;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.binding.DefaultBindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulMessageBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.dnd.DropEvent;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.impl.XulEventHandler;
import org.pentaho.ui.xul.swt.custom.DialogConstant;
import org.pentaho.ui.xul.util.XulDialogCallback;

public class RepositoryLockController extends AbstractXulEventHandler implements IUISupportController,
    java.io.Serializable {

  private static final long serialVersionUID = -1770253263177354811L; /* EESOURCE: UPDATE SERIALVERUID */

  private static final Class<?> PKG = IUIEEUser.class;

  private BrowseController browseController = null;

  private BindingFactory bindingFactory = null;

  private XulMessageBox messageBox;

  private ILockService service = null;

  private Repository repository = null;

  private XulMenuitem lockFileMenuItem;

  private XulMenuitem deleteFileMenuItem;

  private XulMenuitem renameFileMenuItem;

  private XulTree fileTable;
  private XulTree folderTree;

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

  public void init( Repository rep ) throws ControllerInitializationException {
    try {
      if ( rep != null && rep.hasService( ILockService.class ) ) {
        repository = rep;
        service = (ILockService) rep.getService( ILockService.class );
      } else {
        throw new ControllerInitializationException( BaseMessages.getString( PKG,
            "RepositoryLockController.ERROR_0001_UNABLE_TO_INITIAL_REPOSITORY_SERVICE", ILockService.class ) ); //$NON-NLS-1$

      }

      bindingFactory = new DefaultBindingFactory();
      bindingFactory.setDocument( getXulDomContainer().getDocumentRoot() );

      XulEventHandler eventHandler = getXulDomContainer().getEventHandler( "browseController" ); //$NON-NLS-1$

      if ( eventHandler instanceof BrowseController ) {
        browseController = (BrowseController) eventHandler;
      }

      // Disable row dragging if it is locked and the user does not have permissions
      fileTable = (XulTree) getXulDomContainer().getDocumentRoot().getElementById( "file-table" ); //$NON-NLS-1$
      folderTree = (XulTree) document.getElementById( "folder-tree" ); //$NON-NLS-1$
      lockFileMenuItem = (XulMenuitem) getXulDomContainer().getDocumentRoot().getElementById( "file-context-lock" ); //$NON-NLS-1$
      deleteFileMenuItem = (XulMenuitem) getXulDomContainer().getDocumentRoot().getElementById( "file-context-delete" ); //$NON-NLS-1$
      renameFileMenuItem = (XulMenuitem) getXulDomContainer().getDocumentRoot().getElementById( "file-context-rename" ); //$NON-NLS-1$

      messageBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$

      createBindings();
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  public void onDragFromGlobalTree( DropEvent event ) {
    Collection<Object> selectedItems = folderTree.getSelectedItems();
    if ( selectedItems.size() > 0 ) {
      for ( Object object : selectedItems ) {
        if ( object instanceof UIRepositoryDirectory ) {
          try {
            if ( !( repositoryDirectoryHasLockedObject( event, (UIRepositoryDirectory) object ) ) ) {
              // All the contents in the folder are not locked, check default permissions
              browseController.onDragFromGlobalTree( event );
            }
          } catch ( KettleException e ) {
            throw new RuntimeException( e );
          }
        }
      }
    }
  }

  private boolean repositoryDirectoryHasLockedObject( DropEvent event, UIRepositoryDirectory dir )
    throws KettleException {
    if ( areAnyRepositoryObjectsLocked( event, dir.getRepositoryObjects() ) ) {
      return true;
    }
    for ( UIRepositoryObject ro : dir.getChildren() ) {
      if ( ro instanceof UIRepositoryDirectory ) {
        UIRepositoryDirectory directory = (UIRepositoryDirectory) ro;
        repositoryDirectoryHasLockedObject( event, directory );
      }
    }
    return false;
  }

  private boolean areAnyRepositoryObjectsLocked( DropEvent event, UIRepositoryObjects repositoryObjects )
    throws KettleException {
    for ( UIRepositoryObject ro : repositoryObjects ) {
      if ( ro instanceof ILockObject ) {
        final UIRepositoryContent contentToLock = (UIRepositoryContent) ro;
        if ( ( (ILockObject) contentToLock ).isLocked() ) {
          // Content is locked, move is not allowed.
          event.setAccepted( false );
          messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
          messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
          messageBox.setMessage( BaseMessages.getString( PKG, "BrowseController.FolderMoveNotAllowed" ) ); //$NON-NLS-1$
          messageBox.open();
          return true;
        }
      }
    }
    return false;
  }

  // Object being dragged from the file listing table
  public void onDragFromLocalTable( DropEvent event ) {
    try {
      Collection<Object> selectedRepoObjects = fileTable.getSelectedItems();
      if ( selectedRepoObjects.size() > 0 ) {
        for ( Object ro : selectedRepoObjects ) {
          if ( ro instanceof UIRepositoryObject && ro instanceof ILockObject ) {
            final UIRepositoryContent contentToLock = (UIRepositoryContent) ro;
            if ( ( (ILockObject) contentToLock ).isLocked() ) {
              // Content is locked, not allowed to move
              event.setAccepted( false );
              messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
              messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
              messageBox.setMessage( BaseMessages.getString( PKG, "BrowseController.MoveNotAllowed" ) ); //$NON-NLS-1$
              messageBox.open();
              break;
            } else {
              // Content is not locked, check default permissions
              browseController.onDragFromLocalTable( event );
            }
          } else if ( ro instanceof UIRepositoryDirectory
              && repositoryDirectoryHasLockedObject( event, (UIRepositoryDirectory) ro ) ) {
            // locked content nested in this directory. move not allowed.
            event.setAccepted( false );
            break;
          }
        }
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }
  }

  private BindingConvertor<List<UIRepositoryObject>, Boolean> checkLockPermissions =
    new BindingConvertor<List<UIRepositoryObject>, Boolean>() {

      @Override
      public Boolean sourceToTarget( List<UIRepositoryObject> selectedRepoObjects ) {
        boolean result = false;

        try {
          if ( selectedRepoObjects.size() == 1 && selectedRepoObjects.get( 0 ) instanceof UIRepositoryDirectory ) {
            return true;
          } else if ( selectedRepoObjects.size() == 1 && selectedRepoObjects.get( 0 ) instanceof ILockObject ) {
            final UIRepositoryContent contentToLock = (UIRepositoryContent) selectedRepoObjects.get( 0 );

            if ( ( (ILockObject) contentToLock ).isLocked() ) {
              if ( repository instanceof PurRepository
                      //repository can be Proxy of repository and first part condition will fail
                      || repository.getRepositoryMeta() instanceof PurRepositoryMeta ) {
                result = service.canUnlockFileById( contentToLock.getObjectId() );
              } else {
                result =
                    ( (ILockObject) contentToLock ).getRepositoryLock().getLogin().equalsIgnoreCase(
                        repository.getUserInfo().getLogin() );
              }
            } else {
              // Content is not locked, permit locking
              result = true;
            }
          }
        } catch ( Exception e ) {
          throw new RuntimeException( e );
        }

        return result;
      }

      @Override
      public List<UIRepositoryObject> targetToSource( Boolean arg0 ) {
        return null;
      }
    };

  private BindingConvertor<List<UIRepositoryObject>, Boolean> forButtons =
    new BindingConvertor<List<UIRepositoryObject>, Boolean>() {

      @Override
      public Boolean sourceToTarget( List<UIRepositoryObject> value ) {
        return value != null && value.size() == 1;
      }

      @Override
      public List<UIRepositoryObject> targetToSource( Boolean value ) {
        return null;
      }
    };

  public String getName() {
    return "repositoryLockController"; //$NON-NLS-1$
  }

  private BindingConvertor<List<UIRepositoryObject>, Boolean> checkLockedStateBool =
    new BindingConvertor<List<UIRepositoryObject>, Boolean>() {

      @Override
      public Boolean sourceToTarget( List<UIRepositoryObject> value ) {
        boolean result = false;

        try {
          if ( value != null && value.size() == 1 && value.get( 0 ) != null ) {
            if ( value.get( 0 ) instanceof ILockObject ) {
              result = ( (ILockObject) value.get( 0 ) ).isLocked();
            }
          }
        } catch ( KettleException e ) {
          throw new RuntimeException( e );
        }

        return result;
      }

      @Override
      public List<UIRepositoryObject> targetToSource( Boolean value ) {
        return null;
      }
    };

  // This needs to exist until we have better method override support in the DefaultBinding
  private BindingConvertor<List<UIRepositoryObject>, Boolean> checkLockedState =
    new BindingConvertor<List<UIRepositoryObject>, Boolean>() {

      @Override
      public Boolean sourceToTarget( List<UIRepositoryObject> value ) {
        boolean result = false;

        try {
          if ( value != null && value.size() == 1 && value.get( 0 ) != null ) {
            if ( value.get( 0 ) instanceof ILockObject ) {
              result = ( (ILockObject) value.get( 0 ) ).isLocked();
            }
          }
        } catch ( KettleException e ) {
          throw new RuntimeException( e );
        }

        return result;
      }

      @Override
      public List<UIRepositoryObject> targetToSource( Boolean value ) {
        return null;
      }
    };

  protected void createBindings() {
    // Lock bindings

    bindingFactory.setBindingType( Type.ONE_WAY );
    bindingFactory.createBinding( browseController, "repositoryObjects", "lock-menu", "!disabled", forButtons ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bindingFactory.createBinding( browseController,
        "repositoryObjects", "file-context-lock", "selected", checkLockedState ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bindingFactory.createBinding( browseController, "repositoryObjects", this, "menuItemEnabledState" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bindingFactory.createBinding( browseController,
        "repositoryObjects", "file-context-locknotes", "!disabled", checkLockedStateBool ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bindingFactory.createBinding( browseController,
        "repositoryObjects", "lock-context-lock", "selected", checkLockedState ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bindingFactory.createBinding( browseController,
        "repositoryObjects", "lock-context-lock", "!disabled", checkLockPermissions ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    bindingFactory.createBinding( browseController,
        "repositoryObjects", "lock-context-locknotes", "!disabled", checkLockedStateBool ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public void lockContent() throws Exception {
    List<UIRepositoryObject> selectedRepoObjects = browseController.getSelectedFileItems();

    if ( selectedRepoObjects.size() > 0 && selectedRepoObjects.get( 0 ) instanceof UIRepositoryContent ) {
      final UIRepositoryContent contentToLock = (UIRepositoryContent) selectedRepoObjects.get( 0 );

      if ( ( (ILockObject) contentToLock ).isLocked() ) {
        // Unlock the item
        ( (ILockObject) contentToLock ).unlock();
        browseController.getSelectedItemsBinding().fireSourceChanged();
      } else {
        // Lock the item
        XulPromptBox lockNotePrompt = promptLockMessage( document, messages, null );
        lockNotePrompt.addDialogCallback( new XulDialogCallback<String>() {
          public void onClose( XulComponent component, Status status, String value ) {

            if ( !status.equals( Status.CANCEL ) ) {
              try {
                ( (ILockObject) contentToLock ).lock( value );
                browseController.getSelectedItemsBinding().fireSourceChanged();
              } catch ( Exception e ) {
                // convert to runtime exception so it bubbles up through the UI
                throw new RuntimeException( e );
              }
            } else {

              XulMenuitem lockMenuItem = (XulMenuitem) document.getElementById( "lock-context-lock" ); //$NON-NLS-1$
              lockMenuItem.setSelected( false );
              lockMenuItem = (XulMenuitem) document.getElementById( "file-context-lock" ); //$NON-NLS-1$
              lockMenuItem.setSelected( false );

            }
          }

          public void onError( XulComponent component, Throwable err ) {
            throw new RuntimeException( err );
          }
        } );

        lockNotePrompt.open();
      }
    }
  }

  public void viewLockNote() throws Exception {
    List<UIRepositoryObject> selectedRepoObjects = browseController.getSelectedFileItems();
    if ( selectedRepoObjects.size() > 0 && selectedRepoObjects.get( 0 ) instanceof UIRepositoryContent ) {
      final UIRepositoryContent contentToLock = (UIRepositoryContent) selectedRepoObjects.get( 0 );

      XulMessageBox msgBox = (XulMessageBox) document.createElement( "messagebox" ); //$NON-NLS-1$
      msgBox.setTitle( BaseMessages.getString( PKG, "PurRepository.LockNote.Title" ) ); //$NON-NLS-1$
      msgBox.setMessage( ( (ILockObject) contentToLock ).getLockMessage() );

      msgBox.open();
    }
  }

  private XulPromptBox promptLockMessage( org.pentaho.ui.xul.dom.Document document, ResourceBundle messages,
      String defaultMessage ) throws XulException {
    XulPromptBox prompt = (XulPromptBox) document.createElement( "promptbox" ); //$NON-NLS-1$

    prompt.setTitle( BaseMessages.getString( PKG, "RepositoryExplorer.LockMessage.Title" ) ); //$NON-NLS-1$
    prompt.setButtons( new DialogConstant[] { DialogConstant.OK, DialogConstant.CANCEL } );

    prompt.setMessage( BaseMessages.getString( PKG, "RepositoryExplorer.LockMessage.Label" ) ); //$NON-NLS-1$
    prompt.setValue( defaultMessage == null
        ? BaseMessages.getString( PKG, "RepositoryExplorer.DefaultLockMessage" ) : defaultMessage ); //$NON-NLS-1$
    return prompt;
  }

  public void setMenuItemEnabledState( List<UIRepositoryObject> selectedRepoObjects ) {
    try {
      boolean result = false;
      if ( selectedRepoObjects.size() == 1 && selectedRepoObjects.get( 0 ) instanceof UIRepositoryDirectory ) {
        lockFileMenuItem.setDisabled( true );
        deleteFileMenuItem.setDisabled( false );
        renameFileMenuItem.setDisabled( false );
      } else if ( selectedRepoObjects.size() == 1 && selectedRepoObjects.get( 0 ) instanceof ILockObject ) {
        final UIRepositoryContent contentToLock = (UIRepositoryContent) selectedRepoObjects.get( 0 );
        if ( ( (ILockObject) contentToLock ).isLocked() ) {
          if ( repository instanceof PurRepository
                  //repository can be Proxy of repository and first part condition will fail
                  || repository.getRepositoryMeta() instanceof PurRepositoryMeta ) {
            result = service.canUnlockFileById( contentToLock.getObjectId() );

          } else {
            result =
                ( (ILockObject) contentToLock ).getRepositoryLock().getLogin().equalsIgnoreCase(
                    repository.getUserInfo().getLogin() );
          }
          lockFileMenuItem.setDisabled( !result );
          deleteFileMenuItem.setDisabled( !result );
          renameFileMenuItem.setDisabled( !result );
        } else {
          lockFileMenuItem.setDisabled( false );
          deleteFileMenuItem.setDisabled( false );
          renameFileMenuItem.setDisabled( false );
        }
      } else {
        lockFileMenuItem.setDisabled( true );
        deleteFileMenuItem.setDisabled( true );
        renameFileMenuItem.setDisabled( true );
      }
    } catch ( Exception e ) {
      throw new RuntimeException( e );
    }

  }
}
