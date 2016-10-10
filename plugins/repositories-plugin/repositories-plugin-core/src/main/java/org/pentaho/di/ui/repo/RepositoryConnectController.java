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

import org.apache.commons.lang.ClassUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.RepositoryPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.AbstractRepository;
import org.pentaho.di.repository.ReconnectableRepository;
import org.pentaho.di.repository.RepositoriesMeta;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.repository.RepositoryMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.spoon.Spoon;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
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
  public static final String SUCCESS = "success";
  public static final String ERROR_401 = "401";

  private static Class<?> PKG = RepositoryConnectController.class;
  private static LogChannelInterface log =
    KettleLogStore.getLogChannelInterfaceFactory().create( RepositoryConnectController.class );

  private RepositoryMeta currentRepository;
  private RepositoryMeta connectedRepository;
  private RepositoriesMeta repositoriesMeta;
  private PluginRegistry pluginRegistry;
  private Spoon spoon;
  private List<RepositoryContollerListener> listeners = new ArrayList<>();

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
          if ( isCompatibleRepositoryEdit( repositoryMeta ) ) {
            setConnectedRepository( repositoryMeta );
          }
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

  private boolean isCompatibleRepositoryEdit( RepositoryMeta repositoryMeta ) {
    if ( repositoriesMeta.indexOfRepository( currentRepository ) >= 0
        && connectedRepository != null
        && repositoryEquals( connectedRepository, currentRepository ) ) {
      // only name / description / default changed ?
      RepositoryMeta clone = repositoryMeta.clone();
      clone.setName( connectedRepository.getName() );
      clone.setDescription( connectedRepository.getDescription() );
      clone.setDefault( connectedRepository.isDefault() );
      return repositoryEquals( connectedRepository, clone );
    }
    return false;
  }

  private boolean repositoryEquals( RepositoryMeta repo1, RepositoryMeta repo2 ) {
    return repo1.toJSONObject().equals( repo2.toJSONObject() );
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
      if ( repository instanceof ReconnectableRepository ) {
        repository = wrapWithRepositoryTimeoutHandler( (ReconnectableRepository) repository );
      }
      repository.init( repositoryMeta );
      repository.connect( username, password );
      if ( username != null ) {
        getPropsUI().setLastRepositoryLogin( username );
      }
      Spoon spoon = getSpoon();
      if ( spoon.getRepository() != null ) {
        spoon.closeRepository();
      } else {
        spoon.closeAllJobsAndTransformations( true );
      }
      spoon.setRepository( repository );
      setConnectedRepository( repositoryMeta );
      fireListeners();
      jsonObject.put( SUCCESS, true );
    } catch ( KettleException ke ) {
      if ( ke.getMessage().contains( ERROR_401 ) ) {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidCredentials" ) );
      } else {
        jsonObject.put( ERROR_MESSAGE, BaseMessages.getString( PKG, "RepositoryConnection.Error.InvalidServer" ) );
      }
      jsonObject.put( SUCCESS, false );
      log.logError( "Unable to connect to repository", ke );
    }
    return jsonObject.toString();
  }

  public String reconnectToRepository( String username, String password ) {
    Repository currentRepositoryInstance = getConnectedRepositoryInstance();
    return reconnectToRepository( currentRepository, (ReconnectableRepository) currentRepositoryInstance, username,
        password );
  }

  public String reconnectToRepository( RepositoryMeta repositoryMeta, ReconnectableRepository repository,
      String username, String password ) {
    JSONObject jsonObject = new JSONObject();
    try {
      if ( username != null ) {
        getPropsUI().setLastRepositoryLogin( username );
      }
      if ( repository.isConnected() ) {
        repository.disconnect();
      }
      repository.init( repositoryMeta );
      repository.connect( username, password );
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
      if ( getSpoon().getRepositoryName() != null && getSpoon().getRepositoryName()
        .equals( repositoryMeta.getName() ) ) {
        getSpoon().closeRepository();
        setConnectedRepository( null );
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
    return getPropsUI().getLastRepositoryLogin();
  }

  public void setCurrentRepository( RepositoryMeta repositoryMeta ) {
    this.currentRepository = repositoryMeta;
  }

  public RepositoryMeta getCurrentRepository() {
    return this.currentRepository;
  }

  public RepositoryMeta getConnectedRepository() {
    return connectedRepository;
  }

  public void setConnectedRepository( RepositoryMeta connectedRepository ) {
    this.connectedRepository = connectedRepository;
  }

  public RepositoryMeta getDefaultRepositoryMeta() {
    for ( int i = 0; i < repositoriesMeta.nrRepositories(); i++ ) {
      RepositoryMeta repositoryMeta = repositoriesMeta.getRepository( i );
      if ( repositoryMeta.isDefault() ) {
        return repositoryMeta;
      }
    }
    return null;
  }

  public Repository getRepository( RepositoryMeta repositoryMeta ) {
    try {
      Repository repository = pluginRegistry.loadClass( RepositoryPluginType.class, repositoryMeta.getId(), Repository.class );
      return repository;
    } catch ( KettlePluginException kpe ) {
      log.logDebug( "Unabled to load repository", kpe );
    }

    return null;
  }

  public RepositoryMeta getRepositoryMetaByName( String name ) {
    return repositoriesMeta.findRepository( name );
  }

  public boolean isConnected( String name ) {
    if ( getSpoon().rep != null ) {
      return getSpoon().rep.getName().equals( name );
    }
    return false;
  }

  public boolean isConnected() {
    return getSpoon().rep != null;
  }

  public Repository getConnectedRepositoryInstance() {
    return getSpoon().rep;
  }

  public void save() {
    try {
      repositoriesMeta.writeData();
    } catch ( KettleException ke ) {
      log.logError( "Unable to write to repositories", ke );
    }
  }

  private Spoon getSpoon() {
    if ( spoon == null ) {
      spoon = Spoon.getInstance();
    }
    return spoon;
  }

  @SuppressWarnings( "unchecked" )
  private Repository wrapWithRepositoryTimeoutHandler( ReconnectableRepository repository ) {
    List<Class<?>> repositoryIntrerfaces = ClassUtils.getAllInterfaces( repository.getClass() );
    Class<?>[] repositoryIntrerfacesArray = repositoryIntrerfaces.toArray( new Class<?>[repositoryIntrerfaces.size()] );
    return (Repository) Proxy.newProxyInstance( repository.getClass().getClassLoader(), repositoryIntrerfacesArray,
        new RepositorySessionTimeoutHandler( repository, this ) );
  }

  public PropsUI getPropsUI() {
    return PropsUI.getInstance();
  }

  public void addListener( RepositoryContollerListener listener ) {
    listeners.add( listener );
  }

  public void fireListeners() {
    for ( RepositoryContollerListener listener : listeners ) {
      listener.update();
    }
  }

  public interface RepositoryContollerListener {
    void update();
  }

}
