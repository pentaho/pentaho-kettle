/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.core.database;

/**
 * Vertica Analytic Database version 5 and later (changed driver class name) 
 * 
 * @author DEinspanjer
 * @since  2009-03-16
 * @author Matt
 * @since  May-2008
 * @author Jens
 * @since  Aug-2012
 */

public class Vertica5DatabaseMeta extends VerticaDatabaseMeta
{
	@Override
	public String getDriverClass()
	{
        if (getAccessType()==DatabaseMeta.TYPE_ACCESS_NATIVE)
        {
            return "com.vertica.jdbc.Driver";            
        }
        else
        {
            return "sun.jdbc.odbc.JdbcOdbcDriver"; // always ODBC!
        }

	}
	
	/**
	 * @return false as the database does not support timestamp to date conversion.
	 */
	@Override
	public boolean supportsTimeStampToDateConversion() {
	    return false;
	}
}
