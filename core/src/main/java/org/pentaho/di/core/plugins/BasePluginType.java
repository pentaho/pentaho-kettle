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


package org.pentaho.di.core.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.DefaultLogLevel;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.GlobalMessageUtil;
import org.scannotation.AnnotationDB;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class BasePluginType implements PluginTypeInterface {
  protected static final Class<?> PKG = BasePluginType.class; // for i18n purposes, needed by Translator2!!

  protected String id;
  protected String name;
  protected List<PluginFolderInterface> pluginFolders;

  protected PluginRegistry registry;

  protected LogChannel log;

  protected Map<Class<?>, String> objectTypes = new HashMap<>();

  protected boolean searchLibDir;

  Class<? extends java.lang.annotation.Annotation> pluginClass;

  public BasePluginType( Class<? extends java.lang.annotation.Annotation> pluginClass ) {
    this.pluginFolders = new ArrayList<>();
    this.log = new LogChannel( "Plugin type" );

    registry = PluginRegistry.getInstance();
    this.pluginClass = pluginClass;
  }

  /**
   * @param id
   *          The plugin type ID
   * @param name
   *          the name of the plugin
   */
  public BasePluginType( Class<? extends java.lang.annotation.Annotation> pluginClass, String id, String name ) {
    this( pluginClass );
    this.id = id;
    this.name = name;
  }

  /**
   * This method return parameter for registerNatives() method
   *
   * @return XML plugin file
   */
  protected String getXmlPluginFile() {
    return null;
  }

  /**
   * This method return parameter for registerNatives() method
   *
   * @return Alternative XML plugin file
   */
  protected String getAlternativePluginFile() {
    return null;
  }

  /**
   * This method return parameter for registerPlugins() method
   *
   * @return Main XML tag
   */
  protected String getMainTag() {
    return null;
  }

  /**
   * This method return parameter for registerPlugins() method
   *
   * @return Subordinate XML tag
   */
  protected String getSubTag() {
    return null;
  }

  /**
   * This method return parameter for registerPlugins() method
   *
   * @return Path
   */
  protected String getPath() {
    return null;
  }

  /**
   * This method return parameter for registerNatives() method
   *
   * @return Flag ("return;" or "throw exception")
   */
  protected boolean isReturn() {
    return false;
  }

  /**
   * this is a utility method for subclasses so they can easily register which folders contain plugins
   *
   * @param xmlSubfolder
   *          the sub-folder where xml plugin definitions can be found
   */
  protected void populateFolders( String xmlSubfolder ) {
    pluginFolders.addAll( PluginFolder.populateFolders( xmlSubfolder ) );
  }

  public Map<Class<?>, String> getAdditionalRuntimeObjectTypes() {
    return objectTypes;
  }

  @Override
  public void addObjectType( Class<?> clz, String xmlNodeName ) {
    objectTypes.put( clz, xmlNodeName );
  }

  @Override
  public String toString() {
    return name + "(" + id + ")";
  }

  /**
   * Let's put in code here to search for the step plugins..
   */
  @Override
  public void searchPlugins() throws KettlePluginException {
    registerNatives();
    registerPluginJars();
    registerXmlPlugins();
  }

  protected void registerNatives() throws KettlePluginException {
    // Scan the native steps...
    //
    String xmlFile = getXmlPluginFile();
    String alternative = null;
    if ( !Utils.isEmpty( getAlternativePluginFile() ) ) {
      alternative = getPropertyExternal( getAlternativePluginFile(), null );
      if ( !Utils.isEmpty( alternative ) ) {
        xmlFile = alternative;
      }
    }

    // Load the plugins for this file...
    //
    InputStream inputStream = null;
    try {
      inputStream = getResAsStreamExternal( xmlFile );
      if ( inputStream == null ) {
        inputStream = getResAsStreamExternal( "/" + xmlFile );
      }

      if ( !Utils.isEmpty( getAlternativePluginFile() ) && inputStream == null && !Utils.isEmpty( alternative ) ) {
        // Retry to load a regular file...
        try {
          inputStream = getFileInputStreamExternal( xmlFile );
        } catch ( Exception e ) {
          throw new KettlePluginException( "Unable to load native plugins '" + xmlFile + "'", e );
        }
      }

      if ( inputStream == null ) {
        if ( isReturn() ) {
          return;
        } else {
          throw new KettlePluginException( "Unable to find native plugins definition file: " + xmlFile );
        }
      }

      registerPlugins( inputStream );

    } catch ( KettleXMLException e ) {
      throw new KettlePluginException( "Unable to read the kettle XML config file: " + xmlFile, e );
    } finally {
      IOUtils.closeQuietly( inputStream );
    }
  }

  @VisibleForTesting
  protected String getPropertyExternal( String key, String def ) {
    return System.getProperty( key, def );
  }

  @VisibleForTesting
  protected InputStream getResAsStreamExternal( String name ) {
    return getClass().getResourceAsStream( name );
  }

  @VisibleForTesting
  protected InputStream getFileInputStreamExternal( String name ) throws FileNotFoundException {
    return new FileInputStream( name );
  }

  /**
   * This method registers plugins from the InputStream with the XML Resource
   *
   * @param inputStream
   * @throws KettlePluginException
   * @throws KettleXMLException
   */
  protected void registerPlugins( InputStream inputStream ) throws KettlePluginException, KettleXMLException {
    Document document = XMLHandler.loadXMLFile( inputStream, null, true, false );

    Node repsNode = XMLHandler.getSubNode( document, getMainTag() );
    List<Node> repsNodes = XMLHandler.getNodes( repsNode, getSubTag() );

    for ( Node repNode : repsNodes ) {
      registerPluginFromXmlResource( repNode, getPath(), this.getClass(), true, null );
    }
  }

  protected abstract void registerXmlPlugins() throws KettlePluginException;

  /**
   * @return the id
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * @param id
   *          the id to set
   */
  public void setId( String id ) {
    this.id = id;
  }

  /**
   * @return the name
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public void setName( String name ) {
    this.name = name;
  }

  /**
   * @return the pluginFolders
   */
  @Override
  public List<PluginFolderInterface> getPluginFolders() {
    return pluginFolders;
  }

  /**
   * @param pluginFolders
   *          the pluginFolders to set
   */
  public void setPluginFolders( List<PluginFolderInterface> pluginFolders ) {
    this.pluginFolders = pluginFolders;
  }

  protected static String getCodedTranslation( String codedString ) {
    if ( codedString == null ) {
      return null;
    }

    if ( codedString.startsWith( "i18n:" ) ) {
      String[] parts = codedString.split( ":" );
      if ( parts.length != 3 ) {
        return codedString;
      } else {
        return BaseMessages.getString( parts[1], parts[2] );
      }
    } else {
      return codedString;
    }
  }

  protected static String getTranslation( String string, String packageName, String altPackageName,
    Class<?> resourceClass ) {
    if ( string == null ) {
      return null;
    }

    if ( string.startsWith( "i18n:" ) ) {
      String[] parts = string.split( ":" );
      if ( parts.length != 3 ) {
        return string;
      } else {
        return BaseMessages.getString( parts[1], parts[2] );
      }
    } else {
      // Try the default package name
      //
      String translation;
      if ( !Utils.isEmpty( packageName ) ) {
        LogLevel oldLogLevel = DefaultLogLevel.getLogLevel();

        // avoid i18n messages for missing locale
        //
        DefaultLogLevel.setLogLevel( LogLevel.BASIC );

        translation = BaseMessages.getString( packageName, string, resourceClass );
        if ( translation.startsWith( "!" ) && translation.endsWith( "!" ) ) {
          translation = BaseMessages.getString( PKG, string, resourceClass );
        }

        // restore loglevel, when the last alternative fails, log it when loglevel is detailed
        //
        DefaultLogLevel.setLogLevel( oldLogLevel );
        if ( !Utils.isEmpty( altPackageName ) && translation.startsWith( "!" ) && translation.endsWith( "!" ) ) {
          translation = BaseMessages.getString( altPackageName, string, resourceClass );
        }
      } else {
        // Translations are not supported, simply keep the original text.
        //
        translation = string;
      }

      return translation;
    }
  }

  protected List<JarFileAnnotationPlugin> findAnnotatedClassFiles( String annotationClassName ) {
    JarFileCache jarFileCache = JarFileCache.getInstance();
    List<JarFileAnnotationPlugin> classFiles = new ArrayList<>();

    // We want to scan the plugins folder for plugin.xml files...
    //
    for ( PluginFolderInterface pluginFolder : getPluginFolders() ) {

      if ( pluginFolder.isPluginAnnotationsFolder() ) {

        FileObject[] fileObjects = null;
        try {
          // Get all the jar files in the plugin folder...
          //
          fileObjects = jarFileCache.getFileObjects( pluginFolder );
        } catch ( Exception e ) {
          log.logError( e.getMessage(), e );
        }

        if ( fileObjects != null ) {
          for ( FileObject fileObject : fileObjects ) {
            // These are the jar files : find annotations in it...
            //
            try {
              AnnotationDB annotationDB = jarFileCache.getAnnotationDB( fileObject );
              Set<String> impls = annotationDB.getAnnotationIndex().get( annotationClassName );
              if ( impls != null ) {

                for ( String fil : impls ) {
                  classFiles.add( new JarFileAnnotationPlugin( fil, fileObject.getURL(), fileObject
                    .getParent().getURL() ) );
                }
              }
            } catch ( Exception jarPluginLoadError ) {
              LogChannel.GENERAL.logError( "Error while finding annotations for jar plugin: '"
                + fileObject + "'" );
              LogChannel.GENERAL.logDebug( "Error while finding annotations for jar plugin: '"
                + fileObject + "'", jarPluginLoadError );
            }
          }
        }
      }
    }
    return classFiles;
  }

  protected List<FileObject> findPluginXmlFiles( String folder ) {

    return findPluginFiles( folder, ".*\\/plugin\\.xml$" );
  }

  protected List<FileObject> findPluginFiles( String folder, final String regex ) {

    List<FileObject> list = new ArrayList<>();
    try {
      FileObject folderObject = KettleVFS.getFileObject( folder );
      FileObject[] files = folderObject.findFiles( new FileSelector() {

        @Override
        public boolean traverseDescendents( FileSelectInfo fileSelectInfo ) throws Exception {
          return true;
        }

        @Override
        public boolean includeFile( FileSelectInfo fileSelectInfo ) throws Exception {
          return fileSelectInfo.getFile().toString().matches( regex );
        }
      } );
      if ( files != null ) {
        Collections.addAll( list, files );
      }
    } catch ( Exception e ) {
      // ignore this: unknown folder, insufficient permissions, etc
    }
    return list;
  }

  /**
   * This method allows for custom registration of plugins that are on the main classpath. This was originally created
   * so that test environments could register test plugins programmatically.
   *
   * @param clazz
   *          the plugin implementation to register
   * @param cat
   *          the category of the plugin
   * @param id
   *          the id for the plugin
   * @param name
   *          the name for the plugin
   * @param desc
   *          the description for the plugin
   * @param image
   *          the image for the plugin
   * @throws KettlePluginException
   */
  public void registerCustom( Class<?> clazz, String cat, String id, String name, String desc, String image ) throws KettlePluginException {
    Class<? extends PluginTypeInterface> pluginType = getClass();
    Map<Class<?>, String> classMap = new HashMap<>();
    PluginMainClassType mainClassTypesAnnotation = pluginType.getAnnotation( PluginMainClassType.class );
    classMap.put( mainClassTypesAnnotation.value(), clazz.getName() );
    PluginInterface stepPlugin =
      new Plugin(
        new String[] { id }, pluginType, mainClassTypesAnnotation.value(), cat, name, desc, image, false,
        false, classMap, new ArrayList<String>(), null, null, null, null, null );
    registry.registerPlugin( pluginType, stepPlugin );
  }

  protected PluginInterface registerPluginFromXmlResource( Node pluginNode, String path,
    Class<? extends PluginTypeInterface> pluginType, boolean nativePlugin, URL pluginFolder ) throws KettlePluginException {
    try {

      String idAttr = XMLHandler.getTagAttribute( pluginNode, "id" );
      String description = getTagOrAttribute( pluginNode, "description" );
      String iconfile = getTagOrAttribute( pluginNode, "iconfile" );
      String tooltip = getTagOrAttribute( pluginNode, "tooltip" );
      String category = getTagOrAttribute( pluginNode, "category" );
      String classname = getTagOrAttribute( pluginNode, "classname" );
      String errorHelpfile = getTagOrAttribute( pluginNode, "errorhelpfile" );
      String documentationUrl = getTagOrAttribute( pluginNode, "documentation_url" );
      String casesUrl = getTagOrAttribute( pluginNode, "cases_url" );
      String forumUrl = getTagOrAttribute( pluginNode, "forum_url" );
      String suggestion = getTagOrAttribute( pluginNode, "suggestion" );

      Node libsnode = XMLHandler.getSubNode( pluginNode, "libraries" );
      int nrlibs = XMLHandler.countNodes( libsnode, "library" );

      List<String> jarFiles = new ArrayList<>();
      if ( path != null ) {
        for ( int j = 0; j < nrlibs; j++ ) {
          Node libnode = XMLHandler.getSubNodeByNr( libsnode, "library", j );
          String jarfile = XMLHandler.getTagAttribute( libnode, "name" );
          jarFiles.add( new File( path + Const.FILE_SEPARATOR + jarfile ).getAbsolutePath() );
        }
      }

      // Localized categories, descriptions and tool tips
      //
      Map<String, String> localizedCategories = readPluginLocale( pluginNode, "localized_category", "category" );
      category = getAlternativeTranslation( category, localizedCategories );

      Map<String, String> localDescriptions =
        readPluginLocale( pluginNode, "localized_description", "description" );
      description = getAlternativeTranslation( description, localDescriptions );
      description += addDeprecation( category );

      suggestion = getAlternativeTranslation( suggestion, localDescriptions );

      Map<String, String> localizedTooltips = readPluginLocale( pluginNode, "localized_tooltip", "tooltip" );
      tooltip = getAlternativeTranslation( tooltip, localizedTooltips );

      String iconFilename = ( path == null ) ? iconfile : path + Const.FILE_SEPARATOR + iconfile;
      String errorHelpFileFull = errorHelpfile;
      if ( !Utils.isEmpty( errorHelpfile ) ) {
        errorHelpFileFull = ( path == null ) ? errorHelpfile : path + Const.FILE_SEPARATOR + errorHelpfile;
      }

      Map<Class<?>, String> classMap = new HashMap<>();

      PluginMainClassType mainClassTypesAnnotation = pluginType.getAnnotation( PluginMainClassType.class );
      classMap.put( mainClassTypesAnnotation.value(), classname );

      // process annotated extra types
      PluginExtraClassTypes classTypesAnnotation = pluginType.getAnnotation( PluginExtraClassTypes.class );
      if ( classTypesAnnotation != null ) {
        for ( int i = 0; i < classTypesAnnotation.classTypes().length; i++ ) {
          Class<?> classType = classTypesAnnotation.classTypes()[i];
          String className = getTagOrAttribute( pluginNode, classTypesAnnotation.xmlNodeNames()[i] );

          classMap.put( classType, className );
        }
      }

      // process extra types added at runtime
      Map<Class<?>, String> objectMap = getAdditionalRuntimeObjectTypes();
      for ( Map.Entry<Class<?>, String> entry : objectMap.entrySet() ) {
        String clzName = getTagOrAttribute( pluginNode, entry.getValue() );
        classMap.put( entry.getKey(), clzName );
      }

      PluginInterface pluginInterface =
        new Plugin(
          idAttr.split( "," ), pluginType, mainClassTypesAnnotation.value(), category, description, tooltip,
          iconFilename, false, nativePlugin, classMap, jarFiles, errorHelpFileFull, pluginFolder,
          documentationUrl, casesUrl, forumUrl, suggestion );
      registry.registerPlugin( pluginType, pluginInterface );

      return pluginInterface;
    } catch ( Exception e ) {
      throw new KettlePluginException( BaseMessages.getString(
        PKG, "BasePluginType.RuntimeError.UnableToReadPluginXML.PLUGIN0001" ), e );
    }
  }

  protected String getTagOrAttribute( Node pluginNode, String tag ) {
    String string = XMLHandler.getTagValue( pluginNode, tag );
    if ( string == null ) {
      string = XMLHandler.getTagAttribute( pluginNode, tag );
    }
    return string;
  }

  /**
   *
   * @param input
   * @param localizedMap
   * @return
   */
  protected String getAlternativeTranslation( String input, Map<String, String> localizedMap ) {

    if ( Utils.isEmpty( input ) ) {
      return null;
    }

    if ( input.startsWith( "i18n" ) ) {
      return getCodedTranslation( input );
    } else {
      for ( final Locale locale : GlobalMessageUtil.getActiveLocales() ) {
        String alt = localizedMap.get( locale.toString().toLowerCase() );
        if ( !Utils.isEmpty( alt ) ) {
          return alt;
        }
      }
      // Nothing found?
      // Return the original!
      //
      return input;
    }
  }

  protected Map<String, String> readPluginLocale( Node pluginNode, String localizedTag, String translationTag ) {
    Map<String, String> map = new HashMap<>();

    Node locTipsNode = XMLHandler.getSubNode( pluginNode, localizedTag );
    int nrLocTips = XMLHandler.countNodes( locTipsNode, translationTag );
    for ( int j = 0; j < nrLocTips; j++ ) {
      Node locTipNode = XMLHandler.getSubNodeByNr( locTipsNode, translationTag, j );
      if ( locTipNode != null ) {
        String locale = XMLHandler.getTagAttribute( locTipNode, "locale" );
        String locTip = XMLHandler.getNodeValue( locTipNode );

        if ( !Utils.isEmpty( locale ) && !Utils.isEmpty( locTip ) ) {
          map.put( locale.toLowerCase(), locTip );
        }
      }
    }

    return map;
  }

  /**
   * Create a new URL class loader with the jar file specified. Also include all the jar files in the lib folder next to
   * that file.
   *
   * @param jarFileUrl
   *          The jar file to include
   * @param classLoader
   *          the parent class loader to use
   * @return The URL class loader
   */
  protected URLClassLoader createUrlClassLoader( URL jarFileUrl, ClassLoader classLoader ) {
    List<URL> urls = new ArrayList<>();

    // Also append all the files in the underlying lib folder if it exists...
    //
    try {
      String libFolderName = new File( URLDecoder.decode( jarFileUrl.getFile(), "UTF-8" ) ).getParent()
        + Const.FILE_SEPARATOR + "lib";
      if ( new File( libFolderName ).exists() ) {
        PluginFolder pluginFolder = new PluginFolder( libFolderName, false, true, searchLibDir );
        FileObject[] libFiles = pluginFolder.findJarFiles( true );
        for ( FileObject libFile : libFiles ) {
          urls.add( libFile.getURL() );
        }
      }
    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( "Unexpected error searching for jar files in lib/ folder next to '"
        + jarFileUrl + "'", e );
    }

    urls.add( jarFileUrl );

    KettleURLClassLoader urlClassLoader = new KettleURLClassLoader( urls.toArray( new URL[ urls.size() ] ), classLoader );
    return processPluginClasspath( urlClassLoader, jarFileUrl, urls, classLoader );
  }

  /**
   * Adds the entries defined in the classpath.properties located at the root of the plugin to the plugin classpath
   */
  private KettleURLClassLoader processPluginClasspath(
    KettleURLClassLoader urlClassLoader, URL jarFileUrl, List<URL> urls, ClassLoader classLoader ) {
    try {
      String pluginRootFolderName = new File( URLDecoder.decode( jarFileUrl.getFile(), "UTF-8" ) ).getParent();
      File pluginRootFolder = new File( pluginRootFolderName );
      if( pluginRootFolder.exists() ) {
        File classPathFile = new File( pluginRootFolder, "classpath.properties" );
        if ( classPathFile.exists() ) {
          FileInputStream classPathFileInputStream = new FileInputStream( classPathFile );
          Properties classPathProperties = new Properties();
          classPathProperties.load( classPathFileInputStream );
          String classpathProperty = classPathProperties.getProperty( "classpath" );
          String[] sourceDirectories = classpathProperty.split( ":" );
          for ( String sourceDirectory : sourceDirectories ) {
            File sourceDirectoryFile = new File( pluginRootFolder, sourceDirectory );
            if ( sourceDirectoryFile.getCanonicalFile().exists() ) {
              PluginFolder pluginFolder = new PluginFolder(
                sourceDirectoryFile.getCanonicalPath(), false, true, searchLibDir );
              FileObject[] libFiles = pluginFolder.findJarFiles( true );
              for ( FileObject libFile : libFiles ) {
                urls.add( libFile.getURL() );
              }
            }
          }
          urlClassLoader = new KettleURLClassLoader( urls.toArray( new URL[ urls.size() ] ), classLoader );
        }
      }

    } catch ( Exception e ) {
      LogChannel.GENERAL.logError( e.getMessage() );
    }
    return urlClassLoader;
  }

  protected abstract String extractID( java.lang.annotation.Annotation annotation );

  protected abstract String extractName( java.lang.annotation.Annotation annotation );

  protected abstract String extractDesc( java.lang.annotation.Annotation annotation );

  protected abstract String extractCategory( java.lang.annotation.Annotation annotation );

  protected abstract String extractImageFile( java.lang.annotation.Annotation annotation );

  protected abstract boolean extractSeparateClassLoader( java.lang.annotation.Annotation annotation );

  protected abstract String extractI18nPackageName( java.lang.annotation.Annotation annotation );

  protected abstract String extractDocumentationUrl( java.lang.annotation.Annotation annotation );

  protected abstract String extractSuggestion( java.lang.annotation.Annotation annotation );

  protected abstract String extractCasesUrl( java.lang.annotation.Annotation annotation );

  protected abstract String extractForumUrl( java.lang.annotation.Annotation annotation );

  @SuppressWarnings( "squid:S1172" )  //Overriding classes use the parameter
  protected String extractClassLoaderGroup( java.lang.annotation.Annotation annotation ) {
    return null;
  }

  /**
   * When set to true the PluginFolder objects created by this type will be instructed to search for additional plugins
   * in the lib directory of plugin folders.
   *
   * @param transverseLibDirs
   */
  protected void setTransverseLibDirs( boolean transverseLibDirs ) {
    this.searchLibDir = transverseLibDirs;
  }

  protected void registerPluginJars() throws KettlePluginException {
    List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles( pluginClass.getName() );
    for ( JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins ) {

      URLClassLoader urlClassLoader =
        createUrlClassLoader( jarFilePlugin.getJarFile(), getClass().getClassLoader() );

      try {
        Class<?> clazz = urlClassLoader.loadClass( jarFilePlugin.getClassName() );
        if ( clazz == null ) {
          throw new KettlePluginException( "Unable to load class: " + jarFilePlugin.getClassName() );
        }
        List<String> libraries = Arrays.stream( urlClassLoader.getURLs() )
          .map( URL::getFile )
          .collect( Collectors.toList() );
        Annotation annotation = clazz.getAnnotation( pluginClass );

        handlePluginAnnotation( clazz, annotation, libraries, false, jarFilePlugin.getPluginFolder() );
      } catch ( Exception e ) {
        // Ignore for now, don't know if it's even possible.
        LogChannel.GENERAL.logError(
          "Unexpected error registering jar plugin file: " + jarFilePlugin.getJarFile(), e );
      } finally {
        if ( urlClassLoader instanceof KettleURLClassLoader ) {
          ( (KettleURLClassLoader) urlClassLoader ).closeClassLoader();
        }
      }
    }
  }

  /**
   * Handle an annotated plugin
   *
   * @param clazz
   *          The class to use
   * @param annotation
   *          The annotation to get information from
   * @param libraries
   *          The libraries to add
   * @param nativePluginType
   *          Is this a native plugin?
   * @param pluginFolder
   *          The plugin folder to use
   * @throws KettlePluginException
   */
  @Override
  public void handlePluginAnnotation( Class<?> clazz, java.lang.annotation.Annotation annotation,
    List<String> libraries, boolean nativePluginType, URL pluginFolder ) throws KettlePluginException {

    String idList = extractID( annotation );
    if ( Utils.isEmpty( idList ) ) {
      throw new KettlePluginException( "No ID specified for plugin with class: " + clazz.getName() );
    }

    // Only one ID for now
    String[] ids = idList.split( "," );

    String packageName = extractI18nPackageName( annotation );
    String altPackageName = clazz.getPackage().getName();
    String pluginName = getTranslation( extractName( annotation ), packageName, altPackageName, clazz );
    String description = getTranslation( extractDesc( annotation ), packageName, altPackageName, clazz );
    String category = getTranslation( extractCategory( annotation ), packageName, altPackageName, clazz );
    String imageFile = extractImageFile( annotation );
    boolean separateClassLoader = extractSeparateClassLoader( annotation );
    String documentationUrl = extractDocumentationUrl( annotation );
    String casesUrl = extractCasesUrl( annotation );
    String forumUrl = extractForumUrl( annotation );
    String suggestion = getTranslation( extractSuggestion( annotation ), packageName, altPackageName, clazz );
    String classLoaderGroup = extractClassLoaderGroup( annotation );

    pluginName += addDeprecation( category );

    Map<Class<?>, String> classMap = new HashMap<>();

    PluginMainClassType mainType = getClass().getAnnotation( PluginMainClassType.class );

    classMap.put( mainType.value(), clazz.getName() );

    addExtraClasses( classMap, clazz, annotation );

    PluginInterface plugin =
      new Plugin(
        ids, this.getClass(), mainType.value(), category, pluginName, description, imageFile, separateClassLoader,
        classLoaderGroup, nativePluginType, classMap, libraries, null, pluginFolder, documentationUrl,
        casesUrl, forumUrl, suggestion );

    ParentFirst parentFirstAnnotation = clazz.getAnnotation( ParentFirst.class );
    if ( parentFirstAnnotation != null ) {
      registry.addParentClassLoaderPatterns( plugin, parentFirstAnnotation.patterns() );
    }
    registry.registerPlugin( this.getClass(), plugin );

    if ( libraries != null && !libraries.isEmpty() ) {
      LogChannel.GENERAL.logDetailed( "Plugin with id ["
        + ids[0] + "] has " + libraries.size() + " libaries in its private class path" );
    }
  }

  /**
   * Extract extra classes information from a plugin annotation.
   *
   * @param classMap
   * @param clazz
   * @param annotation
   */
  protected abstract void addExtraClasses( Map<Class<?>, String> classMap, Class<?> clazz, Annotation annotation );

  private String addDeprecation( String category ) {
    String deprecated = BaseMessages.getString( PKG, "PluginRegistry.Category.Deprecated" );
    if ( deprecated.equals( category )  ) {
      return " (" + deprecated.toLowerCase() + ")";
    }
    return "";
  }
}
