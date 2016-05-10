/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.ui.repo;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.spoon.Spoon;

import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Created by bmorrise on 4/18/16.
 */
public class RepositoryConnectController {

  public static final String DEFAULT_URL = "defaultUrl";

  private static Class<?> PKG = RepositoryConnectController.class;
  private static LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectController.class );

  private RepositoryMeta currentRepository;
  private RepositoriesMeta repositoriesMeta;
  private PluginRegistry pluginRegistry;
  private Spoon spoon;

  public RepositoryConnectController( PluginRegistry pluginRegistry, Spoon spoon, RepositoriesMeta repositoriesMeta ) {
    this.pluginRegistry = pluginRegistry;
    this.spoon = spoon;
    this.repositoriesMeta = repositoriesMeta;
    try {
      repositoriesMeta.readData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to load repositories", ke );
    }
  }

  public RepositoryConnectController() {
    this( PluginRegistry.getInstance(), Spoon.getInstance(), new RepositoriesMeta() );
  }

  @SuppressWarnings( "unchecked" )
  public String getPlugins() {
    List<PluginInterface> plugins = pluginRegistry.getPlugins( RepositoryPluginType.class );
    JSONArray list = new JSONArray();
    for ( PluginInterface pluginInterface : plugins ) {
      if ( !pluginInterface.getIds()[0].equals( "PentahoEnterpriseRepository" ) ) {
        JSONObject repoJSON = new JSONObject();
        repoJSON.put( "id", pluginInterface.getIds()[ 0 ] );
        repoJSON.put( "name", pluginInterface.getName() );
        repoJSON.put( "description", pluginInterface.getDescription() );
        list.add( repoJSON );
      }
    }
    return list.toString();
  }

  public boolean createRepository( String id, Map<String, Object> items ) {
    try {
      RepositoryMeta repositoryMeta = pluginRegistry.loadClass( RepositoryPluginType.class, id, RepositoryMeta.class );
      repositoryMeta.populate( items, repositoriesMeta );

      if ( repositoryMeta.getName() != null ) {
        Repository repository =
          pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class );
        repository.init( repositoryMeta );
        if ( currentRepository != null ) {
          repositoriesMeta.removeRepository( repositoriesMeta.indexOfRepository( currentRepository ) );
        }
        repositoriesMeta.addRepository( repositoryMeta );
        repositoriesMeta.writeData();
        currentRepository = repositoryMeta;
        if ( !( (AbstractRepository) repository ).test() ) {
          return false;
        }
        ( (AbstractRepository) repository ).create();
      }
    } catch ( KettleException ke ) {
      log.logError( "Unable to load repository type", ke );
      return false;
    }
    return true;
  }

  @SuppressWarnings( "unchecked" )
  public String getRepositories() {
    JSONArray list = new JSONArray();
    if ( repositoriesMeta != null ) {
      for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
        String name = repositoriesMeta.getRepository( i ).getName();
        String description = repositoriesMeta.getRepository( i ).getDescription();
        String id = repositoriesMeta.getRepository( i ).getId();
        Boolean isDefault = Boolean.valueOf( repositoriesMeta.getRepository( i ).isDefault() );
        JSONObject repoJSON = new JSONObject();
        repoJSON.put( "id", id );
        repoJSON.put( "name", name );
        repoJSON.put( "description", description );
        repoJSON.put( "isDefault", isDefault );
        list.add( repoJSON );
      }
    }
    return list.toString();
  }

  @SuppressWarnings( "unchecked" )
  public String getDatabases() {
    JSONArray list = new JSONArray();
    for ( int i = 0; i < repositoriesMeta.nrDatabases(); i++ ) {
      JSONObject databaseJSON = new JSONObject();
      databaseJSON.put( "name", repositoriesMeta.getDatabase( i ).getName() );
      list.add( databaseJSON );
    }
    return list.toString();
  }

  public boolean connectToRepository() {
    return connectToRepository( currentRepository );
  }

  public boolean connectToRepository( String username, String password ) {
    return connectToRepository( currentRepository, username, password );
  }

  public boolean connectToRepository( RepositoryMeta repositoryMeta ) {
    return connectToRepository( repositoryMeta, null, null );
  }

  public boolean connectToRepository( RepositoryMeta repositoryMeta, String username, String password ) {
    try {
      Repository repository =
        pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class );
      repository.init( repositoryMeta );
      repository.connect( username, password );
      if ( spoon != null ) {
        spoon.setRepository( repository );
      }
      return true;
    } catch ( KettleException ke ) {
      log.logError( "Unable to connect to repository", ke );
    }
    return false;
  }

  public boolean deleteRepository( String name ) {
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    int index = repositoriesMeta.indexOfRepository( repositoryMeta );
    repositoriesMeta.removeRepository( index );
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to write to repositories", ke );
    }
    return true;
  }

  public void addDatabase( DatabaseMeta databaseMeta ) {
    repositoriesMeta.addDatabase( databaseMeta );
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to write to repositories", ke );
    }
  }

  public boolean setDefaultRepository( String name ) {
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    int index = repositoriesMeta.indexOfRepository( repositoryMeta );
    for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
      repositoriesMeta.getRepository( i ).setDefault( false );
    }
    repositoriesMeta.getRepository( index ).setDefault( true );
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to set default repository", ke );
    }
    return true;
  }

  public String getDefaultUrl() {
    ResourceBundle resourceBundle = PropertyResourceBundle.getBundle( PKG.getPackage().getName() + ".plugin" );
    return resourceBundle.getString( DEFAULT_URL );
  }

  public void setCurrentRepository( RepositoryMeta repositoryMeta ) {
    this.currentRepository = repositoryMeta;
  }
}
