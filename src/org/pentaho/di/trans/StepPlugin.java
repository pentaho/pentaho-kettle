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

/*
 * Created on 6-okt-2004
 *
 */
package org.pentaho.di.trans;

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.LanguageChoice;

/**
 * @author Matt
 * 
 */
public class StepPlugin extends Plugin<String[]>
{
	private String category;
	private String errorHelpFile;
    
    public static final String[] typeDesc = new String[] { Messages.getString("StepPlugin.Type.All.Desc"), Messages.getString("StepPlugin.Type.Native.Desc"), Messages.getString("StepPlugin.Type.Plugin.Desc"), }; 

	private boolean separateClassloaderNeeded;

	private Map<String, String> localizedCategories;

	private Map<String, String> localizedDescriptions;

	private Map<String, String> localizedTooltips;

	public StepPlugin(int type, String id[], String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname, String category, String errorHelpFile)
	{
		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);

		this.category = category;
		this.errorHelpFile = errorHelpFile;
		this.separateClassloaderNeeded = false;

		this.localizedCategories = new Hashtable<String, String>();
		this.localizedDescriptions = new Hashtable<String, String>();
		this.localizedTooltips = new Hashtable<String, String>();
	}

	@Override
	public String getDescription()
	{
		return getDescription(LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
	}

	public String getDescription(String locale)
	{
		String localizedDescription = (String) localizedDescriptions.get(locale.toLowerCase());
		if (localizedDescription != null)
		{
			return localizedDescription;
		}
		return description;
	}

	@Override
	public String getTooltip()
	{
		return getTooltip(LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
	}

	public String getTooltip(String locale)
	{
		String localizedTooltip = (String) localizedTooltips.get(locale.toLowerCase());
		if (localizedTooltip != null)
		{
			return localizedTooltip;
		}
		return super.getTooltip();
	}

	public String getCategory()
	{
		return getCategory(LanguageChoice.getInstance().getDefaultLocale().toString().toLowerCase());
	}

	public String getCategory(String locale)
	{
		String localizedCategory = (String) localizedCategories.get(locale.toLowerCase());
		if (localizedCategory != null)
		{
			return localizedCategory;
		}
		if (category == null)
			return Messages.getString("StepPlugin.Label"); //$NON-NLS-1$
		return category;
	}

	public void setCategory(String category)
	{
		this.category = category;
	}

	public int hashCode()
	{
		return getID().hashCode();
	}

	public boolean equals(Object obj)
	{
		return handles(((StepPlugin) obj).getID());
	}

	public boolean handles(String pluginID)
	{
		String[] id = getID();

		for (int i = 0; i < id.length; i++)
		{
			if (id[i].equals(pluginID))
				return true;
		}
		return false;
	}

	public boolean handles(String pluginID[])
	{
		String[] id = getID();

		for (int i = 0; i < id.length; i++)
		{
			for (int j = 0; j < pluginID.length; j++)
			{
				if (id[i].equals(pluginID[j]))
					return true;
			}
		}
		return false;
	}
    
    public String getJarfilesList()
    {
    	String list = "";
    	
    	String jarfiles[] = super.getJarfiles();
    	
    	if (jarfiles!=null)
    	{
	    	for (int i=0;i<jarfiles.length;i++)
	    	{
	    		if (i>0) list+=Const.PATH_SEPARATOR;
	    		list+=jarfiles[i];
	    	}
    	}
        return list;
    }

	public void setErrorHelpFile(String errorHelpText)
	{
		this.errorHelpFile = errorHelpText;
	}

	public String getErrorHelpFile()
	{
		return errorHelpFile;
	}

	/**
	 * @return Returns the separateClassloaderNeeded.
	 */
	public boolean isSeparateClassloaderNeeded()
	{
		return separateClassloaderNeeded;
	}

	/**
	 * @param separateClassloaderNeeded
	 *            The separateClassloaderNeeded to set.
	 */
	public void setSeparateClassloaderNeeded(boolean separateClassloaderNeeded)
	{
		this.separateClassloaderNeeded = separateClassloaderNeeded;
	}

	public void setLocalizedCategories(Map<String, String> localizedCategories)
	{
		this.localizedCategories = localizedCategories;

	}

	/**
	 * @return the localized categories map.
	 */
	public Map<String, String> getLocalizedCategories()
	{
		return localizedCategories;
	}

	public void setLocalizedDescriptions(Map<String, String> localizedDescriptions)
	{
		this.localizedDescriptions = localizedDescriptions;
	}

	/**
	 * @return the localized descriptions map.
	 */
	public Map<String, String> getLocalizedDescriptions()
	{
		return localizedDescriptions;
	}

	/**
	 * @return the localizedTooltips
	 */
	public Map<String, String> getLocalizedTooltips()
	{
		return localizedTooltips;
	}

	/**
	 * @param localizedTooltips
	 *            the localizedTooltips to set
	 */
	public void setLocalizedTooltips(Map<String, String> localizedTooltips)
	{
		this.localizedTooltips = localizedTooltips;
	}

    public String getTypeDesc()
    {
    	return typeDesc[super.getType()];
    }

    public static RowMetaInterface getPluginInformationRowMeta()
    {
    	RowMetaInterface row = new RowMeta();
    	
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Type.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ID.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Description.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ToolTip.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Directory.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.JarFiles.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.IconFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ClassName.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.Category.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.ErrorHelpFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(Messages.getString("StepPlugin.Information.SeparateClassloader.Label"), ValueMetaInterface.TYPE_BOOLEAN));

        return row;
    }

    public Object[] getPluginInformation()
    {
    	Object[] row = new Object[getPluginInformationRowMeta().size()];
    	int rowIndex=0;
    	
    	row[rowIndex++] = getTypeDesc();
    	row[rowIndex++] = getID()[0];
    	row[rowIndex++] = getDescription();
    	row[rowIndex++] = getTooltip();
    	row[rowIndex++] = getDirectory();
    	row[rowIndex++] = getJarfilesList();
    	row[rowIndex++] = getIconFilename();
    	row[rowIndex++] = getClassname();
    	row[rowIndex++] = getCategory();
    	row[rowIndex++] = getErrorHelpFile();
    	row[rowIndex++] = isSeparateClassloaderNeeded();

        return row;
    }
}
