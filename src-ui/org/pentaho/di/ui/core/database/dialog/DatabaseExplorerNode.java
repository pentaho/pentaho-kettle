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

import java.util.ArrayList;

import org.pentaho.ui.xul.util.AbstractModelNode;

public class DatabaseExplorerNode extends AbstractModelNode<DatabaseExplorerNode> {

  private static final long serialVersionUID = -7409853507740739091L;
  
  private String name;
	private String schema;
	private String image;
	private boolean isTable;
	private boolean isSchema;
	
	// possibly a combination of schema and table
	private String label;
	

	public DatabaseExplorerNode() {
		this.children = new ArrayList<DatabaseExplorerNode>();
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String toString() {
		return "Database Node: " + this.name;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(String aImage) {
		this.image = aImage;
	}
	
	public void setIsSchema(boolean isSchema){
	  this.isSchema = isSchema;
	}
	
	public boolean isSchema(){
	  return isSchema;
	}
	
	public boolean isTable() {
		return this.isTable;
	}
	
	public void setIsTable(boolean aIsTable) {
		this.isTable = aIsTable;
	}

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public String getSchema() {
    return schema;
  }

  public String getLabel() {
    if (label != null) {
      return label;
    } else {
      return name;
    }
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
}
