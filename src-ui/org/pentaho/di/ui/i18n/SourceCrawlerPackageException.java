/*
 * Copyright (c) 2010 Pentaho Corporation.  All rights reserved. 
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
package org.pentaho.di.ui.i18n;

public class SourceCrawlerPackageException {
	private String startsWith;
	private String packageName;

	/**
	 * @param startsWiths
	 * @param packageName
	 */
	public SourceCrawlerPackageException(String startsWith, String packageName) {
		this.startsWith = startsWith;
		this.packageName = packageName;
	}

	/**
	 * @return the startsWith
	 */
	public String getStartsWith() {
		return startsWith;
	}

	/**
	 * @param startsWith
	 *            the startsWith to set
	 */
	public void setStartsWith(String startsWith) {
		this.startsWith = startsWith;
	}

	/**
	 * @return the packageName
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @param packageName
	 *            the packageName to set
	 */
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

}
