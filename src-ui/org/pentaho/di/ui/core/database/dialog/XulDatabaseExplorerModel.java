/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2009 Pentaho Corporation..  All rights reserved.
 * 
 * Author: Ezequiel Cuellar
 */
package org.pentaho.di.ui.core.database.dialog;

import java.util.ListIterator;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class XulDatabaseExplorerModel extends XulEventSourceAdapter {

	private XulDatabaseExplorerNode database;
	private DatabaseMeta databaseMeta;
	private String table;
	private String schema;

	public XulDatabaseExplorerModel(DatabaseMeta aDatabaseMeta) {
		this.database = new XulDatabaseExplorerNode();
		this.databaseMeta = aDatabaseMeta;
	}

	public DatabaseMeta getDatabaseMeta() {
		return this.databaseMeta;
	}

	public XulDatabaseExplorerNode getDatabase() {
		return this.database;
	}

	public void setDatabase(XulDatabaseExplorerNode aDatabase) {
		this.database = aDatabase;
	}

	public void setTable(String aTable) {
		this.table = aTable;
	}

	public String getTable() {
		return this.table;
	}

	public void setSchema(String aSchema) {
		this.schema = aSchema;
	}

	public String getSchema() {
		return this.schema;
	}

	public DatabaseExplorerNode findBy(String aName) {
		ListIterator<DatabaseExplorerNode> theNodes = this.database.listIterator();
		return drillDown(theNodes, aName);
	}

	private DatabaseExplorerNode drillDown(ListIterator<DatabaseExplorerNode> aNodes, String aName) {
		DatabaseExplorerNode theNode = null;
		while (aNodes.hasNext()) {
			theNode = aNodes.next();
			if (theNode.getName().equals(aName)) {
				break;
			} else {
				theNode = drillDown(theNode.getChildren().listIterator(), aName);
				if (theNode != null) {
					break;
				}
			}
		}
		return theNode;
	}

	public static class XulDatabaseExplorerNode extends AbstractModelNode<DatabaseExplorerNode> {
	}
}
