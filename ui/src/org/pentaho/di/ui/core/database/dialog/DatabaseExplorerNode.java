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
