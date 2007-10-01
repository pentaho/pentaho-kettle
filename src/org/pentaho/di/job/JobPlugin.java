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

import org.pentaho.di.core.plugins.Plugin;

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

	public JobPlugin(int type, String id, JobEntryType jobType, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{

		super(type, id, jobType.getDescription(), tooltip, directory, jarfiles, icon_filename, classname);
		this.jobType = jobType;
	}

	public JobPlugin(int type, String id, String description, String tooltip, String directory,
			String jarfiles[], String icon_filename, String classname)
	{
		super(type, id, description, tooltip, directory, jarfiles, icon_filename, classname);
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
		return getClass().getName() + ": " + getID() + "(" + (getType() == TYPE_NATIVE ? "NATIVE" : "PLUGIN")
				+ ")";
	}
}