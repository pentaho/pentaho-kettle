/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.connections;

import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.pentaho.metastore.util.PentahoDefaults.NAMESPACE;

/**
 * Created by bmorrise on 2/3/19.
 */
public class ConnectionManager {

  private static ConnectionManager instance;

  private List<LookupFilter> lookupFilters = new ArrayList<>();
  private Supplier<IMetaStore> metaStoreSupplier;
  private ConcurrentHashMap<String, ConnectionProvider<? extends ConnectionDetails>> connectionProviders =
    new ConcurrentHashMap<>();
  private List<String> nameCache = new ArrayList<>();

  public static ConnectionManager getInstance() {
    if ( instance == null ) {
      instance = new ConnectionManager();
    }
    return instance;
  }

  private <T extends ConnectionDetails> MetaStoreFactory<T> getMetaStoreFactory( Class<T> clazz ) {
    return new MetaStoreFactory<>( clazz, metaStoreSupplier.get(), NAMESPACE );
  }

  private <T extends ConnectionDetails> MetaStoreFactory<T> getMetaStoreFactory( IMetaStore metaStore,
                                                                                 Class<T> clazz ) {
    return new MetaStoreFactory<>( clazz, metaStore, NAMESPACE );
  }

  public void setMetastoreSupplier( Supplier<IMetaStore> metaStoreSupplier ) {
    this.metaStoreSupplier = metaStoreSupplier;
  }

  public void addLookupFilter( LookupFilter lookupFilter ) {
    lookupFilters.add( lookupFilter );
  }

  public void addConnectionProvider( String key, ConnectionProvider<? extends ConnectionDetails> connectionProvider ) {
    connectionProviders.putIfAbsent( key, connectionProvider );
  }

  public ConnectionProvider<? extends ConnectionDetails> getConnectionProvider( String key ) {
    return connectionProviders.get( getLookupKey( key ) );
  }

  protected String getLookupKey( String value ) {
    for ( LookupFilter lookupFilter : lookupFilters ) {
      String filterValue = lookupFilter.filter( value );
      if ( filterValue != null ) {
        return filterValue;
      }
    }
    return value;
  }

  @SuppressWarnings( "unchecked" )
  public <T extends ConnectionDetails> boolean save( T connectionDetails ) {
    if ( connectionDetails.getType() == null ) {
      return false;
    }
    ConnectionProvider<T> connectionProvider =
      (ConnectionProvider<T>) connectionProviders.get( connectionDetails.getType() );
    if ( connectionProvider.prepare( connectionDetails ) == null ) {
      return false;
    }
    try {
      getMetaStoreFactory( (Class<T>) connectionDetails.getClass() ).saveElement( connectionDetails );
      if ( !nameCache.contains( connectionDetails.getName() ) ) {
        nameCache.add( connectionDetails.getName() );
      }
      return true;
    } catch ( MetaStoreException mse ) {
      return false;
    }
  }

  @SuppressWarnings( "unchecked" )
  public <T extends ConnectionDetails> boolean test( T connectionDetails ) {
    ConnectionProvider<T> connectionProvider =
      (ConnectionProvider<T>) connectionProviders.get( connectionDetails.getType() );
    return connectionProvider.test( connectionDetails );
  }

  public void delete( String name ) {
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      try {
        ConnectionDetails connectionDetails = getMetaStoreFactory( provider.getClassType() ).loadElement( name );
        if ( connectionDetails != null ) {
          getMetaStoreFactory( provider.getClassType() ).deleteElement( name );
          nameCache.remove( name );
        }
      } catch ( MetaStoreException ignored ) {
        // Isn't in that metastore
      }
    }
  }

  public List<ConnectionProvider<? extends ConnectionDetails>> getProviders() {
    return Collections.list( this.connectionProviders.elements() );
  }

  public List<ConnectionProvider<? extends ConnectionDetails>> getProvidersByType(
    Class<? extends ConnectionProvider> clazz ) {
    return Collections.list( connectionProviders.elements() ).stream().filter(
      connectionProvider -> clazz.isAssignableFrom( connectionProvider.getClass() )
    ).collect( Collectors.toList() );
  }

  private List<String> getNames( ConnectionProvider<? extends ConnectionDetails> provider ) {
    try {
      return getMetaStoreFactory( provider.getClassType() ).getElementNames();
    } catch ( MetaStoreException mse ) {
      return Collections.emptyList();
    }
  }

  public List<String> getNames( boolean clearCache ) {
    if ( clearCache ) {
      nameCache.clear();
    }
    if ( nameCache.size() > 0 ) {
      return nameCache;
    }
    List<String> detailNames = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      detailNames.addAll( getNames( provider ) );
    }
    nameCache.addAll( detailNames );
    return detailNames;
  }

  public List<String> getNames() {
    return getNames( true );
  }

  public boolean exists( String name ) {
    return getNames().contains( name );
  }

  public List<String> getNamesByType( Class<? extends ConnectionProvider> clazz ) {
    List<String> detailNames = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() ).stream().filter(
        connectionProvider -> clazz.isAssignableFrom( connectionProvider.getClass() )
      ).collect( Collectors.toList() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      detailNames.addAll( getNames( provider ) );
    }
    return detailNames;
  }

  public List<String> getNamesByKey( String key ) {
    List<String> detailNames = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() ).stream()
        .filter( connectionProvider -> connectionProvider.getKey().equals( key ) ).collect( Collectors.toList() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      detailNames.addAll( getNames( provider ) );
    }
    return detailNames;
  }

  public ConnectionDetails getConnectionDetails( IMetaStore metaStore, String key, String name ) {
    ConnectionProvider<? extends ConnectionDetails> connectionProvider = getConnectionProvider( key );
    if ( connectionProvider != null ) {
      Class<? extends ConnectionDetails> clazz = connectionProvider.getClassType();
      try {
        return getMetaStoreFactory( metaStore, clazz ).loadElement( name );
      } catch ( MetaStoreException mse ) {
        return null;
      }
    }
    return null;
  }

  public ConnectionDetails getConnectionDetails( String key, String name ) {
    if ( metaStoreSupplier == null || metaStoreSupplier.get() == null ) {
      return null;
    }
    return getConnectionDetails( metaStoreSupplier.get(), key, name );
  }

  public ConnectionDetails getConnectionDetails( String name ) {
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      try {
        ConnectionDetails connectionDetails = getMetaStoreFactory( provider.getClassType() ).loadElement( name );
        if ( connectionDetails != null ) {
          return connectionDetails;
        }
      } catch ( MetaStoreException ignored ) {
        // Isn't in that metastore
      }
    }
    return null;
  }

  public ConnectionDetails createConnectionDetails( String scheme ) {
    try {
      ConnectionProvider<? extends ConnectionDetails> provider = connectionProviders.get( scheme );
      return provider.getClassType().newInstance();
    } catch ( Exception e ) {
      return null;
    }
  }

  @SuppressWarnings( "unchecked" )
  public List<? extends ConnectionDetails> getConnectionDetailsByScheme( String scheme ) {
    ConnectionProvider provider = connectionProviders.get( scheme );
    try {
      return getMetaStoreFactory( provider.getClassType() ).getElements();
    } catch ( Exception e ) {
      return Collections.emptyList();
    }
  }

  public List<Type> getItems() {
    List<Type> types = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider provider : providers ) {
      types.add( new ConnectionManager.Type( provider.getKey(), provider.getName() ) );
    }
    return types;
  }

  public static class Type {

    private String value;
    private String label;

    public Type( String value, String label ) {
      this.value = value;
      this.label = label;
    }

    public String getValue() {
      return value;
    }

    public void setValue( String value ) {
      this.value = value;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel( String label ) {
      this.label = label;
    }
  }
}
