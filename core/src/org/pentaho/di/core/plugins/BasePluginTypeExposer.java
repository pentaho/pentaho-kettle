package org.pentaho.di.core.plugins;

import org.pentaho.di.core.exception.KettlePluginException;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * This Class serves only one purpose, defeat the package protection modifiers on the BasePluginType. We normally would
 * simply move classes needing access to the same package, split between jars. However, in OSGI, split packages are
 * not encouraged and not possible unless the packages are being supplied by bundles with special notation denoting the
 * package split. Kettle is currently imported into OSGI as part of the System Bundle [0], which we cannot modify in 
 * such a way.
 *
 * Unless you're running within OSGI, you should never use this class.
 *
 * Created by nbaker on 2/11/15.
 */
public class BasePluginTypeExposer {
  private BasePluginType pluginType;

  public BasePluginTypeExposer(BasePluginType pluginType) {
    this.pluginType = pluginType;
  }

  public String extractID( Annotation annotation ) {
    return pluginType.extractID( annotation );
  }

  public String extractName( Annotation annotation ) {
    return pluginType.extractName( annotation );
  }

  public String extractDesc( Annotation annotation ) {
    return pluginType.extractDesc( annotation );
  }

  public String extractCategory( Annotation annotation ) {
    return pluginType.extractCategory( annotation );
  }

  public String extractImageFile( Annotation annotation ) {
    return pluginType.extractImageFile( annotation );
  }

  public boolean extractSeparateClassLoader( Annotation annotation ) {
    return pluginType.extractSeparateClassLoader( annotation );
  }

  public String extractI18nPackageName( Annotation annotation ) {
    return pluginType.extractI18nPackageName( annotation );
  }

  public String extractDocumentationUrl( Annotation annotation ) {
    return pluginType.extractDocumentationUrl( annotation );
  }

  public String extractCasesUrl( Annotation annotation ) {
    return pluginType.extractCasesUrl( annotation );
  }

  public String extractForumUrl( Annotation annotation ) {
    return pluginType.extractForumUrl( annotation );
  }

}
