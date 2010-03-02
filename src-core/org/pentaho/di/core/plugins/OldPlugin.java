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


public class OldPlugin<E>
{
	public static final String NAME = "name"; //$NON-NLS-1$

	public static final String LIBRARY = "library"; //$NON-NLS-1$

	public static final String LIBRARIES = "libraries"; //$NON-NLS-1$

	public static final String LOCALIZED_TOOLTIP = "localized_tooltip"; //$NON-NLS-1$

	public static final String LOCALIZED_DESCRIPTION = "localized_description"; //$NON-NLS-1$

	public static final String LOCALE = "locale"; //$NON-NLS-1$

	public static final String ICONFILE = "iconfile"; //$NON-NLS-1$

	public static final String ID = "id"; //$NON-NLS-1$

	public static final String DESCRIPTION = "description"; //$NON-NLS-1$

	public static final String CLASSNAME = "classname"; //$NON-NLS-1$

	public static final String ERRORHELPFILE = "errorhelpfile"; //$NON-NLS-1$

	public static final String LOCALIZED_CATEGORY = "localized_category"; //$NON-NLS-1$

	public static final String TOOLTIP = "tooltip"; //$NON-NLS-1$

	public static final String CATEGORY = "category"; //$NON-NLS-1$

	public static final String PLUGIN = "plugin"; //$NON-NLS-1$

	public static final String PLUGIN_XML_FILE = "plugin.xml"; //$NON-NLS-1$

	public static final String PLUGIN_LOADER = "PluginLoader"; //$NON-NLS-1$
	
	public static final int TYPE_ALL = 0;

	public static final int TYPE_NATIVE = 1;

	public static final int TYPE_PLUGIN = 2;

	private int type;

	private E id;

	protected String description;

	private String tooltip;

	private String directory;

	private String jarfiles[];

	private String icon_filename;

	private String classname;
	
	private ClassLoader classLoader;
	

	public OldPlugin(int type, E id, String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{
		this.type = type;
		this.id = id;
		this.description = description;
		this.tooltip = tooltip;
		this.directory = directory;
		this.jarfiles = jarfiles;
		this.icon_filename = icon_filename;
		this.classname = classname;
	}

	public int getType()
	{
		return type;
	}

	public boolean isNative()
	{
		return type == TYPE_NATIVE;
	}

	public boolean isPlugin()
	{
		return type == TYPE_PLUGIN;
	}

	/**
	 * @return The ID (code String) of the job or job-plugin.
	 */
	public E getID()
	{
		return id;
	}
	
	public String getDescription()
	{
		return description;
	}

	public String getTooltip()
	{
		return tooltip;
	}

	public String getDirectory()
	{
		return directory;
	}

	public String[] getJarfiles()
	{
		return jarfiles;
	}

	public String getIconFilename()
	{
		return icon_filename;
	}

	public void setIconFilename(String filename)
	{
		icon_filename = filename;
	}

	public String getClassname()
	{
		return classname;
	}

	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	public void setClassLoader(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

}
