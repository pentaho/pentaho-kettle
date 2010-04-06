/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.core.plugins;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.jar.JarFileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.PDIClassLoader;
import org.pentaho.di.core.annotations.Job;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.util.ResolverUtil;
import org.pentaho.di.core.vfs.KettleVFS;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobPlugin;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.StepPlugin;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This class handles all plugin loading steps for Kettle/PDI. It uses the
 * ConfigManager class to load <code>PluginConfig</code> objects, which
 * contain all the location from where the plugin should be loaded.
 * 
 * Plugins are configured by modifying the kettle-plugins.xml file.
 * 
 * @see org.pentaho.di.core.plugins.PluginLocation
 * @see org.pentaho.di.job.JobEntryLoader
 * 
 * @author Alex Silva
 * 
 */
@SuppressWarnings("unchecked")
public class PluginLoader
{
	private static final String WORK_DIR = "work"; //$NON-NLS-1$

	public static final String DEFAULT_LIB = "lib/*.jar"; //$NON-NLS-1$

	private static final String JAR = "jar"; //$NON-NLS-1$

	// private static final Pattern patt = Pattern.compile("^*..(jar|zip)$");//$NON-NLS-1$

	private static final LogWriter log = LogWriter.getInstance();

	private final Set<PluginLocation> locs;

	private final Map<Class<? extends Annotation>, Set<? extends Plugin>> plugins;

	private final Map<Class<? extends Annotation>, ResolverUtil.Test> tests = new HashMap<Class<? extends Annotation>, ResolverUtil.Test>();

	private static PluginLoader loader = new PluginLoader();

	private PluginLoader()
	{
		tests.put(Job.class, new ResolverUtil.AnnotatedWith(Job.class));
		tests.put(Step.class, new ResolverUtil.AnnotatedWith(Step.class));

		locs = new HashSet<PluginLocation>();

		plugins = new HashMap<Class<? extends Annotation>, Set<? extends Plugin>>();// HashSet<Plugin>();
		// initialize map
		plugins.put(Job.class, new HashSet<JobPlugin>());
		plugins.put(Step.class, new HashSet<StepPlugin>());
	}

	public static PluginLoader getInstance()
	{
		return loader;
	}

	/**
	 * Loads all plugins identified by the string passed. This method can be
	 * called multiple times with different managers.
	 * 
	 * @param mgr
	 *            The manager id, as defined in kettle-config.xml.
	 * @throws KettleConfigException
	 */
	public void load(String mgr) throws KettleConfigException
	{
		ConfigManager<?> c = KettleConfig.getInstance().getManager(mgr);
		locs.addAll((Collection<PluginLocation>) c.loadAs(PluginLocation.class));
		doConfig();
	}

	/**
	 * This method does the actual plugin configuration and should be called
	 * after load()
	 * 
	 * @return a collection containing the <code>JobPlugin</code> objects
	 *         loaded.
	 * @throws KettleConfigException
	 */
	private void doConfig() throws KettleConfigException
	{
		synchronized (locs)
		{
			String sjar = "." + JAR;
			
			for (PluginLocation plugin : locs)
			{
				// check to see if the resource type is present
				File base = new File(System.getProperty("user.dir"));
				try
				{
					FileSystemManager mgr = VFS.getManager();
					FileObject fobj = mgr.resolveFile(base, plugin.getLocation());
					if (fobj.isReadable())
					{
						String name = fobj.getName().getURI();
						int jindex = name.indexOf(sjar);
						int nlen = name.length();
						boolean isJar = jindex == nlen - 4 || jindex == nlen - 6;

						try
						{

							if (isJar)
								build(fobj, true);
							else
							{
								// loop thru folder
								for (FileObject childFolder : fobj.getChildren())
								{
									boolean isAlsoJar = childFolder.getName().getURI().endsWith(sjar);

									// ignore anything that is not a folder or a
									// jar
									if (!isAlsoJar && childFolder.getType() != FileType.FOLDER)
									{
										continue;
									}

									// ignore any subversion or CVS directories
									if (childFolder.getName().getBaseName().equalsIgnoreCase(".svn"))
									{
										continue;
									} else if (childFolder.getName().getBaseName().equalsIgnoreCase(".cvs"))
									{
										continue;
									}
									try
									{
										build(childFolder, isAlsoJar);
									} catch (KettleConfigException e)
									{
										log.logError(Plugin.PLUGIN_LOADER, e.getMessage());
										continue;
									}
								}

							}

						} catch (FileSystemException e)
						{
							log.logError(Plugin.PLUGIN_LOADER, e.getMessage());
							continue;
						} catch (KettleConfigException e)
						{
							log.logError(Plugin.PLUGIN_LOADER, e.getMessage());
							continue;
						}

					} else
					{
						log.logDebug(Plugin.PLUGIN_LOADER, fobj + " does not exist, ignoring this.");
					}
				} catch (Exception e)
				{
					throw new KettleConfigException(e);
				}

			}
		}
	}

	public <E extends Plugin> Collection<E> getDefinedPlugins(Class<E> pluginType)
			throws KettleConfigException
	{
		Class type = pluginType == JobPlugin.class ? Job.class : Step.class;

		for (Map.Entry<Class<? extends Annotation>, Set<? extends Plugin>> entry : this.plugins.entrySet())
		{
			if (entry.getKey() == type)
			{
				return (Collection<E>) entry.getValue();
			}
		}

		throw new KettleConfigException("Invalid plugin type: " + type);
	}

	private void build(FileObject parent, boolean isJar) throws KettleConfigException
	{
		try
		{
			FileObject xml = null;

			if (isJar)
			{
				FileObject exploded = explodeJar(parent);

				// try reading annotations first ...
				ResolverUtil<Plugin> resPlugins = new ResolverUtil<Plugin>();

				// grab all jar files not part of the "lib"
				File fparent = new File(exploded.getURL().getFile());
				File[] files = fparent.listFiles(new JarNameFilter());

				URL[] classpath = new URL[files.length];
				for (int i = 0; i < files.length; i++)
					classpath[i] = files[i].toURI().toURL();

				ClassLoader cl = new PDIClassLoader(classpath, Thread.currentThread().getContextClassLoader());
				resPlugins.setClassLoader(cl);

				for (FileObject couldBeJar : exploded.getChildren())
				{
					if (couldBeJar.getName().getExtension().equals(JAR))
						resPlugins.loadImplementationsInJar(Const.EMPTY_STRING, couldBeJar.getURL(),
								tests.values().toArray(new ResolverUtil.Test[2]));
				}

				for (Class<? extends Plugin> match : resPlugins.getClasses())
				{
					for (Class<? extends Annotation> cannot : tests.keySet())
					{
						Annotation annot = match.getAnnotation(cannot);
						if (annot != null)
							fromAnnotation(annot, exploded, match);
					}

				}

				// and we also read from the xml if present
				xml = exploded.getChild(Plugin.PLUGIN_XML_FILE);

				if (xml == null || !xml.exists())
					return;

				parent = exploded;

			} else
				xml = parent.getChild(Plugin.PLUGIN_XML_FILE);

			// then read the xml if it is there
			if (xml != null && xml.isReadable())
				fromXML(xml, parent);

		} catch (Exception e)
		{
			throw new KettleConfigException(e);
		}

		// throw new KettleConfigException("Unable to read plugin.xml from " +
		// parent);
	}

	private void fromXML(FileObject xml, FileObject parent) throws IOException, ClassNotFoundException,
			ParserConfigurationException, SAXException
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream xmlInputStream = KettleVFS.getInputStream(xml);
		Document doc = null;
		try {
		  doc = db.parse( xmlInputStream );
		} finally {
		  xmlInputStream.close();
		}
		Node plugin = XMLHandler.getSubNode(doc, Plugin.PLUGIN);
		String id = XMLHandler.getTagAttribute(plugin, Plugin.ID);
		String description = XMLHandler.getTagAttribute(plugin, Plugin.DESCRIPTION);
		String iconfile = XMLHandler.getTagAttribute(plugin, Plugin.ICONFILE);
		String tooltip = XMLHandler.getTagAttribute(plugin, Plugin.TOOLTIP);
		String classname = XMLHandler.getTagAttribute(plugin, Plugin.CLASSNAME);
		String category = XMLHandler.getTagAttribute(plugin, Plugin.CATEGORY);
		String errorHelpfile = XMLHandler.getTagAttribute(plugin, Plugin.ERRORHELPFILE);

		// Localized categories
		//
		Node locCatsNode = XMLHandler.getSubNode(plugin, Plugin.LOCALIZED_CATEGORY);
		int nrLocCats = XMLHandler.countNodes(locCatsNode, Plugin.CATEGORY);
		Map<String, String> localizedCategories = new Hashtable<String, String>();
		for (int j = 0; j < nrLocCats; j++)
		{
			Node locCatNode = XMLHandler.getSubNodeByNr(locCatsNode, Plugin.CATEGORY, j);
			String locale = XMLHandler.getTagAttribute(locCatNode, Plugin.LOCALE);
			String locCat = XMLHandler.getNodeValue(locCatNode);

			if (!Const.isEmpty(locale) && !Const.isEmpty(locCat))
			{
				localizedCategories.put(locale.toLowerCase(), locCat);
			}
		}

		// Localized descriptions
		//
		Node locDescsNode = XMLHandler.getSubNode(plugin, Plugin.LOCALIZED_DESCRIPTION);
		int nrLocDescs = XMLHandler.countNodes(locDescsNode, Plugin.DESCRIPTION);
		Map<String, String> localizedDescriptions = new Hashtable<String, String>();
		for (int j = 0; j < nrLocDescs; j++)
		{
			Node locDescNode = XMLHandler.getSubNodeByNr(locDescsNode, Plugin.DESCRIPTION, j);
			String locale = XMLHandler.getTagAttribute(locDescNode, Plugin.LOCALE);
			String locDesc = XMLHandler.getNodeValue(locDescNode);

			if (!Const.isEmpty(locale) && !Const.isEmpty(locDesc))
			{
				localizedDescriptions.put(locale.toLowerCase(), locDesc);
			}
		}

		// Localized tooltips
		//
		Node locTipsNode = XMLHandler.getSubNode(plugin, Plugin.LOCALIZED_TOOLTIP);
		int nrLocTips = XMLHandler.countNodes(locTipsNode, Plugin.TOOLTIP);
		Map<String, String> localizedTooltips = new Hashtable<String, String>();
		for (int j = 0; j < nrLocTips; j++)
		{
			Node locTipNode = XMLHandler.getSubNodeByNr(locTipsNode, Plugin.TOOLTIP, j);
			String locale = XMLHandler.getTagAttribute(locTipNode, Plugin.LOCALE);
			String locTip = XMLHandler.getNodeValue(locTipNode);

			if (!Const.isEmpty(locale) && !Const.isEmpty(locTip))
			{
				localizedTooltips.put(locale.toLowerCase(), locTip);
			}
		}

		Node libsnode = XMLHandler.getSubNode(plugin, Plugin.LIBRARIES);
		int nrlibs = XMLHandler.countNodes(libsnode, Plugin.LIBRARY);
		String jarfiles[] = new String[nrlibs];
		for (int j = 0; j < nrlibs; j++)
		{
			Node libnode = XMLHandler.getSubNodeByNr(libsnode, Plugin.LIBRARY, j);
			String jarfile = XMLHandler.getTagAttribute(libnode, Plugin.NAME);
			jarfiles[j] = parent.resolveFile(jarfile).getURL().getFile();
			// System.out.println("jar files=" + jarfiles[j]);
		}

		// convert to URL
		List<URL> classpath = new ArrayList<URL>();
		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(
				new FileSystemResourceLoader());
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

		URL urls[] = classpath.toArray(new URL[classpath.size()]);

		URLClassLoader cl = new PDIClassLoader(urls, Thread.currentThread().getContextClassLoader());

		String iconFilename = parent.resolveFile(iconfile).getURL().getFile();

		Class<?> pluginClass = cl.loadClass(classname);

		// here we'll have to use some reflection in order to decide
		// which object we should instantiate!
		if (JobEntryInterface.class.isAssignableFrom(pluginClass))
		{
			Set<JobPlugin> jps = (Set<JobPlugin>) this.plugins.get(Job.class);

			JobPlugin plg = new JobPlugin(Plugin.TYPE_PLUGIN, id, description, tooltip, parent.getName()
					.getURI(), jarfiles, iconFilename, classname, category);
			plg.setClassLoader(cl);

			// Add localized information too...
			plg.setLocalizedCategories(localizedCategories);
			plg.setLocalizedDescriptions(localizedDescriptions);
			plg.setLocalizedTooltips(localizedTooltips);

			jps.add(plg);
		} else
		{
			String errorHelpFileFull = errorHelpfile;
			String path = parent.getName().getURI();
			if (!Const.isEmpty(errorHelpfile))
				errorHelpFileFull = (path == null) ? errorHelpfile : path + Const.FILE_SEPARATOR
						+ errorHelpfile;

			StepPlugin sp = new StepPlugin(Plugin.TYPE_PLUGIN, new String[] { id }, description, tooltip,
					path, jarfiles, iconFilename, classname, category, errorHelpFileFull);

			// Add localized information too...
			sp.setLocalizedCategories(localizedCategories);
			sp.setLocalizedDescriptions(localizedDescriptions);
			sp.setLocalizedTooltips(localizedTooltips);

			Set<StepPlugin> sps = (Set<StepPlugin>) this.plugins.get(Step.class);
			sps.add(sp);
		}

	}

	private void fromAnnotation(Annotation annot, FileObject directory, Class<?> match) throws IOException
	{
		Class type = annot.annotationType();

		if (type == Job.class)
		{
			Job jobAnnot = (Job) annot;
			String[] libs = getLibs(directory);
			Set<JobPlugin> jps = (Set<JobPlugin>) this.plugins.get(Job.class);
			JobPlugin pg = new JobPlugin(Plugin.TYPE_PLUGIN, jobAnnot.id(), jobAnnot.type().getDescription(),
					jobAnnot.tooltip(), directory.getURL().getFile(), libs, jobAnnot.image(), match.getName(), jobAnnot.categoryDescription());

			pg.setClassLoader(match.getClassLoader());

			jps.add(pg);

		} else if (type == Step.class)
		{
			Step jobAnnot = (Step) annot;
			String[] libs = getLibs(directory);
			Set<StepPlugin> jps = (Set<StepPlugin>) this.plugins.get(Step.class);
			StepPlugin pg = new StepPlugin(Plugin.TYPE_PLUGIN, jobAnnot.name(), jobAnnot.description(),
					jobAnnot.tooltip(), directory.getURL().getFile(), libs, jobAnnot.image(),
					match.getName(), jobAnnot.categoryDescription(), Const.EMPTY_STRING);

			pg.setClassLoader(match.getClassLoader());

			jps.add(pg);
		}
	}

	private String[] getLibs(FileObject pluginLocation) throws IOException
	{
		File[] jars = new File(pluginLocation.getURL().getFile()).listFiles(new JarNameFilter());

		String[] libs = new String[jars.length];
		for (int i = 0; i < jars.length; i++)
			libs[i] = jars[i].getPath();

		Arrays.sort(libs);
		int idx = Arrays.binarySearch(libs, DEFAULT_LIB);

		String[] retVal = null;

		if (idx < 0) // does not contain
		{
			String[] completeLib = new String[libs.length + 1];
			System.arraycopy(libs, 0, completeLib, 0, libs.length);
			completeLib[libs.length] = pluginLocation.resolveFile(DEFAULT_LIB).getURL().getFile();
			retVal = completeLib;
		} else
			retVal = libs;

		return retVal;
	}

	/**
	 * "Deploys" the plugin jar file.
	 * 
	 * @param parent
	 * @return
	 * @throws FileSystemException
	 */
	private FileObject explodeJar(FileObject parent) throws FileSystemException
	{
		// By Alex, 7/13/07
		// Since the JVM does not support nested jars and
		// URLClassLoaders, we have to hack it
		// see
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4735639
		// 
		// We do so by exploding the jar, sort of like deploying it
		FileObject dest = VFS.getManager().resolveFile(Const.getKettleDirectory() + File.separator + WORK_DIR);
		dest.createFolder();

		FileObject destFile = dest.resolveFile(parent.getName().getBaseName());

		if (!destFile.exists())
			destFile.createFolder();
		else
			// delete children
			for (FileObject child : destFile.getChildren())
				child.delete(new AllFileSelector());

		// force VFS to treat it as a jar file explicitly with children,
		// etc. and copy
		destFile.copyFrom(!(parent instanceof JarFileObject) ? VFS.getManager().resolveFile(
				JAR + ":" + parent.getName().getURI()) : parent, new AllFileSelector());

		return destFile;
	}

	private static class JarNameFilter implements FilenameFilter
	{
		//
		public boolean accept(File dir, String name)
		{
			//return patt.matcher(name).matches();
			return name.endsWith(JAR) || name.endsWith(".zip");
		}
	}

}
