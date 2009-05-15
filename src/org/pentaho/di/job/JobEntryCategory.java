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

import org.pentaho.di.i18n.BaseMessages;

/**
 * Different types of job entry categories.<br>
 * Used by the Job annotation as well as in the kettlle-jobs.xml file.<br>
 * 
 * @author Matt Casters
 *
 */
public class JobEntryCategory
{
	private static Class<?> PKG = JobEntryCategory.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	// Note: also set the Spoon category order in JobEntryBase.category_order...
	@Deprecated
	public static final JobEntryCategory GENERAL         = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.General"));
	@Deprecated
	public static final JobEntryCategory MAIL            = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.Mail"));
	@Deprecated
	public static final JobEntryCategory FILE_MANAGEMENT = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.FileManagement"));
	@Deprecated
	public static final JobEntryCategory CONDITIONS      = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.Conditions"));
	@Deprecated
	public static final JobEntryCategory SCRIPTING       = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.Scripting"));
	@Deprecated
	public static final JobEntryCategory BULK_LOADING    = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.BulkLoading"));
	@Deprecated
	public static final JobEntryCategory XML             = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.XML"));
	@Deprecated
	public static final JobEntryCategory REPOSITORY      = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.Repository"));
	@Deprecated
	public static final JobEntryCategory FILE_TRANSFER   = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.FileTransfer"));
	@Deprecated
	public static final JobEntryCategory EXPERIMENTAL   = new JobEntryCategory(BaseMessages.getString(PKG, "JobCategory.Category.Experimental"));

	public static final int CATEGORY_GENERAL         =  0;
	public static final int CATEGORY_MAIL            =  1;
	public static final int CATEGORY_FILE_MANAGEMENT =  2;
	public static final int CATEGORY_CONDITIONS      =  3;
	public static final int CATEGORY_SCRIPTING       =  4;
	public static final int CATEGORY_BULK_LOADING    =  5;
	public static final int CATEGORY_XML             =  6;
	public static final int CATEGORY_REPOSITORY      =  7;
	public static final int CATEGORY_FILE_TRANSFER   =  8;
	public static final int CATEGORY_EXPERIMENTAL    =  9;
	
	public static final String STANDARD_CATEGORIES[] = { 
		BaseMessages.getString(PKG, "JobCategory.Category.General"),
		BaseMessages.getString(PKG, "JobCategory.Category.Mail"),
		BaseMessages.getString(PKG, "JobCategory.Category.FileManagement"),
		BaseMessages.getString(PKG, "JobCategory.Category.Conditions"),
		BaseMessages.getString(PKG, "JobCategory.Category.Scripting"),
		BaseMessages.getString(PKG, "JobCategory.Category.BulkLoading"),
		BaseMessages.getString(PKG, "JobCategory.Category.XML"),
		BaseMessages.getString(PKG, "JobCategory.Category.Repository"),
		BaseMessages.getString(PKG, "JobCategory.Category.FileTransfer"),
		BaseMessages.getString(PKG, "JobCategory.Category.Experimental"),

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