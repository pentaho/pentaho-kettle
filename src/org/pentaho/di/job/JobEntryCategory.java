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
package org.pentaho.di.job;

/**
 * Different types of job entry categories.<br>
 * Used by the Job annotation as well as in the kettlle-jobs.xml file.<br>
 * 
 * @author Matt Casters
 *
 */
public class JobEntryCategory
{
	public static final JobEntryCategory GENERAL         = new JobEntryCategory(Messages.getString("JobCategory.Category.General"));
	public static final JobEntryCategory MAIL            = new JobEntryCategory(Messages.getString("JobCategory.Category.Mail"));
	public static final JobEntryCategory FILE_MANAGEMENT = new JobEntryCategory(Messages.getString("JobCategory.Category.FileManagement"));
	public static final JobEntryCategory CONDITIONS      = new JobEntryCategory(Messages.getString("JobCategory.Category.Conditions"));
	public static final JobEntryCategory SCRIPTING       = new JobEntryCategory(Messages.getString("JobCategory.Category.Scripting"));
	public static final JobEntryCategory BULK_LOADING    = new JobEntryCategory(Messages.getString("JobCategory.Category.BulkLoading"));
	public static final JobEntryCategory XML             = new JobEntryCategory(Messages.getString("JobCategory.Category.XML"));
	public static final JobEntryCategory REPOSITORY      = new JobEntryCategory(Messages.getString("JobCategory.Category.Repository"));
	
	public static final int CATEGORY_USER_DEFINED    = -1;
	public static final int CATEGORY_GENERAL         =  0;
	public static final int CATEGORY_MAIL            =  1;
	public static final int CATEGORY_FILE_MANAGEMENT =  2;
	public static final int CATEGORY_CONDITIONS      =  3;
	public static final int CATEGORY_SCRIPTING       =  4;
	public static final int CATEGORY_BULK_LOADING    =  5;
	public static final int CATEGORY_XML             =  6;
	public static final int CATEGORY_REPOSITORY      =  7;
	
	public static final JobEntryCategory[] STANDARD_CATEGORIES = new JobEntryCategory[] { 
			GENERAL, MAIL, FILE_MANAGEMENT, CONDITIONS, SCRIPTING, BULK_LOADING, XML, REPOSITORY,
		};

	private String name;
	
	public JobEntryCategory(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
}