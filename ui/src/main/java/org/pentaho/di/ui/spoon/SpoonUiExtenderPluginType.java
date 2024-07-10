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

package org.pentaho.di.ui.spoon;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.PluginTypeInterface;

@PluginMainClassType( SpoonUiExtenderPluginInterface.class )
@PluginAnnotationType( SpoonUiExtenderPlugin.class )
public class SpoonUiExtenderPluginType extends BasePluginType implements PluginTypeInterface {

  private SpoonUiExtenderPluginType() {
    super( SpoonUiExtenderPlugin.class, "SPOONUIEXTENDERPLUGIN", "Spoon UI Extender Plugin" );
    populateFolders( null );
  }

  private static SpoonUiExtenderPluginType pluginType;

  public static SpoonUiExtenderPluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new SpoonUiExtenderPluginType();
    }
    return pluginType;
  }

  public List<SpoonUiExtenderPluginInterface> getRelevantExtenders( Class<?> clazz, String uiEvent ) {

    PluginRegistry instance = PluginRegistry.getInstance();
    List<PluginInterface> pluginInterfaces = instance.getPlugins( SpoonUiExtenderPluginType.class );

    List<SpoonUiExtenderPluginInterface> relevantPluginInterfaces = new ArrayList<SpoonUiExtenderPluginInterface>(  );
    if ( pluginInterfaces != null ) {
      for ( PluginInterface pluginInterface : pluginInterfaces ) {
        try {
          Object loadClass = instance.loadClass( pluginInterface );

          SpoonUiExtenderPluginInterface spoonUiExtenderPluginInterface = (SpoonUiExtenderPluginInterface) loadClass;

          Set<String> events = spoonUiExtenderPluginInterface.respondsTo().get( clazz );
          if ( events != null && events.contains( uiEvent ) ) {
            relevantPluginInterfaces.add( spoonUiExtenderPluginInterface );
          }
        } catch ( KettlePluginException e ) {
          e.printStackTrace();
        }
      }
    }

    return relevantPluginInterfaces;
  }

  @Override
  protected void registerNatives() throws KettlePluginException {
    // TODO Auto-generated method stub
  }

  @Override
  protected void registerXmlPlugins() throws KettlePluginException {
    // TODO Auto-generated method stub
  }

  @Override
  protected String extractCategory( Annotation annotation ) {
    return ( (SpoonUiExtenderPlugin) annotation ).categoryDescription();
  }

  @Override
  protected String extractDesc( Annotation annotation ) {
    return ( (SpoonUiExtenderPlugin) annotation ).description();
  }

  @Override
  protected String extractID( Annotation annotation ) {
    return ( (SpoonUiExtenderPlugin) annotation ).id();
  }

  @Override
  protected String extractName( Annotation annotation ) {
    return ( (SpoonUiExtenderPlugin) annotation ).name();
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
    return ( (SpoonUiExtenderPlugin) annotation ).i18nPackageName();
  }

  @Override
  protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {
  }

  @Override
  protected String extractDocumentationUrl( Annotation annotation ) {
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
  protected String extractSuggestion( Annotation annotation ) {
    return null;
  }

  @Override
  protected String extractClassLoaderGroup( Annotation annotation ) {
    return ( (SpoonUiExtenderPlugin) annotation ).classLoaderGroup();
  }
}
