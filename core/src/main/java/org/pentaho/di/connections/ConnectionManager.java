/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.connections.utils.EncryptUtils;
import org.pentaho.di.connections.vfs.VFSConnectionDetails;
import org.pentaho.di.connections.vfs.VFSConnectionManagerHelper;
import org.pentaho.di.connections.vfs.VFSConnectionProvider;
import org.pentaho.di.connections.vfs.VFSConnectionTestOptions;
import org.pentaho.di.core.bowl.Bowl;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.api.exceptions.MetaStoreException;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

  private static final Logger logger = LoggerFactory.getLogger( ConnectionManager.class );

  private List<LookupFilter> lookupFilters = new ArrayList<>();

  @NonNull
  private Supplier<IMetaStore> metaStoreSupplier;

  @NonNull
  private final Bowl bowl;

  private ConcurrentHashMap<String, ConnectionProvider<? extends ConnectionDetails>> connectionProviders =
    new ConcurrentHashMap<>();

  private final Map<String, List<String>> namesByConnectionProvider = new ConcurrentHashMap<>();
  private final Map<String, ConnectionDetails> detailsByName = new ConcurrentHashMap<>();

  @NonNull
  private final VFSConnectionManagerHelper vfsConnectionManagerHelper;

  private boolean initialized;

  private ConnectionManager() {
    // Must throw a RuntimeException on metastore error to not break compatibility.
    this( getMetastoreSupplierUnchecked( DefaultBowl.getInstance() ), DefaultBowl.getInstance() );
  }

  @VisibleForTesting
  ConnectionManager( @NonNull Supplier<IMetaStore> metaStoreSupplier, @NonNull Bowl bowl ) {
    this( metaStoreSupplier, bowl, VFSConnectionManagerHelper.getInstance() );
  }

  @VisibleForTesting
  ConnectionManager( @NonNull Supplier<IMetaStore> metaStoreSupplier,
                    @NonNull Bowl bowl,
                    @NonNull VFSConnectionManagerHelper vfsConnectionManagerHelper ) {
    this.metaStoreSupplier = Objects.requireNonNull( metaStoreSupplier );
    this.bowl = Objects.requireNonNull( bowl );
    this.vfsConnectionManagerHelper = Objects.requireNonNull( vfsConnectionManagerHelper );
  }

  @NonNull
  private static Supplier<IMetaStore> getMetastoreSupplierUnchecked( @NonNull Bowl bowl ) {
    try {
      IMetaStore metastore = bowl.getMetastore();
      return () -> metastore;
    } catch ( MetaStoreException e ) {
      throw new MetaStoreInitializationException( e );
    }
  }

  // This isn't really a cache. We load *all* the connection information from the metastore, and then only
  // contact the metastore again when we write changes.
  // public APIs that have a metastore as an argument bypass these in-memory structures
  private synchronized void initialize() {
    if ( !initialized ) {
      List<ConnectionProvider<? extends ConnectionDetails>> providers = getProviders();
      for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
        List<String> names = loadNames( provider );
        if ( names != null && !names.isEmpty() ) {
          namesByConnectionProvider.put( provider.getName(), names );
          for ( String name : names ) {
            detailsByName.put( name, getConnectionDetails( metaStoreSupplier.get(), name ) );
          }
        }
      }
      initialized = true;
    }
  }

  /**
   * Resets the in-memory storage. The next call that needs it will refresh the connection information from the
   * metastore.
   *
   */
  public synchronized void reset() {
    // synchronized isn't actually enough to make this thread safe if we ever thought there would be readers at the
    // same time as something calling this method. Since this is only currently driven by user input (e.g. connecting
    // and disconnecting from a repository) just synchronized seems safe enough.
    namesByConnectionProvider.clear();
    detailsByName.clear();
    initialized = false;
  }

  /**
   * This getter should not generally be used because it is limited to the global scope. It would be better to have
   * almost all callers use Bowl.getConnectionManager().
   * <p>
   * This instance may still be used to register ConnectionProviders and Lookup Filters.
   *
   * @return ConnectionManager
   */
  @NonNull
  public static ConnectionManager getInstance() {
    return instance;
  }

  /**
   * Construct a new instance of a ConnectionManager associated with a given meta-store and bowl.
   * <p>
   * Instances returned by this will not share in-memory state with any other instances. If you need the
   * ConnectionManager for a Bowl, use Bowl.getConnectionManager() instead.
   *
   * @param metaStoreSupplier The meta-store supplier.
   * @param bowl              The bowl.
   * @return ConnectionManager
   */
  @NonNull
  public static ConnectionManager getInstance( @NonNull Supplier<IMetaStore> metaStoreSupplier, @NonNull Bowl bowl ) {
    ConnectionManager newManager = new ConnectionManager( metaStoreSupplier, bowl );
    // share the same set of connection providers and lookup filters. Everyone already registers with the one
    // from getInstance()
    newManager.connectionProviders = instance.connectionProviders;
    newManager.lookupFilters = instance.lookupFilters;
    return newManager;
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
  public void setMetastoreSupplier( @NonNull Supplier<IMetaStore> metaStoreSupplier ) {
    this.metaStoreSupplier = Objects.requireNonNull( metaStoreSupplier );
  }

  /**
   * Gets the default meta store supplier for the Connection Manager.
   *
   * @return A meta store supplier.
   */
  @VisibleForTesting
  @NonNull
  Supplier<IMetaStore> getMetastoreSupplier() {
    return metaStoreSupplier;
  }

  /**
   * Gets the bowl of the connection manager.
   *
   * @return A bowl.
   */
  @NonNull
  public Bowl getBowl() {
    return bowl;
  }

  /**
   * Gets the connection manager helper for VFS connections.
   * @return A VFS connection manager helper.
   */
  @VisibleForTesting
  @NonNull
  VFSConnectionManagerHelper getVfsConnectionManagerHelper(  ) {
    return vfsConnectionManagerHelper;
  }

  // region Provider

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
    if ( key == null ) {
      return null;
    }

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
  // endregion

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
    initialize();
    if ( connectionDetails.getType() == null ) {
      return false;
    }
    ConnectionProvider<T> connectionProvider =
      (ConnectionProvider<T>) connectionProviders.get( connectionDetails.getType() );
    try {
      if ( prepare ) {
        connectionProvider.prepare( connectionDetails );
      }
    } catch ( KettleException e ) {
      logger.error( "Error saving connection {}", connectionDetails.getName(), e );
      // Ignore the exception and save anyway.
    }

    if ( !saveElement( getMetaStoreFactory( metaStore, (Class<T>) connectionDetails.getClass() ),
      connectionDetails ) ) {
      return false;
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
    initialize();
    IMetaStore metaStore = metaStoreSupplier.get();
    boolean success = save( metaStore, connectionDetails );
    if ( success ) {
      // saveElement modified the connectionDetails! load it fresh
      ConnectionProvider<T> connectionProvider =
        (ConnectionProvider<T>) connectionProviders.get( connectionDetails.getType() );
      ConnectionDetails reloaded =
        loadElement( getMetaStoreFactory( metaStore, (Class<T>) connectionDetails.getClass() ),
                     connectionDetails.getName() );
      detailsByName.put( connectionDetails.getName(), reloaded );
      List<String> names = namesByConnectionProvider.get( connectionProvider.getName() );
      if ( names == null ) {
        names = new ArrayList<>();
        namesByConnectionProvider.put( connectionProvider.getName(), names );
      }
      if ( !names.contains( connectionDetails.getName() ) ) {
        names.add( connectionDetails.getName() );
      }
    }
    return success;
  }

  /**
   * Tests if a connection is valid, given its details, with default testing options.
   * <p>
   * If the given connection details is a VFS connection, this method delegates to
   * {@link VFSConnectionManagerHelper#test(ConnectionManager, VFSConnectionDetails, VFSConnectionTestOptions)}.
   * Otherwise, this method delegates directly to {@link ConnectionProvider#test(ConnectionDetails)}.
   *
   * @param details The details of the connection to test.
   * @return {@code true} if the connection is valid; {@code false} otherwise.
   */
  @SuppressWarnings( "unchecked" )
  public <T extends ConnectionDetails> boolean test( @NonNull T details ) throws KettleException {
    // FIXME: At least Catalog is VFSConnectionDetails but only ConnectionProvider!
    // Supposedly, as a means to not be browsable by File Open Save Dialog (which only shows VFS providers).
    // Instead, define VFSConnectionProvider#isBrowsable().
    ConnectionProvider<T> provider = (ConnectionProvider<T>) connectionProviders.get( details.getType() );
    if ( provider instanceof VFSConnectionProvider && details instanceof VFSConnectionDetails ) {
      return vfsConnectionManagerHelper.test( this, (VFSConnectionDetails) details, null );
    }

    // The specified connection details may not exist saved in the meta-store,
    // but still needs to have a non-empty name in it, to be able to form a temporary PVFS URI.
    if ( StringUtils.isEmpty( details.getName() ) ) {
      return false;
    }

    return provider.test( details );
  }

  /**
   * Delete a connection by name from the default
   *
   * @param name The name of the named connection
   */
  public void delete( String name ) {
    initialize();
    delete( metaStoreSupplier.get(), name, true );
  }

  /**
   * Delete a connection by name from a specified meta store
   *
   * @param metaStore A meta store
   * @param name      The name of the named connection
   */
  public void delete( IMetaStore metaStore, String name ) {
    initialize();
    delete( metaStore, name, false );
  }

  private void delete( IMetaStore metaStore, String name, boolean removeFromMemory ) {
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      try {
        ConnectionDetails connectionDetails =
          loadElement( getMetaStoreFactory( metaStore, provider.getClassType() ), name );
        if ( connectionDetails != null ) {
          getMetaStoreFactory( metaStore, provider.getClassType() ).deleteElement( name );
          if ( removeFromMemory ) {
            detailsByName.remove( connectionDetails.getName() );
            List<String> names = namesByConnectionProvider.get( provider.getName() );
            if ( names != null ) {
              names.remove( connectionDetails.getName() );
            }
          }
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
   * @param providerClass The type of provider to filter by
   * @return A list of connection providers
   */
  public List<ConnectionProvider<? extends ConnectionDetails>> getProvidersByType(
    Class<? extends ConnectionProvider> providerClass ) {

    return Collections.list( connectionProviders.elements() )
      .stream()
      .filter( provider -> providerClass.isAssignableFrom( provider.getClass() ) )
      .collect( Collectors.toList() );
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
      logger.error( "Error calling metastore getElementNames()", mse );
      return Collections.emptyList();
    }
  }

  /**
   * Get the names of named connections by provider from default meta store
   *
   * @param provider A provider
   * @return A list of named connection names
   */
  private List<String> loadNames( ConnectionProvider<? extends ConnectionDetails> provider ) {
    if ( metaStoreSupplier == null || metaStoreSupplier.get() == null ) {
      return Collections.emptyList();
    }
    return getNames( metaStoreSupplier.get(), provider );
  }

  /**
   * Get the names of named connections by provider from default meta store
   *
   * @param provider A provider
   * @return A list of named connection names
   */
  private List<String> getNames( ConnectionProvider<? extends ConnectionDetails> provider ) {
    initialize();
    return namesByConnectionProvider.get( provider.getName() );
  }

  /**
   * Get the names of named connections by provider from specified meta store
   *
   * @param metaStore  A meta store
   * @param clearCache Whether or not to clear cache. (UNUSED)
   * @return A list of named connection names
   */
  private List<String> getNames( IMetaStore metaStore, boolean clearCache ) {
    List<String> detailNames = new ArrayList<>();
    List<ConnectionProvider<? extends ConnectionDetails>> providers =
      Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<? extends ConnectionDetails> provider : providers ) {
      List<String> names = getNames( metaStore, provider );
      if ( names != null) {
        detailNames.addAll( names );
      }
    }
    return detailNames;
  }

  /**
   * Get the names of named connections by provider from the default meta store
   *
   * @param clearCache - Whether or not to clear cache (unused)
   * @return A list of named connection names
   */
  public List<String> getNames( boolean clearCache ) {
    return getNames();
  }

  /**
   * Get the names of named connections by provider from the default meta store
   *
   * @return A list of named connection names
   */
  public List<String> getNames() {
    initialize();
    return new ArrayList<>( detailsByName.keySet() );
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
    initialize();
    return detailsByName.containsKey( name );
  }

  /**
   * Get the names of named connection by connection provider type
   *
   * @param providerClass The connection provider type
   * @return A list of named connection names
   */
  public <T extends ConnectionProvider<?>> List<String> getNamesByType( Class<T> providerClass ) {
    List<String> detailNames = new ArrayList<>();

    for ( ConnectionProvider<? extends ConnectionDetails> provider : getProvidersByType( providerClass ) ) {
      List<String> names = getNames( provider );
      if ( names != null) {
        detailNames.addAll( names );
      }
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
      List<String> names = getNames( provider );
      if ( names != null) {
        detailNames.addAll( names );
      }
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
    initialize();
    ConnectionProvider<? extends ConnectionDetails> connectionProvider = getConnectionProvider( key );
    if ( connectionProvider != null ) {
      List<String> names = namesByConnectionProvider.get( connectionProvider.getName() );
      if ( names == null || !names.contains(name) ) {
        return null;
      }
      return detailsByName.get( name );
    }
    return null;
  }

  /**
   * Get the named connection from a specified meta store
   *
   * @param name The connection name
   * @return The named connection details
   */
  public ConnectionDetails getConnectionDetails( String name ) {
    initialize();
    return detailsByName.get( name );
  }

  /* The following are sugar methods: `getDetails` and `getExistingDetails`. These perform casting of the result to the
   * caller's result type. This removes the need for a lot of helper methods or casting code with unchecked warnings
   * proliferating out there. The variant which assumes existence, returns non-null or throws, also adds to usage.
   * Regarding the name simplification, from `getConnectionDetails` to `getDetails`, this is because changing the
   * original `getConnectionDetails` signature to have a generic return type would be a breaking change. Also, arguably,
   * in this context, it's clear that "details" and "provider" are of connections.
   */

  /**
   * Gets a connection given its name, casting it to the type parameter.
   *
   * @param name The connection name
   * @return The named connection details
   */
  @SuppressWarnings( "unchecked" )
  @Nullable
  public <T extends ConnectionDetails> T getDetails( @Nullable String name ) {
    return (T) getConnectionDetails( name );
  }

  /**
   * Gets the details of a connection, given its name, casting it to the type parameter,
   * and throwing if it does not exist.
   *
   * @param name The connection name
   * @return The named connection details
   * @throws KettleException When a connection with the given name is not defined.
   */
  @NonNull
  public <T extends ConnectionDetails> T getExistingDetails( @Nullable String name )
    throws KettleException {

    T details = getDetails( name );
    if ( details == null ) {
      throw new KettleException( String.format( "Undefined connection '%s'.", name ) );
    }

    return details;
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
    } catch ( MetaStoreException e ) {
      logger.error( "Error in saveElement {}", connectionDetails.getName(), e );
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
      logger.error( "Error in loadElement {}", name, e );
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
      logger.error( "Error in createConnectionDetails {}", scheme, e );
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
    initialize();
    ConnectionProvider provider = connectionProviders.get( scheme );
    if ( provider != null ) {
      List<String> names = namesByConnectionProvider.get( provider.getName() );
      if ( names != null && !names.isEmpty() ) {
        List<ConnectionDetails> details = new ArrayList<>();
        for ( String name : names ) {
          details.add( detailsByName.get( name ) );
        }
        return details;
      }
    }
    return Collections.emptyList();
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
    return getItemsByType( null );
  }

  /**
   * Get a list of value/label pairs of named connection types, optionally filtered to be of a given provider class.
   *
   * @param providerClass The provider class; when {@code null}, all providers are returned.
   * @return A list of value/label pairs of named connection types.
   */
  public <T extends ConnectionProvider<?>> List<Type> getItemsByType( @Nullable Class<T> providerClass ) {

    List<Type> types = new ArrayList<>();

    List<ConnectionProvider<?>> providers = Collections.list( connectionProviders.elements() );
    for ( ConnectionProvider<?> provider : providers ) {
      if ( providerClass == null || providerClass.isAssignableFrom( provider.getClass() ) ) {
        types.add( new ConnectionManager.Type( provider.getKey(), provider.getName() ) );
      }
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

  public static class MetaStoreInitializationException extends RuntimeException {
    public MetaStoreInitializationException( MetaStoreException cause ) {
      super( cause );
    }

    @Override
    public synchronized MetaStoreException getCause() {
      return (MetaStoreException) super.getCause();
    }
  }
}
