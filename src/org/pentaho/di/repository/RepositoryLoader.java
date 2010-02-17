package org.pentaho.di.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleRepositoryNotSupportedException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.KettleURLClassLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Singleton class that will load the various repositories as a plugin.
 * 
 * @author matt
 *
 */
public class RepositoryLoader {
	private static Class<?> PKG = RepositoryLoader.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	private static RepositoryLoader loader;
	private List<RepositoryPluginMeta> pluginMetaList;
	private String pluginDirectory[];
	
    private Map<String, ClassLoader> classLoaders;
	private LogChannelInterface	log;

	
	private RepositoryLoader() {
		pluginMetaList = new ArrayList<RepositoryPluginMeta>();
		classLoaders = new HashMap<String, ClassLoader>();
		
		pluginDirectory = new String[] { Const.PLUGIN_REPOSITORIES_DIRECTORY_PUBLIC, Const.PLUGIN_REPOSITORIES_DIRECTORY_PRIVATE, };
		
		this.log = new LogChannel("RepositoryLoader");
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
		repositoryLoader.readPlugins();
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
	    		ConfigManager<?> repositoriesAnntCfg = KettleConfig.getInstance().getManager("repositories-annotation-config");
	    		Collection<RepositoryPluginMeta> mainRepositories = repositoriesAnntCfg.loadAs(RepositoryPluginMeta.class);
    			
	    		pluginMetaList.addAll(mainRepositories);
	    		
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
    		
    		ClassLoader classLoader = getClassLoader(meta);
    		Class<?> repositoryClass = classLoader.loadClass( meta.getClassName() );
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
    
    public ClassLoader getClassLoader(RepositoryPluginMeta meta) throws MalformedURLException {

    	// Only create the class loader once...
    	//
    	ClassLoader cl = classLoaders.get(meta.getId()); 
        if (cl!=null)
        {
        	return cl;
        }

		if (meta.getJarFiles()==null) {
			cl = meta.getClass().getClassLoader();
		} else {
			String jarfiles[] = meta.getJarFiles();
            URL urls[] = new URL[jarfiles.length];
            for (int i = 0; i < jarfiles.length; i++)
            {
                File jarfile = new File(jarfiles[i]);
                urls[i] = jarfile.toURI().toURL();
            }

            // Load the class!!
            // 
            // First get the class loader: get the one that's the webstart classloader, not the thread classloader
            //
            ClassLoader classLoader = meta.getClass().getClassLoader(); 
             
            cl = new KettleURLClassLoader(urls, classLoader, meta.getDescription());
        }

        classLoaders.put(meta.getId(), cl); // save for later use...

		return cl;
	}

	public static Repository createRepository(RepositoryMeta repositoryMeta) throws KettleException {
    	RepositoryLoader loader = RepositoryLoader.getInstance();
    	Repository repository = loader.createRepositoryObject(repositoryMeta.getId());
    	repository.init(repositoryMeta);
    	return repository;
    }

    public static RepositoryMeta createRepositoryMeta(String id) throws KettleException {
    	try {
	    	RepositoryLoader loader = RepositoryLoader.getInstance();
	    	RepositoryPluginMeta pluginMeta = loader.findPluginMeta(id);
	    	if (pluginMeta==null) {
	    		throw new KettleRepositoryNotSupportedException("Unable to find repository plugin for id ["+id+"]");
	    	}
    		ClassLoader classLoader = getInstance().getClassLoader(pluginMeta);
    		Class<?> clazz = classLoader.loadClass(pluginMeta.getMetaClassName());
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
	
    public void readPlugins() throws KettleException
    {
    	try {
    		// try reading plugins defined in JAR file META-INF/step_plugin.xml
    		InputStream content = getClass().getClassLoader().getResourceAsStream("META-INF/repository_plugin.xml");
    		if (content != null) readPluginFromResource(content, null, null);
    	} catch (Exception e) {
    		throw new KettleException("Unable to load plugins specified in 'META-INF/repository_plugin.xml' files", e);
    	}
    	

        for (int dirNr = 0;dirNr<pluginDirectory.length;dirNr++)
        {
            try
            {
	            File f = new File(pluginDirectory[dirNr]);
		        if (f.isDirectory() && f.exists())
		        {
                    log.logDetailed(BaseMessages.getString(PKG, "RepositoryLoader.Log.RepositoryLoader.Title"), BaseMessages.getString(PKG, "RepositoryLoader.Log.RepositoryLoader.Description")+pluginDirectory[dirNr]); //$NON-NLS-1$ //$NON-NLS-2$
	
		            String dirs[] = f.list();
		            for (int i = 0; i < dirs.length; i++)
		            {
		                String piDir = pluginDirectory[dirNr] + Const.FILE_SEPARATOR + dirs[i];
	
		                File pi = new File(piDir);
		                if (pi.isDirectory()) // Only consider directories here!
		                {
	                        String pixml = pi.toString() + Const.FILE_SEPARATOR + "plugin.xml"; //$NON-NLS-1$
		                    File fpixml = new File(pixml);
		
		                    if (fpixml.canRead()) // Yep, files exists...
		                    {
		                        /*
		                         * Now read the xml file containing the jars etc.
		                         */

		            			readPluginFromResource( new FileInputStream(fpixml), pi.getPath(), dirs[i] );
		                    }
		                    else
		                    {
		    		            log.logDetailed(BaseMessages.getString(PKG, "RepositoryLoader.Log.RepositoryLoader.Title"), "Plugin file ["+fpixml+"] is not readable."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		                    }
		                }
		                else
		                {
	    		            log.logDetailed(BaseMessages.getString(PKG, "RepositoryLoader.Log.RepositoryLoader.Title"), "Plugin directory ["+piDir+"] is not a directory."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		                }
		            }		           
		        }
		        else
		        {
		            log.logDebug(BaseMessages.getString(PKG, "RepositoryLoader.Log.RepositoryLoader.Title"), "Plugin directory not found, ignoring this: ["+pluginDirectory[dirNr]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        }
            }
            catch(Exception e)
            {
                throw new KettleException(BaseMessages.getString(PKG, "RepositoryLoader.RuntimeError.CouldNotFindDirectory.TRANS0002",pluginDirectory[dirNr]), e); //$NON-NLS-1$
            }
        }
    }
    
    protected void readPluginFromResource( InputStream docStream, String path, String dir ) throws KettleException {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(docStream);

            // Read the details from the XML file:
            
            // see if we have multiple plugins defined
            //
            Node plugins = XMLHandler.getSubNode(doc, "plugins"); //$NON-NLS-1$
            if( plugins != null ) {
            	Node plugin = plugins.getFirstChild();
            	while( plugin != null ) {
                	readPluginFromResource( plugin, path, dir );
                	plugin = plugin.getNextSibling();
            	}
            }
            
            // backward compatible, a single plugin in the root doc
            //
            Node plugin = XMLHandler.getSubNode(doc, "plugin"); //$NON-NLS-1$
            if( plugin != null ) {
        		readPluginFromResource( plugin, path, dir);
            }

        }
        catch (Exception e)
        {
            throw new KettleException( BaseMessages.getString(PKG, "RepositoryLoader.RuntimeError.UnableToReadPluginXML.TRANS0001"), e); //$NON-NLS-1$
        }
    }

    protected void readPluginFromResource( Node plugin, String path, String dir ) throws KettleException {
        try
        {
            String id = XMLHandler.getTagAttribute(plugin, "id"); //$NON-NLS-1$
            String name = XMLHandler.getTagAttribute(plugin, "name"); //$NON-NLS-1$
            String description = XMLHandler.getTagAttribute(plugin, "description"); //$NON-NLS-1$
            String classname = XMLHandler.getTagAttribute(plugin, "classname"); //$NON-NLS-1$
            String metaClassname = XMLHandler.getTagAttribute(plugin, "metaclassname"); //$NON-NLS-1$
            String dialogClassname = XMLHandler.getTagAttribute(plugin, "dialogclassname"); //$NON-NLS-1$
            String versionBrowserClassName = XMLHandler.getTagAttribute(plugin, "versionbrowserclassname"); //$NON-NLS-1$
            
            Node libsnode = XMLHandler.getSubNode(plugin, "libraries"); //$NON-NLS-1$
            int nrlibs = XMLHandler.countNodes(libsnode, "library"); //$NON-NLS-1$

            String jarfiles[] = new String[nrlibs];
            if( path != null ) {
                for (int j = 0; j < nrlibs; j++)
                {
                    Node libnode = XMLHandler.getSubNodeByNr(libsnode, "library", j); //$NON-NLS-1$
                    String jarfile = XMLHandler.getTagAttribute(libnode, "name"); //$NON-NLS-1$
                    jarfiles[j] = path + Const.FILE_SEPARATOR + jarfile;
                }
            }
            
            // Localized names
            //
            Node locNamesNode = XMLHandler.getSubNode(plugin, "localized_name");
            int nrNameNodes = XMLHandler.countNodes(locNamesNode, "name");
            Map<String, String> localizedNames = new Hashtable<String, String>();              
            for (int j=0 ; j < nrNameNodes ; j++)
            {
                Node locNameNode = XMLHandler.getSubNodeByNr(locNamesNode, "name", j);
                String locale = XMLHandler.getTagAttribute(locNameNode, "locale");
                String locName = XMLHandler.getNodeValue(locNameNode);
                
                if (!Const.isEmpty(locale) && !Const.isEmpty(locName))
                {
                    localizedNames.put(locale.toLowerCase(), locName);
                }
            }

            // Localized descriptions
            //
            Node locDescsNode = XMLHandler.getSubNode(plugin, "localized_description");
            int nrLocDescs = XMLHandler.countNodes(locDescsNode, "description");
            Map<String, String> localizedDescriptions = new Hashtable<String, String>();              
            for (int j=0 ; j < nrLocDescs; j++)
            {
                Node locDescNode = XMLHandler.getSubNodeByNr(locDescsNode, "description", j);
                String locale = XMLHandler.getTagAttribute(locDescNode, "locale");
                String locDesc = XMLHandler.getNodeValue(locDescNode);
                
                if (!Const.isEmpty(locale) && !Const.isEmpty(locDesc))
                {
                    localizedDescriptions.put(locale.toLowerCase(), locDesc);
                }
            }
            
            RepositoryPluginMeta pluginMeta = new RepositoryPluginMeta(id, name, description, classname, metaClassname, dialogClassname, versionBrowserClassName, jarfiles, localizedNames, localizedDescriptions);

            /*
             * If the step plugin is not yet in the list with the specified ID, just add it.
             * If the step plugin is already in the list, overwrite with the latest version.
             * Note that you can overwrite standard steps with plugins and standard plugins 
             * with user plugins in the .kettle directory.
             */
            if (findPluginMeta(id)==null)
            {
                pluginMetaList.add(pluginMeta);
            }
            else
            {
                int idx = pluginMetaList.indexOf(pluginMeta);
                pluginMetaList.set(idx, pluginMeta);
            }
        }
        catch (Throwable e)
        {
            throw new KettleException( BaseMessages.getString(PKG, "RepositoryLoader.RuntimeError.UnableToReadPluginXML.TRANS0001"), e); //$NON-NLS-1$
        }
    }
}
