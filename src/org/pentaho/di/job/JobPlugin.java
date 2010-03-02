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

import java.util.Hashtable;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.OldPlugin;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.i18n.LanguageChoice;

/**
 * Contains the description of a job-entry of a job-entry plugin, what jars to
 * load, the icon, etc.
 * 
 * @since 2005-may-09
 * @author Matt
 * 
 */
public class JobPlugin extends OldPlugin<String>
{
	private static Class<?> PKG = JobPlugin.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	private String category;

    public static final String[] typeDesc = new String[] { BaseMessages.getString(PKG, "JobEntryPlugin.Type.All.Desc"), BaseMessages.getString(PKG, "JobEntryPlugin.Type.Native.Desc"), BaseMessages.getString(PKG, "JobPlugin.Type.Plugin.Desc"), }; 

	private Map<String, String> localizedCategories;

	private Map<String, String> localizedDescriptions;

	private Map<String, String> localizedTooltips;

	public JobPlugin(int type, String id, String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname, String category)
	{
		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);
		this.category = category;
		
		this.localizedCategories = new Hashtable<String, String>();
		this.localizedDescriptions = new Hashtable<String, String>();
		this.localizedTooltips = new Hashtable<String, String>();
	}

	public int hashCode()
	{
		return getID().hashCode();
	}

	public boolean equals(Object obj)
	{
		return getID().equals(((JobPlugin) obj).getID());
	}

	public String toString()
	{
		return getClass().getName() + ": " + getID() + "(" + (getType() == TYPE_NATIVE ? "NATIVE" : "PLUGIN")
				+ ")";
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
			return BaseMessages.getString(PKG, "JobPlugin.Label"); //$NON-NLS-1$
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
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
	
    public String getTypeDesc()
    {
    	return typeDesc[super.getType()];
    }

	/**
	 * @param localizedTooltips
	 *            the localizedTooltips to set
	 */
	public void setLocalizedTooltips(Map<String, String> localizedTooltips)
	{
		this.localizedTooltips = localizedTooltips;
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
	
    public static RowMetaInterface getPluginInformationRowMeta()
    {
    	RowMetaInterface row = new RowMeta();
    	
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.Type.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.ID.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.Description.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.ToolTip.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.Directory.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.JarFiles.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.IconFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.ClassName.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.Category.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.ErrorHelpFile.Label"), ValueMetaInterface.TYPE_STRING));
    	row.addValueMeta(new ValueMeta(BaseMessages.getString(PKG, "JobPlugin.Information.SeparateClassloader.Label"), ValueMetaInterface.TYPE_BOOLEAN));

        return row;
    }

    public Object[] getPluginInformation()
    {
    	Object[] row = new Object[getPluginInformationRowMeta().size()];
    	int rowIndex=0;
    	
    	row[rowIndex++] = getTypeDesc();
    	row[rowIndex++] = getID();
    	row[rowIndex++] = getDescription();
    	row[rowIndex++] = getTooltip();
    	row[rowIndex++] = getDirectory();
    	row[rowIndex++] = getJarfilesList();
    	row[rowIndex++] = getIconFilename();
    	row[rowIndex++] = getClassname();
    	row[rowIndex++] = getCategory();
    	row[rowIndex++] = null;
    	row[rowIndex++] = false;

        return row;
    }
}