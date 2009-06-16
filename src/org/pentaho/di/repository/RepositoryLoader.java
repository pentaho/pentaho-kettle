package org.pentaho.di.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.exception.KettleException;

/**
 * Singleton class that will load the various repositories as a plugin.
 * 
 * @author matt
 *
 */
public class RepositoryLoader {
	private static RepositoryLoader loader;
	
	private List<RepositoryPluginMeta> pluginMetaList;
	// private List<RepositoryPlugin> pluginList;
	
	private RepositoryLoader() {
		pluginMetaList = new ArrayList<RepositoryPluginMeta>();
		// pluginList = new ArrayList<RepositoryPlugin>();
	}
	
	public static RepositoryLoader getInstance() {
		if (loader!=null) {
			return loader;
		}
		loader = new RepositoryLoader();
		return loader;
	}
	
	/**
	 * @return the pluginMetaList
	 */
	public List<RepositoryPluginMeta> getPluginMetaList() {
		return pluginMetaList;
	}
	
	public static void init() throws KettleException {
		RepositoryLoader repositoryLoader = getInstance();
		repositoryLoader.initRepositoriesPluginMeta();
	}
	
	   /**
     * Reads the step metadata, names, descriptions, IDs, icon filenames, etc.
     * @throws KettleException
     */
    private void initRepositoriesPluginMeta() throws KettleException
    {
    	synchronized(RepositoryLoader.getInstance())
    	{
    		try
	    	{
	    		// annotated classes first
	    		// ConfigManager<?> stepsAnntCfg = KettleConfig.getInstance().getManager("steps-annotation-config");
	    		// Collection<StepPluginMeta> mainSteps = stepsAnntCfg.loadAs(StepPluginMeta.class);
    			
	    		ConfigManager<?> repositoriesConfigManager = KettleConfig.getInstance().getManager("repositories-xml-config");
	    		Collection<RepositoryPluginMeta> repositories = repositoriesConfigManager.loadAs(RepositoryPluginMeta.class);
	    	
	    		pluginMetaList.addAll(repositories);
	    	}
	    	catch(KettleConfigException e)
	    	{
	    		throw new KettleException("There was an unexpected error while reading the step plugin metadata", e);
	    	}
    	}
    }
    
    public Repository createRepositoryObject(String id) throws KettleException {
    	
    	try {
    		RepositoryPluginMeta meta = findPluginMeta(id);
    		if (meta==null) {
    			throw new Exception("Unable to find repository plugin meta with type ["+id+"]");
    		}
    		Class<?> repositoryClass = Class.forName( meta.getClassName() );
    		Object object = repositoryClass.newInstance();
    		if (!(object instanceof Repository)) {
    			throw new Exception("Repository plugin class name ["+meta.getClassName()+"] does not implement the Repository interface");
    		}
    		return (Repository)object;
    	}
    	catch(Exception e) {
    		throw new KettleException("Unable to create a repository object for repository type id ["+id+"]", e);
    	}
    }
    
    public static Repository createRepository(RepositoryMeta repositoryMeta, UserInfo userInfo) throws KettleException {
    	RepositoryLoader loader = RepositoryLoader.getInstance();
    	Repository repository = loader.createRepositoryObject(repositoryMeta.getId());
    	repository.init(repositoryMeta, userInfo);
    	return repository;
    }

    public static RepositoryMeta createRepositoryMeta(String id) throws KettleException {
    	try {
	    	RepositoryLoader loader = RepositoryLoader.getInstance();
	    	RepositoryPluginMeta pluginMeta = loader.findPluginMeta(id);
	    	Class<?> clazz = Class.forName(pluginMeta.getMetaClassName());
	    	RepositoryMeta repositoryMeta = (RepositoryMeta) clazz.newInstance();
	    	repositoryMeta.setId(id);
	    	
	    	return repositoryMeta;
    	} catch(Exception e) {
    		throw new KettleException("Unable to create Repository metadata object with ID ["+id+"]", e); 
    	}
    }

	public RepositoryPluginMeta findPluginMeta(String id) {
		for (RepositoryPluginMeta meta : pluginMetaList) {
			if (meta.getId().equalsIgnoreCase(id)) {
				return meta;
			}
		}
		return null;
	}
}
