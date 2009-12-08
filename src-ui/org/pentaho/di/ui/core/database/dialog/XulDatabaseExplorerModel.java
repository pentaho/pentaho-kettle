package org.pentaho.di.ui.core.database.dialog;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.ui.xul.XulEventSourceAdapter;
import org.pentaho.ui.xul.util.AbstractModelNode;

public class XulDatabaseExplorerModel extends XulEventSourceAdapter {

	private XulDatabaseExplorerNode database;
	private DatabaseMeta databaseMeta;
	private String table;

	private static Log logger = LogFactory.getLog(XulDatabaseExplorerModel.class);

	private static final String DATABASE_IMAGE = "ui/images/folder_connection.png";
	private static final String FOLDER_IMAGE = "ui/images/BOL.png";
	private static final String TABLE_IMAGE = "ui/images/table.png";

	private static final String STRING_TABLES = Messages.getString("DatabaseExplorerDialog.Tables.Label");
	private static final String STRING_VIEWS = Messages.getString("DatabaseExplorerDialog.Views.Label");

	public XulDatabaseExplorerModel(DatabaseMeta aDatabaseMeta) {

		this.database = new XulDatabaseExplorerNode();
		this.databaseMeta = aDatabaseMeta;
		createDatabaseNodes();
	}

	public DatabaseMeta getDatabaseMeta() {
		return this.databaseMeta;
	}

	private void createDatabaseNodes() {

		try {

			Database theDatabase = new Database(this.databaseMeta);
			theDatabase.connect();

			// Adds the main database node.
			DatabaseExplorerNode theDatabaseNode = new DatabaseExplorerNode();
			theDatabaseNode.setName(this.databaseMeta.getName());
			theDatabaseNode.setImage(DATABASE_IMAGE);
			this.database.add(theDatabaseNode);

			// Adds the Tables database node.
			DatabaseExplorerNode theTablesNode = new DatabaseExplorerNode();
			theTablesNode.setName(STRING_TABLES);
			theTablesNode.setImage(FOLDER_IMAGE);
			theDatabaseNode.addChild(theTablesNode);

			// Adds the Views database node.
			DatabaseExplorerNode theViewsNode = new DatabaseExplorerNode();
			theViewsNode.setName(STRING_VIEWS);
			theViewsNode.setImage(FOLDER_IMAGE);
			theDatabaseNode.addChild(theViewsNode);

			// Adds the database tables.
			String[] theTableNames = theDatabase.getTablenames();
			DatabaseExplorerNode theTableNode = null;
			for (int i = 0; i < theTableNames.length; i++) {
				theTableNode = new DatabaseExplorerNode();
				theTableNode.setIsTable(true);
				theTableNode.setName(theTableNames[i]);
				theTableNode.setImage(TABLE_IMAGE);
				theTablesNode.addChild(theTableNode);
			}

			// Adds the database views.
			String[] theViewNames = theDatabase.getViews();
			DatabaseExplorerNode theViewNode = null;
			for (int i = 0; i < theViewNames.length; i++) {
				theViewNode = new DatabaseExplorerNode();
				theViewNode.setIsTable(true);
				theViewNode.setName(theViewNames[i]);
				theViewNode.setImage(TABLE_IMAGE);
				theViewsNode.addChild(theViewNode);
			}
		} catch (Exception e) {
			logger.info(e);
		}
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
