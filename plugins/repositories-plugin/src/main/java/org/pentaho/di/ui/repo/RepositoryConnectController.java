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

import com.sun.xml.ws.client.ClientTransportException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.PropsUI;
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
  public static final String ERROR_MESSAGE = "errorMessage";
  public static final String ERROR_401 = "401";

  private static Class<?> PKG = RepositoryConnectController.class;
  private static LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectController.class );

  private RepositoryMeta currentRepository;
  private RepositoriesMeta repositoriesMeta;
  private PluginRegistry pluginRegistry;
  private Spoon spoon;
  private PropsUI propsUI;

  public RepositoryConnectController( PluginRegistry pluginRegistry, Spoon spoon, RepositoriesMeta repositoriesMeta,
                                      PropsUI propsUI ) {
    this.pluginRegistry = pluginRegistry;
    this.spoon = spoon;
    this.repositoriesMeta = repositoriesMeta;
    this.propsUI = propsUI;
    try {
      repositoriesMeta.readData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to load repositories", ke );
    }
  }

  public RepositoryConnectController() {
    this( PluginRegistry.getInstance(), Spoon.getInstance(), new RepositoriesMeta(), PropsUI.getInstance() );
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
        list.add( repositoriesMeta.getRepository( i ).toJSONObject() );
      }
    }
    return list.toString();
  }

  public String getRepository( String name ) {
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    if ( repositoryMeta != null ) {
      currentRepository = repositoryMeta;
      return repositoryMeta.toJSONObject().toString();
    }
    return "";
  }

  public DatabaseMeta getDatabase( String name ) {
    return repositoriesMeta.searchDatabase( name );
  }

  public void removeDatabase( String name ) {
    int index = repositoriesMeta.indexOfDatabase( repositoriesMeta.searchDatabase( name ) );
    if ( index != -1 ) {
      repositoriesMeta.removeDatabase( index );
    }
    save();
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

  public String connectToRepository() {
    return connectToRepository( currentRepository );
  }

  public String connectToRepository( String username, String password ) {
    return connectToRepository( currentRepository, username, password );
  }

  public String connectToRepository( RepositoryMeta repositoryMeta ) {
    return connectToRepository( repositoryMeta, null, null );
  }

  public String connectToRepository( RepositoryMeta repositoryMeta, String username, String password ) {
    JSONObject jsonObject = new JSONObject();
    try {
      Repository repository =
        pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class );
      repository.init( repositoryMeta );
      repository.connect( username, password );
      if ( username != null ) {
        propsUI.setLastRepositoryLogin( username );
      }
      if ( spoon != null ) {
        spoon.closeAllJobsAndTransformations();
        spoon.setRepository( repository );
      }
      jsonObject.put( "success", true );
    } catch ( KettleException ke ) {
      if ( ke.getMessage().contains( ERROR_401 ) ) {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidCredentials" ) );
      } else {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) );
      }
      jsonObject.put( "success", false );
      log.logError( "Unable to connect to repository", ke );
    }
    return jsonObject.toString();
  }

  public boolean deleteRepository( String name ) {
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    int index = repositoriesMeta.indexOfRepository( repositoryMeta );
    if ( index != -1 ) {
      if ( spoon != null && spoon.getRepositoryName() != null && spoon.getRepositoryName()
        .equals( repositoryMeta.getName() ) ) {
        spoon.closeRepository();
      }
      repositoriesMeta.removeRepository( index );
      save();
    }
    return true;
  }

  public void addDatabase( DatabaseMeta databaseMeta ) {
    if ( databaseMeta != null ) {
      repositoriesMeta.addDatabase( databaseMeta );
      save();
    }
  }

  public boolean setDefaultRepository( String name ) {
    RepositoryMeta repositoryMeta = repositoriesMeta.findRepository( name );
    for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
      repositoriesMeta.getRepository( i ).setDefault( false );
    }
    if ( repositoryMeta != null ) {
      repositoryMeta.setDefault( true );
    }
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

  public String getCurrentUser() {
    return propsUI.getLastRepositoryLogin();
  }

  public void setCurrentRepository( RepositoryMeta repositoryMeta ) {
    this.currentRepository = repositoryMeta;
  }

  public RepositoryMeta getCurrentRepository() {
    return this.currentRepository;
  }

  public void save() {
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to write to repositories", ke );
    }
  }
}
