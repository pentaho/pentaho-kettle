package org.pentaho.di.core.plugins;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.Node;

public abstract class BasePluginType {
	private static Class<?> PKG = BasePluginType.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	protected String id;
	protected String name;
	protected List<PluginFolderInterface> pluginFolders;
	
	protected PluginRegistry registry;

	protected LogChannel log;
	
	protected Map<Class<?>, String> objectTypes = new HashMap<Class<?>, String>();

	public BasePluginType() {
		this.pluginFolders = new ArrayList<PluginFolderInterface>();
		this.log = new LogChannel("Plugin type");
		
		registry = PluginRegistry.getInstance();
	}
	
	/**
	 * @param id The plugin type ID
	 * @param name the name of the plugin
	 */
	public BasePluginType(String id, String name) {
		this();
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
		registerAnnotations();
		registerPluginJars();
		registerXmlPlugins();
	}
	
	protected abstract void registerNatives() throws KettlePluginException;
	protected abstract void registerAnnotations() throws KettlePluginException;
	protected abstract void registerPluginJars() throws KettlePluginException;
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
							JarFile jarFile = new JarFile(KettleVFS.getFilename(fileObject));
							Enumeration<JarEntry> entries = jarFile.entries();
							while (entries.hasMoreElements()) {
								JarEntry entry = entries.nextElement();
								try {
									ClassFile classFile = new ClassFile( new DataInputStream(new BufferedInputStream(jarFile.getInputStream(entry))) );
									AnnotationsAttribute visible = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
									if (visible!=null) {
										Annotation[] anns = visible.getAnnotations();
										for (Annotation ann : anns) {
											if (ann.getTypeName().equals(annotationClassName)) {
												classFiles.add(new JarFileAnnotationPlugin(fileObject.getURL(), classFile, ann, fileObject.getParent().getURL()));
												break;
											}
										}
									}
								} catch(Exception e) {
									// Not a class, ignore exception
								}
							}
						}
					}					
				} catch(Exception e) {
					// The plugin folder could not be found or we can't read it
					// Let's not through an exception here
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
            PluginClassTypes classTypesAnnotation = pluginType.getAnnotation(PluginClassTypes.class);
            if(classTypesAnnotation != null){
              for(int i=0; i< classTypesAnnotation.classTypes().length; i++){
                Class<?> classType = classTypesAnnotation.classTypes()[i];
                Class<?> implementationType = (classTypesAnnotation.implementationClass().length > i) ? classTypesAnnotation.implementationClass()[i] : null;
                String className = null;
                if(implementationType != null){
                  className = implementationType.getName();
                } else {
                  className = getTagOrAttribute(pluginNode, classTypesAnnotation.xmlNodeNames()[i]); //$NON-NLS-1$
                }
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
}
