 /* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

package org.pentaho.di.trans;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.i18n.LoaderInputStreamProvider;
import org.pentaho.di.trans.step.BaseStep;
import org.pentaho.di.trans.step.StepCategory;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * 
 * @since 6-okt-2004
 * @author Matt
 *  
 */
public class StepLoader implements LoaderInputStreamProvider
{
    private static StepLoader             stepLoader = null;
    private String                        pluginDirectory[];
    private List<StepPlugin>              pluginList;
    private Map<String[], URLClassLoader> classLoaders;
    private static Map<String,Partitioner>	  partitionerMap;
    
    private static final int PLUGIN_TYPE_STEP = 1;
    private static final int PLUGIN_TYPE_PARTIONER = 2;

    private StepLoader(String pluginDirectory[])
    {
        this.pluginDirectory = pluginDirectory;
        pluginList           = new ArrayList<StepPlugin>();
        classLoaders         = new Hashtable<String[], URLClassLoader>();
        partitionerMap		 = new Hashtable<String,Partitioner>();
    }

    public static final StepLoader getInstance(String pluginDirectory[])
    {
        if (stepLoader != null) return stepLoader;

        stepLoader = new StepLoader(pluginDirectory);

        return stepLoader;
    }

    public static final StepLoader getInstance()
    {
        if (stepLoader != null) return stepLoader;

        stepLoader = new StepLoader(new String[] { Const.PLUGIN_STEPS_DIRECTORY_PUBLIC, Const.PLUGIN_STEPS_DIRECTORY_PRIVATE } );

        return stepLoader;
    }

    /**
     * Read & initialize all the steps and plugins
     * @throws KettleException In case a plug-in could not be loaded or something else went wrong in the process.
     * @return true in case all goes well.
     * @deprecated in favor of static method init() to flag the exception throwing in this method. (change of contract)
     */
    public boolean read() throws KettleException
    {
        readNatives(); 
        readPlugins();
        
        return true;
    }
    
    /**
     * Read & initialize all the steps and plugins
     * @param pluginDirectory the directories to read plugins from
     * @throws KettleException In case a plug-in could not be loaded or something else went wrong in the process.
     */
    public static final void init(String[] pluginDirectory) throws KettleException
    {
    	StepLoader loader = getInstance(pluginDirectory);
        loader.readNatives(); 
        loader.readPlugins();
    }
    
    /**
     * Read & initialize all the steps and plugins
     * @throws KettleException In case a plug-in could not be loaded or something else went wrong in the process.
     */
    public static final void init() throws KettleException
    {
    	init(new String[] { Const.PLUGIN_STEPS_DIRECTORY_PUBLIC, Const.PLUGIN_STEPS_DIRECTORY_PRIVATE });
    }

    public void readNatives()
    {
        for (int i = 0; i < BaseStep.steps.length; i++)
        {
            StepPluginMeta pluginMeta = BaseStep.steps[i];
            String id[] = pluginMeta.getId();
            String long_desc = pluginMeta.getLongDesc();
            String tooltip = pluginMeta.getTooltipDesc();
            String iconfile = pluginMeta.getImageFileName();;
            String classname = pluginMeta.getClassName().getName(); // TOEVOEGEN!
            String directory = null; // Not used
            String jarfiles[] = null; // Not used
            String category = pluginMeta.getCategory();

            StepPlugin sp = new StepPlugin(StepPlugin.TYPE_NATIVE, id, long_desc, tooltip, directory, jarfiles, iconfile, classname, category, null);
            if (sp.handles("ScriptValues")) sp.setSeparateClassloaderNeeded(true);  //$NON-NLS-1$

            pluginList.add(sp);
        }
    }

    protected void readPluginFromResource( InputStream docStream, String path, String dir, int type ) throws KettleException {
        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(docStream);

            // Read the details from the XML file:
            
            // see if we have multiple plugins defined
            Node plugins = XMLHandler.getSubNode(doc, "plugins"); //$NON-NLS-1$
            if( plugins != null ) {
            	Node plugin = plugins.getFirstChild();
            	while( plugin != null ) {
                	if( "plugin".equals(plugin.getNodeName() ) ) {
                		readPluginFromResource( plugin, path, dir, type, PLUGIN_TYPE_STEP );
                	}
                	if( "plugin-partitioner".equals( plugin.getNodeName() ) ) {
                		readPluginFromResource( plugin, path, dir, type, PLUGIN_TYPE_PARTIONER );
                	}
                	plugin = plugin.getNextSibling();
            	}
            }
            
            // see if we have multiple plugins defined
            Node plugin = XMLHandler.getSubNode(doc, "plugin"); //$NON-NLS-1$
            if( plugin != null ) {
            	if( "plugin".equals(plugin.getNodeName() ) ) {
            		readPluginFromResource( plugin, path, dir, type, PLUGIN_TYPE_STEP );
            	}
            	if( "plugin-partitioner".equals( plugin.getNodeName() ) ) {
            		readPluginFromResource( plugin, path, dir, type, PLUGIN_TYPE_PARTIONER );
            	}
            }

        }
        catch (Exception e)
        {
            throw new KettleException( Messages.getString("StepLoader.RuntimeError.UnableToReadPluginXML.TRANS0001"), e); //$NON-NLS-1$
        }
    }

    protected void readPluginFromResource( Node plugin, String path, String dir, int type, int pluginType ) throws KettleException {
        try
        {
            String id = XMLHandler.getTagAttribute(plugin, "id"); //$NON-NLS-1$
            String description = XMLHandler.getTagAttribute(plugin, "description"); //$NON-NLS-1$
            String iconfile = XMLHandler.getTagAttribute(plugin, "iconfile"); //$NON-NLS-1$
            String tooltip = XMLHandler.getTagAttribute(plugin, "tooltip"); //$NON-NLS-1$
            String category = XMLHandler.getTagAttribute(plugin, "category"); //$NON-NLS-1$
            String classname = XMLHandler.getTagAttribute(plugin, "classname"); //$NON-NLS-1$
            String errorHelpfile = XMLHandler.getTagAttribute(plugin, "errorhelpfile"); //$NON-NLS-1$

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
            
            // Localized categories
            //
            Node locCatsNode = XMLHandler.getSubNode(plugin, "localized_category");
            int nrLocCats = XMLHandler.countNodes(locCatsNode, "category");
            Map<String, String> localizedCategories = new Hashtable<String, String>();              
            for (int j=0 ; j < nrLocCats ; j++)
            {
                Node locCatNode = XMLHandler.getSubNodeByNr(locCatsNode, "category", j);
                String locale = XMLHandler.getTagAttribute(locCatNode, "locale");
                String locCat = XMLHandler.getNodeValue(locCatNode);
                
                if (!Const.isEmpty(locale) && !Const.isEmpty(locCat))
                {
                    localizedCategories.put(locale.toLowerCase(), locCat);
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

            // Localized tooltips
            //
            Node locTipsNode = XMLHandler.getSubNode(plugin, "localized_tooltip");
            int nrLocTips = XMLHandler.countNodes(locTipsNode, "tooltip");
            Map<String, String> localizedTooltips = new Hashtable<String, String>();              
            for (int j=0 ; j < nrLocTips; j++)
            {
                Node locTipNode = XMLHandler.getSubNodeByNr(locTipsNode, "tooltip", j);
                String locale = XMLHandler.getTagAttribute(locTipNode, "locale");
                String locTip = XMLHandler.getNodeValue(locTipNode);
                
                if (!Const.isEmpty(locale) && !Const.isEmpty(locTip))
                {
                    localizedTooltips.put(locale.toLowerCase(), locTip);
                }
            }
            
            String iconFilename = (path == null) ? iconfile : path + Const.FILE_SEPARATOR + iconfile;
            String errorHelpFileFull = errorHelpfile;
            if (!Const.isEmpty(errorHelpfile)) errorHelpFileFull = (path == null) ? errorHelpfile : path+Const.FILE_SEPARATOR+errorHelpfile;
            
            if( pluginType == PLUGIN_TYPE_STEP ) {
                StepPlugin sp = new StepPlugin(type, new String[] { id }, description, tooltip, dir, jarfiles, iconFilename, classname, category, errorHelpFileFull);
                
                // Add localized information too...
                sp.setLocalizedCategories(localizedCategories);
                sp.setLocalizedDescriptions(localizedDescriptions);
                sp.setLocalizedTooltips(localizedTooltips);
                
                
                /*
                 * If the step plugin is not yet in the list with the specified ID, just add it.
                 * If the step plugin is already in the list, overwrite with the latest version.
                 * Note that you can overwrite standard steps with plugins and standard plugins 
                 * with user plugins in the .kettle directory.
                 */
                if (findStepPluginWithID(id)==null)
                {
                    pluginList.add(sp);
                }
                else
                {
                    int idx = pluginList.indexOf(sp);
                    pluginList.set(idx, sp);
                    // System.out.println(Messages.getString("StepLoader.Log.ReplaceExistingPlugid")+id); //$NON-NLS-1$
                }
            }
            else if( pluginType == PLUGIN_TYPE_PARTIONER ) 
            {
            	try 
            	{
                	Class<?> pClass = Class.forName( classname );
                	if( pClass != null )
                	{
                		Partitioner partitioner = (Partitioner) pClass.newInstance();
                		partitioner.setId( id );
                		partitioner.setDescription( description );
                		partitionerMap.put( id, partitioner );
                	}
                	
            	}
            	catch( Throwable e ) 
            	{
                    LogWriter.getInstance().logError("StepLoader", Messages.getString("StepLoader.RuntimeError.UnableToReadPluginXML.TRANS0001") + e.toString()); //$NON-NLS-1$
                    LogWriter.getInstance().logError("StepLoader", Const.getStackTracker( e )); //$NON-NLS-1$
            	}
            }
        }
        catch (Throwable e)
        {
            throw new KettleException( Messages.getString("StepLoader.RuntimeError.UnableToReadPluginXML.TRANS0001"), e); //$NON-NLS-1$
        }
    }

    public void readPlugins() throws KettleException
    {
    	LogWriter log = LogWriter.getInstance();
        
    	try {
    		// try reading plugins defined in JAR file META-INF/step_plugin.xml
    		InputStream content = getClass().getClassLoader().getResourceAsStream("META-INF/step_plugin.xml");
    		if (content != null) readPluginFromResource(content, null, null, StepPlugin.TYPE_NATIVE );
    	} catch (Exception e) {
    		throw new KettleException("Unable to load plugins specified in 'META-INF/step_plugin.xml' files", e);
    	}
    	
    	try {
    		// try reading plugins defined in JAR file META-INF/kettle-partition-plugins.xml
    		InputStream content = getClass().getClassLoader().getResourceAsStream("META-INF/kettle-partition-plugins.xml");
    		if (content != null) readPluginFromResource(content, null, null, StepPlugin.TYPE_NATIVE );
    		// also look in /kettle-partition-plugins.xml
    		content = getClass().getClassLoader().getResourceAsStream("kettle-partition-plugins.xml");
    		if (content != null) readPluginFromResource(content, null, null, StepPlugin.TYPE_NATIVE );
    	} catch (Exception e) {
    		throw new KettleException("Unable to load plugins specified in 'META-INF/kettle-partition-plugins.xml' files", e);
    	}
    	
    	//TODO: THis is what will move into the plugin loader class
        for (int dirNr = 0;dirNr<pluginDirectory.length;dirNr++)
        {
            try
            {
	            File f = new File(pluginDirectory[dirNr]);
		        if (f.isDirectory() && f.exists())
		        {
                    log.logDetailed(Messages.getString("StepLoader.Log.StepLoader.Title"), Messages.getString("StepLoader.Log.StepLoader.Description")+pluginDirectory[dirNr]); //$NON-NLS-1$ //$NON-NLS-2$
	
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
		                      FileInputStream fpixmlInputStream = new FileInputStream(fpixml);
		                      try {
		                        readPluginFromResource( fpixmlInputStream, pi.getPath(), dirs[i], StepPlugin.TYPE_PLUGIN );
		                      } finally {
		                        try {
		                          fpixmlInputStream.close();
		                        } catch (IOException ignored) {
		                          // Nothing to do on an exception during close
		                        }
		                      }
		                    }
		                    else
		                    {
		    		            log.logDetailed(Messages.getString("StepLoader.Log.StepLoader.Title"), "Plugin file ["+fpixml+"] is not readable."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		                    }
		                }
		                else
		                {
	    		            log.logDetailed(Messages.getString("StepLoader.Log.StepLoader.Title"), "Plugin directory ["+piDir+"] is not a directory."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		                }
		            }		           
		        }
		        else
		        {
		            log.logDebug(Messages.getString("StepLoader.Log.StepLoader.Title"), "Plugin directory not found, ignoring this: ["+pluginDirectory[dirNr]+"]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		        }
            }
            catch(Exception e)
            {
                throw new KettleException(Messages.getString("StepLoader.RuntimeError.CouldNotFindDirectory.TRANS0002",pluginDirectory[dirNr]), e); //$NON-NLS-1$
            }
        }
    }

    public StepMetaInterface getStepClass(String desc) throws KettleStepLoaderException
    {
        StepPlugin sp = findStepPluginWithDescription(desc);
        if (sp!=null)
        {
            return getStepClass(sp);
        }
        else
        {
            throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.UnableToLoadClass.TRANS0003",desc+"]."+Const.CR)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    public StepMetaInterface getStepClass(StepPlugin sp) throws KettleStepLoaderException
    {
        if (sp != null)
        {
            try
            {
                Class<?> cl = null;
                switch (sp.getType())
                {
                case StepPlugin.TYPE_NATIVE:
                {
                    cl = Class.forName(sp.getClassname());
                }
                    break;
                case StepPlugin.TYPE_PLUGIN:
                {
                    String jarfiles[] = sp.getJarfiles();
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
                    ClassLoader classLoader = getClass().getClassLoader(); 
                    
                    URLClassLoader ucl = null;
                    if (sp.isSeparateClassloaderNeeded())
                    {
                        ucl = new KettleURLClassLoader(urls, classLoader, sp.getDescription());
                    }
                    else
                    {
                         // Construct a new URLClassLoader based on this one...
                        ucl = classLoaders.get(sp.getID()); 
                        if (ucl==null)
                        {
                            ucl = new KettleURLClassLoader(urls, classLoader, sp.getDescription());
                            classLoaders.put(sp.getID(), ucl); // save for later use...
                        }
                    }
                  
                    // What's the protection domain of this class?
                    // ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
                    
                    // Load the class.
                    cl = ucl.loadClass(sp.getClassname());
                }
                    break;
                default:
                    throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.UnknownPluginType.TRANS0004") + sp.getType()); //$NON-NLS-1$
                }

                return (StepMetaInterface) cl.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.ClassNotFound.TRANS0005"), e); //$NON-NLS-1$
            }
            catch (InstantiationException e)
            {
                throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.UnableToInstantiateClass.TRANS0006"), e); //$NON-NLS-1$
            }
            catch (IllegalAccessException e)
            {
                throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.IllegalAccessToClass.TRANS0007"), e); //$NON-NLS-1$
            }
            catch (MalformedURLException e)
            {
                throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.MalformedURL.TRANS0008"), e); //$NON-NLS-1$
            }
            catch (Throwable e)
            {
                throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.UnExpectedErrorLoadingClass.TRANS0009"), e); //$NON-NLS-1$
            }
        } 
        else
        {
            throw new KettleStepLoaderException(Messages.getString("StepLoader.RuntimeError.NoValidStepOrPlugin.TRANS0010")); //$NON-NLS-1$
        }
    }

    /**
     * Count's the number of steps with a certain type.
     * 
     * @param type
     *            One of StepPlugin.TYPE_NATIVE, StepPlugin.TYPE_PLUGIN,
     *            StepPlugin.TYPE_ALL
     * @return The number of steps with a certain type.
     */
    public int nrStepsWithType(int type)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = pluginList.get(i);
            if (sp.getType() == type || type == StepPlugin.TYPE_ALL) nr++;
        }
        return nr;
    }

    public StepPlugin getStepWithType(int type, int position)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = pluginList.get(i);
            if (sp.getType() == type || type == StepPlugin.TYPE_ALL)
            {
                if (nr == position) return sp;
                nr++;
            }
        }
        return null;
    }

    public StepPlugin[] getStepsWithType(int type)
    {
        int nr = nrStepsWithType(type);
        StepPlugin steps[] = new StepPlugin[nr];
        for (int i = 0; i < steps.length; i++)
        {
            StepPlugin sp = getStepWithType(type, i);
            // System.out.println("sp #"+i+" = "+sp.getID());
            steps[i] = sp;
        }
        return steps;
    }

    /**
     * @param stepid
     * @return The StepPlugin for the step with the specified ID.
     *         Null is returned when the ID couldn't be found!
     */
    public StepPlugin findStepPluginWithID(String stepid)
    {
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = pluginList.get(i);
            if (sp.handles(stepid)) return sp;
        }
        return null;
    }

    public StepPlugin findStepPluginWithDescription(String description)
    {
        return findStepPluginWithDescription(description, LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
    }
    
    public StepPlugin findStepPluginWithDescription(String description, String locale)
    {
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = pluginList.get(i);
            if (sp.getDescription(locale).equalsIgnoreCase(description)) 
            {
                return sp;
            }
        }
        return null;
    }

    /**
     * Get a unique list of categories. We can use this to display in trees etc.
     * 
     * @param type The type of step plugins for which we want to categories...
     * @return a unique list of categories
     */
    public String[] getCategories(int type)
    {
        return getCategories(type, LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
    }
    
    /**
     * Get a unique list of categories. We can use this to display in trees etc.
     * 
     * @param type The type of step plugins for which we want to categories...
     * @return a unique list of categories
     */
    public String[] getCategories(int type, String locale)
    {
        Hashtable<String, String> cat = new Hashtable<String, String>();
        for (int i = 0; i < nrStepsWithType(type); i++)
        {
            StepPlugin sp = getStepWithType(type, i);
            if (sp != null)
            {
                cat.put(sp.getCategory(locale), sp.getCategory(locale));
            }
        }
        Enumeration<String> keys = cat.keys();
        String retval[] = new String[cat.size()];
        int i = 0;
        while (keys.hasMoreElements())
        {
            retval[i] = keys.nextElement();
            i++;
        }

        // Sort the resulting array...
        // It has to be sorted the same way as the String array BaseStep.category_order
        //
        for (int a = 0; a < retval.length; a++)
        {
            for (int b = 0; b < retval.length - 1; b++)
            {
                // What is the index of retval[b] and retval[b+1]?
                int idx1 = -1;
                for (int x=0;x<StepCategory.STANDARD_CATEGORIES.length;x++) {
                	StepCategory category = StepCategory.STANDARD_CATEGORIES[x];
                	if (category.getName().equalsIgnoreCase(retval[b])) {
                		idx1=x;
                		break;
                	}
                }
                int idx2 = -1;
                for (int x=0;x<StepCategory.STANDARD_CATEGORIES.length;x++) {
                	StepCategory category = StepCategory.STANDARD_CATEGORIES[x];
                	if (category.getName().equalsIgnoreCase(retval[b+1])) {
                		idx2=x;
                		break;
                	}
                }
                
                if (idx1>idx2)
                {
                    String dummy = retval[b];
                    retval[b] = retval[b + 1];
                    retval[b + 1] = dummy;
                }
            }
        }
        return retval;
    }

    public int nrStepsWithCategory(String category)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = pluginList.get(i);
            if (sp.getCategory().equalsIgnoreCase(category)) nr++;
        }
        return nr;
    }

    public StepPlugin getStepWithCategory(String category, int position)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = pluginList.get(i);
            if (sp.getCategory().equalsIgnoreCase(category))
            {
                if (nr == position) return sp;
                nr++;
            }
        }
        return null;
    }

    public StepPlugin[] getStepsWithCategory(String category)
    {
        int nr = nrStepsWithCategory(category);
        StepPlugin steps[] = new StepPlugin[nr];
        for (int i = 0; i < steps.length; i++)
        {
            StepPlugin sp = getStepWithCategory(category, i);
            steps[i] = sp;
        }
        return steps;
    }

    /**
     * Determine the step's id based upon the StepMetaInterface we get...
     * 
     * @param sii
     *            The StepMetaInterface
     * @return the step's id or null if we couldn't find anything.
     */
    public String getStepPluginID(StepMetaInterface sii)
    {
    	for (StepPlugin sp : pluginList) 
    	{
    		if (sp.getClassname().equals( sii.getClass().getName() )) {
    			return sp.getID()[0]; // return the first = default
    		}
    	}

        return null;
    }
    
    /**
     * Determine the step's plugin based upon the StepMetaInterface we get...
     * 
     * @param sii The StepMetaInterface
     * @return the step plugin or null if we couldn't find anything.
     */
    public StepPlugin getStepPlugin(StepMetaInterface sii)
    {
        for (StepPlugin sp : pluginList)
        {
            if (sp.getClassname().equals( sii.getClass().getName() )) {
            	return sp; // OK, we found the plugin
            }
        }
        return null;
    }

    
    /**
     * Search through all jarfiles in all steps and try to find a certain file in it.
     * 
     * @param filename
     * @return an inputstream for the given file.
     */
    public InputStream getInputStreamForFile(String filename)
    {
        StepPlugin[] stepPlugins = getStepsWithType(StepPlugin.TYPE_PLUGIN);
        for (int i = 0; i < stepPlugins.length; i++)
        {
            try
            {
                StepPlugin stepPlugin = stepPlugins[i];
                
                String[] jarfiles = stepPlugin.getJarfiles();
                if (jarfiles!=null)
                {
                    for (int j=0;j<jarfiles.length;j++)
                    {
                        JarFile jarFile = new JarFile(jarfiles[j]);
                        JarEntry jarEntry;
                        if (filename.startsWith("/"))
                        {
                            jarEntry = jarFile.getJarEntry(filename.substring(1));
                        }
                        else
                        {
                            jarEntry = jarFile.getJarEntry(filename);
                        }
                        if (jarEntry!=null)
                        {
                            InputStream inputStream = jarFile.getInputStream(jarEntry);
                            if (inputStream!=null) 
                            {
                                return inputStream;
                            }
                            jarFile.close();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                // Just look for the next one...
            }
        }
        return null;
    }

    /**
     * @return the pluginList
     */
    public List<StepPlugin> getPluginList()
    {
        return pluginList;
    }

	public static Map<String,Partitioner> getPartitionerList() {
		return partitionerMap;
	}

	public static Partitioner getPartitioner( String id ) {
		return partitionerMap.get( id );
	}
	
	public List<Object[]> getPluginInformation()
	{
		List<Object[]> list = new ArrayList<Object[]>();
		for (StepPlugin plugin : pluginList) {
			list.add(plugin.getPluginInformation());
		}
		return list;
	}

	/**
	 * @return a unique array of all the step plugin package names
	 */
	public String[] getPluginPackages() 
	{
		List<String> list = new ArrayList<String>();
		for (StepPlugin stepPlugin : pluginList)
		{
			String className = stepPlugin.getClassname();
			int lastIndex = className.lastIndexOf(".");
			String packageName = className.substring(0, lastIndex); 
			if (!list.contains(packageName)) list.add(packageName);
		}
		Collections.sort(list);
		return list.toArray(new String[list.size()]);
	}
}
