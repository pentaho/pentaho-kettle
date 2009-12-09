package org.pentaho.di.ui.core.database.dialog;

import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class XulDatabaseExplorerModel extends XulEventSourceAdapter {

	private XulDatabaseExplorerNode database;
	private DatabaseMeta databaseMeta;
	private String table;

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

	public static class XulDatabaseExplorerNode extends AbstractModelNode<DatabaseExplorerNode> {
	}

	public void setTable(String aTable) {
		this.table = aTable;
	}

	public String getTable() {
		return this.table;
	}
}
