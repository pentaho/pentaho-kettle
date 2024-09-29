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

public interface ServiceProviderInterface<T> {

  /**
   * Returns a boolean if the PluginServiceLoader should use the
   * java.lang.reflect.Proxy that sets the Thread Context ClassLoader
   * on the service
   * @return A boolean where <code>true</code> indicates to proxy wrap
   */
  default boolean useProxyWrap() {
    return false;
  }
  
  /**
   * Returns a boolean that indicates if the PluginServiceLoader should
   * use the factoryCreate method to build and return the service
   * provided by the provides tag. If <code>true</code>, the factoryCreate method
   * must be implemented. Otherwise, the PluginRegistry will load the class
   * and call newInstance() of the Class<?> in the provides property of the
   * ServiceLoader annotation.
   * @return A boolean where <code>true</code> indicates to use the <code>factoryCreate</code> method.
   */
  default boolean useFactory() {
    return false;
  }
  
  /**
   * Provides a factory method to create an instance of the ServiceProvider provides
   * item.
   * @return An instance of the provided service
   */
  default T factoryCreate() {
    return null;
  }

  /**
   * Indicate whether one instance of this service should be created and reused by all clients
   * or if a new instance should be created each time.
   * @return A boolean where <code>true</code> indicates that one instance should be created and reused
   * for the lifetime of the application.
   */
  default boolean isSingleton() {
    return false;
  }

  /**
   * Indicate the order in which to use this provider if there are multiple providers for a given service
   * @return An int where a higher number is a higher priority (should be used before lower priorities)
   */
  default int getPriority() {
    return 0;
  }
}
