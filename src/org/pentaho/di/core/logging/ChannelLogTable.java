package org.pentaho.di.core.logging;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.w3c.dom.Node;

/**
 * This class describes a logging channel logging table
 * 
 * @author matt
 *
 */
public class ChannelLogTable extends BaseLogTable implements Cloneable, LogTableInterface {

	private static Class<?> PKG = ChannelLogTable.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

	public static final String	XML_TAG	= "channel-log-table";
	
	public enum ID {
		
		ID_BATCH("ID_BATCH"),
		CHANNEL_ID("CHANNEL_ID"),
		LOG_DATE("LOG_DATE"),
		LOGGING_OBJECT_TYPE("LOGGING_OBJECT_TYPE"),
		OBJECT_NAME("OBJECT_NAME"),
		OBJECT_COPY("OBJECT_COPY"),
		REPOSITORY_DIRECTORY("REPOSITORY_DIRECTORY"),
		FILENAME("FILENAME"),
		OBJECT_ID("OBJECT_ID"),
		OBJECT_REVISION("OBJECT_REVISION"),
		PARENT_CHANNEL_ID("PARENT_CHANNEL_ID"),
		ROOT_CHANNEL_ID("ROOT_CHANNEL_ID"),
		;
		
		private String id;
		private ID(String id) {
			this.id = id;
		}

		public String toString() {
			return id;
		}
	}
		
	/**
	 * Create a new transformation logging table description.
	 * It contains an empty list of log table fields.
	 * 
	 * @param databaseMeta
	 * @param schemaName
	 * @param tableName
	 */
	public ChannelLogTable(DatabaseMeta databaseMeta, String schemaName, String tableName) {
		super(databaseMeta, schemaName, tableName);
	}
	
	public ChannelLogTable() {
		this(null, null, null);
	}
	
	@Override
	public Object clone() {
		try {
			ChannelLogTable table = (ChannelLogTable) super.clone();
			table.fields = new ArrayList<LogTableField>();
			for (LogTableField field : this.fields) {
				table.fields.add((LogTableField) field.clone());
			}
			return table;
		}
		catch(CloneNotSupportedException e) {
			return null;
		}
	}

	public String getXML() {
		StringBuffer retval = new StringBuffer();

		retval.append(XMLHandler.openTag(XML_TAG));
        retval.append(XMLHandler.addTagValue("connection", databaseMeta==null ?  null  : databaseMeta.getName())); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        retval.append(XMLHandler.addTagValue("schema", schemaName)); //$NON-NLS-1$ //$NON-NLS-2$
        retval.append(XMLHandler.addTagValue("table", tableName)); //$NON-NLS-1$ //$NON-NLS-2$
		retval.append(super.getFieldsXML());
		retval.append(XMLHandler.closeTag(XML_TAG)).append(Const.CR);
		
		return retval.toString();
	}
	
	public void loadXML(Node node, List<DatabaseMeta> databases) {
		databaseMeta = DatabaseMeta.findDatabase(databases, XMLHandler.getTagValue(node, "connection"));
		schemaName = XMLHandler.getTagValue(node, "schema");
		tableName = XMLHandler.getTagValue(node, "table");
		
		for (int i=0;i<fields.size();i++) {
			LogTableField field = fields.get(i);
			Node fieldNode = XMLHandler.getSubNodeByNr(node, BaseLogTable.XML_TAG, i);
			field.setFieldName( XMLHandler.getTagValue(fieldNode, "name") );
			field.setEnabled( "Y".equalsIgnoreCase(XMLHandler.getTagValue(fieldNode, "enabled")) );
		}
	}

	public static ChannelLogTable getDefault() {
		ChannelLogTable table = new ChannelLogTable();
				
		table.fields.add( new LogTableField(ID.ID_BATCH.id, true, false, "ID_BATCH", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.IdBatch"), ValueMetaInterface.TYPE_INTEGER, 8) );
		table.fields.add( new LogTableField(ID.CHANNEL_ID.id, true, false, "CHANNEL_ID", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ChannelId"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.LOG_DATE.id, true, false, "LOG_DATE", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.BatchID"), ValueMetaInterface.TYPE_DATE, -1) );
		table.fields.add( new LogTableField(ID.LOGGING_OBJECT_TYPE.id, true, false, "LOGGING_OBJECT_TYPE", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ObjectType"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.OBJECT_NAME.id, true, false, "OBJECT_NAME", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ObjectName"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.OBJECT_COPY.id, true, false, "OBJECT_COPY", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ObjectCopy"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.REPOSITORY_DIRECTORY.id, true, false, "REPOSITORY_DIRECTORY", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.RepositoryDirectory"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.FILENAME.id, true, false, "FILENAME", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.Filename"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.OBJECT_ID.id, true, false, "OBJECT_ID", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ObjectId"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.OBJECT_REVISION.id, true, false, "OBJECT_REVISION", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ObjectRevision"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.PARENT_CHANNEL_ID.id, true, false, "PARENT_CHANNEL_ID", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.ParentChannelId"), ValueMetaInterface.TYPE_STRING, 255) );
		table.fields.add( new LogTableField(ID.ROOT_CHANNEL_ID.id, true, false, "ROOT_CHANNEL_ID", BaseMessages.getString(PKG, "ChannelLogTable.FieldDescription.RootChannelId"), ValueMetaInterface.TYPE_STRING, 255) );
		
		return table;
	}
		
	/**
	 * This method calculates all the values that are required
	 * @param id the id to use or -1 if no id is needed
	 * @param status the log status to use
	 */
	public RowMetaAndData getLogRecord(LogStatus status, Object subject) {
		if (subject==null || subject instanceof LoggingHierarchy) {
			
			LoggingHierarchy loggingHierarchy = (LoggingHierarchy) subject;
			LoggingObjectInterface loggingObject = null;
			if (subject!=null) loggingObject = loggingHierarchy.getLoggingObject();
			
			RowMetaAndData row = new RowMetaAndData();
			
			for (LogTableField field : fields) {
				if (field.isEnabled()) {
					Object value = null;
					if (subject!=null) {
						switch(ID.valueOf(field.getId())){
						
						case ID_BATCH : value = new Long(loggingHierarchy.getBatchId()); break;
						case CHANNEL_ID : value = loggingObject.getLogChannelId(); break;
						case LOG_DATE : value = new Date(); break;
						case LOGGING_OBJECT_TYPE : value = loggingObject.getObjectType().toString(); break;
						case OBJECT_NAME : value = loggingObject.getObjectName(); break;
						case OBJECT_COPY : value = loggingObject.getObjectCopy(); break;
						case REPOSITORY_DIRECTORY : value = loggingObject.getRepositoryDirectory()==null ? null : loggingObject.getRepositoryDirectory().getPath(); break;
						case FILENAME : value = loggingObject.getFilename(); break;
						case OBJECT_ID : value = loggingObject.getObjectId()==null ? null : loggingObject.getObjectId().toString(); break;
						case OBJECT_REVISION : value = loggingObject.getObjectRevision()==null ? null : loggingObject.getObjectRevision().toString(); break;
						case PARENT_CHANNEL_ID : value = loggingObject.getParent() == null ? null : loggingObject.getParent().getLogChannelId(); break;
						case ROOT_CHANNEL_ID : value = loggingHierarchy.getRootChannelId(); break;
						}
						
					}

					row.addValue(field.getFieldName(), field.getDataType(), value);
					row.getRowMeta().getValueMeta(row.size()-1).setLength(field.getLength());
				}
			}
				
			return row;
		}
		else {
			return null;
		}
	}

	public String getLogTableType() {
		return BaseMessages.getString(PKG, "ChannelLogTable.Type.Description");
	}

	
}
