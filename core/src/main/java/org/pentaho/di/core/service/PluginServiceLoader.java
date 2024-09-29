/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/

package org.pentaho.di.core.service;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PluginServiceLoader {

  private static final ConcurrentHashMap<PluginInterface, ServiceProviderInterface<?>> spiCache = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<ServiceProviderInterface<?>, ProviderServicePriority<?>> singletonCache = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<String, Collection<ProviderServicePriority<?>>> dynamicallyAddedServices = new ConcurrentHashMap<>();

  @SuppressWarnings( "unchecked" )
  public static <T> Collection<T> loadServices( Class<T> apiInterface ) throws KettlePluginException {

    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins( ServiceProviderPluginType.class );

    List<ProviderServicePriority<?>> unsortedServices = new ArrayList<>();

    for ( PluginInterface pi : plugins ) {
      for ( Entry<Class<?>, String> e : pi.getClassMap().entrySet() ) {
        Class<?> clz = e.getKey();
        boolean result = apiInterface.isAssignableFrom( clz );
        if ( result ) {

          // Load the Service Provider Interface
          ServiceProviderInterface<?> spi = spiCache.computeIfAbsent( pi, pinf -> {
            try {
              return (ServiceProviderInterface<?>) PluginRegistry.getInstance().loadClass( pinf );
            } catch ( KettlePluginException e1 ) {
              throw new RuntimeException( e1 );
            }
          } );

          ProviderServicePriority<?> providerServicePriority = null;
          T service;
          // try to get singletons from the cache first
          if ( spi.isSingleton() ) {
            providerServicePriority = singletonCache.get( spi );
            if ( null != providerServicePriority ) {
              unsortedServices.add( providerServicePriority );
              continue;
            }
          }

          if ( spi.useFactory() ) {
            // This service had its own factory in the ServiceProviderInterface
            service = (T) spi.factoryCreate();
          } else {
            // This service was its own thang, just use the plugin registry one
            service = apiInterface.cast( PluginRegistry.getInstance().loadClass( pi, e.getKey() ) );
          }

          // If the SPI says this class needs to be proxy wrapped (so all calls are executed under the
          // classloader of the plugin, then wrap it
          if ( spi.useProxyWrap() ) {
            service =
                (T) Proxy.newProxyInstance( PluginServiceLoader.class.getClassLoader(),
                  new Class[] {
                    apiInterface },
                  new WrappingClassLoaderChangingInvocationHandler( service ) );
          }

          // TODO: Consider throwing a KettlePluginException instead
          if ( service != null ) {
            providerServicePriority = new ProviderServicePriority<>( null, service, spi.getPriority() );
            if ( spi.isSingleton() ) {
              singletonCache.put( spi, providerServicePriority );
            }
            unsortedServices.add( providerServicePriority );
          }
        }
      }
    }
    // add any providers created dynamically (not part of plugin registry initialization)
    if ( dynamicallyAddedServices.containsKey( apiInterface.getName() ) ) {
      unsortedServices.addAll( dynamicallyAddedServices.get( apiInterface.getName() ) );
    }

    // sort by priority, extract the service, and cast to the interface type
    return unsortedServices.stream().sorted( Comparator.comparing( ProviderServicePriority::getPriority ) ).
      map( ProviderServicePriority::getService ).map( s -> (T) s ).collect( Collectors.toCollection( ArrayList::new ) );
  }

  // only allow one service per provider per API
  public static <I, S extends I> void registerService( Object provider, Class<I> apiInterface, S service, int priority ) {
    Collection<ProviderServicePriority<?>> providersAndServices;
    if ( dynamicallyAddedServices.containsKey( apiInterface.getName() ) ) {
      providersAndServices = dynamicallyAddedServices.get( apiInterface.getName() );
      providersAndServices.removeIf( e -> e.getProvider().equals( provider ) );
    } else {
      providersAndServices = new ArrayList<>();
    }
    providersAndServices.add( new ProviderServicePriority<>( provider, service, priority ) );
    dynamicallyAddedServices.put( apiInterface.getName(), providersAndServices );
  }

  private static class WrappingClassLoaderChangingInvocationHandler implements InvocationHandler {

    private final Object o;

    public WrappingClassLoaderChangingInvocationHandler( Object o ) {
      this.o = o;
    }

    @Override
    public Object invoke( Object proxy, Method method, Object[] args ) throws Throwable {

      ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
      try {
        Thread.currentThread().setContextClassLoader( o.getClass().getClassLoader() );
        return method.invoke( o, args );
      } finally {
        Thread.currentThread().setContextClassLoader( originalClassLoader );
      }

    }

  }
}
