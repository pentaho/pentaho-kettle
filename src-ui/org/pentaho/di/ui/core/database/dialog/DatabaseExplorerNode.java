package org.pentaho.di.ui.core.database.dialog;

import java.util.ArrayList;
import java.util.List;

import org.pentaho.ui.xul.XulEventSourceAdapter;

public class DatabaseExplorerNode extends XulEventSourceAdapter {

	private String name;
	private String image;
	private List<DatabaseExplorerNode> children;
	private boolean isTable;
	

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

	public void addChild(DatabaseExplorerNode aNode) {
		this.children.add(aNode);
	}

	public List<DatabaseExplorerNode> getChildren() {
		return this.children;
	}

	public void setChildren(List<DatabaseExplorerNode> aChildren) {
		this.children = aChildren;
	}
	
	public boolean isTable() {
		return this.isTable;
	}
	
	public void setIsTable(boolean aIsTable) {
		this.isTable = aIsTable;
	}
}
