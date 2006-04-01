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

package be.ibridge.kettle.trans;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.LogWriter;
import be.ibridge.kettle.core.XMLHandler;
import be.ibridge.kettle.core.exception.KettleStepLoaderException;
import be.ibridge.kettle.trans.step.BaseStep;
import be.ibridge.kettle.trans.step.StepMetaInterface;

/**
 * 
 * @since 6-okt-2004
 * @author Matt
 *  
 */
public class StepLoader
{
    private static StepLoader stepLoader = null;

    private String            pluginDirectory[];

    private ArrayList         pluginList;
    
    private Hashtable         classLoaders;

    private StepLoader(String pluginDirectory[])
    {
        this.pluginDirectory = pluginDirectory;
        pluginList   = new ArrayList();
        classLoaders = new Hashtable();
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

    public boolean read()
    {
        if (readNatives()) { return readPlugins(); }
        return false;
    }

    public boolean readNatives()
    {
        for (int i = 1; i < BaseStep.type_desc.length; i++)
        {
            String id = BaseStep.type_desc[i];
            String long_desc = BaseStep.type_long_desc[i];
            String tooltip = BaseStep.type_tooltip_desc[i];
            String iconfile = Const.IMAGE_DIRECTORY + BaseStep.image_filename[i];
            String classname = BaseStep.type_classname[i].getName(); // TOEVOEGEN!
            String directory = null; // Not used
            String jarfiles[] = null; // Not used
            String category = BaseStep.category[i];

            StepPlugin sp = new StepPlugin(StepPlugin.TYPE_NATIVE, id, long_desc, tooltip, directory, jarfiles, iconfile, classname, category, null);
            if (id.equalsIgnoreCase("ScriptValues")) sp.setSeparateClassloaderNeeded(true); 

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
		            log.logDetailed("StepLoader", "Looking for plugins in directory: "+pluginDirectory[dirNr]);
	
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
		                            String category = XMLHandler.getTagAttribute(plugin, "category");
		                            String classname = XMLHandler.getTagAttribute(plugin, "classname");
                                    String errorHelpfile = XMLHandler.getTagAttribute(plugin, "errorhelpfile");
		
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
                                    
		                            StepPlugin sp = new StepPlugin(StepPlugin.TYPE_PLUGIN, id, description, tooltip, dirs[i], jarfiles, iconFilename, classname, category, pi.getPath()+Const.FILE_SEPARATOR+errorHelpfile);
		                            
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

    public StepMetaInterface getStepClass(String desc) throws KettleStepLoaderException
    {
        StepPlugin sp = findStepPluginWithDescription(desc);
        if (sp!=null)
        {
            return getStepClass(sp);
        }
        else
        {
            throw new KettleStepLoaderException("Unable to load class for step/plugin with description ["+desc+"]."+Const.CR+"Check if the plugin is available in the plugins subdirectory of the Kettle distribution.");
        }
    }

    public StepMetaInterface getStepClass(StepPlugin sp) throws KettleStepLoaderException
    {
        if (sp != null)
        {
            try
            {
                Class cl = null;
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
                        urls[i] = jarfile.toURL();
                    }

                    // Load the class!!
                    // 
                    // First get the class loader: get the one that's the webstart classloader, not the thread classloader
                    //
                    ClassLoader classLoader = getClass().getClassLoader(); 
                    
                    URLClassLoader ucl = null;
                    if (sp.isSeparateClassloaderNeeded())
                    {
                        ucl = new URLClassLoader(urls, classLoader);
                    }
                    else
                    {
                         // Construct a new URLClassLoader based on this one...
                        ucl = (URLClassLoader) classLoaders.get(sp.getID()); 
                        if (ucl==null)
                        {
                            ucl = new URLClassLoader(urls, classLoader);
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
                    throw new KettleStepLoaderException("Unknown plugin type : " + sp.getType());
                }

                return (StepMetaInterface) cl.newInstance();
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
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getType() == type || type == StepPlugin.TYPE_ALL) nr++;
        }
        return nr;
    }

    public StepPlugin getStepWithType(int type, int position)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = (StepPlugin) pluginList.get(i);
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
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getID().equalsIgnoreCase(stepid)) return sp;
        }
        return null;
    }

    public StepPlugin findStepPluginWithDescription(String description)
    {
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getDescription().equalsIgnoreCase(description)) return sp;
        }
        return null;
    }

    /**
     * Get a unique list of categories. We can use this to display in trees etc.
     * 
     * @param type
     *            The type of step plugins for which we want to categories...
     * @return a unique list of categories
     */
    public String[] getCategories(int type)
    {
        Hashtable cat = new Hashtable();
        for (int i = 0; i < nrStepsWithType(type); i++)
        {
            StepPlugin sp = getStepWithType(type, i);
            if (sp != null)
            {
                cat.put(sp.getCategory(), sp.getCategory());
            }
        }
        Enumeration keys = cat.keys();
        String retval[] = new String[cat.size()];
        int i = 0;
        while (keys.hasMoreElements())
        {
            retval[i] = (String) keys.nextElement();
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
                int idx1 = Const.indexOfString(retval[b  ], BaseStep.category_order);
                int idx2 = Const.indexOfString(retval[b+1], BaseStep.category_order);
                
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
            StepPlugin sp = (StepPlugin) pluginList.get(i);
            if (sp.getCategory().equalsIgnoreCase(category)) nr++;
        }
        return nr;
    }

    public StepPlugin getStepWithCategory(String category, int position)
    {
        int nr = 0;
        for (int i = 0; i < pluginList.size(); i++)
        {
            StepPlugin sp = (StepPlugin) pluginList.get(i);
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
        for (int i = 0; i < nrStepsWithType(StepPlugin.TYPE_ALL); i++)
        {
            StepPlugin sp = getStepWithType(StepPlugin.TYPE_ALL, i);
            if (sp.getClassname() == sii.getClass().getName()) return sp.getID();
        }
        return null;
    }
}
