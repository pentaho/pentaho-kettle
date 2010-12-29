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

import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class XulDatabaseExplorerModel extends XulEventSourceAdapter {

  // TODO can this be renamed?  it's actually just the root node
	private XulDatabaseExplorerNode database;
	private DatabaseMeta databaseMeta;
	private DatabaseExplorerNode selectedNode;

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

	public String getTable() {
	  if (selectedNode != null && selectedNode.isTable()) {
	    return selectedNode.getName();
	  }
		return null;
	}

	public String getSchema() {
		if (selectedNode != null) {
		  return selectedNode.getSchema();
		}
    return null;
	}

	/**
	 * Finds the node.
	 * @param aSchema can be null
	 * @param aName can be null
	 * @return node
	 */
	public DatabaseExplorerNode findBy(String aSchema, String aTable) {
		ListIterator<DatabaseExplorerNode> theNodes = this.database.listIterator();
		return drillDown(theNodes, aSchema, aTable);
	}

	private DatabaseExplorerNode drillDown(ListIterator<DatabaseExplorerNode> aNodes, String aSchema, String aTable) {
	  boolean lookingForSchema = aTable == null || Const.isEmpty(aTable);
		DatabaseExplorerNode theNode = null;
		while (aNodes.hasNext()) {
			theNode = aNodes.next();
			if (lookingForSchema && theNode.isSchema() && theNode.getName().equals(aSchema)) {
			  break;
			} else if (!lookingForSchema && theNode.isTable() && theNode.getName().equals(aTable) && (theNode.getSchema() != null ? theNode.getSchema().equals(aSchema) : aSchema == null)) {
				break;
			} else {
				theNode = drillDown(theNode.listIterator(), aSchema, aTable);
				if (theNode != null) {
					break;
				}
			}
		}
		return theNode;
	}
  // TODO mlowery why is this subclass needed?
	public static class XulDatabaseExplorerNode extends AbstractModelNode<DatabaseExplorerNode> {
		private static final long	serialVersionUID	= 2466708563640027488L;
	}

  public DatabaseExplorerNode getSelectedNode() {
    return selectedNode;
  }

  public void setSelectedNode(DatabaseExplorerNode selectedNode) {
    DatabaseExplorerNode prevVal = this.selectedNode;
    this.selectedNode = selectedNode;
    firePropertyChange("selectedNode", prevVal, this.selectedNode);
  }
}
