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
/*
 *
 *
 */

package org.pentaho.di.core.database;

/**
 * Contains the information that's stored in a single catalog.
 * 
 * @author Matt
 * @since  7-apr-2005
 */
public class Catalog
{
	private String   catalogName;
	private String[] items;
	
	public Catalog(String catalogName, String[] items)
	{
		this.catalogName = catalogName;
		this.items = items;
	}
	
	public Catalog(String catalogName)
	{
		this(catalogName, new String[] {});
	}
	/**
	 * @return Returns the catalogName.
	 */
	public String getCatalogName()
	{
		return catalogName;
	}
	
	/**
	 * @param catalogName The catalogName to set.
	 */
	public void setCatalogName(String catalogName)
	{
		this.catalogName = catalogName;
	}
	
	/**
	 * @return Returns the items.
	 */
	public String[] getItems()
	{
		return items;
	}
	
	/**
	 * @param items The items to set.
	 */
	public void setItems(String[] items)
	{
		this.items = items;
	}
};
