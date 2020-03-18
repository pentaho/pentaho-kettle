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

import org.pentaho.di.connections.utils.EncryptUtils;
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
 * A class from managing named connections in PDI
 * <p>
 * Created by bmorrise on 2/3/19.
 */
public class ConnectionManager {

  private static final ConnectionManager instance = new ConnectionManager();

  private List<LookupFilter> lookupFilters = new ArrayList<>();
  private Supplier<IMetaStore> metaStoreSupplier;
  private ConcurrentHashMap<String, ConnectionProvider<? extends ConnectionDetails>> connectionProviders =
    new ConcurrentHashMap<>();
  private List<String> nameCache = new ArrayList<>();

  public static ConnectionManager getInstance() {
    return instance;
  }

  /**
   * Construct a meta store factory for a specific class using the default meta store supplier
   *
   * @param clazz Type of meta store object on which the factory will operate
   * @return Meta store factory for specified type
   */
  private <T extends ConnectionDetails> MetaStoreFactory<T> getMetaStoreFactory( Class<T> clazz ) {
    return new MetaStoreFactory<>( clazz, metaStoreSupplier.get(), NAMESPACE );
  }

  /**
   * Construct a meta store factory for a specific class using the supplied meta store
   *
   * @param metaStore The meta store from which to operate
   * @param clazz     Type of meta store object on which the factory will operate
   * @return Meta store factory for specified type
   */
  private <T extends ConnectionDetails> MetaStoreFactory<T> getMetaStoreFactory( IMetaStore metaStore,
                                                                                 Class<T> clazz ) {
    return new MetaStoreFactory<>( clazz, metaStore, NAMESPACE );
  }

  /**
   * Set the default meta store supplier for the Connection Manager
   *
   * @param metaStoreSupplier A meta store supplier
   */
  public void setMetastoreSupplier( Supplier<IMetaStore> metaStoreSupplier ) {
    this.metaStoreSupplier = metaStoreSupplier;
  }

  /**
   * Add a key lookup filter
   *
   * @param lookupFilter The lookup filter to add
   */
  public void addLookupFilter( LookupFilter lookupFilter ) {
    lookupFilters.add( lookupFilter );
  }

  /**
   * Add a connection provider with a specific key
   *
   * @param key                The key used to query the connection provider
   * @param connectionProvider The connection provider
   */
  public void addConnectionProvider( String key, ConnectionProvider<? extends ConnectionDetails> connectionProvider ) {
    connectionProviders.putIfAbsent( key, connectionProvider );
  }

  /**
   * Get a connection provider from the key
   *
   * @param key The connection provider key
   * @return The connection provider
   */
  public ConnectionProvider<? extends ConnectionDetails> getConnectionProvider( String key ) {
    return connectionProviders.get( getLookupKey( key ) );
  }

  /**
   * Get the lookup key for a provider
   *
   * @param value The key to lookup
   * @return the key returned from the lookup
   */
  protected String getLookupKey( String value ) {
    for ( LookupFilter lookupFilter : lookupFilters ) {
      String filterValue = lookupFilter.filter( value );
      if ( filterValue != null ) {
        return filterValue;
      }
    }
    return value;
  }

  /**
   * Save a named connection to a specific meta store
   *
   * @param metaStore         A meta store
   * @param connectionDetails The named connection details to save
   * @return A boolean signifying the success of the save operation
   */
  @SuppressWarnings( "unchecked" )
  public <T extends ConnectionDetails> boolean save( IMetaStore metaStore, T connectionDetails ) {
    return save( metaStore, connectionDetails, true );
  }

  /**
   * Save a named connection to a specific meta store
   *
   * @param metaStore         A meta store
   * @param connectionDetails The named connection details to save
   * @param prepare           Prepare the named connection
   * @return A boolean signifying the success of the save operation
   */
  @SuppressWarnings( "unchecked" )
  public <T extends ConnectionDetails> boolean save( IMetaStore metaStore, T connectionDetails, boolean prepare ) {
    if ( connectionDetails.getType() == null ) {
      return false;
    }
    ConnectionProvider<T> connectionProvider =
      (ConnectionProvider<T>) connectionProviders.get( connectionDetails.getType() );
    if ( prepare && connectionProvider.prepare( connectionDetails ) == null ) {
      return false;
    }
    if ( !saveElement( getMetaStoreFactory( metaStore, (Class<T>) connectionDetails.getClass() ),
      connectionDetails ) ) {
      return false;
    }
    if ( !nameCache.contains( connectionDetails.getName() ) ) {
      nameCache.add( connectionDetails.getName() );
    }
    return true;
  }

  /**
   * Save a named connection to the default meta store
   *
   * @param connectionDetails The named connection details to save
   * @return A boolean signifying the success of the save operation
   */
  public <T extends ConnectionDetails> boolean save( T connectionDetails ) {
    return save( metaStoreSupplier.get(), connectionDetails );
  }

  /**
   * Run a test operation on the named connection
   *
   * @param connectionDetails The named connection details to test
   * @return A boolean signifying the success of the test operation
   */
  @SuppressWarnings( "unchecked" )
  public <T extends ConnectionDetails> boolean test( T connectionDetails ) {
    ConnectionProvider<T> connectionProvider =
      (ConnectionProvider<T>) connectionProviders.get( connectionDetails.getType() );
    return connectionProvider.test( connectionDetails );
  }

  /**
   * Delete a connection by name from the default
   *
   * @param name The name of the named connection
   */
  public void delete( String name ) {
    delete( metaStoreSupplier.get(), name );
  }

  /**
   * Delete a connection by name from a specified meta store
   *
   * @param metaStore A meta store
   * @param name      The name of the named connection
   */
  public void delete( IMetaStore metaStore, String name ) {
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      try {
        ConnectionDetails connectionDetails =
          loadElement( getMetaStoreFactory( metaStore, provider.getClassType() ), name );
        if ( connectionDetails != null ) {
          getMetaStoreFactory( metaStore, provider.getClassType() ).deleteElement( name );
          nameCache.remove( name );
        }
      } catch ( MetaStoreException ignored ) {
        // Isn't in that metastore
      }
    }
  }

  /**
   * Get a list of connection providers
   *
   * @return A list of connection providers
   */
  public List<ConnectionProvider<? extends ConnectionDetails>> getProviders() {
    return Collections.list( this.connectionProviders.elements() );
  }

  /**
   * Get a list of connection providers by type
   *
   * @param clazz The type of provider to filter by
   * @return A list of connection providers
   */
  public List<ConnectionProvider<? extends ConnectionDetails>> getProvidersByType(
    Class<? extends ConnectionProvider> clazz ) {
    return Collections.list( connectionProviders.elements() ).stream().filter(
      connectionProvider -> clazz.isAssignableFrom( connectionProvider.getClass() )
    ).collect( Collectors.toList() );
  }

  /**
   * Get the names of named connections by provider from specified meta store
   *
   * @param metaStore A meta store
   * @param provider  A provider
   * @return A list of named connection names
   */
  private List<String> getNames( IMetaStore metaStore, ConnectionProvider<? extends ConnectionDetails> provider ) {
    try {
      return getMetaStoreFactory( metaStore, provider.getClassType() ).getElementNames();
    } catch ( MetaStoreException mse ) {
      return Collections.emptyList();
    }
  }

  /**
   * Get the names of named connections by provider from default meta store
   *
   * @param provider A provider
   * @return A list of named connection names
   */
  private List<String> getNames( ConnectionProvider<? extends ConnectionDetails> provider ) {
    if ( metaStoreSupplier == null || metaStoreSupplier.get() == null ) {
      return Collections.emptyList();
    }
    return getNames( metaStoreSupplier.get(), provider );
  }

  /**
   * Get the names of named connections by provider from specified meta store
   *
   * @param metaStore  A meta store
   * @param clearCache Whether or not to clear cache
   * @return A list of named connection names
   */
  public List<String> getNames( IMetaStore metaStore, boolean clearCache ) {
    if ( clearCache ) {
      nameCache.clear();
    }
    if ( !nameCache.isEmpty() ) {
      return nameCache;
    }
    List<String> detailNames = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      detailNames.addAll( getNames( metaStore, provider ) );
    }
    nameCache.addAll( detailNames );
    return detailNames;
  }

  /**
   * Get the names of named connections by provider from the default meta store
   *
   * @param clearCache - Whether or not to clear cache
   * @return A list of named connection names
   */
  public List<String> getNames( boolean clearCache ) {
    return getNames( metaStoreSupplier.get(), clearCache );
  }

  /**
   * Get the names of named connections by provider from the default meta store
   *
   * @return A list of named connection names
   */
  public List<String> getNames() {
    return getNames( metaStoreSupplier.get(), true );
  }

  /**
   * Get the names of named connections by provider from specified meta store
   *
   * @param metaStore A meta store
   * @return A list of named connection names
   */
  public List<String> getNames( IMetaStore metaStore ) {
    return getNames( metaStore, true );
  }

  /**
   * Find out if a named connection exists
   *
   * @param name The named connection name to check
   * @return A boolean whether or not the connection exists
   */
  public boolean exists( String name ) {
    return getNames().stream().anyMatch( name::equalsIgnoreCase );
  }

  /**
   * Get the names of named connection by connection provider type
   *
   * @param clazz The connection provider type
   * @return A list of named connection names
   */
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

  /**
   * Get the names of named connections by connection type key
   *
   * @param key The connection type key
   * @return A list of named connection names
   */
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

  /**
   * Get the named connection from a specified meta store
   *
   * @param metaStore A meta store
   * @param key       The provider key
   * @param name      The connection name
   * @return The named connection details
   */
  public ConnectionDetails getConnectionDetails( IMetaStore metaStore, String key, String name ) {
    ConnectionProvider<? extends ConnectionDetails> connectionProvider = getConnectionProvider( key );
    if ( connectionProvider != null ) {
      Class<? extends ConnectionDetails> clazz = connectionProvider.getClassType();
      return loadElement( getMetaStoreFactory( metaStore, clazz ), name );
    }
    return null;
  }

  /**
   * Get the named connection from the default meta store
   *
   * @param key  The provider key
   * @param name The connection name
   * @return The named connection details
   */
  public ConnectionDetails getConnectionDetails( String key, String name ) {
    if ( metaStoreSupplier == null || metaStoreSupplier.get() == null ) {
      return null;
    }
    return getConnectionDetails( metaStoreSupplier.get(), key, name );
  }

  /**
   * Get the named connection from a specified meta store
   *
   * @param name The connection name
   * @return The named connection details
   */
  public ConnectionDetails getConnectionDetails( String name ) {
    if ( metaStoreSupplier == null || metaStoreSupplier.get() == null ) {
      return null;
    }
    return getConnectionDetails( metaStoreSupplier.get(), name );
  }

  /**
   * Get the named connection from a specified meta store
   *
   * @param metaStore A meta store
   * @param name      The connection name
   * @return The named connection details
   */
  public ConnectionDetails getConnectionDetails( IMetaStore metaStore, String name ) {
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      ConnectionDetails connectionDetails =
        loadElement( getMetaStoreFactory( metaStore, provider.getClassType() ), name );
      if ( connectionDetails != null ) {
        return connectionDetails;
      }
    }
    return null;
  }

  /**
   * Save an element to the meta store
   *
   * @param metaStoreFactory  A meta store factory
   * @param connectionDetails The named connection details
   * @return Boolean whether or not the save was successful
   */
  private <T extends ConnectionDetails> boolean saveElement( MetaStoreFactory<T> metaStoreFactory,
                                                             T connectionDetails ) {
    try {
      EncryptUtils.encryptFields( connectionDetails );
      metaStoreFactory.saveElement( connectionDetails );
      return true;
    } catch ( MetaStoreException ignored ) {
      return false;
    }
  }

  /**
   * Load an element from the meta store
   *
   * @param metaStoreFactory A meta store factory
   * @param name             The named connection name
   * @return A named connection details object
   */
  private ConnectionDetails loadElement( MetaStoreFactory metaStoreFactory, String name ) {
    try {
      ConnectionDetails connectionDetails = (ConnectionDetails) metaStoreFactory.loadElement( name );
      if ( connectionDetails != null ) {
        EncryptUtils.decryptFields( connectionDetails );
      }
      return connectionDetails;
    } catch ( MetaStoreException e ) {
      return null;
    }
  }

  /**
   * Get a new connection details object by scheme/key
   *
   * @param scheme The scheme/key
   * @return A empty named connection
   */
  public ConnectionDetails createConnectionDetails( String scheme ) {
    try {
      ConnectionProvider<? extends ConnectionDetails> provider = connectionProviders.get( scheme );
      return provider.getClassType().newInstance();
    } catch ( Exception e ) {
      return null;
    }
  }

  /**
   * Get all named connections by key/scheme
   *
   * @param scheme The scheme/key
   * @return A list of named connections
   */
  @SuppressWarnings( "unchecked" )
  public List<? extends ConnectionDetails> getConnectionDetailsByScheme( String scheme ) {
    ConnectionProvider provider = connectionProviders.get( scheme );
    try {
      return getMetaStoreFactory( provider.getClassType() ).getElements();
    } catch ( Exception e ) {
      return Collections.emptyList();
    }
  }

  /**
   * Delete all named connections stored in a meta store
   *
   * @param metaStore A meta store
   */
  public void clear( IMetaStore metaStore ) {
    List<String> names = getNames( metaStore );
    for ( String name : names ) {
      delete( metaStore, name );
    }
  }

  /**
   * Copy the named connections stored in one meta store into another meta store
   *
   * @param sourceMetaStore      The meta store to copy from
   * @param destinationMetaStore The meta store to copy to
   */
  public void copy( IMetaStore sourceMetaStore, IMetaStore destinationMetaStore ) {
    List<String> sourceNames = getNames( sourceMetaStore, true );
    List<String> destinationNames = getNames( destinationMetaStore, true );

    for ( String sourceName : sourceNames ) {
      if ( !destinationNames.contains( sourceName ) ) {
        ConnectionDetails connectionDetails = getConnectionDetails( sourceMetaStore, sourceName );
        save( destinationMetaStore, connectionDetails, false );
      }
    }
  }

  /**
   * Get a list of value/label pairs of named connection types
   *
   * @return A list of value/label pairs of named connection types
   */
  public List<Type> getItems() {
    List<Type> types = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider provider : providers ) {
      types.add( new ConnectionManager.Type( provider.getKey(), provider.getName() ) );
    }
    return types;
  }

  /**
   * Represents the key/value of the name provider type
   */
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
