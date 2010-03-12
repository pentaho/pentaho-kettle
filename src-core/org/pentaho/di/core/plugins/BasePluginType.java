package org.pentaho.di.core.plugins;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.annotation.Annotation;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSelectInfo;
import org.apache.commons.vfs.FileSelector;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;
import org.scannotation.AnnotationDB;
import org.w3c.dom.Node;

public abstract class BasePluginType implements PluginTypeInterface{
	private static Class<?> PKG = BasePluginType.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	protected String id;
	protected String name;
	protected List<PluginFolderInterface> pluginFolders;
	
	protected PluginRegistry registry;

	protected LogChannel log;
	
	protected Map<Class<?>, String> objectTypes = new HashMap<Class<?>, String>();
	
	Class<? extends java.lang.annotation.Annotation> pluginType;

	public BasePluginType(Class<? extends java.lang.annotation.Annotation> pluginType) {
		this.pluginFolders = new ArrayList<PluginFolderInterface>();
		this.log = new LogChannel("Plugin type");
		
		registry = PluginRegistry.getInstance();
		this.pluginType = pluginType;
	}
	
	/**
	 * @param id The plugin type ID
	 * @param name the name of the plugin
	 */
	public BasePluginType(Class<? extends java.lang.annotation.Annotation> pluginType, String id, String name) {
		this(pluginType);
		this.id = id;
		this.name = name;
	}
	
	/**
   * this is a utility method for subclasses so they can easily register
   * which folders contain plugins
   *
   * @param xmlSubfolder the sub-folder where xml plugin definitions can be found
   */
	protected void populateFolders(String xmlSubfolder) {
	  pluginFolders.addAll(PluginFolder.populateFolders(xmlSubfolder));
	}
	
	public Map<Class<?>, String> getAdditionalRuntimeObjectTypes(){
	  return objectTypes;
	}
	
	public void addObjectType(Class<?> clz, String xmlNodeName){
	  objectTypes.put(clz, xmlNodeName);
	}
	
	@Override
	public String toString() {
		return name+"("+id+")";
	}

	/**
	 * Let's put in code here to search for the step plugins..
	 */
	public void searchPlugins() throws KettlePluginException {
		registerNatives();
		long startScan = System.currentTimeMillis();
		registerPluginJars();
    
long endScan = System.currentTimeMillis();

System.out.println("    jar scan took "+ (endScan - startScan)+"ms");
    
		registerXmlPlugins();
	}
	
	protected abstract void registerNatives() throws KettlePluginException;
	protected abstract void registerXmlPlugins() throws KettlePluginException;
	
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the pluginFolders
	 */
	public List<PluginFolderInterface> getPluginFolders() {
		return pluginFolders;
	}

	/**
	 * @param pluginFolders the pluginFolders to set
	 */
	public void setPluginFolders(List<PluginFolderInterface> pluginFolders) {
		this.pluginFolders = pluginFolders;
	}
	
	protected static String getCodedTranslation(String codedString) {
		if (codedString==null) return null;
		
		if (codedString.startsWith("i18n:")) {
			String[] parts = codedString.split(":");
			if (parts.length!=3) return codedString;
			else {
				return BaseMessages.getString(parts[1], parts[2]);
			}
		} else {
			return codedString;
		}
	}
	
	protected static String getTranslation(String string, String packageName, String altPackageName, Class<?> resourceClass) {
		
		if (string.startsWith("i18n:")) {
			String[] parts = string.split(":");
			if (parts.length!=3) return string;
			else {
				return BaseMessages.getString(parts[1], parts[2]);
			}
		} else {
			int oldLogLevel = LogWriter.getInstance().getLogLevel();
			
			// avoid i18n messages for missing locale
			//
			LogWriter.getInstance().setLogLevel(LogWriter.LOG_LEVEL_BASIC);
						
			// Try the default package name
			//
			String translation = BaseMessages.getString(packageName, string, resourceClass);
			if (translation.startsWith("!") && translation.endsWith("!")) translation=BaseMessages.getString(PKG, string, resourceClass);
			
			// restore loglevel, when the last alternative fails, log it when loglevel is detailed
			//
			LogWriter.getInstance().setLogLevel(oldLogLevel); 
			if (translation.startsWith("!") && translation.endsWith("!")) translation=BaseMessages.getString(altPackageName, string, resourceClass);
			
			return translation;
		}
	}
	
	protected List<JarFileAnnotationPlugin> findAnnotatedClassFiles(String annotationClassName) {
		
		List<JarFileAnnotationPlugin> classFiles = new ArrayList<JarFileAnnotationPlugin>();
		
		// We want to scan the plugins folder for plugin.xml files...
		//
		for (PluginFolderInterface pluginFolder : getPluginFolders()) {
			
			if (pluginFolder.isPluginAnnotationsFolder()) {
				
				try {
					// Get all the jar files in the plugin folder...
					//
					FileObject[] fileObjects = pluginFolder.findJarFiles();
					if (fileObjects!=null) {
						for (FileObject fileObject : fileObjects) {
							
							// These are the jar files : find annotations in it...
							//
				      AnnotationDB annotationDB = new AnnotationDB();
				      
				      annotationDB.scanArchives(fileObject.getURL());
				      
				      // These are the jar files : find annotations in it...
				      //
				      Set<String> impls = annotationDB.getAnnotationIndex().get(annotationClassName);
				      if(impls != null){
  				      
  				      for(String fil : impls)
  				        classFiles.add(new JarFileAnnotationPlugin(fil, fileObject.getURL(),fileObject.getParent().getURL()));
				      }    
				    }

					}					
				} catch(Exception e) {
				  e.printStackTrace();
				}
			}
		}
		
		return classFiles;
	}
	
	
	protected List<FileObject> findPluginXmlFiles(String folder) {
		
		List<FileObject> list = new ArrayList<FileObject>();
		try {
			FileObject folderObject = KettleVFS.getFileObject(folder);
			FileObject[] files = folderObject.findFiles(
				new FileSelector() {
					
					public boolean traverseDescendents(FileSelectInfo fileSelectInfo) throws Exception {
						return true;
					}
					
					public boolean includeFile(FileSelectInfo fileSelectInfo) throws Exception {
						return fileSelectInfo.getFile().toString().matches(".*\\/plugin\\.xml$");
					}
				}
			);
			for (FileObject file : files) {
				list.add(file);
			}
			
		} catch(Exception e) {
			// ignore this: unknown folder, insufficient permissions, etc
		}
		return list;
	}
	
	 /**
   * This method allows for custom registration of plugins that are on the main classpath.  This was originally
   * created so that test environments could register test plugins programmatically.
   * 
   * @param clazz the plugin implementation to register 
   * @param category the category of the plugin
   * @param id the id for the plugin
   * @param name the name for the plugin
   * @param description the description for the plugin
   * @param image the image for the plugin
   * @throws KettlePluginException
   */
  @SuppressWarnings("unchecked")
  public void registerCustom(Class<?> clazz, String category, String id, String name, String description, String image) throws KettlePluginException {
    Class<? extends PluginTypeInterface> pluginType = (Class<? extends PluginTypeInterface>)getClass();
    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
    PluginMainClassType mainClassTypesAnnotation = pluginType.getAnnotation(PluginMainClassType.class);
    classMap.put(mainClassTypesAnnotation.value(), clazz.getName());
    PluginInterface stepPlugin = new Plugin(new String[]{id}, pluginType, mainClassTypesAnnotation.value(), category, name, description, image, false, false, classMap, new ArrayList<String>(), null, null);
    registry.registerPlugin(pluginType, stepPlugin);
  }


    protected PluginInterface registerPluginFromXmlResource( Node pluginNode, String path, Class<? extends PluginTypeInterface> pluginType, boolean nativePlugin, URL pluginFolder) throws KettlePluginException {
        try
        {

        	String id = XMLHandler.getTagAttribute(pluginNode, "id"); //$NON-NLS-1$
            String description = getTagOrAttribute(pluginNode, "description"); //$NON-NLS-1$
            String iconfile = getTagOrAttribute(pluginNode, "iconfile"); //$NON-NLS-1$
            String tooltip = getTagOrAttribute(pluginNode, "tooltip"); //$NON-NLS-1$
            String category = getTagOrAttribute(pluginNode, "category"); //$NON-NLS-1$
            String classname = getTagOrAttribute(pluginNode, "classname"); //$NON-NLS-1$
            String errorHelpfile = getTagOrAttribute(pluginNode, "errorhelpfile"); //$NON-NLS-1$
            
            Node libsnode = XMLHandler.getSubNode(pluginNode, "libraries"); //$NON-NLS-1$
            int nrlibs = XMLHandler.countNodes(libsnode, "library"); //$NON-NLS-1$

            List<String> jarFiles = new ArrayList<String>();
            if( path != null ) {
                for (int j = 0; j < nrlibs; j++)
                {
                    Node libnode = XMLHandler.getSubNodeByNr(libsnode, "library", j); //$NON-NLS-1$
                    String jarfile = XMLHandler.getTagAttribute(libnode, "name"); //$NON-NLS-1$
                    jarFiles.add( new File(path + Const.FILE_SEPARATOR + jarfile).getAbsolutePath() );
                }
            }
            
            // Localized categories, descriptions and tool tips
            //
            Map<String, String> localizedCategories = readPluginLocale(pluginNode, "localized_category", "category");
            category = getAlternativeTranslation(category, localizedCategories);
            
            Map<String, String> localizedDescriptions = readPluginLocale(pluginNode, "localized_description", "description");
            description = getAlternativeTranslation(description, localizedDescriptions);
            
            Map<String, String> localizedTooltips = readPluginLocale(pluginNode, "localized_tooltip", "tooltip");
            tooltip = getAlternativeTranslation(tooltip, localizedTooltips);
            
            String iconFilename = (path == null) ? iconfile : path + Const.FILE_SEPARATOR + iconfile;
            String errorHelpFileFull = errorHelpfile;
            if (!Const.isEmpty(errorHelpfile)) errorHelpFileFull = (path == null) ? errorHelpfile : path+Const.FILE_SEPARATOR+errorHelpfile;
            
            Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
            
            PluginMainClassType mainClassTypesAnnotation = pluginType.getAnnotation(PluginMainClassType.class);
            classMap.put(mainClassTypesAnnotation.value(), classname);
            
            // process annotated extra types
            PluginExtraClassTypes classTypesAnnotation = pluginType.getAnnotation(PluginExtraClassTypes.class);
            if(classTypesAnnotation != null){
              for(int i=0; i< classTypesAnnotation.classTypes().length; i++){
                Class<?> classType = classTypesAnnotation.classTypes()[i];
                String className = getTagOrAttribute(pluginNode, classTypesAnnotation.xmlNodeNames()[i]); //$NON-NLS-1$
                
                classMap.put(classType, className);
              }
            }
            
            // process extra types added at runtime
            Map<Class<?>, String> objectMap = getAdditionalRuntimeObjectTypes();
            for(Map.Entry<Class<?>, String> entry : objectMap.entrySet()){
              String clzName = getTagOrAttribute(pluginNode, entry.getValue()); //$NON-NLS-1$
              classMap.put(entry.getKey(), clzName); 
            }
            
            PluginInterface pluginInterface = new Plugin(id.split(","), pluginType, mainClassTypesAnnotation.value(), category, description, tooltip, iconFilename, false, nativePlugin, classMap, jarFiles, errorHelpFileFull, pluginFolder);
            registry.registerPlugin(pluginType, pluginInterface);
            
            return pluginInterface;
        }
        catch (Throwable e)
        {
            throw new KettlePluginException( BaseMessages.getString(PKG, "BasePluginType.RuntimeError.UnableToReadPluginXML.PLUGIN0001"), e); //$NON-NLS-1$
        }
    }

    private String getTagOrAttribute(Node pluginNode, String tag) {
		String string = XMLHandler.getTagValue(pluginNode, tag);
		if (string==null) {
            string = XMLHandler.getTagAttribute(pluginNode, tag); //$NON-NLS-1$
		}
		return string;
	}

	/**
     * 
     * @param input
     * @param localizedMap
     * @return
     */
	private String getAlternativeTranslation(String input, Map<String, String> localizedMap) {
		
		if (Const.isEmpty(input)) {
			return null;
		}
		
		if (input.startsWith("i18n")) {
			return getCodedTranslation(input);
		} else {
			String defLocale = LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase();
			String alt = localizedMap.get(defLocale);
			if (!Const.isEmpty(alt)) {
				return alt;
			}
			String failoverLocale = LanguageChoice.getInstance().getFailoverLocale().toString().toLowerCase();
			alt = localizedMap.get(failoverLocale);
			if (!Const.isEmpty(alt)) {
				return alt;
			}
			// Nothing found? 
			// Return the original!
			//
			return input;
		}
	}

	private Map<String, String> readPluginLocale(Node pluginNode, String localizedTag, String translationTag) {
		Map<String, String> map = new Hashtable<String, String>();
        
        Node locTipsNode = XMLHandler.getSubNode(pluginNode, localizedTag);
        int nrLocTips = XMLHandler.countNodes(locTipsNode, translationTag);
        for (int j=0 ; j < nrLocTips; j++)
        {
            Node locTipNode = XMLHandler.getSubNodeByNr(locTipsNode, translationTag, j);
            if (locTipNode!=null) {
	            String locale = XMLHandler.getTagAttribute(locTipNode, "locale");
	            String locTip = XMLHandler.getNodeValue(locTipNode);
	            
	            if (!Const.isEmpty(locale) && !Const.isEmpty(locTip))
	            {
	                map.put(locale.toLowerCase(), locTip);
	            }
            }
        }
        
        return map;
	}
	
	/**
	 * Create a new URL class loader with the jar file specified.  Also include all the jar files in the lib folder next to that file.
	 * 
	 * @param jarFileUrl The jar file to include
	 * @param classLoader the parent class loader to use
	 * @return The URL class loader
	 */
	protected URLClassLoader createUrlClassLoader(URL jarFileUrl, ClassLoader classLoader) {
		List<URL> urls = new ArrayList<URL>();
		
		// Also append all the files in the underlying lib folder if it exists...
		//
		try {
			String libFolderName = new File(URLDecoder.decode(jarFileUrl.getFile(), "UTF-8")).getParent()+"/lib";
			if (new File(libFolderName).exists()) {
				PluginFolder pluginFolder = new PluginFolder(libFolderName, false, true);
				FileObject[] libFiles = pluginFolder.findJarFiles();
				for (FileObject libFile : libFiles) {
					urls.add(libFile.getURL());
				}
			}
		} catch(Exception e) {
			LogChannel.GENERAL.logError("Unexpected error searching for jar files in lib/ folder next to '"+jarFileUrl+"'", e);
		}

		urls.add(jarFileUrl);

		return new KettleURLClassLoader(urls.toArray(new URL[urls.size()]), classLoader);
	}

	protected abstract String extractID(java.lang.annotation.Annotation annotation);
	protected abstract String extractName(java.lang.annotation.Annotation annotation);
	protected abstract String extractDesc(java.lang.annotation.Annotation annotation);
	protected abstract String extractCategory(java.lang.annotation.Annotation annotation);
    
	protected void registerPluginJars() throws KettlePluginException {
	    
	    List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(pluginType.getName());
	    for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
	      
	      URLClassLoader urlClassLoader = createUrlClassLoader(jarFilePlugin.getJarFile(), getClass().getClassLoader());

	      try {
	        Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassName());
	        if (clazz==null) {
	        	throw new KettlePluginException("Unable to load class: "+jarFilePlugin.getClassName());
	        }
	        List<String> libraries = new ArrayList<String>();
	        java.lang.annotation.Annotation annotation = null;
	        try {
	        	annotation = clazz.getAnnotation(pluginType);
		        
		        File f = new File(jarFilePlugin.getJarFile().getFile());
		        File parent = f.getParentFile();
		        for(File fil : parent.listFiles()){
		          try {
		            libraries.add(fil.toURI().toURL().getFile());
		          } catch (MalformedURLException e) {
		            e.printStackTrace();
		          }
		        }
		        File libDir = new File(parent.toString()+File.separator+"lib");;
		        if(libDir.exists()){
		          for(File fil : libDir.listFiles()){
		            if(fil.getName().indexOf(".jar") > 0){
		              try {
		                libraries.add(fil.toURI().toURL().getFile());
		              } catch (MalformedURLException e) {
		                e.printStackTrace();
		              }
		            }
		          }
		        }
	        } catch(Exception e) {
	        	throw new KettlePluginException("Unexpected error loading class "+clazz.getName()+" of plugin type: "+pluginType, e);
	        }

	        handlePluginAnnotation(clazz, annotation, libraries, false, jarFilePlugin.getPluginFolder());
	      } catch(ClassNotFoundException e) {
	        // Ignore for now, don't know if it's even possible.
	      }
	    }
	  }
	  
    
	private void handlePluginAnnotation(Class<?> clazz, java.lang.annotation.Annotation annotation, List<String> libraries, boolean nativeRepositoryType, URL pluginFolder) throws KettlePluginException {
      
      // Only one ID for now
      String[] ids = new String[] { extractID(annotation), }; 
      
      if (ids.length == 1 && Const.isEmpty(ids[0])) { 
          throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
      }
      
      String name = extractName(annotation);
      String description = extractDesc(annotation); 
      String category = extractCategory(annotation); 
      
      
      Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();

      PluginMainClassType mainType = getClass().getAnnotation(PluginMainClassType.class);
      
      classMap.put(mainType.value(), clazz.getName());
      
      PluginClassTypeMapping extraTypes = clazz.getAnnotation(PluginClassTypeMapping.class);
      if(extraTypes != null){
        for(int i=0; i< extraTypes.classTypes().length; i++){
          classMap.put(extraTypes.classTypes()[i], extraTypes.implementationClass()[i].getName());
        }
      }
      
      PluginInterface plugin = new Plugin(ids, this.getClass(), mainType.value(), category, name, description, null, false, nativeRepositoryType, classMap, libraries, null, pluginFolder);
      registry.registerPlugin(this.getClass(), plugin);
  }
}
