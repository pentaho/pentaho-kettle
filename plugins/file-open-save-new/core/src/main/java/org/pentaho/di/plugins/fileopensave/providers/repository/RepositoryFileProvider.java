/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.providers.repository;

import org.apache.commons.io.FilenameUtils;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.plugins.fileopensave.api.file.FileDetails;
import org.pentaho.di.plugins.fileopensave.api.overwrite.OverwriteStatus;
import org.pentaho.di.plugins.fileopensave.api.providers.BaseFileProvider;
import org.pentaho.di.plugins.fileopensave.api.providers.EntityType;
import org.pentaho.di.plugins.fileopensave.api.providers.File;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.FileExistsException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileOperationException;
import org.pentaho.di.plugins.fileopensave.api.providers.exception.InvalidFileTypeException;
import org.pentaho.di.plugins.fileopensave.controllers.RepositoryBrowserController;
import org.pentaho.di.plugins.fileopensave.dragdrop.Element;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryDirectory;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryObjectId;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryTree;
import org.pentaho.di.plugins.fileopensave.util.Util;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.repository.StringObjectId;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.FileDialogOperation;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 2/14/19.
 */
public class RepositoryFileProvider extends BaseFileProvider<RepositoryFile> {

  public static final String PENTAHO_ENTERPRISE_REPOSITORY = "PentahoEnterpriseRepository";
  public static Repository repository;
  public static final String TRANSFORMATION = "transformation";
  public static final String JOB = "job";
  public static final String FOLDER = "folder";
  public static final String FILTER = "*.ktr|*.kjb";

  private RepositoryDirectoryInterface rootDirectory;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  @Override public Class<RepositoryFile> getFileClass() {
    return RepositoryFile.class;
  }

  public static final String NAME = "Pentaho Repository";
  public static final String TYPE = "repository";

  @Override public String getName() {
    return NAME;
  }

  @Override public String getType() {
    return TYPE;
  }

  @Override public RepositoryTree getTree() {
    RepositoryTree repositoryTree = new RepositoryTree( NAME );
    repositoryTree.setChildren( loadDirectoryTree().getChildren() );
    return repositoryTree;
  }

  @Override
  public List<RepositoryFile> getFiles( RepositoryFile file, String filters, VariableSpace space ) {
    RepositoryDirectoryInterface repositoryDirectoryInterface =
      findDirectory( file.getType().equalsIgnoreCase( RepositoryDirectory.DIRECTORY ) ? file.getPath() : file.getParent() );

    RepositoryDirectory repositoryDirectory = RepositoryDirectory.build( null, repositoryDirectoryInterface );
    populateFolders( repositoryDirectory, repositoryDirectoryInterface );
    try {
      populateFiles( repositoryDirectory, repositoryDirectoryInterface, FILTER );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }
    return repositoryDirectory.getChildren();
  }

  @Override public boolean isAvailable() {
    return spoonSupplier.get() != null && spoonSupplier.get().rep != null;
  }

  // TODO: (Result) objects should be created at the endpoint and these should throw appropriate exceptions
  @Override
  public List<RepositoryFile> delete( List<RepositoryFile> files, VariableSpace space ) {
    List<RepositoryFile> deletedFiles = new ArrayList<>();
    for ( RepositoryFile repositoryFile : files ) {
      try {
        if ( deleteFile( repositoryFile ) ) {
          deletedFiles.add( repositoryFile );
        }
      } catch ( Exception ignored ) {
        // Ignore don't add
      }
    }
    return deletedFiles;
  }

  private void deleteJob( RepositoryFile repositoryFile ) throws KettleException {
    StringObjectId objectId = new StringObjectId( repositoryFile.getObjectId() );
    if ( isJobOpened( repositoryFile.getObjectId(), repositoryFile.getParent(), repositoryFile.getName() ) ) {
      throw new KettleJobException();
    }
    getRepository().deleteJob( objectId );
  }

  private void deleteTrans( RepositoryFile repositoryFile ) throws KettleException {
    StringObjectId objectId = new StringObjectId( repositoryFile.getObjectId() );
    if ( isTransOpened( repositoryFile.getObjectId(), repositoryFile.getParent(), repositoryFile.getName() ) ) {
      throw new KettleTransException();
    }
    getRepository().deleteTransformation( objectId );
  }

  private void deleteFolder( RepositoryFile repositoryFile ) throws KettleException {
    isFileOpenedInFolder( repositoryFile.getPath() );
    // TODO: Handle recents at a higher level
    //          removeRecentsUsingPath( path );
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( repositoryFile.getPath() );
    if ( getRepository() instanceof RepositoryExtended ) {
      ( (RepositoryExtended) getRepository() ).deleteRepositoryDirectory( repositoryDirectoryInterface, true );
    } else {
      getRepository().deleteRepositoryDirectory( repositoryDirectoryInterface );
    }
  }

  private boolean deleteFile( RepositoryFile repositoryFile ) throws Exception {
    try {
      switch ( repositoryFile.getType() ) {
        case JOB:
          deleteJob( repositoryFile );
          break;
        case TRANSFORMATION:
          deleteTrans( repositoryFile );
          break;
        case FOLDER:
          deleteFolder( repositoryFile );
          break;
      }
      return true;
    } catch ( KettleTransException | KettleJobException ke ) {
      throw ke;
    } catch ( Exception e ) {
      return false;
    }
  }

  private boolean isTransOpened( String id, String path, String name ) {
    List<TransMeta> openedTransFiles = getSpoon().delegates.trans.getTransformationList();
    for ( TransMeta t : openedTransFiles ) {
      if ( t.getObjectId() != null && id.equals( t.getObjectId().getId() )
        || ( path.equals( t.getRepositoryDirectory().getPath() ) && name.equals( t.getName() ) ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean isJobOpened( String id, String path, String name ) {
    List<JobMeta> openedJobFiles = getSpoon().delegates.jobs.getJobList();
    for ( JobMeta j : openedJobFiles ) {
      if ( j.getObjectId() != null && id.equals( j.getObjectId().getId() )
        || ( path.equals( j.getRepositoryDirectory().getPath() ) && name.equals( j.getName() ) ) ) {
        return true;
      }
    }
    return false;
  }

  private void isFileOpenedInFolder( String path ) throws KettleException {
    List<TransMeta> openedTransFiles = getSpoon().delegates.trans.getTransformationList();
    for ( TransMeta t : openedTransFiles ) {
      if ( t.getRepositoryDirectory().getPath() != null
        && ( t.getRepositoryDirectory().getPath() + "/" ).startsWith( path + "/" ) ) {
        throw new KettleTransException();
      }
    }
    List<JobMeta> openedJobFiles = getSpoon().delegates.jobs.getJobList();
    for ( JobMeta j : openedJobFiles ) {
      if ( j.getRepositoryDirectory().getPath() != null
        && ( j.getRepositoryDirectory().getPath() + "/" ).startsWith( path + "/" ) ) {
        throw new KettleJobException();
      }
    }
  }

  @Override
  public RepositoryFile add( RepositoryFile folder, VariableSpace space ) throws FileException {
    if ( hasDupeFolder( folder.getParent(), folder.getName() ) ) {
      throw new FileExistsException();
    }
    try {
      RepositoryDirectoryInterface repositoryDirectoryInterface =
        getRepository().createRepositoryDirectory( findDirectory( folder.getParent() ), folder.getName() );
      return RepositoryDirectory.build( folder.getParent(), repositoryDirectoryInterface );
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Checks if there is a duplicate folder in a given directory (i.e. hidden folder)
   *
   * @param parent - Parent directory
   * @param name   - Name of folder
   * @return - true if the parent directory has a folder equal to name, false otherwise
   */
  private boolean hasDupeFolder( String parent, String name ) {
    try {
      RepositoryDirectoryInterface rdi = getRepository().findDirectory( parent ).findChild( name );
      return rdi != null;
    } catch ( Exception e ) {
      return false;
    }
  }

  // TODO: Handle recents on rename/delete/etc.
  @Override public RepositoryFile rename( RepositoryFile file, String newPath, OverwriteStatus overwriteStatus, VariableSpace space ) {
    String newName = newPath.substring( newPath.lastIndexOf( "/" ) + 1 );
    try {
      return doRename( file, newName );
    } catch ( KettleException e ) {
      return null;
    }
  }

  // TODO: Make this actually work
  public RepositoryFile doRename( RepositoryFile file, String newName ) throws KettleException {
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( file.getParent() );
    ObjectId objectId = null;
    switch ( file.getType() ) {
      case JOB:
        if ( getRepository().exists( newName, repositoryDirectoryInterface, RepositoryObjectType.JOB ) ) {
          throw new KettleObjectExistsException();
        }
        if ( isJobOpened( file.getObjectId(), file.getParent(), file.getName() ) ) {
          throw new KettleJobException();
        }
        objectId = getRepository().renameJob( file::getObjectId, repositoryDirectoryInterface, newName );
        break;
      case TRANSFORMATION:
        if ( getRepository().exists( newName, repositoryDirectoryInterface, RepositoryObjectType.TRANSFORMATION ) ) {
          throw new KettleObjectExistsException();
        }
        if ( isTransOpened( file.getObjectId(), file.getParent(), file.getName() ) ) {
          throw new KettleJobException();
        }
        objectId = getRepository().renameTransformation( file::getObjectId, repositoryDirectoryInterface, newName );
        break;
      case FOLDER:
        isFileOpenedInFolder( file.getPath() );
        RepositoryDirectoryInterface parent = findDirectory( file.getPath() ).getParent();
        if ( parent == null ) {
          parent = findDirectory( file.getPath() );
        }
        RepositoryDirectoryInterface child = parent.findChild( newName );
        if ( child != null ) {
          throw new KettleObjectExistsException();
        }
        if ( getRepository() instanceof RepositoryExtended ) {
          objectId =
            ( (RepositoryExtended) getRepository() )
              .renameRepositoryDirectory( file::getObjectId, null, newName, true );
        } else {
          objectId = getRepository().renameRepositoryDirectory( file::getObjectId, null, newName );
        }
        break;
    }
    RepositoryFile repositoryFile = new RepositoryFile();
    return repositoryFile;
  }

  @Override public RepositoryFile move( RepositoryFile file, String toPath,
                                        OverwriteStatus overwriteStatus, VariableSpace space ) {
    return null;
  }

  private RepositoryElementInterface getObject( String objectId, String type ) {
    if ( type.equals( TRANSFORMATION ) ) {
      try {
        return getRepository().loadTransformation( () -> objectId, null );
      } catch ( KettleException e ) {
        return null;
      }
    } else if ( type.equals( JOB ) ) {
      try {
        return getRepository().loadJob( () -> objectId, null );
      } catch ( KettleException e ) {
        return null;
      }
    }
    return null;
  }

  @Override public RepositoryFile copy( RepositoryFile file, String toPath,
                                        OverwriteStatus overwriteStatus, VariableSpace space ) throws FileException {
    overwriteStatus.setCurrentFileInProgressDialog( file.getPath() );
    boolean destExists = false;
    String destinationObjectId = null;

    RepositoryFile toFileParent = getFile( file, Util.getFolder( toPath ), EntityType.REPOSITORY_DIRECTORY, space );
    if ( fileExists(toFileParent, toPath, space ) ){
      destExists = true;
      RepositoryFile toFile = getFile( file, toPath, file.getEntityType(), space );
      destinationObjectId = toFile.getObjectId();
    }

    //Even if we do not have a duplicate we have to make this call to reset the mode, if not apply to all
    overwriteStatus.promptOverwriteIfNecessary( destExists, toPath, file.getEntityType().isDirectory() ? FOLDER : "file" );
    if ( overwriteStatus.isCancel() || overwriteStatus.isSkip() ) {
      return null;
    }
    if ( overwriteStatus.isRename() ) {
      String newDestination = getNewName( toFileParent, toPath, space );
      toPath = newDestination;
      destinationObjectId = null;
    }
    if ( RepositoryDirectory.DIRECTORY.equals( file.getType() ) ) {
      try {
        RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( toPath );
        if ( repositoryDirectoryInterface == null ) {
          //Create the folder
          createDirectory( Util.getFolder( toPath ), file, Util.getName( toPath ) );
        }
        //Loop thru the children
        List<RepositoryFile> children = getFiles( file, null, space );
        for ( RepositoryFile child : children ) {
          copy( child, toPath + "/" + child.getName(), overwriteStatus, space );

        }
      } catch ( KettleException e ) {
        e.printStackTrace();
      }
    } else{
      RepositoryElementInterface repositoryElementInterface = getObject( file.getObjectId(), file.getType() );
      if ( repositoryElementInterface != null ) {
        repositoryElementInterface.setName( Util.getName( toPath ) );
        repositoryElementInterface.setRepositoryDirectory( findDirectory( Util.getFolder( toPath ) ) );
        repositoryElementInterface.setObjectId( new RepositoryObjectId( destinationObjectId ) );
        try {
          getRepository().save( repositoryElementInterface, null, null );
        } catch ( KettleException e ) {
          e.printStackTrace();
        }
      } else {
        throw new InvalidFileOperationException();
      }
    }
    RepositoryFile repositoryFile = new RepositoryFile();
    return repositoryFile;
  }

  @Override public boolean fileExists( RepositoryFile dir, String path, VariableSpace space ) {
    RepositoryDirectoryInterface directoryInterface;
    try {
      //Only returns non-null if the path is not a folder
      directoryInterface = getRepository().findDirectory( dir.getPath() );
      if ( directoryInterface == null ) {
        return false;
      }
    } catch ( KettleException e ) {
      return true;
    }
    try {
      if ( path.endsWith( ".ktr" ) ) {
        return getRepository().exists( Util.getName( path ), directoryInterface, RepositoryObjectType.TRANSFORMATION );
      } else if ( path.endsWith( ".kjb" ) ) {
        return getRepository().exists( Util.getName( path ), directoryInterface, RepositoryObjectType.JOB );
      } else {
        // exists doesn't work with directories in the pur repository
        return getRepository().findDirectory( path ) != null;
      }
    } catch ( KettleException e ) {
      return true;
    }

  }

  @Override
  public boolean isSame( org.pentaho.di.plugins.fileopensave.api.providers.File file1,
                         org.pentaho.di.plugins.fileopensave.api.providers.File file2 ) {
    return file1.getProvider().equals( file2.getProvider() );
  }

  @Override public InputStream readFile( RepositoryFile file, VariableSpace space ) throws FileException {
    RepositoryElementInterface repositoryElementInterface = getObject( file.getObjectId(), file.getType() );
    if ( repositoryElementInterface != null ) {
      String xml = null;
      if ( repositoryElementInterface instanceof TransMeta ) {
        TransMeta transMeta = (TransMeta) repositoryElementInterface;
        try {
          xml = transMeta.getXML();
        } catch ( KettleException e ) {
          return null;
        }
      }
      if ( repositoryElementInterface instanceof JobMeta ) {
        JobMeta jobMeta = (JobMeta) repositoryElementInterface;
        xml = jobMeta.getXML();
      }
      if ( xml != null ) {
        return new ByteArrayInputStream( xml.getBytes( StandardCharsets.UTF_8 ) );
      }
    }
    return null;
  }

  @Override
  public RepositoryFile writeFile( InputStream inputStream, RepositoryFile destDir, String path,
                                   OverwriteStatus overwriteStatus, VariableSpace space )
    throws FileException {
    RepositoryObjectType type = getType( path );
    String name = Util.getName( path ).replace( " ", "_" );
    RepositoryElementInterface repositoryElementInterface = null;
    if ( type != null ) {
      if ( type.equals( RepositoryObjectType.TRANSFORMATION ) ) {
        try {
          repositoryElementInterface = new TransMeta( inputStream, null, false, null, null );
        } catch ( KettleException e ) {
          return null;
        }
      } else if ( type.equals( RepositoryObjectType.JOB ) ) {
        try {
          repositoryElementInterface = new JobMeta( inputStream, null, null );
        } catch ( KettleException e ) {
          return null;
        }
      } else {
        throw new InvalidFileTypeException();
      }
    }
    try {
      RepositoryDirectoryInterface directoryInterface = getRepository().findDirectory( destDir.getPath() );
      if ( repositoryElementInterface != null ) {
        repositoryElementInterface.setRepositoryDirectory( directoryInterface );
        getRepository().save( repositoryElementInterface, null, null );
        // The save worked, now we have to return a RepositoryFile
        return getFile( destDir, path, EntityType.REPOSITORY_FILE, space );
      }
      return null;
    } catch ( KettleException e ) {
      return null;
    }
  }

  private RepositoryObjectType getType( String path ) {
    if ( path.endsWith( ".ktr" ) ) {
      return RepositoryObjectType.TRANSFORMATION;
    }
    if ( path.endsWith( ".kjb" ) ) {
      return RepositoryObjectType.JOB;
    }
    return null;
  }

  @Override public String getNewName( RepositoryFile destDir, String newPath, VariableSpace space ) {
    RepositoryDirectoryInterface directoryInterface = null;
    RepositoryObjectType type = getType( newPath );
    try {
      directoryInterface = getRepository().findDirectory( destDir.getPath() );
    } catch ( KettleException e ) {
      return newPath;
    }
    int index = 1;
    String name = Util.getName( newPath );
    String test = name + "_" + index;
    if ( directoryInterface != null && type != null ) {
      try {
        while ( getRepository().exists( test, directoryInterface, type ) ) {
          test = name + "_" + ++index;
        }
      } catch ( KettleException e ) {
        return test;
      }
    }
    test = newPath.replace( name, test );
    return test;
  }

  public RepositoryTree loadDirectoryTree() {
    if ( getRepository() != null ) {
      try {
        if ( getRepository() instanceof RepositoryExtended ) {
          rootDirectory = ( (RepositoryExtended) getRepository() ).loadRepositoryDirectoryTree( false );
        } else {
          rootDirectory = getRepository().loadRepositoryDirectoryTree();
        }
        RepositoryTree repositoryTree = new RepositoryTree( null );
        RepositoryDirectory repositoryDirectory = RepositoryDirectory.build( null, rootDirectory );
        populateFolders( repositoryDirectory, rootDirectory );
        boolean isPentahoRepository =
          getRepository().getRepositoryMeta().getId().equals( PENTAHO_ENTERPRISE_REPOSITORY );
        if ( !isPentahoRepository ) {
          populateFiles( repositoryDirectory, rootDirectory, FILTER );
          repositoryTree.addChild( repositoryDirectory );
        } else {
          for ( RepositoryFile child : repositoryDirectory.getChildren() ) {
            repositoryTree.addChild( child );
          }
        }
        return repositoryTree;
      } catch ( Exception e ) {
        return null;
      }
    }
    return null;
  }

  private void populateFiles( RepositoryDirectory repositoryDirectory,
                              RepositoryDirectoryInterface repositoryDirectoryInterface, String filter )
    throws KettleException {
    if ( getRepository() instanceof RepositoryExtended && !repositoryDirectory.getPath().equals( "/" ) ) {
      populateFilesLazy( repositoryDirectory, filter );
    } else {
      Date latestDate = null;
      for ( RepositoryObjectInterface repositoryObject : getRepositoryElements( repositoryDirectoryInterface ) ) {
        org.pentaho.di.repository.RepositoryObject ro = (org.pentaho.di.repository.RepositoryObject) repositoryObject;
        String extension = ro.getObjectType().getExtension();
        if ( !Util.isFiltered( extension, filter ) ) {
          RepositoryFile repositoryFile = RepositoryFile.build( ro );
          repositoryDirectory.addChild( repositoryFile );
        }
        if ( latestDate == null || ro.getModifiedDate().after( latestDate ) ) {
          latestDate = ro.getModifiedDate();
        }
      }
      repositoryDirectory.setDate( latestDate );
    }
  }

  public void populateFilesLazy( RepositoryDirectory repositoryDirectory, String filter ) {
    RepositoryRequest repositoryRequest = new RepositoryRequest();
    repositoryRequest.setPath( repositoryDirectory.getPath() );
    repositoryRequest.setDepth( 1 );
    repositoryRequest.setShowHidden( true );
    repositoryRequest.setTypes( RepositoryRequest.FILES_TYPE_FILTER.FILES );
    repositoryRequest.setChildNodeFilter( filter );

    RepositoryFileTree tree = getRepository().getUnderlyingRepository().getTree( repositoryRequest );

    for ( RepositoryFileTree repositoryFileTree : tree.getChildren() ) {
      org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile = repositoryFileTree.getFile();
      RepositoryFile repositoryFile1 = RepositoryFile.build( repositoryDirectory.getPath(), repositoryFile, isAdmin() );
      repositoryDirectory.addChild( repositoryFile1 );
    }
  }

  private List<RepositoryElementMetaInterface> getRepositoryElements(
    RepositoryDirectoryInterface repositoryDirectoryInterface ) {
    List<RepositoryElementMetaInterface> elements = repositoryDirectoryInterface.getRepositoryObjects();
    if ( elements == null ) {
      try {
        return getRepository().getJobAndTransformationObjects( repositoryDirectoryInterface.getObjectId(), false );
      } catch ( KettleException ke ) {
        ke.printStackTrace();
      }
    } else {
      return elements;
    }
    return Collections.emptyList();
  }

  private void populateFolders( RepositoryDirectory repositoryDirectory,
                                RepositoryDirectoryInterface repositoryDirectoryInterface ) {
    if ( getRepository() instanceof RepositoryExtended ) {
      populateFoldersLazy( repositoryDirectory );
    } else {
      List<RepositoryDirectoryInterface> children = repositoryDirectoryInterface.getChildren();
      repositoryDirectory.setHasChildren( !Utils.isEmpty( children ) );
      if ( !Utils.isEmpty( children ) ) {
        for ( RepositoryDirectoryInterface child : children ) {
          repositoryDirectory.addChild( RepositoryDirectory.build( repositoryDirectory.getPath(), child ) );
        }
      }
    }
  }

  public void populateFoldersLazy( RepositoryDirectory repositoryDirectory ) {
    RepositoryRequest repositoryRequest = new RepositoryRequest( repositoryDirectory.getPath(), true, 1, null );
    repositoryRequest.setTypes( RepositoryRequest.FILES_TYPE_FILTER.FOLDERS );
    repositoryRequest.setIncludeSystemFolders( false );

    RepositoryFileTree tree = getRepository().getUnderlyingRepository().getTree( repositoryRequest );

    for ( RepositoryFileTree repositoryFileTree : tree.getChildren() ) {
      org.pentaho.platform.api.repository2.unified.RepositoryFile repositoryFile = repositoryFileTree.getFile();
      RepositoryDirectory repositoryDirectory1 =
        RepositoryDirectory.build( repositoryDirectory.getPath(), repositoryFile, isAdmin() );
      repositoryDirectory.addChild( repositoryDirectory1 );
    }
  }

  private Spoon getSpoon() {
    return spoonSupplier.get();
  }

  private RepositoryDirectoryInterface findDirectory( String path ) {
    return rootDirectory.findDirectory( path );
  }

  private Boolean isAdmin() {
    return getRepository().getUserInfo().isAdmin();
  }

  @Override public RepositoryFile getParent( RepositoryFile file ) {
    RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
    repositoryDirectory.setPath( file.getParent() );
    return repositoryDirectory;
  }

  public void clearProviderCache() {
    //Any local caches that this provider might use should be cleared here.
  }

  @Override public RepositoryFile getFile( RepositoryFile file, VariableSpace space ) {
    return null;
  }

  @Override public void setFileProperties( FileDetails fileDetails, FileDialogOperation fileDialogOperation ) {
    RepositoryObject repositoryObject = new RepositoryObject();
    repositoryObject.setObjectId( fileDetails::getObjectId );
    repositoryObject.setName( fileDetails.getName() );
    try {
      repositoryObject
        .setRepositoryDirectory( getRepository().findDirectory( fileDetails.getParentPath() ) );
    } catch ( KettleException ignored ) {
      // Don't set directory if not found
    }
    if ( fileDetails.getType() != null ) {
      repositoryObject.setObjectType(
        fileDetails.getType().equals( TRANSFORMATION ) ? RepositoryObjectType.TRANSFORMATION
          : RepositoryObjectType.JOB );
    }
    fileDialogOperation.setRepositoryObject( repositoryObject );
    fileDialogOperation.setProvider( fileDetails.getProvider() );
    fileDialogOperation.setFilename( fileDetails.getName() );
  }

  @Override public RepositoryFile createDirectory( String parentPath, RepositoryFile repositoryFile, String newDirectoryName )
    throws FileException {
    RepositoryDirectory newRepositoryDirectory = RepositoryDirectory.build( parentPath, findDirectory( repositoryFile.getPath() ) );
    newRepositoryDirectory.setName( newDirectoryName );
    newRepositoryDirectory.setPath( parentPath + "/" + newDirectoryName );
    if ( hasDupeFolder( newRepositoryDirectory.getParent(), newRepositoryDirectory.getName() ) ) {
      return RepositoryDirectory.build( newRepositoryDirectory.getParent(),
        findDirectory( newRepositoryDirectory.getPath() ) );
    }
    return add( newRepositoryDirectory, null );
  }

  private Repository getRepository() {
    return RepositoryBrowserController.repository != null ? RepositoryBrowserController.repository
      : spoonSupplier.get().getRepository();
  }

  //Create a RepositoryFile object given the parent dir repositoryFile, an absolute path, and resulting file entityType,
  // none of which can be null.
  private RepositoryFile getFile( RepositoryFile destDir, String toPath, EntityType entityType, VariableSpace space ) {
    Element returnElement = new Element( destDir ); //The folder gives us the additional values we need like type
    returnElement.setPath( toPath ); //Then we override the path name and convert it back to a File
    returnElement.setName( Util.getName( toPath ) );
    returnElement.setEntityType( entityType );
    return (RepositoryFile) returnElement.convertToFile( space );
  }

}
