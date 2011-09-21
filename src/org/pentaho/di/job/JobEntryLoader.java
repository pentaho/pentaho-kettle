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

package org.pentaho.di.job;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.vfs.FileSystemException;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.PDIClassLoader;
import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.plugins.PluginLoader;
import org.pentaho.di.i18n.LanguageChoice;
import org.pentaho.di.i18n.LoaderInputStreamProvider;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

/**
 * Takes care of loading job-entries or job-entry plugins.
 * 
 * @since 9-may-2005
 * @author Matt	
 * 
 */
public class JobEntryLoader implements LoaderInputStreamProvider
{
	private static JobEntryLoader jobEntryLoader = null;

	private List<JobPlugin> pluginList;

	private Hashtable<String, URLClassLoader> classLoaders;

	private boolean initialized;

	private JobEntryLoader()
	{
		pluginList = new ArrayList<JobPlugin>();
		classLoaders = new Hashtable<String, URLClassLoader>();
		initialized = false;
	}

	public static final JobEntryLoader getInstance()
	{
		if (jobEntryLoader != null)
			return jobEntryLoader;

		jobEntryLoader = new JobEntryLoader();

		return jobEntryLoader;
	}

	/**
	 * Read all native and plug-in job entries
	 * @return true if all went well
	 * @throws KettleException in case an error occurs.
     * @deprecated in favor of static method init() to flag the exception throwing in this method. (change of contract)
	 */
	public boolean read() throws KettleException
	{
		readNatives();
		readPlugins();
		initialized = true;
		return true;
	}

	/**
	 * Read all native and plug-in job entries
     * @throws KettleException In case a plug-in could not be loaded or something else went wrong in the process.
     */
	public static final void init() throws KettleException 
	{
		JobEntryLoader loader = getInstance();
		loader.readNatives();
		loader.readPlugins();
		loader.initialized = true;
	}

	public boolean readNatives()
	{
		try
		{
			// annotated classes first
			ConfigManager<?> jobsAnntCfg = KettleConfig.getInstance().getManager("jobs-annotation-config");
			Collection<JobPluginMeta> jobs = jobsAnntCfg.loadAs(JobPluginMeta.class);
			ConfigManager<?> jobsCfg = KettleConfig.getInstance().getManager("jobs-xml-config");
			Collection<JobPluginMeta> cjobs = jobsCfg.loadAs(JobPluginMeta.class);

			jobs.addAll(cjobs);

			for (JobPluginMeta job : jobs) {
				if (job.getType() != JobEntryType.NONE) {
					pluginList.add(new JobPlugin(JobPlugin.TYPE_NATIVE, job.getId(), job.getType(), job.getTooltipDesc(), null, null, job.getImageFileName(), job.getClassName().getName(), job.getCategory()));
				} else {
					if (!Const.isEmpty(job.getName())) {
						// Annotated plugin...
						//
						pluginList.add(new JobPlugin(JobPlugin.TYPE_PLUGIN, job.getId(), job.getName(), job.getTooltipDesc(), null, null, job.getImageFileName(), job.getClassName().getName(), job.getCategory()));
					}
				}
			}
		} catch (KettleConfigException e)
		{
			e.printStackTrace();
			return false;
		}

		return true;

	}

	/**
	 * The 'new' method. Uses plugin loader, which uses VFS to load plugins.
	 */
	public boolean readPlugins()
	{
		try
		{
			PluginLoader loader = PluginLoader.getInstance();
			loader.load("plugins-config");
			pluginList.addAll(loader.getDefinedPlugins(JobPlugin.class));
			return true;
		} catch (KettleConfigException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	public JobEntryInterface getJobEntryClass(String desc) throws KettleStepLoaderException
	{
		JobPlugin jp = findJobEntriesWithDescription(desc);
		return getJobEntryClass(jp);
	}

	public Class<?> loadClass(JobPlugin sp, String className) throws KettleStepLoaderException
	{
		try
		{
			switch (sp.getType())
			{
			case JobPlugin.TYPE_NATIVE:

				return Class.forName(className);

			case JobPlugin.TYPE_PLUGIN:
				ClassLoader cl = getClassLoader(sp);
				return cl.loadClass(className);
			default:
				throw new KettleStepLoaderException("Unknown plugin type : " + sp.getType());
			}
		} catch (Exception e)
		{
			throw new KettleStepLoaderException(e);
		}
	}

	public Class<?> loadClass(String desc, String className) throws KettleStepLoaderException
	{
		try
		{
			return loadClass(findJobEntriesWithDescription(desc), className);
		} catch (Exception e)
		{
			throw new KettleStepLoaderException(e);
		}
	}

	public Class<?> loadClassByID(String id, String className) throws KettleStepLoaderException
	{
		try
		{
			return loadClass(findJobEntriesWithID(id), className);
		} catch (Exception e)
		{
			throw new KettleStepLoaderException(e);
		}
	}

	public JobEntryInterface getJobEntryClass(JobPlugin sp) throws KettleStepLoaderException
	{
		if (sp != null)
		{
			try
			{
				Class<?> cl = null;
				switch (sp.getType())
				{
				case JobPlugin.TYPE_NATIVE:
				{
					cl = Class.forName(sp.getClassname());
				}
					break;
				case JobPlugin.TYPE_PLUGIN:
				{
					ClassLoader ucl = getClassLoader(sp);

					// What's the protection domain of this class?
					// ProtectionDomain protectionDomain =
					// this.getClass().getProtectionDomain();

					// Load the class.
					// Thread.currentThread().setContextClassLoader(ucl);
					cl = ucl.loadClass(sp.getClassname());
				}
					break;
				default:
					throw new KettleStepLoaderException("Unknown plugin type : " + sp.getType());
				}

				JobEntryInterface res = (JobEntryInterface) cl.newInstance();

				if (sp.getType() == JobPlugin.TYPE_PLUGIN)
				{
					res.setPluginID(sp.getID());
				}

				res.setDescription(sp.getDescription());
				res.setName(sp.getID());
				res.setConfigId(sp.getID());

				// set the type
				if (res.getJobEntryType() == null)
					res.setJobEntryType(sp.getJobType());

				return res;
			} catch (ClassNotFoundException e)
			{
				throw new KettleStepLoaderException("Class not found", e);
			} catch (InstantiationException e)
			{
				throw new KettleStepLoaderException("Unable to instantiate class", e);
			} catch (IllegalAccessException e)
			{
				throw new KettleStepLoaderException("Illegal access to class", e);
			} catch (Throwable e)
			{
				throw new KettleStepLoaderException("Unexpected error loading class", e);
			}
		} else
		{
			throw new KettleStepLoaderException("No valid step/plugin specified (plugin=null).");
		}
	}

	private ClassLoader getClassLoader(JobPlugin sp) throws FileSystemException, MalformedURLException
	{
		String jarfiles[] = sp.getJarfiles();
		List<URL> classpath = new ArrayList<URL>();
		// safe to use filesystem because at this point it is all local
		// and we are using this so we can do things like */lib/*.jar and so
		// forth, as with ant
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				new FileSystemResourceLoader());
		if (jarfiles!=null) {
			for (int i = 0; i < jarfiles.length; i++)
			{
				try
				{
					Resource[] paths = resolver.getResources(jarfiles[i]);
					for (Resource path : paths)
					{
						classpath.add(path.getURL());
					}
				} catch (IOException e)
				{
					e.printStackTrace();
					continue;
				}
			}
		}

		URL urls[] = classpath.toArray(new URL[] {});

		// Load the class!!
		// 
		// First get the class loader: get the one that's the
		// webstart classloader, not the thread classloader
		//
		ClassLoader classLoader = getClass().getClassLoader();

		// ClassLoader classLoader = getClass().getClassLoader();

		// Construct a new URLClassLoader based on this one...
		URLClassLoader ucl = (URLClassLoader) classLoaders.get(sp.getID());
		if (ucl == null)
		{
			synchronized (classLoaders)
			{
				ucl = new PDIClassLoader(urls, classLoader);
				classLoaders.put(sp.getID(), ucl); // save for later use...
			}
		}

		return ucl;
	}

	/**
	 * Count's the number of steps with a certain type.
	 * 
	 * @param type
	 *            One of StepPlugin.TYPE_NATIVE, StepPlugin.TYPE_PLUGIN,
	 *            StepPlugin.TYPE_ALL
	 * @return The number of steps with a certain type.
	 */
	public int nrJobEntriesWithType(int type)
	{
		int nr = 0;
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getType() == type || type == JobPlugin.TYPE_ALL)
				nr++;
		}
		return nr;
	}

	public JobPlugin getJobEntryWithType(int type, int index)
	{
		int nr = 0;
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getType() == type || type == JobPlugin.TYPE_ALL)
			{
				if (nr == index)
					return sp;
				nr++;
			}
		}
		return null;
	}

	/**
	 * @param stepid
	 * @return The StepPlugin for the step with the specified ID. Null is
	 *         returned when the ID couldn't be found!
	 */
	public JobPlugin findJobPluginWithID(String stepid)
	{
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getID().equalsIgnoreCase(stepid))
				return sp;
		}
		return null;
	}

	public JobPlugin[] getJobEntriesWithType(int type)
	{
		int nr = nrJobEntriesWithType(type);
		JobPlugin steps[] = new JobPlugin[nr];
		for (int i = 0; i < steps.length; i++)
		{
			JobPlugin sp = getJobEntryWithType(type, i);
			// System.out.println("sp #"+i+" = "+sp.getID());
			steps[i] = sp;
		}
		return steps;
	}

	public JobPlugin findJobEntriesWithID(String stepid)
	{
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getID().equalsIgnoreCase(stepid))
				return sp;
		}
		return null;
	}

	public JobPlugin findJobEntriesWithClassName(String cn)
	{
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getClassname().equalsIgnoreCase(cn))
				return sp;
		}
		return null;
	}

	public JobPlugin findJobEntriesWithDescription(String description)
	{
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getDescription().equalsIgnoreCase(description) || sp.getID().equalsIgnoreCase(description))
				return sp;
		}
		return null;
	}

	/**
	 * Determine the step's id based upon the StepMetaInterface we get...
	 * 
	 * @param jei
	 *            The StepMetaInterface
	 * @return the step's id or null if we couldn't find anything.
	 */
	public String getJobEntryID(JobEntryInterface jei)
	{
		for (int i = 0; i < nrJobEntriesWithType(JobPlugin.TYPE_ALL); i++)
		{
			JobPlugin sp = getJobEntryWithType(JobPlugin.TYPE_ALL, i);
			if (jei.getClass().getName().equals(sp.getClassname()))
				return sp.getID();
		}
		return null;
	}

	/**
	 * @return Returns the initialized.
	 */
	public boolean isInitialized()
	{
		return initialized;
	}

	/**
	 * @param initialized
	 *            The initialized to set.
	 */
	public void setInitialized(boolean initialized)
	{
		this.initialized = initialized;
	}

	/**
	 * Search through all jarfiles in all steps and try to find a certain file
	 * in it.
	 * 
	 * @param filename
	 * @return an inputstream for the given file.
	 */
	public InputStream getInputStreamForFile(String filename)
	{
		JobPlugin[] jobplugins = getJobEntriesWithType(JobPlugin.TYPE_PLUGIN);
		for (JobPlugin jobPlugin : jobplugins)
		{
			try
			{
				String[] jarfiles = jobPlugin.getJarfiles();
				if (jarfiles != null)
				{
					for (int j = 0; j < jarfiles.length; j++)
					{
						JarFile jarFile = new JarFile(jarfiles[j]);
						JarEntry jarEntry;
						if (filename.startsWith("/"))
						{
							jarEntry = jarFile.getJarEntry(filename.substring(1));
						} else
						{
							jarEntry = jarFile.getJarEntry(filename);
						}
						if (jarEntry != null)
						{
							InputStream inputStream = jarFile.getInputStream(jarEntry);
							if (inputStream != null)
							{
								return inputStream;
							}
							jarFile.close();
						}
					}
				}
			} catch (Exception e)
			{
				// Just look for the next one...
			}
		}
		return null;
	}

	/**
	 * @return a unique array of all the job entry plugin package names
	 */
	public String[] getPluginPackages() 
	{
		List<String> list = new ArrayList<String>();
		for (JobPlugin stepPlugin : pluginList)
		{
			String className = stepPlugin.getClassname();
			int lastIndex = className.lastIndexOf(".");
			String packageName = className.substring(0, lastIndex); 
			if (!list.contains(packageName)) list.add(packageName);
		}
		Collections.sort(list);
		return list.toArray(new String[list.size()]);
	}
	
    /**
     * Get a unique list of categories. We can use this to display in trees etc.
     * 
     * @param type The type of job entry plugins for which we want to categories...
     * @return a unique list of categories
     */
    public String[] getCategories(int type)
    {
        return getCategories(type, LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
    }
    
    /**
     * Get a unique list of categories. We can use this to display in trees etc.
     * 
     * @param type The type of job entry plugins for which we want to categories...
     * @return a unique list of categories
     */
    public String[] getCategories(int type, String locale)
    {
        Hashtable<String, String> cat = new Hashtable<String, String>();
        for (int i = 0; i < nrJobEntriesWithType(type); i++)
        {
            JobPlugin sp = getJobEntryWithType(type, i);
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
        // It has to be sorted the same way as the String array JobStep.category_order
        //
        for (int a = 0; a < retval.length; a++)
        {
            for (int b = 0; b < retval.length - 1; b++)
            {
                // What is the index of retval[b] and retval[b+1]?
            	//
                int idx1 = Const.indexOfString(retval[b  ], JobEntryBase.category_order);
                int idx2 = Const.indexOfString(retval[b+1], JobEntryBase.category_order);
                
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
    
	public List<Object[]> getPluginInformation()
	{
		List<Object[]> list = new ArrayList<Object[]>();
		for (JobPlugin plugin : pluginList) {
			list.add(plugin.getPluginInformation());
		}
		return list;
	}
}
