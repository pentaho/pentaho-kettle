/*
 *   This file is part of PaloKettlePlugin.
 *
 *   PaloKettlePlugin is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PaloKettlePlugin is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with PaloKettlePlugin.  If not, see <http://www.gnu.org/licenses/>.
 *
 *   Portions Copyright 2011 De Bortoli Wines Pty Limited (Australia)
 *   Portions Copyright 2011 - 2017 Hitachi Vantara
 */

package org.pentaho.di.core.database;

import org.pentaho.di.core.plugins.DatabaseMetaPlugin;

/**
 * Contains Database Connection information through static final members for a PALO database. These connections are
 * typically custom-made. That means that reading, writing, etc, is not done through JDBC.
 * 
 * @author Matt
 * @since 18-Sep-2007
 */

@DatabaseMetaPlugin( type = "PALO", typeDescription = "Palo MOLAP Server" )
public class PALODatabaseMeta extends GenericDatabaseMeta implements DatabaseInterface {
  public int[] getAccessTypeList() {
    return new int[] { DatabaseMeta.TYPE_ACCESS_PLUGIN, };
  }

  public int getDefaultDatabasePort() {
    return 7777;
  }

  public String getDatabaseFactoryName() {
    return "org.pentaho.di.palo.core.PaloHelper";
  }

  /**
   * @return true if this is a relational database you can explore. Return false for SAP, PALO, etc.
   */
  public boolean isExplorable() {
    return false;
  }
}
