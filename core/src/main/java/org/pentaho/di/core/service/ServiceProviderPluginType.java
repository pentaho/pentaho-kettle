/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
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
