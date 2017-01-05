package org.pentaho.di.engine;

import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.plugins.BasePluginType;
import org.pentaho.di.core.plugins.PluginAnnotationType;
import org.pentaho.di.core.plugins.PluginMainClassType;
import org.pentaho.di.core.plugins.PluginTypeInterface;

import java.lang.annotation.Annotation;
import java.util.Map;

@PluginMainClassType( IExecutionEngine.class )
@PluginAnnotationType( ExecutionEngine.class )
public class ExecutionEnginePluginType extends BasePluginType implements PluginTypeInterface {

  private static ExecutionEnginePluginType pluginType;

  public ExecutionEnginePluginType( ) {
    super( ExecutionEngine.class, "EXECUTION_ENGINE", "Execution Engine" );
  }

  public static ExecutionEnginePluginType getInstance() {
    if ( pluginType == null ) {
      pluginType = new ExecutionEnginePluginType(  );
    }
    return pluginType;
  }

  @Override protected void registerNatives()  throws KettlePluginException {
  }

  @Override protected void registerXmlPlugins() throws KettlePluginException {
  }

  @Override protected String extractID( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).id();
  }

  @Override protected String extractName( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).name();
  }

  @Override protected String extractDesc( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).description();
  }

  @Override protected String extractCategory( Annotation annotation ) {
    return null;
  }

  @Override protected String extractImageFile( Annotation annotation ) {
    return null;
  }

  @Override protected boolean extractSeparateClassLoader( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).isSeparateClassLoaderNeeded();
  }

  @Override protected String extractI18nPackageName( Annotation annotation ) {
    return null;
  }

  @Override protected String extractDocumentationUrl( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).documentationUrl();
  }

  @Override protected String extractCasesUrl( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).casesUrl();
  }

  @Override protected String extractForumUrl( Annotation annotation ) {
    return ( (ExecutionEngine) annotation ).forumUrl();
  }

  @Override protected void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation ) {

  }
}

