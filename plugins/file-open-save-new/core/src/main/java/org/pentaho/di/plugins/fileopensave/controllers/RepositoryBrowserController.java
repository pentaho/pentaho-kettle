/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.plugins.fileopensave.controllers;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.LastUsedFile;
import org.pentaho.di.repository.IUser;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryDirectoryInterface;
import org.pentaho.di.repository.RepositoryExtended;
import org.pentaho.di.repository.RepositoryObject;
import org.pentaho.di.repository.RepositoryObjectInterface;
import org.pentaho.di.repository.RepositoryObjectType;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.plugins.fileopensave.providers.repository.RepositoryFileProvider;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryDirectory;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryFile;
import org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryName;

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
  private final RepositoryFileProvider repositoryFileProvider;

  public RepositoryBrowserController( RepositoryFileProvider repositoryFileProvider ) {
    this.repositoryFileProvider = repositoryFileProvider;
  }

  public String getActiveFileName() {
    try {
      return getSpoon().getActiveMeta().getName();
    } catch ( Exception e ) {
      return "";
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

  public List<RepositoryFile> getRecentFiles() {
    if ( spoonSupplier.get().rep == null ) {
      return Collections.emptyList();
    }

    PropsUI props = PropsUI.getInstance();

    List<RepositoryFile> repositoryFiles = new ArrayList<>();
    IUser userInfo = spoonSupplier.get().rep.getUserInfo();
    String repoAndUser = spoonSupplier.get().rep.getName() + ":" + ( userInfo != null ? userInfo.getLogin() : "" );
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
        repositoryFile.setObjectId( index );
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

  public List<org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryObject> search( String path, String filter ) {
    RepositoryDirectoryInterface repositoryDirectoryInterface = findDirectory( path );
    List<org.pentaho.di.plugins.fileopensave.providers.repository.model.RepositoryObject> repositoryObjects = new ArrayList<>();
    List<RepositoryObjectInterface> repositoryObjects1 = ( (RepositoryExtended) getRepository() ).getChildren(
      repositoryDirectoryInterface.getObjectId().getId(), filter );
    for ( RepositoryObjectInterface repositoryObject : repositoryObjects1 ) {
      if ( repositoryObject instanceof RepositoryDirectoryInterface ) {
        RepositoryDirectory repositoryDirectory = new RepositoryDirectory();
        repositoryDirectory.setPath( path + "/" + repositoryObject.getName() );
        repositoryDirectory.setName( repositoryObject.getName() );
        repositoryDirectory.setObjectId( repositoryObject.getObjectId().getId() );
        repositoryObjects.add( repositoryDirectory );
      } else {
        RepositoryFile repositoryFile = new RepositoryFile();
        repositoryFile.setPath( path + "/" + repositoryObject.getName() );
        repositoryFile.setName( repositoryObject.getName() );
        repositoryFile.setType( ( (RepositoryObject) repositoryObject ).getObjectType() == RepositoryObjectType
          .TRANSFORMATION ? TRANSFORMATION : JOB );
        repositoryFile.setObjectId( repositoryObject.getObjectId().getId() );
        repositoryObjects.add( repositoryFile );
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
