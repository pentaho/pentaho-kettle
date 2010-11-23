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

package org.pentaho.di.core.database;



/**
 * Contains PostgreSQL specific information through static final members 
 * 
 * @author Matt
 * @since  11-mrt-2005
 */

public class GreenplumDatabaseMeta extends PostgreSQLDatabaseMeta implements DatabaseInterface
{
	@Override
	public String[] getReservedWords() {
		int extraWords = 1;
		
		String[] pgWords = super.getReservedWords();
		String[] gpWords = new String[pgWords.length+extraWords];
		for (int i=0;i<pgWords.length;i++) gpWords[i]=pgWords[i];
		
		int index = pgWords.length;
		
		// Just add the ERRORS keyword for now
		//
		gpWords[index++] = "ERRORS";
		
		return gpWords;
	}
	
	/**
	 * Returns a false as Greenplum does not allow for the releasing of savepoints.
	 */
	@Override
	public boolean releaseSavepoint() {
	   return false;
	}
}
