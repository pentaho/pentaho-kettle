/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2022 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;

public class PluginServiceLoader {

  private static ConcurrentHashMap<PluginInterface, ServiceProviderInterface<?>> spiCache = new ConcurrentHashMap<>();

  @SuppressWarnings( "unchecked" )
  public static <T> Collection<T> loadServices( Class<T> apiInterface ) throws KettlePluginException {

    PluginRegistry registry = PluginRegistry.getInstance();
    List<PluginInterface> plugins = registry.getPlugins( ServiceProviderPluginType.class );

    List<T> services = new ArrayList<>();

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
          
          T service = null;
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
            services.add( service );
          }

        }
      }
    }

    return services;
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
