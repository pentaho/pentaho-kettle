package org.pentaho.di.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.row.RowBuffer;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.scannotation.AnnotationDB;

/**
 * This singleton provides access to all the plugins in the Kettle universe.<br>
 * It allows you to register types and plugins, query plugin lists per category, list plugins per type, etc.<br>
 * 
 * @author matt
 *
 */
public class PluginRegistry {
	private static Class<?> PKG = PluginRegistry.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private static PluginRegistry pluginRegistry;
	
	private Map<Class<? extends PluginTypeInterface>, List<PluginInterface>> pluginMap;
	
	private Map<String, URLClassLoader> folderBasedClassLoaderMap = new HashMap<String, URLClassLoader>();
	private Map<Class<? extends PluginTypeInterface>, Map<PluginInterface, URLClassLoader>> classLoaderMap;
	
	private Map<Class<? extends PluginTypeInterface>, List<String>> categoryMap;
	
	private static AnnotationDB annotationDB;

	private static ClassPathFinder	classPathFinder;

  private static List<PluginTypeInterface> pluginTypes = new ArrayList<PluginTypeInterface>();
  
	/**
	 * Initialize the registry, keep private to keep this a singleton 
	 */
	private PluginRegistry() {
		pluginMap = new HashMap<Class<? extends PluginTypeInterface>, List<PluginInterface>>();
		classLoaderMap = new HashMap<Class<? extends PluginTypeInterface>, Map<PluginInterface,URLClassLoader>>();
		categoryMap = new HashMap<Class<? extends PluginTypeInterface>, List<String>>();
	}
	
	/**
	 * @return The one and only PluginRegistry instance
	 */
	public static PluginRegistry getInstance() {
		if (pluginRegistry==null) {
			pluginRegistry=new PluginRegistry();
		}
		return pluginRegistry;
	}
	
	public static AnnotationDB getAnnotationDB() {
		return annotationDB;
	}
	
	public void registerPluginType(Class<? extends PluginTypeInterface> pluginType) {
		pluginMap.put(pluginType, new ArrayList<PluginInterface>());
		
		// Keep track of the categories separately for performance reasons...
		//
		if (categoryMap.get(pluginType)==null) {
			List<String> categories = new ArrayList<String>();
			categoryMap.put(pluginType, categories);
		}

	}
	
	public void registerPlugin(Class<? extends PluginTypeInterface> pluginType, PluginInterface plugin) throws KettlePluginException {
		
		if (plugin.getIds()[0]==null) {
			throw new KettlePluginException("Not a valid id specified in plugin :"+plugin);
		}
		
		if (plugin.getName().startsWith("i18n:")) {
			System.out.println("i18n untranslated key detected: "+plugin.getName());
		}

		if (plugin.getName().startsWith("!") && plugin.getName().endsWith("!")) {
			System.out.println("i18n untranslated key detected: "+plugin.getName());
		}

		List<PluginInterface> list = pluginMap.get(pluginType);
		if (list==null) {
			list = new ArrayList<PluginInterface>();
			pluginMap.put(pluginType, list);
//			classLoaderMap.put(pluginType, new HashMap<PluginInterface, URLClassLoader>());
		}
		
		int index = list.indexOf(plugin);
		if (index<0) {
			list.add(plugin);
		} else {
			list.set(index, plugin); // replace with the new one
		}		
		
		// Keep the list of plugins sorted by name...
		//
		Collections.sort(list, new Comparator<PluginInterface>() {
			public int compare(PluginInterface p1, PluginInterface p2) {
				return p1.getName().compareToIgnoreCase(p2.getName()); 
			}
		});
		
		if (!Const.isEmpty(plugin.getCategory())) {
			List<String> categories = categoryMap.get(pluginType);
			if (!categories.contains(plugin.getCategory())) {
				categories.add(plugin.getCategory());
				
				// Keep it sorted in the natural order here too!
				//
				// Sort the categories in the correct order.
				//
				String[] naturalOrder = null;
				

				PluginTypeCategoriesOrder naturalOrderAnnotation = pluginType.getAnnotation(PluginTypeCategoriesOrder.class);
				if(naturalOrderAnnotation != null){
				  String[] naturalOrderKeys = naturalOrderAnnotation.getNaturalCategoriesOrder();
				  Class<?> i18nClass = naturalOrderAnnotation.i18nPackageClass();
				  naturalOrder = new String[naturalOrderKeys.length];
				  for(int i=0; i< naturalOrderKeys.length; i++){
				    naturalOrder[i] = BaseMessages.getString(i18nClass, naturalOrderKeys[i]);
				  }
				}
				if (naturalOrder!=null) {
				  final String[] fNaturalOrder = naturalOrder;
					Collections.sort(categories, new Comparator<String>() {
						
						public int compare(String one, String two) {
							int idx1 = Const.indexOfString(one, fNaturalOrder);
							int idx2 = Const.indexOfString(two, fNaturalOrder);
							return idx1 - idx2;
						}
					});
				}				
			}
		}
	}
	
	/**
	 * @return An unmodifiable list of plugin types
	 */
	public List<Class<? extends PluginTypeInterface>> getPluginTypes() {
		return Collections.unmodifiableList(new ArrayList<Class<? extends PluginTypeInterface>>(pluginMap.keySet()));
	}	
	
	/**
	 * @param type The plugin type to query
	 * @return The list of plugins
	 */
	@SuppressWarnings("unchecked")
  public <T extends PluginInterface, K extends PluginTypeInterface> List<T> getPlugins(Class<K> type) {
	  List<T> list = new ArrayList<T>();
	  List<PluginInterface> mapList = pluginMap.get(type);
	  if(mapList != null){
  	  for(PluginInterface p : mapList){
  	    list.add((T) p);
  	  }
	  }
	  return list;
//		return pluginMap.get(type);
	}

	/**
	 * Get a plugin from the registry
	 * 
	 * @param stepplugintype The type of plugin to look for
	 * @param id The ID to scan for
	 * 
	 * @return the plugin or null if nothing was found.
	 */
	public PluginInterface getPlugin(Class<? extends PluginTypeInterface> pluginType, String id) {
		List<PluginInterface> plugins = getPlugins(pluginType);
		if (plugins==null) {
			return null;
		}
		
		for (PluginInterface plugin : plugins) {
			if (plugin.matches(id)) {
				return plugin;
			}
		}
		
		return null;
	}

	/**
	 * Retrieve a list of plugins per category.
	 * 
	 * @param pluginType The type of plugins to search
	 * @param pluginCategory The category to look in
	 * 
	 * @return An unmodifiable list of plugins that belong to the specified type and category.
	 */
	public <T extends PluginTypeInterface> List<PluginInterface> getPluginsByCategory(Class<T> pluginType, String pluginCategory) {
		List<PluginInterface> plugins = new ArrayList<PluginInterface>();
		
		for (PluginInterface verify : getPlugins(pluginType)) {
			if (verify.getCategory()!=null && verify.getCategory().equals(pluginCategory)) {
				plugins.add(verify);
			}
		}
		
		// Also sort
		return Collections.unmodifiableList(plugins);
	}

	/**
	 * Retrieve a list of all categories for a certain plugin type.
	 * @param pluginType The plugin type to search categories for.
	 * @return The list of categories for this plugin type.  The list can be modified (sorted etc) but will not impact the registry in any way.
	 */
	public List<String> getCategories(Class<? extends PluginTypeInterface> pluginType) {
		List<String> categories = categoryMap.get(pluginType);
		return categories;
	}

	/**
	 * Load and instantiate the main class of the plugin specified.
	 * 
	 * @param plugin The plugin to load the main class for.
	 * @return The instantiated class
	 * @throws KettlePluginException In case there was a loading problem.
	 */
	public Object loadClass(PluginInterface plugin) throws KettlePluginException {
	  return loadClass(plugin, plugin.getMainType());
	}
	
	/**
	 * Load the class of the type specified for the plugin that owns the class of the specified object.
	 *  
	 * @param pluginType the type of plugin
	 * @param object The object for which we want to search the class to find the plugin
	 * @param classType The type of class to load  
	 * @return the instantiated class.
	 * @throws KettlePluginException
	 */
	public <T> T loadClass(Class<? extends PluginTypeInterface> pluginType, Object object, Class<T> classType) throws KettlePluginException {
		PluginInterface plugin = getPlugin(pluginType, object);
		if (plugin==null) return null;
		return loadClass(plugin, classType);
	}

	/**
	 * Load the class of the type specified for the plugin with the ID specified.
	 *  
	 * @param pluginType the type of plugin
	 * @param plugiId The plugin id to use
	 * @param classType The type of class to load  
	 * @return the instantiated class.
	 * @throws KettlePluginException
	 */
	public <T> T loadClass(Class<? extends PluginTypeInterface> pluginType, String pluginId, Class<T> classType) throws KettlePluginException {
		PluginInterface plugin = getPlugin(pluginType, pluginId);
		if (plugin==null) return null;
		return loadClass(plugin, classType);
	}

	/**
	 * Load and instantiate the plugin class specified 
	 * @param plugin the plugin to load
	 * @param pluginClass the class to be loaded
	 * @return The instantiated class
	 * 
	 * @throws KettlePluginException In case there was a class loading problem somehow
	 */
	@SuppressWarnings("unchecked")
    public <T> T loadClass(PluginInterface plugin, Class<T> pluginClass) throws KettlePluginException {
        if (plugin == null)
        {
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.NoValidStepOrPlugin.PLUGINREGISTRY001")); //$NON-NLS-1$
        }

    	String className = plugin.getClassMap().get(pluginClass);
    	if (className==null) {
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.NoValidClassRequested.PLUGINREGISTRY002", pluginClass.getName())); //$NON-NLS-1$
    	}
    	
        try
        {
        	Class<? extends T> cl = null;
            if (plugin.isNativePlugin()) {
                cl = (Class<? extends T>) Class.forName(className);
            } else {
                List<String> jarfiles = plugin.getLibraries();
                URL urls[] = new URL[jarfiles.size()];
                for (int i = 0; i < jarfiles.size(); i++)
                {
                    File jarfile = new File(jarfiles.get(i));
                    urls[i] = new URL(URLDecoder.decode(jarfile.toURI().toURL().toString(), "UTF-8"));
                }

                // Load the class!!
                // 
                // First get the class loader: get the one that's the webstart classloader, not the thread classloader
                //
                ClassLoader classLoader = getClass().getClassLoader(); 
                
                URLClassLoader ucl = null;
                
                // If the plugin needs to have a separate class loader for each instance of the plugin.
                // This is not the default.  By default we cache the class loader for each plugin ID.
                //
                if (plugin.isSeparateClassLoaderNeeded()) {
                	
                	// Create a new one each time
                	//
                    ucl = new KettleURLClassLoader(urls, classLoader, plugin.getDescription());
                    
                } else {
                	
                    // See if we can find a class loader to re-use.
                	//
                  
                	Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get(plugin.getPluginType());
                	if (classLoaders==null) {
                		classLoaders=new HashMap<PluginInterface, URLClassLoader>();
                		classLoaderMap.put(plugin.getPluginType(), classLoaders);
                	} else {
                		ucl = classLoaders.get(plugin);
                	}
                  if (ucl==null)
                  {

                    if(plugin.getPluginDirectory() != null){
                      ucl = folderBasedClassLoaderMap.get(plugin.getPluginDirectory().toString());
                      if(ucl == null){
                        ucl = new KettleURLClassLoader(urls, classLoader, plugin.getDescription());
                        classLoaders.put(plugin, ucl); // save for later use...
                        folderBasedClassLoaderMap.put(plugin.getPluginDirectory().toString(), ucl);
                      }
                    }
                    
                  }
                }
              
                // Load the class.
                cl = (Class<? extends T>) ucl.loadClass(className);
            }

            return cl.newInstance();
        }
        catch (ClassNotFoundException e)
        {
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.ClassNotFound.PLUGINREGISTRY003"), e); //$NON-NLS-1$
        }
        catch (InstantiationException e)
        {
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.UnableToInstantiateClass.PLUGINREGISTRY004"), e); //$NON-NLS-1$
        }
        catch (IllegalAccessException e)
        {
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.IllegalAccessToClass.PLUGINREGISTRY005"), e); //$NON-NLS-1$
        }
        catch (MalformedURLException e)
        {
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.MalformedURL.PLUGINREGISTRY006"), e); //$NON-NLS-1$
        }
        catch (Throwable e)
        {
        	e.printStackTrace();
            throw new KettlePluginException(BaseMessages.getString(PKG, "PluginRegistry.RuntimeError.UnExpectedErrorLoadingClass.PLUGINREGISTRY007"), e); //$NON-NLS-1$
        }
	}
	
	
	/**
	 * Add a PluginType to be managed by the registry
	 * @param type
	 */
	public static void addPluginType(PluginTypeInterface type){
	  pluginTypes.add(type);
	}
	
	/**
	 * This method registers plugin types and loads their respective plugins 
	 * 
	 * @throws KettlePluginException
	 */
	public static void init() throws KettlePluginException {
		PluginRegistry registry = getInstance();
		
		try {
			annotationDB = new AnnotationDB();
			URLClassLoader urlClassLoader = ((URLClassLoader) registry.getClass().getClassLoader());
			URL[] tempurls = urlClassLoader.getURLs();
			
			URL[] urls = new URL[tempurls.length];
			for(int i=0; i< tempurls.length; i++){
			  urls[i] = new URL(URLDecoder.decode(tempurls[i].toString(), "UTF-8"));

			}
			
			LogChannel.GENERAL.logDetailed("Found "+urls.length+" objects in the classpath.");
			long startScan = System.currentTimeMillis();
			annotationDB.scanArchives(urls);
			LogChannel.GENERAL.logDetailed("Finished annotation scan in "+(System.currentTimeMillis()-startScan)+"ms.");
			
			classPathFinder = new ClassPathFinder(urlClassLoader);
		} catch(IOException e) {
			throw new KettlePluginException("Unable to scan for annotations in the classpath", e);
		}
		
		for (PluginTypeInterface pluginType : pluginTypes) {
			// Register the plugin type 
			//
			registry.registerPluginType(pluginType.getClass());
			
			// Search plugins for this type...
			//
			long startScan = System.currentTimeMillis();
			pluginType.searchPlugins();
			
			LogChannel.GENERAL.logDetailed("Registered "+registry.getPlugins(pluginType.getClass()).size()+" plugins of type '"+pluginType.getName()+"' in "+(System.currentTimeMillis()-startScan)+"ms.");
		}
	}

	/**
	 * Find the plugin ID based on the class
	 * @param pluginClass
	 * @return The ID of the plugin to which this class belongs (checks the plugin class maps)
	 */
	public String getPluginId(Object pluginClass) {
		for (Class<? extends PluginTypeInterface> pluginType : getPluginTypes()) {
			String id = getPluginId(pluginType, pluginClass);
			if (id!=null) {
				return id;
			}
		}
		return null;
	}
	
	/**
	 * Find the plugin ID based on the class
	 * 
	 * @param pluginType the type of plugin
	 * @param pluginClass The class to look for
	 * @return The ID of the plugin to which this class belongs (checks the plugin class maps) or null if nothing was found.
	 */
	public String getPluginId(Class<? extends PluginTypeInterface> pluginType, Object pluginClass) {
		String className = pluginClass.getClass().getName();
		for (PluginInterface plugin : getPlugins(pluginType)) {
			for (String check : plugin.getClassMap().values()) {
				if (check != null && check.equals(className)) {
					return plugin.getIds()[0];
				}
			}
		}
		return null;
	}
	
	/**
	 * Retrieve the Plugin for a given class 
	 * @param pluginType The type of plugin to search for
	 * @param pluginClass The class of this object is used to look around
	 * @return the plugin or null if nothing could be found
	 */
	public PluginInterface getPlugin(Class<? extends PluginTypeInterface> pluginType, Object pluginClass) {
		String pluginId = getPluginId(pluginType, pluginClass);
		if (pluginId==null) {
			return null;
		}
		return getPlugin(pluginType, pluginId);
	}
	
	/**
	 * Find the plugin ID based on the name of the plugin
	 * 
	 * @param pluginType the type of plugin
	 * @param pluginName The name to look for
	 * @return The plugin with the specified name or null if nothing was found.
	 */
	public PluginInterface findPluginWithName(Class<? extends PluginTypeInterface> pluginType, String pluginName) {
		for (PluginInterface plugin : getPlugins(pluginType)) {

			if (plugin.getName().equals(pluginName)) {
				return plugin;
			}
		}
		return null;
	}

	/**
	 * Find the plugin ID based on the description of the plugin
	 * 
	 * @param pluginType the type of plugin
	 * @param pluginDescription The description to look for
	 * @return The plugin with the specified description or null if nothing was found.
	 */
	public PluginInterface findPluginWithDescription(Class<? extends PluginTypeInterface> pluginType, String pluginDescription) {
		for (PluginInterface plugin : getPlugins(pluginType)) {

			if (plugin.getDescription().equals(pluginDescription)) {
				return plugin;
			}
		}
		return null;
	}

	/**
	 * Find the plugin ID based on the name of the plugin
	 * 
	 * @param pluginType the type of plugin
	 * @param pluginName The name to look for
	 * @return The plugin with the specified name or null if nothing was found.
	 */
	public PluginInterface findPluginWithId(Class<? extends PluginTypeInterface> pluginType, String pluginId) {
		for (PluginInterface plugin : getPlugins(pluginType)) {
			if (plugin.matches(pluginId)) {
				return plugin;
			}
		}
		return null;
	}
	
	/**
	 * @return a unique list of all the step plugin package names
	 */
	public List<String> getPluginPackages(Class<? extends PluginTypeInterface> pluginType) 
	{
		List<String> list = new ArrayList<String>();
		for (PluginInterface plugin : getPlugins(pluginType))
		{
			for (String className : plugin.getClassMap().values()) {
				if (className==null) {
					System.out.println("SNAFU!!!");
				}
				int lastIndex = className.lastIndexOf(".");
				String packageName = className.substring(0, lastIndex); 
				if (!list.contains(packageName)) list.add(packageName);
			}
		}
		Collections.sort(list);
		return list;
	}
	
	private RowMetaInterface getPluginInformationRowMeta() {
    	RowMetaInterface row = new RowMeta();
    	
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.Type.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.ID.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.Name.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.Description.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.Libraries.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.ImageFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.ClassName.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "PluginRegistry.Information.Category.Label"), ValueMetaInterface.TYPE_STRING));

        return row;
	}

	/**
	 * @param the type of plugin to get information for
	 * @return a row buffer containing plugin information for the given plugin type
	 */
	public RowBuffer getPluginInformation(Class<? extends PluginTypeInterface> pluginType)
	{
		RowBuffer rowBuffer = new RowBuffer(getPluginInformationRowMeta());
		for (PluginInterface plugin : getPlugins(pluginType)) {
			
	    	Object[] row = new Object[getPluginInformationRowMeta().size()];
	    	int rowIndex=0;
	    	
	    	row[rowIndex++] = plugin.getPluginType().getName();
	    	row[rowIndex++] = plugin.getIds()[0];
	    	row[rowIndex++] = plugin.getName();
	    	row[rowIndex++] = plugin.getDescription();
	    	row[rowIndex++] = plugin.getLibraries().toString();
	    	row[rowIndex++] = plugin.getImageFile();
	    	row[rowIndex++] = plugin.getClassMap().values().toString();
	    	row[rowIndex++] = plugin.getCategory();

	        rowBuffer.getBuffer().add(row);			
		}
		return rowBuffer;
	}
	
	/**
	 * Load the class with a certain name using the class loader of certain plugin.
	 * 
	 * @param plugin The plugin for which we want to use the class loader
	 * @param className The name of the class to load
	 * @return the name of the class
	 * @throws KettlePluginException In case there is something wrong
	 */
	@SuppressWarnings("unchecked")
  public <T> T getClass(PluginInterface plugin, String className) throws KettlePluginException {
		try {
		  
			if (plugin.isNativePlugin()) {

				return (T) Class.forName(className);
			} else {
			  
			  
			  URLClassLoader ucl = null;
			   Map<PluginInterface, URLClassLoader> classLoaders = classLoaderMap.get(plugin.getPluginType());
               if (classLoaders==null) {
                   classLoaders=new HashMap<PluginInterface, URLClassLoader>();
                   classLoaderMap.put(plugin.getPluginType(), classLoaders);
               } else {
                   ucl = classLoaders.get(plugin);
               }
             if (ucl==null)
             {

               if(plugin.getPluginDirectory() != null){
                 ucl = folderBasedClassLoaderMap.get(plugin.getPluginDirectory().toString());

                 classLoaders.put(plugin, ucl); // save for later use...
                 
               }
               
             }
			  
             if (ucl==null) {
               throw new KettlePluginException("Unable to find class loader for plugin: "+plugin);
             }
             return (T) ucl.loadClass(className);
  			  
			}
		} catch (Exception e) {
			throw new KettlePluginException("Unexpected error loading class with name: "+className, e);
		}
	}

	/**
	 * Load the class with a certain name using the class loader of certain plugin.
	 * 
	 * @param plugin The plugin for which we want to use the class loader
	 * @param classType The type of class to load
	 * @return the name of the class
	 * @throws KettlePluginException In case there is something wrong
	 */
	@SuppressWarnings("unchecked")
  public <T> T getClass(PluginInterface plugin, T classType) throws KettlePluginException {
		String className = plugin.getClassMap().get(classType);
		return (T) getClass(plugin, className);
	}

	/**
	 * @return the classPathFinder
	 */
	public static ClassPathFinder getClassPathFinder() {
		return classPathFinder;
	}
}
