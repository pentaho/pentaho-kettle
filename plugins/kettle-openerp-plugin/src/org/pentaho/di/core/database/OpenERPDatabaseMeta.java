/*
 *   This software is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This software is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 */

package org.pentaho.di.core.database;

import org.pentaho.di.core.plugins.DatabaseMetaPlugin;

@DatabaseMetaPlugin(type = "OpenERPDatabaseMeta", typeDescription = "OpenERP Server")
public class OpenERPDatabaseMeta extends GenericDatabaseMeta implements DatabaseInterface {
	
	public int[] getAccessTypeList()
	{
		return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN, };
	}
	
	public int getDefaultDatabasePort() {
		return 8069;
	}	

	public String getDatabaseFactoryName() {
		return org.pentaho.di.openerp.core.OpenERPHelper.class.getName();
	}

	/**
	 * @return true if this is a relational database you can explore.
	 * Return false for SAP, PALO, etc.
	 */
	public boolean isExplorable() {
		return false;
	}
}

