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

package org.pentaho.di.job;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.exception.KettleStepLoaderException;
import org.pentaho.di.core.plugins.PluginLoader;
import org.pentaho.di.job.entry.JobEntryInterface;

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

	public boolean read()
	{
		if (readNatives())
		{
			if (readPlugins())
			{
				initialized = true;
				return true;
			}
		}
		return false;
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

			for (JobPluginMeta job : jobs)
				if (job.getType() != JobEntryType.NONE)
					pluginList.add(new JobPlugin(JobPlugin.TYPE_NATIVE, job.getId(), job.getType(), job
							.getTooltipDesc(), null, null, job.getImageFileName(), job.getClassName()
							.getName()));
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
			PluginLoader loader = new PluginLoader();
			loader.load("plugins-config");
			pluginList.addAll(loader.doConfig());
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
		URL urls[] = new URL[jarfiles.length];
		for (int i = 0; i < jarfiles.length; i++)
		{
			// use VFS HERE
			FileSystemManager mgr = VFS.getManager();
			FileObject jarfile = mgr.resolveFile(jarfiles[i]);
			urls[i] = new URL(jarfile.getName().getURI());
		}

		// Load the class!!
		// 
		// First get the class loader: get the one that's the
		// webstart classloader, not the thread classloader
		//
		ClassLoader classLoader = getClass().getClassLoader();

		// Construct a new URLClassLoader based on this one...
		URLClassLoader ucl = (URLClassLoader) classLoaders.get(sp.getID());
		if (ucl == null)
		{
			synchronized (classLoaders)
			{
				ucl = new URLClassLoader(urls, classLoader);

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

	public JobPlugin findJobEntriesWithDescription(String description)
	{
		for (int i = 0; i < pluginList.size(); i++)
		{
			JobPlugin sp = (JobPlugin) pluginList.get(i);
			if (sp.getDescription().equalsIgnoreCase(description))
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
			if (sp.getClassname() == jei.getClass().getName())
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
}
