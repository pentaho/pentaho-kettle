package org.pentaho.di.core.plugins;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.vfs.AllFileSelector;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.provider.jar.JarFileObject;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.config.ConfigManager;
import org.pentaho.di.core.config.KettleConfig;
import org.pentaho.di.core.exception.KettleConfigException;
import org.pentaho.di.core.logging.LogWriter;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.job.JobPlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class PluginLoader
{
	private static final String PLUGIN_LOADER = "PluginLoader";

	private static final LogWriter log = LogWriter.getInstance();

	private Set<PluginConfig> configs;

	private Set<JobPlugin> plugins;

	public PluginLoader()
	{
		configs = new HashSet<PluginConfig>();
		plugins = new HashSet<JobPlugin>();
	}

	public void load(String mgr) throws KettleConfigException
	{
		ConfigManager<?> c = KettleConfig.getInstance().getManager(mgr);
		configs.addAll((Collection<PluginConfig>) c.loadAs(PluginConfig.class));
	}

	private void addJob(JobPlugin job)
	{
		// returns false if already in the set
		if (!plugins.add(job))
		{
			// then we replace it with the new one
			plugins.remove(job);
			plugins.add(job);
		}
	}

	public Collection<JobPlugin> doConfig() throws KettleConfigException
	{
		synchronized (configs)
		{
			for (PluginConfig plugin : configs)
			{
				// check to see if the resource type is present
				File base = new File(System.getProperty("user.dir"));
				try
				{
					FileSystemManager mgr = VFS.getManager();
					FileObject fobj = mgr.resolveFile(base, plugin.getLocation());
					if (fobj.isReadable())
					{
						boolean isJar = fobj.getName().getURI().endsWith("jar!/");

						try
						{

							if (isJar)
								addJob(build(fobj, true));
							else
							{
								// loop thru folder
								for (FileObject childFolder : fobj.getChildren())
								{
									boolean isAlsoJar = childFolder.getName().getURI().indexOf(".jar") != -1;

									try
									{
										addJob(build(childFolder, isAlsoJar));
									} catch (KettleConfigException e)
									{
										log.logError("PluginLoader!!", e.getMessage());
										continue;
									}
								}

							}

						} catch (FileSystemException e)
						{
							log.logError(PLUGIN_LOADER, e.getMessage());
							continue;
						} catch (KettleConfigException e)
						{
							log.logError(PLUGIN_LOADER, e.getMessage());
							continue;
						}

					} else
					{
						log.logError(PLUGIN_LOADER, fobj + "cannot be read.");
					}
				} catch (Exception e)
				{
					throw new KettleConfigException(e);
				}

			}
		}

		return plugins;
	}

	private JobPlugin build(FileObject parent, boolean isJar) throws KettleConfigException
	{
		try
		{
			FileObject xml = null;

			if (isJar)
			{
				FileObject exploded = explodeJar(parent);
				// and we read it from there now
				xml = exploded.getChild("plugin.xml");
				parent = exploded;

			} else
				xml = parent.getChild("plugin.xml");

			if (xml != null && xml.isReadable())
			{
				System.out.println("The plugin xml is for " + parent + " is " + xml + " I can read: "
						+ xml.isReadable());

				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(xml.getContent().getInputStream());
				Node plugin = XMLHandler.getSubNode(doc, "plugin");
				String id = XMLHandler.getTagAttribute(plugin, "id");
				String description = XMLHandler.getTagAttribute(plugin, "description");
				String iconfile = XMLHandler.getTagAttribute(plugin, "iconfile");
				String tooltip = XMLHandler.getTagAttribute(plugin, "tooltip");
				String classname = XMLHandler.getTagAttribute(plugin, "classname");

				Node libsnode = XMLHandler.getSubNode(plugin, "libraries");
				int nrlibs = XMLHandler.countNodes(libsnode, "library");
				String jarfiles[] = new String[nrlibs];
				for (int j = 0; j < nrlibs; j++)
				{
					Node libnode = XMLHandler.getSubNodeByNr(libsnode, "library", j);
					String jarfile = XMLHandler.getTagAttribute(libnode, "name");
					jarfiles[j] = parent.getChild(jarfile).getURL().getFile();
					//System.out.println("jar files=" + jarfiles[j]);
				}

				String iconFilename = parent.getChild(iconfile).getURL().getFile();
				//System.out.println("iconfile=" + iconFilename);
				JobPlugin sp = new JobPlugin(JobPlugin.TYPE_PLUGIN, id, description, tooltip, parent
						.getName().getURI(), jarfiles, iconFilename, classname);

				return sp;

			}
		} catch (Exception e)
		{
			throw new KettleConfigException(e);
		}

		throw new KettleConfigException("Unable to read plugin.xml from " + parent);
	}

	private FileObject explodeJar(FileObject parent) throws FileSystemException
	{
		// By Alex, 7/13/07
		// Since the JVM does not support nested jars and
		// URLClassLoaders, we have to hack it
		// see
		// http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4735639
		// 
		// We do so by exploding the jar, sort of like deploying it
		FileObject dest = VFS.getManager().resolveFile(Const.getKettleDirectory());
		dest.createFolder();
		FileObject destFile = dest.resolveFile(parent.getName().getBaseName());
		destFile.createFolder();
		// force VFS to treat it as a jar file explicitly with children,
		// etc. and copy
		destFile.copyFrom(!(parent instanceof JarFileObject)?VFS.getManager().resolveFile("jar:" + parent.getName().getURI()):parent,
				new AllFileSelector());

		return destFile;
	}

}
