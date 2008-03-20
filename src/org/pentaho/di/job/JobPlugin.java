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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.plugins.Plugin;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.Messages;
import org.pentaho.di.trans.StepPlugin;

/**
 * Contains the description of a job-entry of a job-entry plugin, what jars to
 * load, the icon, etc.
 * 
 * @since 2005-may-09
 * @author Matt
 * 
 */
public class JobPlugin extends Plugin<String>
{
	private JobEntryType jobType;

	public JobPlugin(int type, String id, JobEntryType jobType, String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{

		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);
		this.jobType = jobType;
	}

	public JobPlugin(int type, String id, String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{
		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);
		this.jobType = JobEntryType.NONE;
	}

	public JobEntryType getJobType()
	{
		return jobType;
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
		return getClass().getName() + ": " + getID() + "(" + (getType() == TYPE_NATIVE ? "NATIVE" : "PLUGIN") + ")";
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
    	
    	String jobTypeDesc;
    	if (jobType==null || jobType.equals(JobEntryType.NONE)) {
    		jobTypeDesc = StepPlugin.typeDesc[StepPlugin.TYPE_PLUGIN];
    	} else {
    		jobTypeDesc = StepPlugin.typeDesc[StepPlugin.TYPE_NATIVE];
    	}
    	
    	row[rowIndex++] = jobTypeDesc;
    	row[rowIndex++] = getID();
    	row[rowIndex++] = getDescription();
    	row[rowIndex++] = getTooltip();
    	row[rowIndex++] = getDirectory();
    	row[rowIndex++] = getJarfilesList();
    	row[rowIndex++] = getIconFilename();
    	row[rowIndex++] = getClassname();
    	row[rowIndex++] = null;
    	row[rowIndex++] = null;
    	row[rowIndex++] = false;

        return row;
    }
}