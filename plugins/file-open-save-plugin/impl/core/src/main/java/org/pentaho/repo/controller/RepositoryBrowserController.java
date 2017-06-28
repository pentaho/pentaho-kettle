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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryElementMetaInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.repo.model.RepositoryDirectory;
import org.pentaho.repo.model.RepositoryFile;

import java.util.*;
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
    } catch( Exception e ) {
      return "";
    }
  }

  public ObjectId rename( String id, String path, String newName, String type ) {
    try {
      RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( path );
      ObjectId objectId = null;
      switch ( type ) {
        case "job":
          objectId = getRepository().renameJob( () -> id, repositoryDirectoryInterface, newName );
          break;
        case "transformation":
          objectId = getRepository().renameTransformation( () -> id, repositoryDirectoryInterface, newName );
          break;
        case "File folder":
          objectId = getRepository().renameRepositoryDirectory( () -> id, repositoryDirectoryInterface, newName );
          break;
      }
      return objectId;
    } catch ( Exception e ) {
      return null;
    }
  }

  public boolean remove( String id, String type ) {
    try {
      switch ( type ) {
        case "job":
          getRepository().deleteJob( () -> id );
          break;
        case "transformation":
          getRepository().deleteTransformation( () -> id );
          break;
        case "File folder":
          RepositoryDirectoryInterface repositoryDirectoryInterface = getRepository().findDirectory( id );
          getRepository().deleteRepositoryDirectory( repositoryDirectoryInterface );
          break;
      }
      return true;
    } catch ( Exception e ) {
      return false;
    }
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
    if( result ) {
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
    if ( getRepository() != null ) {
      RepositoryDirectoryInterface repositoryDirectoryInterface;
      try {
        if ( getRepository() instanceof RepositoryExtended ) {
          repositoryDirectoryInterface = ( (RepositoryExtended) getRepository() )
            .loadRepositoryDirectoryTree( "/", "*.ktr|*.kjb", -1, true, true, true );
        } else {
          repositoryDirectoryInterface = getRepository().loadRepositoryDirectoryTree();
        }
        List<RepositoryDirectory> repositoryDirectories = new LinkedList<>();
        boolean isPentahoRepository =
          getRepository().getRepositoryMeta().getId().equals( "PentahoEnterpriseRepository" );
        int depth = isPentahoRepository ? -1 : 0;
        createRepositoryDirectory( repositoryDirectoryInterface, repositoryDirectories, depth, null );
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
    List<LastUsedFile> lastUsedFiles = props.getLastUsedRepoFiles().getOrDefault( Spoon.getInstance().rep.getName(),
      Collections.emptyList() );
    for ( int i = 0; i < lastUsedFiles.size(); i++ ) {
      LastUsedFile lastUsedFile = lastUsedFiles.get( i );
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
                                          RepositoryDirectory parent ) {
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
        createRepositoryDirectory( child, repositoryDirectories, depth + 1, repositoryDirectory );
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
      repositoryDirectory.addChild( repositoryFile );
    }
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
      JSONParser jsonParser = new JSONParser();
      JSONObject jsonObject = (JSONObject) jsonParser.parse( jsonValue );
      JSONArray jsonArray = (JSONArray) jsonObject.get( Props.STRING_RECENT_SEARCHES );
      CollectionUtils.addAll( recentSearches, jsonArray.toArray() );
    } catch ( Exception e ) {
      // Log error in console
    }
    return recentSearches;
  }

  public LinkedList<String> storeRecentSearch( String recentSearch ) {

    LinkedList<String> recentSearches = getRecentSearches();
    if( recentSearch == null || recentSearches.contains( recentSearch ) ) {
      return recentSearches;
    }
    recentSearches.push( recentSearch );
    if( recentSearches.size() > 5 ) {
      recentSearches.pollLast();
    }

    JSONArray jsonArray = new JSONArray();
    CollectionUtils.addAll( jsonArray, recentSearches.toArray() );
    JSONObject jsonObject = new JSONObject();
    jsonObject.put( Props.STRING_RECENT_SEARCHES, jsonArray );
    PropsUI props = PropsUI.getInstance();
    props.setRecentSearches( jsonObject.toJSONString() );

    return recentSearches;
  }
}
