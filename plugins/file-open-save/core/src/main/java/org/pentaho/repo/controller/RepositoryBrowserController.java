/*
 * Copyright 2017 Pentaho Corporation. All rights reserved.
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;
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
import org.pentaho.repo.model.RepositoryDirectory;
import org.pentaho.repo.model.RepositoryFile;
import org.pentaho.repo.util.Util;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Supplier;

/**
 * Created by bmorrise on 5/12/17.
 */
public class RepositoryBrowserController {

  public static final String TRANSFORMATION = "transformation";
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
    RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( path );
    ObjectId objectId = null;
    switch ( type ) {
      case "job":
        if ( getRepository().exists( newName, repositoryDirectoryInterface, RepositoryObjectType.JOB ) ) {
          throw new KettleObjectExistsException();
        }
        if ( isJobOpened( id, path, oldName ) ) {
          throw new KettleJobException();
        }
        renameRecent( id, type, newName );
        objectId = getRepository().renameJob( () -> id, repositoryDirectoryInterface, newName );
        break;
      case "transformation":
        if ( getRepository().exists( newName, repositoryDirectoryInterface, RepositoryObjectType.TRANSFORMATION ) ) {
          throw new KettleObjectExistsException();
        }
        if ( isTransOpened( id, path, oldName ) ) {
          throw new KettleTransException();
        }
        renameRecent( id, type, newName );
        objectId = getRepository().renameTransformation( () -> id, repositoryDirectoryInterface, newName );
        break;
      case "folder":
        isFileOpenedInFolder( path );
        RepositoryDirectoryInterface parent = getRepository().findDirectory( path ).getParent();
        if ( parent == null ) {
          parent = getRepository().findDirectory( path );
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
        case "job":
          if ( isJobOpened( id, path, name ) ) {
            throw new KettleJobException();
          }
          removeRecent( id, type );
          getRepository().deleteJob( () -> id );
          break;
        case "transformation":
          if ( isTransOpened( id, path, name ) ) {
            throw new KettleTransException();
          }
          removeRecent( id, type );
          getRepository().deleteTransformation( () -> id );
          break;
        case "folder":
          isFileOpenedInFolder( path );
          removeRecentsUsingPath( path );
          RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( path );
          if ( getRepository() instanceof RepositoryExtended ) {
            ( (RepositoryExtended) getRepository() ).deleteRepositoryDirectory( repositoryDirectoryInterface, true );
          } else {
            getRepository().deleteRepositoryDirectory( repositoryDirectoryInterface );
          }
          break;
      }
      return true;
    } catch ( KettleTransException | KettleJobException ke  ) {
      throw ke;
    } catch ( Exception e ) {
      return false;
    }
  }

  private boolean removeRecent( String id, String type ) {
    RepositoryObject repositoryObject = null;
    try {
      repositoryObject = getRepository().getObjectInformation( () -> id,
        ( type == "transformation" ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB ) );
    } catch ( Exception e ) {
      return false;
    }

    if ( repositoryObject != null ) {
      Collection<List<LastUsedFile>> lastUsedRepoFiles = PropsUI.getInstance().getLastUsedRepoFiles().values();
      for ( List<LastUsedFile> lastUsedFiles : lastUsedRepoFiles ) {
        for ( LastUsedFile lastUsedFile : lastUsedFiles ) {
          if ( lastUsedFile.getDirectory().equals( repositoryObject.getRepositoryDirectory().getPath() ) && lastUsedFile
            .getFilename().equals( repositoryObject.getName() ) ) {
            lastUsedFiles.remove( lastUsedFile );
            return true;
          }
        }
      }
    }

    return true;
  }

  private boolean renameRecent( String id, String type, String name ) {
    RepositoryObject repositoryObject = null;
    try {
      repositoryObject = getRepository().getObjectInformation( () -> id,
        ( type == "transformation" ? RepositoryObjectType.TRANSFORMATION : RepositoryObjectType.JOB ) );
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
    try {
      RepositoryDirectoryInterface repositoryDirectoryInterface =
        getRepository().createRepositoryDirectory( getRepository().findDirectory( parent ), name );
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

  public boolean saveFile( String path, String name ) {
    boolean result = checkSecurity();
    if ( result ) {
      try {
        RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( path );
        getSpoon().getDisplay().asyncExec( () -> {
          try {
            EngineMetaInterface meta = getSpoon().getActiveMeta();
            meta.setRepositoryDirectory( repositoryDirectoryInterface );
            meta.setName( name );
            getSpoon().saveToRepositoryConfirmed( meta );
            getSpoon().delegates.tabs.renameTabs();
          } catch ( Exception e ) {
            System.out.println( e );
          }
        } );
      } catch ( Exception e ) {
        return false;
      }
    }
    return result;
  }

  private boolean checkSecurity() {
    EngineMetaInterface meta = getSpoon().getActiveMeta();
    return getSpoon().saveToRepositoryCheckSecurity( meta );
  }

  public List<RepositoryDirectory> loadDirectoryTree() {
    return loadDirectoryTree( "*.ktr|*.kjb" );
  }

  public List<RepositoryDirectory> loadDirectoryTree( String filter ) {
    if ( getRepository() != null ) {
      RepositoryDirectoryInterface repositoryDirectoryInterface;
      try {
        if ( getRepository() instanceof RepositoryExtended ) {
          repositoryDirectoryInterface = ( (RepositoryExtended) getRepository() )
            .loadRepositoryDirectoryTree( "/", filter, -1, BooleanUtils
              .isTrue( getRepository().getUserInfo().isAdmin() ), true, true );
        } else {
          repositoryDirectoryInterface = getRepository().loadRepositoryDirectoryTree();
        }
        List<RepositoryDirectory> repositoryDirectories = new LinkedList<>();
        boolean isPentahoRepository =
          getRepository().getRepositoryMeta().getId().equals( "PentahoEnterpriseRepository" );
        int depth = isPentahoRepository ? -1 : 0;
        createRepositoryDirectory( repositoryDirectoryInterface, repositoryDirectories, depth, null, filter );
        if ( isPentahoRepository ) {
          repositoryDirectories.remove( 0 );
        }
        return repositoryDirectories;
      } catch ( Exception e ) {
        return null;
      }
    }
    return null;
  }

  public List<RepositoryFile> loadFiles( String id ) {
    try {
      List<RepositoryElementMetaInterface> repositoryElementMetaInterfaces =
        getRepository().getJobAndTransformationObjects( () -> id, false );
      List<RepositoryFile> repositoryFiles = new ArrayList<>();
      for ( RepositoryObjectInterface repositoryObject : repositoryElementMetaInterfaces ) {
        org.pentaho.di.repository.RepositoryObject ro = (org.pentaho.di.repository.RepositoryObject) repositoryObject;
        RepositoryFile repositoryFile = new RepositoryFile();
        repositoryFile.setObjectId( repositoryObject.getObjectId() );
        repositoryFile.setName( repositoryObject.getName() );
        repositoryFile.setType( ro.getObjectType().getTypeDescription() );
        repositoryFile.setExtension( ro.getObjectType().getExtension() );
        repositoryFile.setDate( ro.getModifiedDate() );
        repositoryFile.setObjectId( ro.getObjectId() );
        repositoryFile.setPath( ro.getRepositoryDirectory().getPath() );
        repositoryFiles.add( repositoryFile );
      }
      return repositoryFiles;
    } catch ( KettleException ke ) {
      return Collections.emptyList();
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
        repositoryFile.setType( lastUsedFile.isTransformation() ? "transformation" : "job" );
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

  public void openRecentFile( String repo, String id ) {
    getSpoon().getDisplay().asyncExec( () -> {
      getSpoon().lastRepoFileSelect( repo, id );
    } );
  }

  private void createRepositoryDirectory( RepositoryDirectoryInterface repositoryDirectoryInterface,
                                          List<RepositoryDirectory> repositoryDirectories, int depth,
                                          RepositoryDirectory parent, String filter ) {
    RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
    repositoryDirectory.setName( repositoryDirectoryInterface.getName() );
    repositoryDirectory.setPath( repositoryDirectoryInterface.getPath() );
    repositoryDirectory.setObjectId( repositoryDirectoryInterface.getObjectId() );
    repositoryDirectory.setDepth( depth );
    repositoryDirectories.add( repositoryDirectory );
    if ( parent != null ) {
      repositoryDirectory.setParent( parent.getPath() );
      parent.addChild( repositoryDirectory );
    }
    if ( !Utils.isEmpty( repositoryDirectoryInterface.getChildren() ) ) {
      repositoryDirectory.setHasChildren( true );
      for ( RepositoryDirectoryInterface child : repositoryDirectoryInterface.getChildren() ) {
        createRepositoryDirectory( child, repositoryDirectories, depth + 1, repositoryDirectory, filter );
      }
    }
    List<RepositoryElementMetaInterface> repositoryElementMetaInterfaces = new ArrayList<>();
    if ( repositoryDirectoryInterface.getRepositoryObjects() == null ) {
      try {
        repositoryElementMetaInterfaces =
          getRepository().getJobAndTransformationObjects( repositoryDirectoryInterface.getObjectId(), false );
      } catch ( KettleException ke ) {
        // Ignore for now
      }
    } else {
      repositoryElementMetaInterfaces = repositoryDirectoryInterface.getRepositoryObjects();
    }
    Date latestDate = null;
    for ( RepositoryObjectInterface repositoryObject : repositoryElementMetaInterfaces ) {
      org.pentaho.di.repository.RepositoryObject ro = (org.pentaho.di.repository.RepositoryObject) repositoryObject;
      String extension = ro.getObjectType().getExtension();
      if ( !Util.isFiltered( extension, filter ) ) {
        RepositoryFile repositoryFile = new RepositoryFile();
        repositoryFile.setObjectId( repositoryObject.getObjectId() );
        repositoryFile.setName( repositoryObject.getName() );
        repositoryFile.setType( ro.getObjectType().getTypeDescription() );
        repositoryFile.setExtension( extension );
        repositoryFile.setDate( ro.getModifiedDate() );
        repositoryFile.setObjectId( ro.getObjectId() );
        repositoryFile.setPath( ro.getRepositoryDirectory().getPath() );
        repositoryDirectory.addChild( repositoryFile );
      }
      if ( latestDate == null || ro.getModifiedDate().after( latestDate ) ) {
        latestDate = ro.getModifiedDate();
      }
    }
    repositoryDirectory.setDate( latestDate );
  }

  private Spoon getSpoon() {
    return spoonSupplier.get();
  }

  private Repository getRepository() {
    return getSpoon().rep;
  }

  public LinkedList<String> getRecentSearches() {
    LinkedList<String> recentSearches = new LinkedList<String>();
    try {
      PropsUI props = PropsUI.getInstance();
      String jsonValue = props.getRecentSearches();
      if ( jsonValue != null ) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse( jsonValue );

        String login = "file_repository_no_login";
        if ( Spoon.getInstance().rep.getUserInfo() != null ) {
          login = Spoon.getInstance().rep.getUserInfo().getLogin();
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get( login );
        CollectionUtils.addAll( recentSearches, jsonArray.toArray() );
      }
    } catch ( Exception e ) {
      // Log error in console
    }
    return recentSearches;
  }

  public LinkedList<String> storeRecentSearch( String recentSearch ) {
    LinkedList<String> recentSearches = getRecentSearches();
    try {
      if ( recentSearch == null || recentSearches.contains( recentSearch ) ) {
        return recentSearches;
      }
      recentSearches.push( recentSearch );
      if ( recentSearches.size() > 5 ) {
        recentSearches.pollLast();
      }

      JSONArray jsonArray = new JSONArray();
      CollectionUtils.addAll( jsonArray, recentSearches.toArray() );

      PropsUI props = PropsUI.getInstance();
      String jsonValue = props.getRecentSearches();
      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = jsonValue != null ? (JSONObject) jsonParser.parse( jsonValue ) : new JSONObject();

      String login = "file_repository_no_login";
      if ( Spoon.getInstance().rep.getUserInfo() != null ) {
        login = Spoon.getInstance().rep.getUserInfo().getLogin();
      }

      jsonObject.put( login, jsonArray );
      props.setRecentSearches( jsonObject.toJSONString() );
    } catch ( Exception e ) {
      // Log error in console
    }

    return recentSearches;
  }
}
