/**
 * 
 */
package org.pentaho.di.core.plugins;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.MemberValue;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.annotations.LifecyclePlugin;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.gui.GUIOption;
import org.pentaho.di.core.lifecycle.LifecycleListener;
import org.pentaho.di.core.logging.LogChannel;

/**
 * This class represents the repository plugin type.
 * 
 * @author matt
 *
 */
@PluginMainClassType(GUIOption.class)
public class LifecyclePluginType extends BasePluginType implements PluginTypeInterface {

	private static LifecyclePluginType pluginType;
	
	private LifecyclePluginType() {
		super("LIFECYCLE LISTENERS", "Lifecycle listener plugin type");
		populateFolders("repositories");
	}
	
	public static LifecyclePluginType getInstance() {
		if (pluginType==null) {
			pluginType=new LifecyclePluginType();
		}
		return pluginType;
	}
	
	/**
	 * Let's put in code here to search for the step plugins..
	 */
	public void searchPlugins() throws KettlePluginException {
		registerNatives(); // none
		registerAnnotations(); // no longer performed
		registerPluginJars();
		registerXmlPlugins();
	}

	/**
	 * Scan & register internal step plugins
	 */
	protected void registerNatives() throws KettlePluginException {
		// Up until now, we have no natives.
	}

	/**
	 * Scan & register internal repository type plugins
	 */
	protected void registerAnnotations() throws KettlePluginException {
		// This is no longer done because it was deemed too slow.  Only jar files in the plugins/ folders are scanned for annotations.
	}

	private void handleLifecyclePluginAnnotation(Class<?> clazz, LifecyclePlugin annotation, List<String> libraries, boolean nativeRepositoryType) throws KettlePluginException {
		
		// Only one ID for now
		String[] ids = new String[] { annotation.id(), }; 
		
		if (ids.length == 1 && Const.isEmpty(ids[0])) { 
			throw new KettlePluginException("No ID specified for plugin with class: "+clazz.getName());
		}
		
		String name = annotation.name();
		String description = null; // No description needed for now.
		String category = null; // No categories yet.
		
		// Register this step plugin...
		//
		//Map<PluginClassType, String> classMap = new HashMap<PluginClassType, String>();
		
	    Map<Class<?>, String> classMap = new HashMap<Class<?>, String>();
	    classMap.put(GUIOption.class, clazz.getName());
	    classMap.put(LifecycleListener.class, clazz.getName());
	    
	    PluginClassTypes extraTypes = clazz.getAnnotation(PluginClassTypes.class);
	    if(extraTypes != null){
	      for(int i=0; i< extraTypes.classTypes().length; i++){
	        classMap.put(extraTypes.classTypes()[i], extraTypes.implementationClass()[i].getName());
	      }
	    }
		
		PluginInterface stepPlugin = new Plugin(ids, this.getClass(), GUIOption.class, category, name, description, null, false, nativeRepositoryType, classMap, libraries, null, null);
		registry.registerPlugin(this.getClass(), stepPlugin);
	}

	/**
	 * Scan jar files in a set of plugin folders.  Open these jar files and scan for annotations if they are labeled for annotation scanning.
	 * 
	 * @throws KettlePluginException in case something goes horribly wrong
	 */
	protected void registerPluginJars() throws KettlePluginException {
		
		List<JarFileAnnotationPlugin> jarFilePlugins = findAnnotatedClassFiles(LifecyclePlugin.class.getName());
		for (JarFileAnnotationPlugin jarFilePlugin : jarFilePlugins) {
			
			KettleURLClassLoader urlClassLoader = (KettleURLClassLoader)createUrlClassLoader(jarFilePlugin.getJarFile(), getClass().getClassLoader());
			try {
				Class<?> clazz = urlClassLoader.loadClass(jarFilePlugin.getClassFile().getName());
				LifecyclePlugin lifecyclePlugin = clazz.getAnnotation(LifecyclePlugin.class);
				if (lifecyclePlugin!=null) {
					List<String> libraries = new ArrayList<String>();
					for (URL url : urlClassLoader.getURLs()) {
						libraries.add( URLDecoder.decode(url.getFile(), "UTF-8") );
					}
					handleLifecyclePluginAnnotation(clazz, lifecyclePlugin, libraries, false);
				}
			} catch(Exception e) {
				LogChannel.GENERAL.logError("Error registering plugin for jar file: "+jarFilePlugin.getJarFile(), e);
			}
		}
	}
	
	protected void registerXmlPlugins() throws KettlePluginException {
		// Not supported yet.
	}
	
}
