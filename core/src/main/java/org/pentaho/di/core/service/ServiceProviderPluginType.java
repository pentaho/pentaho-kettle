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

import java.lang.annotation.Annotation;
import java.util.Map;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

@PluginMainClassType( ServiceProviderInterface.class )
@PluginAnnotationType( ServiceProvider.class )
public class ServiceProviderPluginType extends BasePluginType implements PluginTypeInterface {

  public ServiceProviderPluginType() {
    super( ServiceProvider.class, "SERVICEPROVIDER", "Service Provider" );
    populateFolders( "services" );
  }

  private static ServiceProviderPluginType servicePluginType;

  public static ServiceProviderPluginType getInstance() {
    if ( servicePluginType == null ) {
      servicePluginType = new ServiceProviderPluginType();
    }
    return servicePluginType;
  }

  @Override
  protected String getXmlPluginFile() {
    return "kettle-service-plugins.xml";
  }

  @Override
  protected String getMainTag() {
    return "services";
  }

  @Override
  protected String getSubTag() {
    return "service";
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {

  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (ServiceProvider) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return extractID( annotation );
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (ServiceProvider) annotation ).description();
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return false;
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return null;
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
    Class<?> clz = ( (ServiceProvider) annotation ).provides();
    if ( clz.isAssignableFrom( clazz ) ) {
      classMap.put( clazz, clazz.getName() );
    } else {
      classMap.put( clz, clz.getName() );
    }
  }

}
