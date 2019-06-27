/*
 * Copyright 2017-2019 Hitachi Vantara. All rights reserved.
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
 */

package org.pentaho.repo.controller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleJobException;
import org.pentaho.di.core.exception.KettleObjectExistsException;
import org.pentaho.di.core.exception.KettleTransException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.platform.api.repository2.unified.RepositoryFileTree;
import org.pentaho.platform.api.repository2.unified.RepositoryRequest;
import org.pentaho.repo.model.RepositoryDirectory;
import org.pentaho.repo.model.RepositoryFile;
import org.pentaho.repo.model.RepositoryName;
import org.pentaho.repo.model.RepositoryTree;
import org.pentaho.repo.util.Util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 5/12/17.
 */
public class RepositoryBrowserController {

  public static final String PENTAHO_ENTERPRISE_REPOSITORY = "PentahoEnterpriseRepository";
  public static Repository repository;
  public static final String TRANSFORMATION = "transformation";
  public static final String JOB = "job";
  public static final String FOLDER = "folder";
  public static final String FILTER = "*.ktr|*.kjb";

  private RepositoryDirectoryInterface rootDirectory;
  private Supplier<Spoon> spoonSupplier = Spoon::getInstance;

  public boolean loadFile( String id, String type ) {
    try {
      getSpoon().getDisplay().asyncExec( () -> {
        try {
          getSpoon().loadObjectFromRepository( () -> id,
            type.equals( TRANSFORMATION ) ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB, null );
        } catch ( Exception e ) {
          // Log error in console
        }
      } );

    } catch ( Exception e ) {
      return false;
    }

    return true;
  }

  public String getActiveFileName() {
    try {
      return getSpoon().getActiveMeta().getName();
    } catch ( Exception e ) {
      return "";
    }
  }

  public ObjectId rename( String id, String path, String newName, String type, String oldName ) throws KettleException {
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
    ObjectId objectId = null;
    switch ( type ) {
      case JOB:
        if ( getRepository().exists( newName, repositoryDirectoryInterface, RepositoryObjectType.JOB ) ) {
          throw new KettleObjectExistsException();
        }
        if ( isJobOpened( id, path, oldName ) ) {
          throw new KettleJobException();
        }
        renameRecent( id, type, newName );
        objectId = getRepository().renameJob( () -> id, repositoryDirectoryInterface, newName );
        break;
      case TRANSFORMATION:
        if ( getRepository().exists( newName, repositoryDirectoryInterface, RepositoryObjectType.TRANSFORMATION ) ) {
          throw new KettleObjectExistsException();
        }
        if ( isTransOpened( id, path, oldName ) ) {
          throw new KettleTransException();
        }
        renameRecent( id, type, newName );
        objectId = getRepository().renameTransformation( () -> id, repositoryDirectoryInterface, newName );
        break;
      case FOLDER:
        isFileOpenedInFolder( path );
        RepositoryDirectoryInterface parent = findDirectory( path ).getParent();
        if ( parent == null ) {
          parent = findDirectory( path );
        }
        RepositoryDirectoryInterface child = parent.findChild( newName );
        if ( child != null ) {
          throw new KettleObjectExistsException();
        }
        if ( getRepository() instanceof RepositoryExtended ) {
          objectId =
            ( (RepositoryExtended) getRepository() ).renameRepositoryDirectory( () -> id, null, newName, true );
        } else {
          objectId = getRepository().renameRepositoryDirectory( () -> id, null, newName );
        }
        break;
    }
    return objectId;
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

  private void removeRecentsUsingPath( String path ) {
    Collection<List<LastUsedFile>> lastUsedRepoFiles = PropsUI.getInstance().getLastUsedRepoFiles().values();
    for ( List<LastUsedFile> lastUsedFiles : lastUsedRepoFiles ) {
      for ( int i = 0; i < lastUsedFiles.size(); i++ ) {
        if ( ( lastUsedFiles.get( i ).getDirectory() + "/" ).startsWith( path + "/" ) ) {
          lastUsedFiles.remove( i );
          i--;
        }
      }
    }
  }

  public boolean remove( String id, String name, String path, String type ) throws KettleException {
    try {
      switch ( type ) {
        case JOB:
          if ( isJobOpened( id, path, name ) ) {
            throw new KettleJobException();
          }
          getRepository().deleteJob( () -> id );
          break;
        case TRANSFORMATION:
          if ( isTransOpened( id, path, name ) ) {
            throw new KettleTransException();
          }
          getRepository().deleteTransformation( () -> id );
          break;
        case FOLDER:
          isFileOpenedInFolder( path );
          removeRecentsUsingPath( path );
          RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
          if ( getRepository() instanceof RepositoryExtended ) {
            ( (RepositoryExtended) getRepository() ).deleteRepositoryDirectory( repositoryDirectoryInterface, true );
          } else {
            getRepository().deleteRepositoryDirectory( repositoryDirectoryInterface );
          }
          break;
      }
      return true;
    } catch ( KettleTransException | KettleJobException ke ) {
      throw ke;
    } catch ( Exception e ) {
      return false;
    }
  }

  public List<RepositoryFile> getRecentFiles() {
    PropsUI props = PropsUI.getInstance();

    List<RepositoryFile> repositoryFiles = new ArrayList<>();
    IUser userInfo = Spoon.getInstance().rep.getUserInfo();
    String repoAndUser = Spoon.getInstance().rep.getName() + ":" + ( userInfo != null ? userInfo.getLogin() : "" );
    List<LastUsedFile> lastUsedFiles =
      props.getLastUsedRepoFiles().getOrDefault( repoAndUser, Collections.emptyList() );

    Calendar calendar = Calendar.getInstance();
    calendar.add( Calendar.DATE, -30 );
    Date dateBefore = calendar.getTime();

    for ( int i = 0; i < lastUsedFiles.size(); i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
      if ( lastUsedFile.getLastOpened().before( dateBefore ) ) {
        continue;
      }
      if ( lastUsedFile.getRepositoryName() != null && lastUsedFile.getRepositoryName()
        .equals( Spoon.getInstance().rep.getName() ) ) {
        RepositoryFile repositoryFile = new RepositoryFile();
        final String index = String.valueOf( i );
        repositoryFile.setObjectId( () -> index );
        repositoryFile.setType( lastUsedFile.isTransformation() ? TRANSFORMATION : JOB );
        repositoryFile.setName( lastUsedFile.getFilename() );
        repositoryFile.setPath( lastUsedFile.getDirectory() );
        repositoryFile.setDate( lastUsedFile.getLastOpened() );
        repositoryFile.setRepository( lastUsedFile.getRepositoryName() );
        repositoryFile.setUsername( lastUsedFile.getUsername() );
        repositoryFiles.add( repositoryFile );
      }
    }

    return repositoryFiles;
  }

  private boolean renameRecent( String id, String type, String name ) {
    RepositoryObject repositoryObject = null;
    try {
      repositoryObject = getRepository().getObjectInformation( () -> id,
        ( type.equals( TRANSFORMATION ) ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB ) );
    } catch ( Exception e ) {
      return false;
    }

    if ( repositoryObject != null ) {
      Collection<List<LastUsedFile>> lastUsedRepoFiles = PropsUI.getInstance().getLastUsedRepoFiles().values();
      for ( List<LastUsedFile> lastUsedFiles : lastUsedRepoFiles ) {
        for ( LastUsedFile lastUsedFile : lastUsedFiles ) {
          if ( lastUsedFile.getDirectory().equals( repositoryObject.getRepositoryDirectory().getPath() ) && lastUsedFile
            .getFilename().equals( repositoryObject.getName() ) ) {
            lastUsedFile.setFilename( name );
            return true;
          }
        }
      }
    }

    return true;
  }

  public boolean updateRecentFiles( String oldPath, String newPath ) {
    try {
      Collection<List<LastUsedFile>> lastUsedRepoFiles = PropsUI.getInstance().getLastUsedRepoFiles().values();
      for ( List<LastUsedFile> lastUsedFiles : lastUsedRepoFiles ) {
        for ( int i = 0; i < lastUsedFiles.size(); i++ ) {
          if ( ( lastUsedFiles.get( i ).getDirectory() + "/" ).startsWith( oldPath + "/" ) ) {
            if ( lastUsedFiles.get( i ).getDirectory().length() == oldPath.length() ) {
              lastUsedFiles.get( i ).setDirectory( newPath );
            } else {
              String prefix = newPath.substring( 0, newPath.lastIndexOf( "/" ) ) + "/";
              String newFolder = newPath.substring( newPath.lastIndexOf( "/" ) + 1 );
              String suffix = lastUsedFiles.get( i ).getDirectory().substring( oldPath.length() );
              lastUsedFiles.get( i ).setDirectory( prefix + newFolder + suffix );
            }
          }
        }
      }
    } catch ( Exception e ) {
      return false;
    }
    return true;
  }

  public RepositoryDirectory create( String parent, String name ) {
    if ( hasDupeFolder( parent, name ) ) {
      return null;
    }
    try {
      RepositoryDirectoryInterface repositoryDirectoryInterface =
        getRepository().createRepositoryDirectory( findDirectory( parent ), name );
      RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
      repositoryDirectory.setName( repositoryDirectoryInterface.getName() );
      repositoryDirectory.setPath( repositoryDirectoryInterface.getPath() );
      repositoryDirectory.setObjectId( repositoryDirectoryInterface.getObjectId() );
      repositoryDirectory.setParent( parent );
      return repositoryDirectory;
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
      System.out.println( e );
    }
    return false;
  }

  public boolean saveFile( String path, String name, String fileName, boolean override ) {
    boolean result = checkForSecurityOrDupeIssues( path, name, fileName, override );
    if ( result ) {
      try {
        RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
        getSpoon().getDisplay().asyncExec( () -> {
          try {
            EngineMetaInterface meta = getSpoon().getActiveMeta();
            meta.setRepositoryDirectory( repositoryDirectoryInterface );
            meta.setName( name );
            getSpoon().saveToRepositoryConfirmed( meta );
            getSpoon().delegates.tabs.renameTabs();
          } catch ( Exception e ) {
            e.printStackTrace();
          }
        } );
      } catch ( Exception e ) {
        return false;
      }
    }
    return result;
  }

  public boolean checkForSecurityOrDupeIssues( String path, String name, String fileName, boolean override ) {
    return checkSecurity() && !hasDupeFile( path, name, fileName, override );
  }

  /**
   * Checks if there is a duplicate file in a given directory (i.e. hidden file)
   *
   * @param path     - Path to directory in which we are saving
   * @param name     - Name of file to save
   * @param fileName - Possible duplicate file name
   * @param override - True is user wants override file, false otherwise
   * @return - true if a duplicate file is found, false otherwise
   */
  private boolean hasDupeFile( String path, String name, String fileName, boolean override ) {
    try {
      RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( path );
      EngineMetaInterface meta = getSpoon().getActiveMeta();
      RepositoryObjectType type = "Trans".equals( meta.getFileType() )
        ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB;
      if ( getRepository().exists( name, repositoryDirectoryInterface, type ) ) {
        return !override || !name.equals( fileName );
      }
    } catch ( Exception e ) {
      System.out.println( e );
    }
    return false;
  }

  private boolean checkSecurity() {
    EngineMetaInterface meta = getSpoon().getActiveMeta();
    return getSpoon().saveToRepositoryCheckSecurity( meta );
  }

  public RepositoryTree loadDirectoryTree() {
    if ( getRepository() != null ) {
      try {
        if ( getRepository() instanceof RepositoryExtended ) {
          rootDirectory = ( (RepositoryExtended) getRepository() ).loadRepositoryDirectoryTree( false );
        } else {
          rootDirectory = getRepository().loadRepositoryDirectoryTree();
        }
        RepositoryTree repositoryTree = new RepositoryTree();
        RepositoryDirectory repositoryDirectory = RepositoryDirectory.build( null, rootDirectory );
        populateFolders( repositoryDirectory, rootDirectory );
        boolean isPentahoRepository =
          getRepository().getRepositoryMeta().getId().equals( PENTAHO_ENTERPRISE_REPOSITORY );
        repositoryTree.setIncludeRoot( !isPentahoRepository );
        repositoryTree.addChild( repositoryDirectory );
        return repositoryTree;
      } catch ( Exception e ) {
        return null;
      }
    }
    return null;
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

  public boolean openRecentFile( String repo, String id ) {
    // does the file exist?
    if ( getSpoon().recentRepoFileExists( repo, id ) ) {
      getSpoon().getDisplay().asyncExec( () -> {
        getSpoon().lastRepoFileSelect( repo, id );
      } );
      return true;
    } else {
      return false;
    }
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

  public RepositoryDirectory loadFolders( String path ) {
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
    RepositoryDirectory repositoryDirectory = RepositoryDirectory.build( null, repositoryDirectoryInterface );
    populateFolders( repositoryDirectory, repositoryDirectoryInterface );

    return repositoryDirectory;
  }

  public RepositoryDirectory loadFiles( String path ) {
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
    RepositoryDirectory repositoryDirectory = RepositoryDirectory.build( null, repositoryDirectoryInterface );
    try {
      populateFiles( repositoryDirectory, repositoryDirectoryInterface, FILTER );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }

    return repositoryDirectory;
  }

  public RepositoryDirectory loadFilesAndFolders( String path ) {
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
    RepositoryDirectory repositoryDirectory = RepositoryDirectory.build( null, repositoryDirectoryInterface );
    populateFolders( repositoryDirectory, repositoryDirectoryInterface );
    try {
      populateFiles( repositoryDirectory, repositoryDirectoryInterface, FILTER );
    } catch ( KettleException ke ) {
      ke.printStackTrace();
    }
    return repositoryDirectory;
  }

  public List<org.pentaho.repo.model.RepositoryObject> search( String path, String filter ) {
    RepositoryDirectory repositoryDirectory = loadFilesAndFolders( path );
    List<org.pentaho.repo.model.RepositoryObject> repositoryObjects = new ArrayList<>();
    for ( org.pentaho.repo.model.RepositoryObject repositoryObject : repositoryDirectory.getChildren() ) {
      if ( repositoryObject.getName().toLowerCase().contains( filter.toLowerCase() ) || !Util
        .isFiltered( repositoryObject.getName(), filter ) ) {
        repositoryObjects.add( repositoryObject );
      }
    }
    return repositoryObjects;
  }

  public JSONArray getRecentSearches() {
    try {
      PropsUI props = PropsUI.getInstance();
      String jsonString = props.getRecentSearches();
      if ( jsonString != null ) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse( jsonString );
        return (JSONArray) jsonObject.get( getLogin() );
      }
    } catch ( Exception e ) {
      // Log error in console
    }
    return new JSONArray();
  }

  public JSONArray storeRecentSearch( String recentSearch ) {
    JSONArray recentSearches = getRecentSearches();
    try {
      if ( recentSearch == null || recentSearches.contains( recentSearch ) ) {
        return recentSearches;
      }
      recentSearches.add( recentSearch );
      if ( recentSearches.size() > 5 ) {
        recentSearches.remove( 0 );
      }

      PropsUI props = PropsUI.getInstance();
      String jsonValue = props.getRecentSearches();
      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = jsonValue != null ? (JSONObject) jsonParser.parse( jsonValue ) : new JSONObject();
      jsonObject.put( getLogin(), recentSearches );
      props.setRecentSearches( jsonObject.toJSONString() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }

    return recentSearches;
  }

  private RepositoryDirectoryInterface findDirectory( ObjectId objectId ) {
    return rootDirectory.findDirectory( objectId );
  }

  private Spoon getSpoon() {
    return spoonSupplier.get();
  }

  private String getLogin() {
    String login = "file_repository_no_login";
    if ( getSpoon().rep.getUserInfo() != null ) {
      login = getSpoon().rep.getUserInfo().getLogin();
    }
    return login;
  }

  private RepositoryDirectoryInterface findDirectory( String path ) {
    return rootDirectory.findDirectory( path );
  }

  public RepositoryName getCurrentRepo() {
    return new RepositoryName( getRepository().getName() );
  }

  private Repository getRepository() {
    return repository != null ? repository : spoonSupplier.get().rep;
  }

  private Boolean isAdmin() {
    return getRepository().getUserInfo().isAdmin();
  }
}
