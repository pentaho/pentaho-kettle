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

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPointHandler;
import org.pentaho.di.core.extension.KettleExtensionPoint;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectory;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.pur.RepositoryObjectAccessException;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.repository.RepositoryExtension;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.IUIEEUser;
import org.pentaho.di.ui.repository.pur.repositoryexplorer.model.UIEERepositoryDirectory;
import org.pentaho.di.ui.repository.pur.services.ITrashService;
import org.pentaho.di.ui.repository.pur.services.ITrashService.IDeletedObject;
import org.pentaho.di.ui.repository.repositoryexplorer.ControllerInitializationException;
import org.pentaho.di.ui.repository.repositoryexplorer.controllers.BrowseController;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectories;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryDirectory;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObject;
import org.pentaho.di.ui.repository.repositoryexplorer.model.UIRepositoryObjects;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.Binding;
import org.pentaho.ui.xul.binding.BindingConvertor;
import org.pentaho.ui.xul.components.XulButton;
import org.pentaho.ui.xul.components.XulConfirmBox;
import org.pentaho.ui.xul.components.XulPromptBox;
import org.pentaho.ui.xul.containers.XulDeck;
import org.pentaho.ui.xul.containers.XulTree;
import org.pentaho.ui.xul.util.XulDialogCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class TrashBrowseController extends BrowseController implements java.io.Serializable {

  private static final long serialVersionUID = -3822571463115111325L; /* EESOURCE: UPDATE SERIALVERUID */

  // ~ Static fields/initializers ======================================================================================

  private static final Class<?> PKG = IUIEEUser.class;

  // ~ Instance fields =================================================================================================

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

  protected XulTree trashFileTable;

  protected XulDeck deck;

  protected List<UIDeletedObject> selectedTrashFileItems;

  protected TrashDirectory trashDir = new TrashDirectory();

  protected ITrashService trashService;

  protected List<IDeletedObject> trash;

  protected XulButton undeleteButton;

  protected XulButton deleteButton;

  // ~ Constructors ====================================================================================================

  public TrashBrowseController() {
    super();
  }

  // ~ Methods =========================================================================================================

  /**
   * Intercept the repositoryDirectory.children and add the Trash directory to the end.
   */
  @Override
  protected Binding createDirectoryBinding() {
    bf.setBindingType( Binding.Type.ONE_WAY );
    return bf.createBinding( this, "repositoryDirectory", folderTree, "elements", //$NON-NLS-1$//$NON-NLS-2$
        new BindingConvertor<UIRepositoryDirectory, UIRepositoryDirectory>() {

          @Override
          public UIRepositoryDirectory sourceToTarget( final UIRepositoryDirectory value ) {
            if ( value == null || value.size() == 0 ) {
              return null;
            }
            if ( !value.get( value.size() - 1 ).equals( trashDir ) ) {
              // add directly to children collection to bypass events
              value.getChildren().add( trashDir );
            }
            return value;
          }

          @Override
          public UIRepositoryDirectory targetToSource( final UIRepositoryDirectory value ) {
            // not used
            return value;
          }

        } );
  }

  protected class TrashDirectory extends UIEERepositoryDirectory {

    private static final long serialVersionUID = 6184312253116517468L;

    @Override
    public String getImage() {
      return "ui/images/trash_tree.svg"; //$NON-NLS-1$
    }

    @Override
    public String getName() {
      return BaseMessages.getString( PKG, "Trash" ); //$NON-NLS-1$
    }

    @Override
    public UIRepositoryDirectories getChildren() {
      return new UIRepositoryDirectories();
    }

    @Override
    public UIRepositoryObjects getRepositoryObjects() throws KettleException {
      return new UIRepositoryObjects();
    }
  }

  @Override
  public void init( Repository repository ) throws ControllerInitializationException {
    super.init( repository );
    try {
      trashService = (ITrashService) repository.getService( ITrashService.class );
    } catch ( Throwable e ) {
      throw new ControllerInitializationException( e );
    }
  }

  protected void doCreateBindings() {
    deck = (XulDeck) document.getElementById( "browse-tab-right-panel-deck" ); //$NON-NLS-1$

    trashFileTable = selectDeletedFileTable( repository.getUserInfo().isAdmin() );

    deleteButton = (XulButton) document.getElementById( "delete-button" ); //$NON-NLS-1$
    undeleteButton = (XulButton) document.getElementById( "undelete-button" ); //$NON-NLS-1$

    bf.setBindingType( Binding.Type.ONE_WAY );
    BindingConvertor<List<UIDeletedObject>, Boolean>
      buttonConverter =
      new BindingConvertor<List<UIDeletedObject>, Boolean>() {

        @Override public Boolean sourceToTarget( List<UIDeletedObject> value ) {
          if ( value != null && value.size() > 0 ) {
            return true;
          }
          return false;
        }

        @Override public List<UIDeletedObject> targetToSource( Boolean value ) {
          return null;
        }
      };

    createTrashTableBindings( buttonConverter, trashFileTable );
  }

  private XulTree selectDeletedFileTable( boolean isAdmin ) {
    XulDeck treeDeck = (XulDeck) document.getElementById( "tree-deck" );
    treeDeck.setSelectedIndex( isAdmin ? 1 : 0 );
    return (XulTree) document.getElementById( isAdmin ? "deleted-file-table-admin" : "deleted-file-table" );
  }

  private void createTrashTableBindings( BindingConvertor<List<UIDeletedObject>, Boolean> buttonConverter,
      XulTree trashFileTable ) {
    bf.createBinding( trashFileTable, "selectedItems", this, "selectedTrashFileItems" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( trashFileTable, "selectedItems", deleteButton, "!disabled", buttonConverter ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( trashFileTable, "selectedItems", undeleteButton, "!disabled", buttonConverter ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( trashFileTable, "selectedItems", "trash-context-delete", "!disabled", buttonConverter ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
    bf.createBinding( trashFileTable, "selectedItems", "trash-context-restore", "!disabled", buttonConverter ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    bf.setBindingType( Binding.Type.ONE_WAY );
    bf.createBinding( this, "trash", trashFileTable, "elements", //$NON-NLS-1$  //$NON-NLS-2$
        new BindingConvertor<List<IDeletedObject>, List<UIDeletedObject>>() {
          @Override
          public List<UIDeletedObject> sourceToTarget( List<IDeletedObject> trash ) {
            List<UIDeletedObject> newList = new ArrayList<UIDeletedObject>( trash.size() );
            for ( IDeletedObject obj : trash ) {
              newList.add( new UIDeletedObject( obj ) );
            }
            Collections.sort( newList, new UIDeletedObjectComparator() );
            return newList;
          }

          @Override
          public List<IDeletedObject> targetToSource( List<UIDeletedObject> elements ) {
            return null;
          }
        } );
  }

  /**
   * An IDeletedObject that is also a XulEventSource.
   */
  public static class UIDeletedObject extends XulEventSourceAdapter {

    private IDeletedObject obj;

    private static Comparator<UIDeletedObject> comparator = new UIDeletedObjectComparator();

    public UIDeletedObject( final IDeletedObject obj ) {
      this.obj = obj;
    }

    public String getOriginalParentPath() {
      return obj.getOriginalParentPath();
    }

    public String getDeletedDate() {
      Date date = obj.getDeletedDate();
      String str = null;
      if ( date != null ) {
        SimpleDateFormat sdf = new SimpleDateFormat( "d MMM yyyy HH:mm:ss z" ); //$NON-NLS-1$
        str = sdf.format( date );
      }
      return str;
    }

    public String getType() {
      return obj.getType();
    }

    public ObjectId getId() {
      return obj.getId();
    }

    public String getName() {
      return obj.getName();
    }

    public String getOwner() {
      return obj.getOwner();
    }

    public String getImage() {
      if ( RepositoryObjectType.TRANSFORMATION.name().equals( obj.getType() ) ) {
        return "ui/images/transformation_tree.svg"; //$NON-NLS-1$
      } else if ( RepositoryObjectType.JOB.name().equals( obj.getType() ) ) {
        return "ui/images/job_tree.svg"; //$NON-NLS-1$
      } else {
        return "ui/images/folder.svg"; //$NON-NLS-1$
      }
    }

    public Comparator<UIDeletedObject> getComparator() {
      return comparator;
    }

  }

  public static class UIDeletedObjectComparator implements Comparator<UIDeletedObject> {

    public int compare( UIDeletedObject o1, UIDeletedObject o2 ) {
      int cat1 = getValue( o1.getType() );
      int cat2 = getValue( o2.getType() );
      if ( cat1 != cat2 ) {
        return cat1 - cat2;
      }
      String t1 = o1.getName();
      String t2 = o2.getName();
      if ( t1 == null ) {
        t1 = ""; //$NON-NLS-1$
      }
      if ( t2 == null ) {
        t2 = ""; //$NON-NLS-1$
      }
      return t1.compareToIgnoreCase( t2 );
    }

    private int getValue( final String type ) {
      if ( type == null ) {
        return 10;
      } else {
        return 20;
      }
    }

  }

  @Override
  public void setSelectedFolderItems( List<UIRepositoryDirectory> selectedFolderItems ) {
    if ( selectedFolderItems != null && selectedFolderItems.size() == 1
        && selectedFolderItems.get( 0 ).equals( trashDir ) ) {
      try {
        setTrash( trashService.getTrash() );
      } catch ( KettleException e ) {
        if ( mainController == null || !mainController.handleLostRepository( e ) ) {
          throw new RuntimeException( e );
        }
      }
      deck.setSelectedIndex( 1 );
    } else {
      deck.setSelectedIndex( 0 );
      super.setSelectedFolderItems( selectedFolderItems );
    }
  }

  public void setTrash( List<IDeletedObject> trash ) {
    this.trash = trash;
    firePropertyChange( "trash", null, trash ); //$NON-NLS-1$
  }

  public List<IDeletedObject> getTrash() {
    return trash;
  }

  @Override
  protected void moveFiles( List<UIRepositoryObject> objects, UIRepositoryDirectory targetDirectory ) throws Exception {
    // If we're moving into the trash it's really a delete
    if ( targetDirectory != trashDir ) {
      super.moveFiles( objects, targetDirectory );
    } else {
      for ( UIRepositoryObject o : objects ) {
        deleteContent( o );
      }
    }
  }

  public void delete() {
    if ( selectedTrashFileItems != null && selectedTrashFileItems.size() > 0 ) {
      Callable<Void> deleteCallable = () -> {
        List<ObjectId> ids = selectedTrashFileItems.stream().map( obj -> obj.getId() ).collect( Collectors.toList() );
        trashService.delete( ids );
        setTrash( trashService.getTrash() );
        return null;
      };
      try {
        confirmDialog( deleteCallable );
      } catch ( Exception e ) {
        if ( !handleRepositoryLost( e ) ) {
          displayExceptionMessage( getMsg( "TrashBrowseController.UnableToDeleteFile", e.getLocalizedMessage() ) );
        }
      }
    }
  }

  private boolean handleRepositoryLost( Throwable th ) {
    return mainController != null && mainController.handleLostRepository( th );
  }

  private void confirmDialog( Callable<Void> callback ) throws Exception {
    String title = getMsg( "TrashBrowseController.RemoveDeleted.Title" );
    String msg = getMsg( "TrashBrowseController.RemoveDeleted.ConfirmationMessage" );
    String yes = getMsg( "TrashBrowseController.RemoveDeleted.Yes" );
    String no = getMsg( "TrashBrowseController.RemoveDeleted.No" );
    confirmDialog( callback, title, msg, yes, no );
  }

  private String getMsg( String key, String...params ) {
    return BaseMessages.getString( PKG, key, params );
  }

  public void undelete() {
    // make a copy because the selected trash items changes as soon as trashService.undelete is called
    List<UIDeletedObject> selectedTrashFileItemsSnapshot = new ArrayList<UIDeletedObject>( selectedTrashFileItems );
    if ( selectedTrashFileItemsSnapshot != null && selectedTrashFileItemsSnapshot.size() > 0 ) {
      List<ObjectId> ids = new ArrayList<ObjectId>();
      for ( UIDeletedObject uiObj : selectedTrashFileItemsSnapshot ) {
        ids.add( uiObj.getId() );
      }
      try {
        trashService.undelete( ids );
        setTrash( trashService.getTrash() );
        for ( UIDeletedObject uiObj : selectedTrashFileItemsSnapshot ) {
          // find the closest UIRepositoryDirectory that is in the dirMap
          RepositoryDirectoryInterface dir = repository.findDirectory( uiObj.getOriginalParentPath() );
          while ( dir != null && dirMap.get( dir.getObjectId() ) == null ) {
            dir = dir.getParent();
          }
          // now refresh that UIRepositoryDirectory so that the file/folders deck instantly refreshes on undelete
          if ( dir != null ) {
            dirMap.get( dir.getObjectId() ).refresh();
          }
          // if transformation or directory with transformations call extension to restore data services references.
          if ( RepositoryObjectType.TRANSFORMATION.name().equals( uiObj.getType() ) ) {
            TransMeta transMeta = repository.loadTransformation( uiObj.getId(), null );
            ExtensionPointHandler
                .callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.TransAfterOpen.id, transMeta );
            transMeta.clearChanged();
          } else if ( !RepositoryObjectType.JOB.name().equals( uiObj.getType() ) ) {
            // if not a transformation and not a job then is a Directory
            RepositoryDirectoryInterface
                actualDir =
                repository.findDirectory(
                    uiObj.getOriginalParentPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + uiObj.getName() );
            if ( actualDir != null ) {
              List<RepositoryElementMetaInterface> transformations = new ArrayList<>();
              getAllTransformations( actualDir, transformations );
              for ( RepositoryElementMetaInterface repositoryElementMetaInterface : transformations ) {
                TransMeta transMeta = repository.loadTransformation( repositoryElementMetaInterface.getObjectId(), null );
                ExtensionPointHandler
                    .callExtensionPoint( LogChannel.GENERAL, KettleExtensionPoint.TransAfterOpen.id, transMeta );
                transMeta.clearChanged();
              }
            } else {
              displayExceptionMessage( BaseMessages.getString( PKG, "TrashBrowseController.UnableToRestoreDirectory",
                  uiObj.getOriginalParentPath() + RepositoryDirectory.DIRECTORY_SEPARATOR + uiObj.getName() ) );
            }
          }
        }
        deck.setSelectedIndex( 1 );
      } catch ( Throwable th ) {
        if ( mainController == null || !mainController.handleLostRepository( th ) ) {
          displayExceptionMessage( BaseMessages.getString( PKG,
              "TrashBrowseController.UnableToRestoreFile", th.getLocalizedMessage() ) ); //$NON-NLS-1$
        }
      }
    } else {
      // ui probably allowed the button to be enabled when it shouldn't have been enabled
      throw new RuntimeException();
    }
  }

  private static void getAllTransformations( RepositoryDirectoryInterface repositoryObject,
      List<RepositoryElementMetaInterface> objectsTransformations ) throws KettleException {
    //test if has sub-directories
    if ( repositoryObject.getChildren() != null && repositoryObject.getChildren().size() > 0 ) {
      for ( RepositoryDirectoryInterface subDirectory : repositoryObject.getChildren() ) {
        getAllTransformations( subDirectory, objectsTransformations );
      }
    }
    //getting all the transformations
    repositoryObject.getRepositoryObjects().stream()
        .filter( e -> RepositoryObjectType.TRANSFORMATION.equals( e.getObjectType() ) )
        .forEach( objectsTransformations::add );
  }

  public void setSelectedTrashFileItems( List<UIDeletedObject> selectedTrashFileItems ) {
    this.selectedTrashFileItems = selectedTrashFileItems;
  }

  @Override
  protected void deleteFolder( UIRepositoryDirectory repoDir ) throws Exception {
    deleteContent( repoDir );
  }

  @Override
  protected void deleteContent( final UIRepositoryObject repoObject ) throws Exception {
    try {
      try {
        ExtensionPointHandler.callExtensionPoint( LogChannel.GENERAL,
            KettleExtensionPoint.AfterDeleteRepositoryObject.id, new RepositoryExtension( repoObject ) );
      } catch ( Exception ex ) {
        LogChannel.GENERAL.logError( "Error calling AfterDeleteRepositoryObject extension point", ex );
      }

      repoObject.delete();
    } catch ( KettleException ke ) {
      if ( repoDir != null ) {
        repoDir.refresh();
      }
      if ( ke.getCause() instanceof RepositoryObjectAccessException ) {
        moveDeletePrompt( ke, repoObject, new XulDialogCallback<Object>() {

          public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
            if ( returnCode == Status.ACCEPT ) {
              try {
                ( (UIEERepositoryDirectory) repoObject ).delete( true );
              } catch ( Exception e ) {
                if ( mainController == null || !mainController.handleLostRepository( e ) ) {
                  displayExceptionMessage( BaseMessages.getString( PKG, e.getLocalizedMessage() ) );
                }
              }
            }
          }

          public void onError( XulComponent sender, Throwable t ) {
            if ( mainController == null || !mainController.handleLostRepository( t ) ) {
              throw new RuntimeException( t );
            }
          }

        } );
      } else {
        if ( mainController == null || !mainController.handleLostRepository( ke ) ) {
          throw ke;
        }
      }
    }

    if ( repoObject instanceof UIRepositoryDirectory ) {
      directoryBinding.fireSourceChanged();
      if ( repoDir != null ) {
        repoDir.refresh();
      }
    }
    selectedItemsBinding.fireSourceChanged();
  }

  @Override
  protected void renameRepositoryObject( final UIRepositoryObject repoObject ) throws XulException {
    // final Document doc = document;
    XulPromptBox prompt = promptForName( repoObject );
    prompt.addDialogCallback( new XulDialogCallback<String>() {
      public void onClose( XulComponent component, Status status, String value ) {
        if ( status == Status.ACCEPT ) {
          final String newName = value;
          try {
            repoObject.setName( newName );
          } catch ( KettleException ke ) {
            if ( ke.getCause() instanceof RepositoryObjectAccessException ) {
              moveDeletePrompt( ke, repoObject, new XulDialogCallback<Object>() {

                public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
                  if ( returnCode == Status.ACCEPT ) {
                    try {
                      ( (UIEERepositoryDirectory) repoObject ).setName( newName, true );
                    } catch ( Exception e ) {
                      if ( mainController == null || !mainController.handleLostRepository( e ) ) {
                        displayExceptionMessage( BaseMessages.getString( PKG, e.getLocalizedMessage() ) );
                      }
                    }
                  }
                }

                public void onError( XulComponent sender, Throwable t ) {
                  if ( mainController == null || !mainController.handleLostRepository( t ) ) {
                    throw new RuntimeException( t );
                  }
                }

              } );
            } else {
              if ( mainController == null || !mainController.handleLostRepository( ke ) ) {
                throw new RuntimeException( ke );
              }
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

    prompt.open();
  }

  protected boolean moveDeletePrompt( final KettleException ke, final UIRepositoryObject repoObject,
      final XulDialogCallback<Object> action ) {
    if ( ke.getCause() instanceof RepositoryObjectAccessException
        && ( (RepositoryObjectAccessException) ke.getCause() ).getObjectAccessType().equals(
            RepositoryObjectAccessException.AccessExceptionType.USER_HOME_DIR )
        && repoObject instanceof UIEERepositoryDirectory ) {

      try {
        confirmBox = (XulConfirmBox) document.createElement( "confirmbox" ); //$NON-NLS-1$
        confirmBox.setTitle( BaseMessages.getString( PKG, "TrashBrowseController.DeleteHomeFolderWarningTitle" ) ); //$NON-NLS-1$
        confirmBox.setMessage( BaseMessages.getString( PKG, "TrashBrowseController.DeleteHomeFolderWarningMessage" ) ); //$NON-NLS-1$
        confirmBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
        confirmBox.setCancelLabel( BaseMessages.getString( PKG, "Dialog.Cancel" ) ); //$NON-NLS-1$
        confirmBox.addDialogCallback( new XulDialogCallback<Object>() {

          public void onClose( XulComponent sender, Status returnCode, Object retVal ) {
            if ( returnCode == Status.ACCEPT ) {
              action.onClose( sender, returnCode, retVal );
            }
          }

          public void onError( XulComponent sender, Throwable t ) {
            if ( mainController == null || !mainController.handleLostRepository( t ) ) {
              throw new RuntimeException( t );
            }
          }
        } );
        confirmBox.open();
      } catch ( Exception e ) {
        if ( mainController == null || !mainController.handleLostRepository( e ) ) {
          throw new RuntimeException( e );
        }
      }
    }
    return false;
  }

  protected void displayExceptionMessage( String msg ) {
    messageBox.setTitle( BaseMessages.getString( PKG, "Dialog.Error" ) ); //$NON-NLS-1$
    messageBox.setAcceptLabel( BaseMessages.getString( PKG, "Dialog.Ok" ) ); //$NON-NLS-1$
    messageBox.setMessage( msg );
    messageBox.open();
  }

}
