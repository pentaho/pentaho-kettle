 /**********************************************************************
 **                                                                   **
 **               This code belongs to the KETTLE project.            **
 **                                                                   **
 ** Kettle, from version 2.2 on, is released into the public domain   **
 ** under the Lesser GNU Public License (LGPL).                       **
 **                                                                   **
 ** For more details, please read the document LICENSE.txt, included  **
 ** in this project                                                   **
 **                                                                   **
 ** http://www.kettle.be                                              **
 ** info@kettle.be                                                    **
 **                                                                   **
 **********************************************************************/
 
package be.ibridge.kettle.job;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.job.entry.JobEntryInterface;
 
/**
 * Takes care of loading job-entries or job-entry plugins.
 * 
 * @since 9-may-2005
 * @author Matt
 *
 */
public class JobEntryLoader
{
	private static JobEntryLoader jobEntryLoader = null;
	
	private String                pluginDirectory[];
    
	private ArrayList             pluginList;

    private Hashtable             classLoaders;


	private JobEntryLoader(String plugin_directory[])
	{
		this.pluginDirectory = plugin_directory;
		pluginList   = new ArrayList();
        classLoaders = new Hashtable();
	}
	
	public static final JobEntryLoader getInstance(String pluginDirectory[])
	{
		if (jobEntryLoader!=null) return jobEntryLoader;
		
		jobEntryLoader = new JobEntryLoader(pluginDirectory);
		
		return jobEntryLoader;
	}
	
	public static final JobEntryLoader getInstance()
	{
		if (jobEntryLoader!=null) return jobEntryLoader;
		
		jobEntryLoader = new JobEntryLoader( new String[] { Const.PLUGIN_JOBENTRIES_DIRECTORY_PUBLIC, Const.PLUGIN_JOBENTRIES_DIRECTORY_PRIVATE } );
		
		return jobEntryLoader;
	}

	public boolean read()
	{
		if (readNatives())
		{
			return readPlugins();
		}
		return false;
	}

	public boolean readNatives()
    {
		for (int i=1;i< JobEntryInterface.type_desc.length;i++)
        {
			String id             = JobEntryInterface.type_desc[i];
			String long_desc  	  = JobEntryInterface.type_desc_long[i];
			String tooltip        = JobEntryInterface.type_tooltip_desc[i];
			String iconfile       = Const.IMAGE_DIRECTORY + JobEntryInterface.icon_filename[i];
			String classname      = JobEntryInterface.type_classname[i].getName();
			String directory      = null; // Not used
			String jarfiles[]     = null; // Not used
			JobPlugin sp = new JobPlugin(JobPlugin.TYPE_NATIVE, id, long_desc, tooltip, directory, jarfiles, iconfile, classname);
			pluginList.add(sp);
        }

		return true;
    }

    public boolean readPlugins()
    {
        for (int dirNr = 0;dirNr<pluginDirectory.length;dirNr++)
        {
            try
            {
                File f = new File(pluginDirectory[dirNr]);
                if (f.isDirectory() && f.exists())
                {
                    LogWriter log = LogWriter.getInstance();
                    log.logDetailed("JobEntryLoader", "Looking for plugins in directory: "+pluginDirectory[dirNr]);
    
                    String dirs[] = f.list();
                    for (int i = 0; i < dirs.length; i++)
                    {
                        String piDir = pluginDirectory[dirNr] + Const.FILE_SEPARATOR + dirs[i];
    
                        File pi = new File(piDir);
                        if (pi.isDirectory()) // Only consider directories here!
                        {
                            String pixml = pi.toString() + Const.FILE_SEPARATOR + "plugin.xml";
                            File fpixml = new File(pixml);
        
                            if (fpixml.canRead()) // Yep, files exists...
                            {
                                /*
                                 * Now read the xml file containing the jars etc.
                                 */
                                try
                                {
                                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                                    DocumentBuilder db = dbf.newDocumentBuilder();
                                    Document doc = db.parse(fpixml);
        
                                    // Read the details from the XML file:
                                    Node plugin = XMLHandler.getSubNode(doc, "plugin");
        
                                    String id = XMLHandler.getTagAttribute(plugin, "id");
                                    String description = XMLHandler.getTagAttribute(plugin, "description");
                                    String iconfile = XMLHandler.getTagAttribute(plugin, "iconfile");
                                    String tooltip = XMLHandler.getTagAttribute(plugin, "tooltip");
                                    String classname = XMLHandler.getTagAttribute(plugin, "classname");
        
                                    // String jarfile =
                                    // InfoHandler.getTagAttribute(plugin, "jarfile");
        
                                    // System.out.println("id="+id+",
                                    // iconfile="+iconfile+", classname="+classname);
        
                                    Node libsnode = XMLHandler.getSubNode(plugin, "libraries");
                                    //System.out.println("libsnode="+Const.CR+libsnode);
        
                                    int nrlibs = XMLHandler.countNodes(libsnode, "library");
                                    //System.out.println("nrlibs="+nrlibs);
        
                                    String jarfiles[] = new String[nrlibs];
                                    for (int j = 0; j < nrlibs; j++)
                                    {
                                        Node libnode = XMLHandler.getSubNodeByNr(libsnode, "library", j);
                                        String jarfile = XMLHandler.getTagAttribute(libnode, "name");
                                        jarfiles[j] = pi.toString() + Const.FILE_SEPARATOR + jarfile;
                                    }
                                    
                                    String iconFilename = pi.toString() + Const.FILE_SEPARATOR + iconfile;
        
                                    JobPlugin sp = new JobPlugin(JobPlugin.TYPE_PLUGIN, id, description, tooltip, dirs[i], jarfiles, iconFilename, classname);
                                    
                                    /*
                                     * If the job plugin is not yet in the list with the specified ID, just add it.
                                     * If the job plugin is already in the list, overwrite with the latest version.
                                     * Note that you can overwrite standard steps with plugins and standard plugins 
                                     * with user plugins in the .kettle directory.
                                     */
                                    if (findJobPluginWithID(id)==null)
                                    {
                                        pluginList.add(sp);
                                    }
                                    else
                                    {
                                        int idx = pluginList.indexOf(sp);
                                        pluginList.set(idx, sp);
                                        System.out.println("Replaced existing plugin with ID : "+id);
                                    }
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Error reading plugin XML file: " + e.toString());
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                System.out.println("Couldn't find directory ["+pluginDirectory[dirNr]+"]");
            }
        }
        return true;
    }
	
	public JobEntryInterface getJobEntryClass(String desc)
		throws KettleStepLoaderException
	{
		JobPlugin jp = findJobEntriesWithDescription(desc);
		return getJobEntryClass(jp);
	}
	
    public JobEntryInterface getJobEntryClass(JobPlugin sp) throws KettleStepLoaderException
    {
        if (sp != null)
        {
            try
            {
                Class cl = null;
                switch (sp.getType())
                {
                case JobPlugin.TYPE_NATIVE:
                {
                    cl = Class.forName(sp.getClassname());
                }
                    break;
                case JobPlugin.TYPE_PLUGIN:
                {
                    String jarfiles[] = sp.getJarfiles();
                    URL urls[] = new URL[jarfiles.length];
                    for (int i = 0; i < jarfiles.length; i++)
                    {
                        File jarfile = new File(jarfiles[i]);
                        urls[i] = jarfile.toURL();
                    }

                    // Load the class!!
                    // 
                    // First get the class loader: get the one that's the webstart classloader, not the thread classloader
                    //
                    ClassLoader classLoader = getClass().getClassLoader(); 
                    
                     // Construct a new URLClassLoader based on this one...
                    URLClassLoader ucl = (URLClassLoader) classLoaders.get(sp.getID()); 
                    if (ucl==null)
                    {
                        ucl = new URLClassLoader(urls, classLoader);
                        classLoaders.put(sp.getID(), ucl); // save for later use...
                    }
                  
                    // What's the protection domain of this class?
                    // ProtectionDomain protectionDomain = this.getClass().getProtectionDomain();
                    
                    // Load the class.
                    cl = ucl.loadClass(sp.getClassname());
                }
                    break;
                default:
                    throw new KettleStepLoaderException("Unknown plugin type : " + sp.getType());
                }

                return (JobEntryInterface) cl.newInstance();
            }
            catch (ClassNotFoundException e)
            {
                throw new KettleStepLoaderException("Class not found", e);
            }
            catch (InstantiationException e)
            {
                throw new KettleStepLoaderException("Unable to instantiate class", e);
            }
            catch (IllegalAccessException e)
            {
                throw new KettleStepLoaderException("Illegal access to class", e);
            }
            catch (MalformedURLException e)
            {
                throw new KettleStepLoaderException("Malformed URL", e);
            }
            catch (Throwable e)
            {
                throw new KettleStepLoaderException("Unexpected error loading class", e);
            }
        } 
        else
        {
            throw new KettleStepLoaderException("No valid step/plugin specified.");
        }
    }

	/**
	 * Count's the number of steps with a certain type.
	 * @param type One of StepPlugin.TYPE_NATIVE, StepPlugin.TYPE_PLUGIN, StepPlugin.TYPE_ALL
	 * @return The number of steps with a certain type.
	 */
	public int nrJobEntriesWithType(int type)
	{
		int nr = 0;
		for (int i=0;i<pluginList.size();i++)
		{
			JobPlugin sp = (JobPlugin)pluginList.get(i);
			if (sp.getType()==type || type==JobPlugin.TYPE_ALL) nr++;
		}
		return nr;
	}

	public JobPlugin getJobEntryWithType(int type, int index)
	{
		int nr = 0;
		for (int i=0;i<pluginList.size();i++)
		{
			JobPlugin sp = (JobPlugin)pluginList.get(i);
			if (sp.getType()==type || type==JobPlugin.TYPE_ALL) 
			{
				if (nr==index) return sp;
				nr++;
			}
		}
		return null;
	}
    
    /**
     * @param stepid
     * @return The StepPlugin for the step with the specified ID.
     *         Null is returned when the ID couldn't be found!
     */
    public JobPlugin findJobPluginWithID(String stepid)
    {
        for (int i = 0; i < pluginList.size(); i++)
        {
            JobPlugin sp = (JobPlugin) pluginList.get(i);
            if (sp.getID().equalsIgnoreCase(stepid)) return sp;
        }
        return null;
    }

	public JobPlugin[] getJobEntriesWithType(int type)
	{
		int nr = nrJobEntriesWithType(type);
		JobPlugin steps[] = new JobPlugin[nr];
		for (int i=0;i<steps.length;i++)
		{
			JobPlugin sp =getJobEntryWithType(type, i);
			// System.out.println("sp #"+i+" = "+sp.getID());
			steps[i] = sp;
		}
		return steps;
	}
	
	public JobPlugin findJobEntriesWithID(String stepid)
	{
		for (int i=0;i<pluginList.size();i++)
		{
			JobPlugin sp = (JobPlugin)pluginList.get(i);
			if (sp.getID().equalsIgnoreCase(stepid)) return sp;
		}
		return null;
	}

	public JobPlugin findJobEntriesWithDescription(String description)
	{
		for (int i=0;i<pluginList.size();i++)
		{
			JobPlugin sp = (JobPlugin)pluginList.get(i);
			if (sp.getDescription().equalsIgnoreCase(description)) return sp;
		}
		return null;
	}

	/**
	 * Determine the step's id based upon the StepMetaInterface we get...
	 * @param jei The StepMetaInterface
	 * @return the step's id or null if we couldn't find anything.
	 */
	public String getJobEntryID(JobEntryInterface jei)
	{
		for (int i=0;i<nrJobEntriesWithType(JobPlugin.TYPE_ALL);i++)
		{
			JobPlugin sp = getJobEntryWithType(JobPlugin.TYPE_ALL, i);
			if (sp.getClassname() == jei.getClass().getName()) return sp.getID();
		}
		return null;
	}
}
