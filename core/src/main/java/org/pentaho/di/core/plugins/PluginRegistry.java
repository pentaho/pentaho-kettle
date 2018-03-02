/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.core.plugins;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginClassMapException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.logging.Metrics;
import org.pentaho.di.core.row.RowBuffer;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.i18n.BaseMessages;

/**
 * This singleton provides access to all the plugins in the Kettle universe.<br> It allows you to register types and
 * plugins, query plugin lists per category, list plugins per type, etc.<br>
 *
 * @author matt
 */
public class PluginRegistry {

  private static final Class<?> PKG = PluginRegistry.class; // for i18n purposes, needed by Translator2!!

  private static final PluginRegistry pluginRegistry = new PluginRegistry();

  private static final List<PluginTypeInterface> pluginTypes = new ArrayList<PluginTypeInterface>();
  private static final List<PluginRegistryExtension> extensions = new ArrayList<PluginRegistryExtension>();
  public static final LogChannelInterface log = new LogChannel( "PluginRegistry", true );
  public static final String SUPPLEMENTALS_SUFFIX = "-supplementals";

  // the list of plugins
  private final Map<Class<? extends PluginTypeInterface>, List<PluginInterface>> pluginMap = new HashMap<>();

  private final Map<String, URLClassLoader> folderBasedClassLoaderMap = new HashMap<>();

  private final Map<Class<? extends PluginTypeInterface>, Map<PluginInterface, URLClassLoader>> classLoaderMap = new HashMap<>();

  private final Map<String, URLClassLoader> classLoaderGroupsMap = new HashMap<>();

  private final Map<Class<? extends PluginTypeInterface>, List<String>> categoryMap = new HashMap<>();

  private final Map<PluginInterface, String[]> parentClassloaderPatternMap = new HashMap<>();

  private final Map<Class<? extends PluginTypeInterface>, List<PluginTypeListener>> listeners = new HashMap<>();

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Initialize the registry, keep private to keep this a singleton
   */
  private PluginRegistry() {
  }

  /**
   * @return The one and only PluginRegistry instance
   */
  public static PluginRegistry getInstance() {
    return pluginRegistry;
  }

  public void registerPluginType( Class<? extends PluginTypeInterface> pluginType ) {
    lock.writeLock().lock();
    try {
      if ( pluginMap.get( pluginType ) == null ) {
        pluginMap.put( pluginType, new ArrayList<PluginInterface>() );
      }

      // Keep track of the categories separately for performance reasons...
      //
      if ( categoryMap.get( pluginType ) == null ) {
        categoryMap.put( pluginType, new ArrayList<String>() );
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void removePlugin( Class<? extends PluginTypeInterface> pluginType, PluginInterface plugin ) {
    lock.writeLock().lock();
    try {
      List<PluginInterface> list = pluginMap.get( pluginType );
      if ( list != null ) {
        list.remove( plugin );
      }

      Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get( plugin.getPluginType() );
      if ( classLoaders != null ) {
        classLoaders.remove( plugin );
      }

      if ( !Utils.isEmpty( plugin.getClassLoaderGroup() ) ) {
        // Straight away remove the class loader for the whole group...
        //
        classLoaderGroupsMap.remove( plugin.getClassLoaderGroup() );
      }
    } finally {
      lock.writeLock().unlock();
      List<PluginTypeListener> listeners = this.listeners.get( pluginType );
      if ( listeners != null ) {
        for ( PluginTypeListener listener : listeners ) {
          listener.pluginRemoved( plugin );
        }
      }
      synchronized ( this ) {
        notifyAll();
      }
    }
  }

  public void addParentClassLoaderPatterns( PluginInterface plugin, String[] patterns ) {
    lock.writeLock().lock();
    try {
      parentClassloaderPatternMap.put( plugin, patterns );
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void registerPlugin( Class<? extends PluginTypeInterface> pluginType, PluginInterface plugin )
      throws KettlePluginException {
    boolean changed = false; // Is this an add or an update?
    lock.writeLock().lock();
    try {
      if ( plugin.getIds()[0] == null ) {
        throw new KettlePluginException( "Not a valid id specified in plugin :" + plugin );
      }

      List<PluginInterface> list = pluginMap.get( pluginType );
      if ( list == null ) {
        list = new ArrayList<PluginInterface>();
        pluginMap.put( pluginType, list );
      }

      int index = list.indexOf( plugin );
      if ( index < 0 ) {
        list.add( plugin );
      } else {
        list.set( index, plugin ); // replace with the new one
        changed = true;
      }

      // Keep the list of plugins sorted by name...
      //
      Collections.sort( list, new Comparator<PluginInterface>() {
        @Override
        public int compare( PluginInterface p1, PluginInterface p2 ) {
          return p1.getName().compareToIgnoreCase( p2.getName() );
        }
      } );

      if ( !Utils.isEmpty( plugin.getCategory() ) ) {
        List<String> categories = categoryMap.get( pluginType );
        if ( categories == null ) {
          categories = new ArrayList<String>();
          categoryMap.put( pluginType, categories );
        }
        if ( !categories.contains( plugin.getCategory() ) ) {
          categories.add( plugin.getCategory() );

          // Keep it sorted in the natural order here too!
          //
          // Sort the categories in the correct order.
          //
          String[] naturalOrder = null;

          PluginTypeCategoriesOrder naturalOrderAnnotation =
              pluginType.getAnnotation( PluginTypeCategoriesOrder.class );
          if ( naturalOrderAnnotation != null ) {
            String[] naturalOrderKeys = naturalOrderAnnotation.getNaturalCategoriesOrder();
            Class<?> i18nClass = naturalOrderAnnotation.i18nPackageClass();
            naturalOrder = new String[naturalOrderKeys.length];
            for ( int i = 0; i < naturalOrderKeys.length; i++ ) {
              naturalOrder[i] = BaseMessages.getString( i18nClass, naturalOrderKeys[i] );
            }
          }
          if ( naturalOrder != null ) {
            final String[] fNaturalOrder = naturalOrder;
            Collections.sort( categories, new Comparator<String>() {

              @Override
              public int compare( String one, String two ) {
                int idx1 = Const.indexOfString( one, fNaturalOrder );
                int idx2 = Const.indexOfString( two, fNaturalOrder );
                return idx1 - idx2;
              }
            } );
          }
        }
      }
    } finally {
      lock.writeLock().unlock();
      List<PluginTypeListener> listeners = this.listeners.get( pluginType );
      if ( listeners != null ) {
        for ( PluginTypeListener listener : listeners ) {
          // Changed or added?
          if ( changed ) {
            listener.pluginChanged( plugin );
          } else {
            listener.pluginAdded( plugin );
          }
        }
      }
      synchronized ( this ) {
        notifyAll();
      }
    }
  }

  /**
   * @return An unmodifiable list of plugin types
   */
  public List<Class<? extends PluginTypeInterface>> getPluginTypes() {
    lock.readLock().lock();
    try {
      return Collections
          .unmodifiableList( new ArrayList<Class<? extends PluginTypeInterface>>( pluginMap.keySet() ) );
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * @param type The plugin type to query
   * @return The list of plugins
   */
  @SuppressWarnings( "unchecked" )
  public <T extends PluginInterface, K extends PluginTypeInterface> List<T> getPlugins( Class<K> type ) {
    Set<T> set = new HashSet<T>();

    lock.readLock().lock();
    try {
      for ( Class<? extends PluginTypeInterface> pi : pluginMap.keySet() ) {
        if ( Const.classIsOrExtends( pi, type ) ) {
          List<PluginInterface> mapList = pluginMap.get( pi );
          if ( mapList != null ) {
            for ( PluginInterface p : mapList ) {
              T t = (T) p;
              set.add( t );
            }
          }
        }
      }
    } finally {
      lock.readLock().unlock();
    }
    return new ArrayList<T>( set );
  }

  /**
   * Get a plugin from the registry
   *
   * @param stepplugintype The type of plugin to look for
   * @param id             The ID to scan for
   * @return the plugin or null if nothing was found.
   */
  public PluginInterface getPlugin( Class<? extends PluginTypeInterface> pluginType, String id ) {
    if ( Utils.isEmpty( id ) ) {
      return null;
    }

    // getPlugins() never returns null, see his method above
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {
      if ( plugin.matches( id ) ) {
        return plugin;
      }
    }

    return null;
  }

  /**
   * Retrieve a list of plugins per category.
   *
   * @param pluginType     The type of plugins to search
   * @param pluginCategory The category to look in
   * @return An unmodifiable list of plugins that belong to the specified type and category.
   */
  public <T extends PluginTypeInterface> List<PluginInterface> getPluginsByCategory( Class<T> pluginType,
      String pluginCategory ) {
    List<PluginInterface> plugins = new ArrayList<PluginInterface>();

    for ( PluginInterface verify : getPlugins( pluginType ) ) {
      if ( verify.getCategory() != null && verify.getCategory().equals( pluginCategory ) ) {
        plugins.add( verify );
      }
    }

    // Also sort
    return Collections.unmodifiableList( plugins );
  }

  /**
   * Retrieve a list of all categories for a certain plugin type.
   *
   * @param pluginType The plugin type to search categories for.
   * @return The list of categories for this plugin type. The list can be modified (sorted etc) but will not impact the
   *         registry in any way.
   */
  public List<String> getCategories( Class<? extends PluginTypeInterface> pluginType ) {
    lock.readLock().lock();
    try {
      return categoryMap.get( pluginType );
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Load and instantiate the main class of the plugin specified.
   *
   * @param plugin The plugin to load the main class for.
   * @return The instantiated class
   * @throws KettlePluginException In case there was a loading problem.
   */
  public Object loadClass( PluginInterface plugin ) throws KettlePluginException {
    return loadClass( plugin, plugin.getMainType() );
  }

  /**
   * Load the class of the type specified for the plugin that owns the class of the specified object.
   *
   * @param pluginType the type of plugin
   * @param object     The object for which we want to search the class to find the plugin
   * @param classType  The type of class to load
   * @return the instantiated class.
   * @throws KettlePluginException
   */
  public <T> T loadClass( Class<? extends PluginTypeInterface> pluginType, Object object, Class<T> classType )
      throws KettlePluginException {
    PluginInterface plugin = getPlugin( pluginType, object );
    if ( plugin == null ) {
      return null;
    }
    return loadClass( plugin, classType );
  }

  /**
   * Load the class of the type specified for the plugin with the ID specified.
   *
   * @param pluginType the type of plugin
   * @param plugiId    The plugin id to use
   * @param classType  The type of class to load
   * @return the instantiated class.
   * @throws KettlePluginException
   */
  public <T> T loadClass( Class<? extends PluginTypeInterface> pluginType, String pluginId, Class<T> classType )
      throws KettlePluginException {
    PluginInterface plugin = getPlugin( pluginType, pluginId );
    if ( plugin == null ) {
      return null;
    }
    return loadClass( plugin, classType );
  }

  private KettleURLClassLoader createClassLoader( PluginInterface plugin ) throws MalformedURLException,
    UnsupportedEncodingException {
    List<String> jarfiles = plugin.getLibraries();
    URL[] urls = new URL[jarfiles.size()];
    for ( int i = 0; i < jarfiles.size(); i++ ) {
      File jarfile = new File( jarfiles.get( i ) );
      urls[i] = new URL( URLDecoder.decode( jarfile.toURI().toURL().toString(), "UTF-8" ) );
    }
    ClassLoader classLoader = getClass().getClassLoader();
    String[] patterns = parentClassloaderPatternMap.get( plugin );
    if ( patterns != null ) {
      return new KettleSelectiveParentFirstClassLoader( urls, classLoader, plugin.getDescription(), patterns );
    } else {
      return new KettleURLClassLoader( urls, classLoader, plugin.getDescription() );
    }
  }


  /**
   * Add a Class Mapping + factory for a plugin. This allows extra classes to be added to existing plugins.
   *
   * @param pluginType Type of plugin
   * @param tClass     Class to factory
   * @param id         ID of the plugin to extend
   * @param callable   Factory Callable
   * @param <T>        Type of the object factoried
   * @throws KettlePluginException
   */
  public <T> void addClassFactory( Class<? extends PluginTypeInterface> pluginType, Class<T> tClass, String id,
                                   Callable<T> callable ) throws KettlePluginException {

    String key = createSupplemantalKey( pluginType.getName(), id );
    SupplementalPlugin supplementalPlugin = (SupplementalPlugin) getPlugin( pluginType, key );

    if ( supplementalPlugin == null ) {
      supplementalPlugin = new SupplementalPlugin( pluginType, key );
      registerPlugin( pluginType, supplementalPlugin );
    }
    supplementalPlugin.addFactory( tClass, callable );
  }

  private String createSupplemantalKey( String pluginName, String id ) {
    return pluginName + "-" + id + SUPPLEMENTALS_SUFFIX;
  }

  /**
   * Load and instantiate the plugin class specified
   *
   * @param plugin      the plugin to load
   * @param pluginClass the class to be loaded
   * @return The instantiated class
   * @throws KettlePluginException In case there was a class loading problem somehow
   */
  @SuppressWarnings( "unchecked" )
  public <T> T loadClass( PluginInterface plugin, Class<T> pluginClass ) throws KettlePluginException {
    if ( plugin == null ) {
      throw new KettlePluginException( BaseMessages.getString(
          PKG, "PluginRegistry.RuntimeError.NoValidStepOrPlugin.PLUGINREGISTRY001" ) );
    }

    if ( plugin instanceof ClassLoadingPluginInterface ) {
      return ( (ClassLoadingPluginInterface) plugin ).loadClass( pluginClass );
    } else {
      String className = plugin.getClassMap().get( pluginClass );
      if ( className == null ) {
        // Look for supplemental plugin supplying extra classes
        for ( String id : plugin.getIds() ) {
          try {
            T aClass = loadClass( plugin.getPluginType(), createSupplemantalKey( plugin.getPluginType().getName(), id ), pluginClass );
            if ( aClass != null ) {
              return aClass;
            }
          } catch ( KettlePluginException exception ) {
            // ignore. we'll fall through to the other exception if this loop doesn't produce a return
          }
        }
        throw new KettlePluginClassMapException( BaseMessages.getString(
            PKG, "PluginRegistry.RuntimeError.NoValidClassRequested.PLUGINREGISTRY002", pluginClass.getName() ) );
      }

      try {
        Class<? extends T> cl;
        if ( plugin.isNativePlugin() ) {
          cl = (Class<? extends T>) Class.forName( className );
        } else {
          URLClassLoader ucl = null;

          // If the plugin needs to have a separate class loader for each instance of the plugin.
          // This is not the default. By default we cache the class loader for each plugin ID.
          //
          lock.writeLock().lock();
          try {
            if ( plugin.isSeparateClassLoaderNeeded() ) {
              // Create a new one each time
              ucl = createClassLoader( plugin );
            } else {
              // See if we can find a class loader to re-use.
              Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get( plugin.getPluginType() );
              if ( classLoaders == null ) {
                classLoaders = new HashMap<PluginInterface, URLClassLoader>();
                classLoaderMap.put( plugin.getPluginType(), classLoaders );
              } else {
                ucl = classLoaders.get( plugin );
              }
              if ( ucl == null ) {

                if ( plugin.getPluginDirectory() != null ) {
                  ucl = folderBasedClassLoaderMap.get( plugin.getPluginDirectory().toString() );
                  if ( ucl == null ) {
                    ucl = createClassLoader( plugin );
                    classLoaders.put( plugin, ucl ); // save for later use...
                    folderBasedClassLoaderMap.put( plugin.getPluginDirectory().toString(), ucl );
                  }
                } else {
                  ucl = classLoaders.get( plugin );
                  if ( ucl == null ) {
                    ucl = createClassLoader( plugin );
                    classLoaders.put( plugin, ucl ); // save for later use...
                  }
                }
              }
            }
          } finally {
            lock.writeLock().unlock();
          }

          // Load the class.
          cl = (Class<? extends T>) ucl.loadClass( className );
        }

        return cl.newInstance();
      } catch ( ClassNotFoundException e ) {
        throw new KettlePluginException( BaseMessages.getString(
            PKG, "PluginRegistry.RuntimeError.ClassNotFound.PLUGINREGISTRY003" ), e );
      } catch ( InstantiationException e ) {
        throw new KettlePluginException( BaseMessages.getString(
            PKG, "PluginRegistry.RuntimeError.UnableToInstantiateClass.PLUGINREGISTRY004" ), e );
      } catch ( IllegalAccessException e ) {
        throw new KettlePluginException( BaseMessages.getString(
            PKG, "PluginRegistry.RuntimeError.IllegalAccessToClass.PLUGINREGISTRY005" ), e );
      } catch ( MalformedURLException e ) {
        throw new KettlePluginException( BaseMessages.getString(
            PKG, "PluginRegistry.RuntimeError.MalformedURL.PLUGINREGISTRY006" ), e );
      } catch ( Throwable e ) {
        e.printStackTrace();
        throw new KettlePluginException( BaseMessages.getString(
            PKG, "PluginRegistry.RuntimeError.UnExpectedErrorLoadingClass.PLUGINREGISTRY007" ), e );
      }
    }
  }

  /**
   * Add a PluginType to be managed by the registry
   *
   * @param type
   */
  public static synchronized void addPluginType( PluginTypeInterface type ) {
    pluginTypes.add( type );
  }

  /**
   * Added so we can tell when types have been added (but not necessarily registered)
   *
   * @return the list of added plugin types
   */
  public static List<PluginTypeInterface> getAddedPluginTypes() {
    return Collections.unmodifiableList( pluginTypes );
  }

  public static synchronized void init() throws KettlePluginException {
    init( false );
  }

  /**
   * This method registers plugin types and loads their respective plugins
   *
   * @throws KettlePluginException
   */
  public static synchronized void init( boolean keepCache ) throws KettlePluginException {
    final PluginRegistry registry = getInstance();

    log.snap( Metrics.METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_START );

    // Find pluginRegistry extensions
    try {
      registry.registerType( PluginRegistryPluginType.getInstance() );
      List<PluginInterface> plugins = registry.getPlugins( PluginRegistryPluginType.class );
      for ( PluginInterface extensionPlugin : plugins ) {
        log.snap( Metrics.METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSION_START, extensionPlugin.getName() );
        PluginRegistryExtension extension = (PluginRegistryExtension) registry.loadClass( extensionPlugin );
        extension.init( registry );
        extensions.add( extension );
        log.snap( Metrics.METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_STOP, extensionPlugin.getName() );
      }
    } catch ( KettlePluginException e ) {
      e.printStackTrace();
    }
    log.snap( Metrics.METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_STOP );

    log.snap( Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_START );
    for ( final PluginTypeInterface pluginType : pluginTypes ) {
      log.snap( Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_START, pluginType.getName() );
      registry.registerType( pluginType );
      log.snap( Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_STOP, pluginType.getName() );
    }
    log.snap( Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_STOP );

    /*
     * System.out.println(MetricsUtil.getDuration(log.getLogChannelId(),
     * Metrics.METRIC_PLUGIN_REGISTRY_REGISTER_EXTENSIONS_START.getDescription()).get(0));
     * System.out.println(MetricsUtil.getDuration(log.getLogChannelId(),
     * Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_REGISTRATION_START.getDescription()).get(0)); long total=0; for
     * (MetricsDuration duration : MetricsUtil.getDuration(log.getLogChannelId(),
     * Metrics.METRIC_PLUGIN_REGISTRY_PLUGIN_TYPE_REGISTRATION_START.getDescription())) { total+=duration.getDuration();
     * System.out.println("   - "+duration.toString()+"          Total="+total); }
     */

    // Clear the jar file cache so that we don't waste memory...
    //
    if ( !keepCache ) {
      JarFileCache.getInstance().clear();
    }
  }

  private void registerType( PluginTypeInterface pluginType ) throws KettlePluginException {
    registerPluginType( pluginType.getClass() );

    // Search plugins for this type...
    //
    long startScan = System.currentTimeMillis();
    pluginType.searchPlugins();

    for ( PluginRegistryExtension ext : extensions ) {
      ext.searchForType( pluginType );
    }

    List<String> pluginClassNames = new ArrayList<String>();

    // Scan for plugin classes to facilitate debugging etc.
    //
    String pluginClasses = EnvUtil.getSystemProperty( Const.KETTLE_PLUGIN_CLASSES );
    if ( !Utils.isEmpty( pluginClasses ) ) {
      String[] classNames = pluginClasses.split( "," );

      for ( String className : classNames ) {
        if ( !pluginClassNames.contains( className ) ) {
          pluginClassNames.add( className );
        }
      }
    }

    for ( String className : pluginClassNames ) {
      try {
        // What annotation does the plugin type have?
        //
        PluginAnnotationType annotationType = pluginType.getClass().getAnnotation( PluginAnnotationType.class );
        if ( annotationType != null ) {
          Class<? extends Annotation> annotationClass = annotationType.value();

          Class<?> clazz = Class.forName( className );
          Annotation annotation = clazz.getAnnotation( annotationClass );

          if ( annotation != null ) {
            // Register this one!
            //
            pluginType.handlePluginAnnotation( clazz, annotation, new ArrayList<String>(), true, null );
            LogChannel.GENERAL.logBasic( "Plugin class "
                + className + " registered for plugin type '" + pluginType.getName() + "'" );
          } else {
            if ( KettleLogStore.isInitialized() && LogChannel.GENERAL.isDebug() ) {
              LogChannel.GENERAL.logDebug( "Plugin class "
                  + className + " doesn't contain annotation for plugin type '" + pluginType.getName() + "'" );
            }
          }
        } else {
          if ( KettleLogStore.isInitialized() && LogChannel.GENERAL.isDebug() ) {
            LogChannel.GENERAL.logDebug( "Plugin class "
                + className + " doesn't contain valid class for plugin type '" + pluginType.getName() + "'" );
          }
        }
      } catch ( Exception e ) {
        if ( KettleLogStore.isInitialized() ) {
          LogChannel.GENERAL.logError( "Error registring plugin class from KETTLE_PLUGIN_CLASSES: "
              + className + Const.CR + Const.getStackTracker( e ) );
        }
      }
    }

    if ( LogChannel.GENERAL.isDetailed() ) {
      LogChannel.GENERAL.logDetailed( "Registered "
          + getPlugins( pluginType.getClass() ).size() + " plugins of type '" + pluginType.getName() + "' in "
          + ( System.currentTimeMillis() - startScan ) + "ms." );
    }

  }

  /**
   * Find the plugin ID based on the class
   *
   * @param pluginClass
   * @return The ID of the plugin to which this class belongs (checks the plugin class maps)
   */
  public String getPluginId( Object pluginClass ) {
    for ( Class<? extends PluginTypeInterface> pluginType : getPluginTypes() ) {
      String id = getPluginId( pluginType, pluginClass );
      if ( id != null ) {
        return id;
      }
    }
    return null;
  }

  /**
   * Find the plugin ID based on the class
   *
   * @param pluginType  the type of plugin
   * @param pluginClass The class to look for
   * @return The ID of the plugin to which this class belongs (checks the plugin class maps) or null if nothing was
   *         found.
   */
  public String getPluginId( Class<? extends PluginTypeInterface> pluginType, Object pluginClass ) {
    String className = pluginClass.getClass().getName();
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {
      for ( String check : plugin.getClassMap().values() ) {
        if ( check != null && check.equals( className ) ) {
          return plugin.getIds()[0];
        }
      }
    }

    for ( PluginRegistryExtension ext : extensions ) {
      String id = ext.getPluginId( pluginType, pluginClass );
      if ( id != null ) {
        return id;
      }
    }
    return null;
  }

  /**
   * Retrieve the Plugin for a given class
   *
   * @param pluginType  The type of plugin to search for
   * @param pluginClass The class of this object is used to look around
   * @return the plugin or null if nothing could be found
   */
  public PluginInterface getPlugin( Class<? extends PluginTypeInterface> pluginType, Object pluginClass ) {
    String pluginId = getPluginId( pluginType, pluginClass );
    if ( pluginId == null ) {
      return null;
    }
    return getPlugin( pluginType, pluginId );
  }

  /**
   * Find the plugin ID based on the name of the plugin
   *
   * @param pluginType the type of plugin
   * @param pluginName The name to look for
   * @return The plugin with the specified name or null if nothing was found.
   */
  public PluginInterface findPluginWithName( Class<? extends PluginTypeInterface> pluginType, String pluginName ) {
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {

      if ( plugin.getName().equals( pluginName ) ) {
        return plugin;
      }
    }
    return null;
  }

  /**
   * Find the plugin ID based on the description of the plugin
   *
   * @param pluginType        the type of plugin
   * @param pluginDescription The description to look for
   * @return The plugin with the specified description or null if nothing was found.
   */
  public PluginInterface findPluginWithDescription( Class<? extends PluginTypeInterface> pluginType,
      String pluginDescription ) {
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {

      if ( plugin.getDescription().equals( pluginDescription ) ) {
        return plugin;
      }
    }
    return null;
  }

  /**
   * Find the plugin ID based on the name of the plugin
   *
   * @param pluginType the type of plugin
   * @param pluginName The name to look for
   * @return The plugin with the specified name or null if nothing was found.
   */
  public PluginInterface findPluginWithId( Class<? extends PluginTypeInterface> pluginType, String pluginId ) {
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {
      if ( plugin.matches( pluginId ) ) {
        return plugin;
      }
    }
    return null;
  }

  /**
   * @return a unique list of all the step plugin package names
   */
  public List<String> getPluginPackages( Class<? extends PluginTypeInterface> pluginType ) {
    List<String> list = new ArrayList<String>();
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {
      for ( String className : plugin.getClassMap().values() ) {
        int lastIndex = className.lastIndexOf( "." );
        if ( lastIndex > -1 ) {
          String packageName = className.substring( 0, lastIndex );
          if ( !list.contains( packageName ) ) {
            list.add( packageName );
          }
        }
      }
    }
    Collections.sort( list );
    return list;
  }

  private RowMetaInterface getPluginInformationRowMeta() {
    RowMetaInterface row = new RowMeta();

    row.addValueMeta( new ValueMetaString( BaseMessages.getString( PKG, "PluginRegistry.Information.Type.Label" ) ) );
    row.addValueMeta( new ValueMetaString( BaseMessages.getString( PKG, "PluginRegistry.Information.ID.Label" ) ) );
    row.addValueMeta( new ValueMetaString( BaseMessages.getString( PKG, "PluginRegistry.Information.Name.Label" ) ) );
    row.addValueMeta( new ValueMetaString(
        BaseMessages.getString( PKG, "PluginRegistry.Information.Description.Label" ) ) );
    row.addValueMeta( new ValueMetaString(
        BaseMessages.getString( PKG, "PluginRegistry.Information.Libraries.Label" ) ) );
    row.addValueMeta( new ValueMetaString(
        BaseMessages.getString( PKG, "PluginRegistry.Information.ImageFile.Label" ) ) );
    row.addValueMeta( new ValueMetaString(
        BaseMessages.getString( PKG, "PluginRegistry.Information.ClassName.Label" ) ) );
    row.addValueMeta( new ValueMetaString(
        BaseMessages.getString( PKG, "PluginRegistry.Information.Category.Label" ) ) );

    return row;
  }

  /**
   * @param the type of plugin to get information for
   * @return a row buffer containing plugin information for the given plugin type
   * @throws KettlePluginException
   */
  public RowBuffer getPluginInformation( Class<? extends PluginTypeInterface> pluginType )
      throws KettlePluginException {
    RowBuffer rowBuffer = new RowBuffer( getPluginInformationRowMeta() );
    for ( PluginInterface plugin : getPlugins( pluginType ) ) {

      Object[] row = new Object[getPluginInformationRowMeta().size()];
      int rowIndex = 0;

      row[rowIndex++] = getPluginType( plugin.getPluginType() ).getName();
      row[rowIndex++] = plugin.getIds()[0];
      row[rowIndex++] = plugin.getName();
      row[rowIndex++] = Const.NVL( plugin.getDescription(), "" );
      row[rowIndex++] = Utils.isEmpty( plugin.getLibraries() ) ? "" : plugin.getLibraries().toString();
      row[rowIndex++] = Const.NVL( plugin.getImageFile(), "" );
      row[rowIndex++] = plugin.getClassMap().values().toString();
      row[rowIndex++] = Const.NVL( plugin.getCategory(), "" );

      rowBuffer.getBuffer().add( row );
    }
    return rowBuffer;
  }

  /**
   * Load the class with a certain name using the class loader of certain plugin.
   *
   * @param plugin    The plugin for which we want to use the class loader
   * @param className The name of the class to load
   * @return the name of the class
   * @throws KettlePluginException In case there is something wrong
   */
  @SuppressWarnings( "unchecked" )
  public <T> T getClass( PluginInterface plugin, String className ) throws KettlePluginException {
    try {

      if ( plugin.isNativePlugin() ) {

        return (T) Class.forName( className );
      } else {

        URLClassLoader ucl = null;

        lock.writeLock().lock();
        try {
          Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get( plugin.getPluginType() );
          if ( classLoaders == null ) {
            classLoaders = new HashMap<PluginInterface, URLClassLoader>();
            classLoaderMap.put( plugin.getPluginType(), classLoaders );
          } else {
            ucl = classLoaders.get( plugin );
          }
          if ( ucl == null ) {

            if ( plugin.getPluginDirectory() != null ) {
              ucl = folderBasedClassLoaderMap.get( plugin.getPluginDirectory().toString() );

              classLoaders.put( plugin, ucl ); // save for later use...

            }

          }
        } finally {
          lock.writeLock().unlock();
        }

        if ( ucl == null ) {
          throw new KettlePluginException( "Unable to find class loader for plugin: " + plugin );
        }
        return (T) ucl.loadClass( className );

      }
    } catch ( Exception e ) {
      throw new KettlePluginException( "Unexpected error loading class with name: " + className, e );
    }
  }

  /**
   * Load the class with a certain name using the class loader of certain plugin.
   *
   * @param plugin    The plugin for which we want to use the class loader
   * @param classType The type of class to load
   * @return the name of the class
   * @throws KettlePluginException In case there is something wrong
   */
  @SuppressWarnings( "unchecked" )
  public <T> T getClass( PluginInterface plugin, T classType ) throws KettlePluginException {
    String className = plugin.getClassMap().get( classType );
    return (T) getClass( plugin, className );
  }

  /**
   * Create or retrieve the class loader for the specified plugin
   *
   * @param plugin the plugin to use
   * @return The class loader
   * @throws KettlePluginException In case there was a problem
   *                               <p/>
   *                               TODO: remove the similar code in the loadClass() method above with a call to
   *                               getClassLoader();
   */
  public ClassLoader getClassLoader( PluginInterface plugin ) throws KettlePluginException {

    if ( plugin == null ) {
      throw new KettlePluginException( BaseMessages.getString(
          PKG, "PluginRegistry.RuntimeError.NoValidStepOrPlugin.PLUGINREGISTRY001" ) );
    }

    try {
      if ( plugin.isNativePlugin() ) {
        return this.getClass().getClassLoader();
      } else {
        URLClassLoader ucl = null;

        lock.writeLock().lock();
        try {
          // If the plugin needs to have a separate class loader for each instance
          // of the plugin.
          // This is not the default. By default we cache the class loader for
          // each plugin ID.
          //
          if ( plugin.isSeparateClassLoaderNeeded() ) {
            // Create a new one each time
            ucl = createClassLoader( plugin );
          } else {
            // See if we can find a class loader to re-use.
            Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get( plugin.getPluginType() );
            if ( classLoaders == null ) {
              classLoaders = new HashMap<PluginInterface, URLClassLoader>();
              classLoaderMap.put( plugin.getPluginType(), classLoaders );
            } else {
              ucl = classLoaders.get( plugin );
            }
            if ( ucl == null ) {
              if ( !Utils.isEmpty( plugin.getClassLoaderGroup() ) ) {
                ucl = classLoaderGroupsMap.get( plugin.getClassLoaderGroup() );
                if ( ucl == null ) {
                  ucl = createClassLoader( plugin );
                  classLoaders.put( plugin, ucl );
                  classLoaderGroupsMap.put( plugin.getClassLoaderGroup(), ucl );
                }
              } else {
                if ( plugin.getPluginDirectory() != null ) {
                  ucl = folderBasedClassLoaderMap.get( plugin.getPluginDirectory().toString() );
                  if ( ucl == null ) {
                    ucl = createClassLoader( plugin );
                    classLoaders.put( plugin, ucl ); // save for later use...
                    folderBasedClassLoaderMap.put( plugin.getPluginDirectory().toString(), ucl );
                  }
                } else {
                  ucl = classLoaders.get( plugin );
                  if ( ucl == null ) {
                    if ( plugin.getLibraries().size() == 0 ) {
                      if ( plugin instanceof ClassLoadingPluginInterface ) {
                        return ( (ClassLoadingPluginInterface) plugin ).getClassLoader();
                      }
                    }
                    ucl = createClassLoader( plugin );
                    classLoaders.put( plugin, ucl ); // save for later use...
                  }
                }
              }
            }
          }
        } finally {
          lock.writeLock().unlock();
        }

        // Load the class.
        return ucl;
      }
    } catch ( MalformedURLException e ) {
      throw new KettlePluginException( BaseMessages.getString(
          PKG, "PluginRegistry.RuntimeError.MalformedURL.PLUGINREGISTRY006" ), e );
    } catch ( Throwable e ) {
      e.printStackTrace();
      throw new KettlePluginException( BaseMessages.getString(
          PKG, "PluginRegistry.RuntimeError.UnExpectedCreatingClassLoader.PLUGINREGISTRY008" ), e );
    }
  }

  /**
   * Allows the tracking of plugins as they come and go.
   *
   * @param typeToTrack extension of PluginTypeInterface to track.
   * @param listener    receives notification when a plugin of the specified type is added/removed/modified
   * @param <T>         extension of PluginTypeInterface
   */
  public <T extends PluginTypeInterface> void addPluginListener( Class<T> typeToTrack, PluginTypeListener listener ) {
    lock.writeLock().lock();
    try {
      List<PluginTypeListener> list = listeners.get( typeToTrack );
      if ( list == null ) {
        list = new ArrayList<PluginTypeListener>();
        listeners.put( typeToTrack, list );
      }
      if ( !list.contains( listener ) ) {
        list.add( listener );
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void addClassLoader( URLClassLoader ucl, PluginInterface plugin ) {
    lock.writeLock().lock();
    try {
      Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get( plugin.getPluginType() );
      if ( classLoaders == null ) {
        classLoaders = new HashMap<PluginInterface, URLClassLoader>();
        classLoaderMap.put( plugin.getPluginType(), classLoaders );
      }
      classLoaders.put( plugin, ucl );
    } finally {
      lock.writeLock().unlock();
    }
  }

  public PluginTypeInterface getPluginType( Class<? extends PluginTypeInterface> pluginTypeClass )
      throws KettlePluginException {
    try {
      // All these plugin type interfaces are singletons...
      // So we should call a static getInstance() method...
      //
      Method method = pluginTypeClass.getMethod( "getInstance", new Class<?>[0] );
      PluginTypeInterface pluginTypeInterface = (PluginTypeInterface) method.invoke( null, new Object[0] );

      return pluginTypeInterface;
    } catch ( Exception e ) {
      throw new KettlePluginException( "Unable to get instance of plugin type: " + pluginTypeClass.getName(), e );
    }
  }

  public List<PluginInterface> findPluginsByFolder( URL folder ) {
    String path = folder.getPath();
    try {
      path = folder.toURI().normalize().getPath();
    } catch ( URISyntaxException e ) {
      log.logError( e.getLocalizedMessage(), e );
    }
    if ( path.endsWith( "/" ) ) {
      path = path.substring( 0, path.length() - 1 );
    }
    List<PluginInterface> result = new ArrayList<PluginInterface>();
    lock.readLock().lock();
    try {
      for ( List<PluginInterface> typeInterfaces : pluginMap.values() ) {
        for ( PluginInterface plugin : typeInterfaces ) {
          URL pluginFolder = plugin.getPluginDirectory();
          try {
            if ( pluginFolder != null && pluginFolder.toURI().normalize().getPath().startsWith( path ) ) {
              result.add( plugin );
            }
          } catch ( URISyntaxException e ) {
            log.logError( e.getLocalizedMessage(), e );
          }
        }
      }
    } finally {
      lock.readLock().unlock();
    }
    return result;
  }
}
