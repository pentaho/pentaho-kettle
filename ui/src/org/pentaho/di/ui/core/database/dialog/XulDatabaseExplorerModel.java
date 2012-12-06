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
