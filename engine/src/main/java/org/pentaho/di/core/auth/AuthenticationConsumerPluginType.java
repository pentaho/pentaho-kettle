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


package org.pentaho.di.core.auth;

import java.lang.annotation.Annotation;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Map;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

/**
 * This class represents the authentication plugin type.
 *
 */
@PluginMainClassType( AuthenticationConsumerType.class )
@PluginAnnotationType( AuthenticationConsumerPlugin.class )
public class AuthenticationConsumerPluginType extends BasePluginType implements PluginTypeInterface {
  protected static AuthenticationConsumerPluginType pluginType = new AuthenticationConsumerPluginType();

  private AuthenticationConsumerPluginType() {
    super( AuthenticationProviderPlugin.class, "AUTHENTICATION_CONSUMER", "AuthenticationConsumer" );
    populateFolders( "authentication" );
  }

  public void registerPlugin( URLClassLoader classLoader, Class<? extends AuthenticationConsumerType> clazz ) throws KettlePluginException {
    AuthenticationConsumerPlugin pluginAnnotation =
      clazz.getAnnotation( AuthenticationConsumerPlugin.class );
    AuthenticationConsumerPluginType.getInstance().handlePluginAnnotation( clazz, pluginAnnotation,
      new ArrayList<String>(), false, null );
    PluginRegistry.getInstance().addClassLoader( classLoader,
      PluginRegistry.getInstance().getPlugin( AuthenticationConsumerPluginType.class, pluginAnnotation.id() ) );
  }

  public static AuthenticationConsumerPluginType getInstance() {
    return pluginType;
  }

  public String[] getNaturalCategoriesOrder() {
    return new String[0];
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return "";
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).name();
  }

  @Override
  protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).isSeparateClassLoaderNeeded();
  }

  @Override
  protected String extractI18nPackageName( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).i18nPackageName();
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).documentationUrl();
  }

  @Override
  protected String extractCasesUrl( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).casesUrl();
  }

  @Override
  protected String extractForumUrl( Annotation annotation ) {
    return ( (AuthenticationConsumerPlugin) annotation ).forumUrl();
  }

  @Override
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractImageFile( Annotation annotation ) {
    return "";
  }

  @Override
  protected void registerNatives() throws KettlePluginException {

  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {

  }
}
