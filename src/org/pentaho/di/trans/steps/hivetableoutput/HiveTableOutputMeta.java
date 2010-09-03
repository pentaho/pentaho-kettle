package org.pentaho.di.trans.steps.hivetableoutput;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.pentaho.di.core.Const;
import org.pentaho.di.core.Counter;
import org.pentaho.di.core.SQLStatement;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.HiveDatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.shared.SharedObjectInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputMeta;
import org.w3c.dom.Node;


public class HiveTableOutputMeta 
       extends TextFileOutputMeta 
       implements StepMetaInterface {

	private DatabaseMeta databaseMeta;
	private String targetTableName;
	private boolean blocking;
	private int loggingInterval; 
	private boolean truncateTable; 
	private boolean tableNameInField;
	private String fieldWithTableName; 
	private boolean storeTableName;
   private String[] fieldStream;
   private String[] fieldDatabase;
	  
	private static boolean IS_BLOCKING = true;
	
	private static Class<?> CLZ = HiveTableOutput.class; // for i18n purposes, needed by Translator2!!   $NON-NLS-1$
	
	public HiveTableOutputMeta() {
      fieldStream   = new String[0];
      fieldDatabase = new String[0];
      databaseMeta = new DatabaseMeta();
	}
	
	@Override
	public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta, Trans trans) {
		return new HiveTableOutput(stepMeta, stepDataInterface, cnr, transMeta, trans);
	}

	@Override
	public StepDataInterface getStepData() {
		return new HiveTableOutputData();
	}
	
	@Override
   public void setDefault() {
	   super.setDefault();
	   //TODO: change to a java temp file name
      setFileName("file:///home/sflatley/datafile");
      setExtension("txt");
      setSeparator("\t");
      setEnclosure("\"");
      setEnclosureForced(false);
      setEnclosureFixDisabled(false);     
      setFileFormat(Const.getOS().toLowerCase(Locale.ROOT).startsWith("WINDOWS")?"DOS":"Unix");
      setFooterEnabled(false);
      setHeaderEnabled(false);
      setFileNameInField(false);
      setFieldWithTableName("");
      setTargetTableName("");
	}
	
	  public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String, Counter> counters)
     throws KettleXMLException
  {
     readData(stepnode, databases);
  }
	
	public void readData(Node stepnode, List<? extends SharedObjectInterface> databases) throws KettleXMLException {
		try {
			super.readData(stepnode);
			String con     = XMLHandler.getTagValue(stepnode, "connection");
         databaseMeta   = DatabaseMeta.findDatabase(databases, con);
	      truncateTable = ("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "truncateTable"))?true:false);
	      targetTableName = XMLHandler.getTagValue(stepnode, "targetTableName");
	      tableNameInField = ("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "tableNameInField"))?true:false);
	      fieldWithTableName = XMLHandler.getTagValue(stepnode, "fieldWithTableName");
	      storeTableName = ("Y".equalsIgnoreCase(XMLHandler.getTagValue(stepnode, "storeTableName"))?true:false);

	      Node fields = XMLHandler.getSubNode(stepnode, "hiveFields");   //$NON-NLS-1$
	      int nrRows  = XMLHandler.countNodes(fields, "field");      //$NON-NLS-1$
	      allocateHiveFields(nrRows);
	         
	      for (int i=0;i<nrRows;i++) {
            Node knode = XMLHandler.getSubNodeByNr(fields, "field", i);         //$NON-NLS-1$
	         fieldDatabase   [i] = XMLHandler.getTagValue(knode, "column_name");  //$NON-NLS-1$
	         fieldStream     [i] = XMLHandler.getTagValue(knode, "stream_name"); //$NON-NLS-1$
	      }
		}
		catch (KettleException ke) {
			throw new KettleXMLException("Unable to load step info from XML", ke);
		}
		
	}
	
	
   public void allocateHiveFields(int nrfields) {
      fieldStream   = new String[nrfields];
      fieldDatabase = new String[nrfields];
	}
	
	@Override
	public String getXML() {
		StringBuffer retval = new StringBuffer();
		retval.append("    "+XMLHandler.addTagValue("connection", databaseMeta==null?"":databaseMeta.getName()));
		retval.append("    ").append(XMLHandler.addTagValue("truncateTable", truncateTable));
		retval.append("    ").append(XMLHandler.addTagValue("targetTableName", targetTableName));
		retval.append("    ").append(XMLHandler.addTagValue("tableNameInField", tableNameInField));
		retval.append("    ").append(XMLHandler.addTagValue("fieldWithTableName", fieldWithTableName));
		retval.append("    ").append(XMLHandler.addTagValue("storeTableName", storeTableName));
      retval.append("    <hiveFields>").append(Const.CR); //$NON-NLS-1$
		for (int i=0;i<fieldDatabase.length;i++) {
	         retval.append("        <field>").append(Const.CR); //$NON-NLS-1$
	         retval.append("          ").append(XMLHandler.addTagValue("column_name", fieldDatabase[i])); //$NON-NLS-1$ //$NON-NLS-2$
	         retval.append("          ").append(XMLHandler.addTagValue("stream_name", fieldStream[i])); //$NON-NLS-1$ //$NON-NLS-2$
	         retval.append("        </field>").append(Const.CR); //$NON-NLS-1$
	      }
      retval.append("    </hiveFields>").append(Const.CR); //$NON-NLS-1$
		
		return super.getXML()+retval.toString();
	}
	
	public void readRep(Repository rep, ObjectId id_step, List<DatabaseMeta> databases, Map<String, Counter> counters)
			throws KettleException {
		
		super.readRep(rep, id_step, databases, counters);
		databaseMeta = rep.loadDatabaseMetaFromStepAttribute(id_step, "id_connection", databases);  //$NON-NLS-1$
		truncateTable =  ("Y".equalsIgnoreCase(rep.getStepAttributeString (id_step, "truncateTable"))?true:false);
		targetTableName =  rep.getStepAttributeString (id_step, "targetTableName");
		tableNameInField =  ("Y".equalsIgnoreCase(rep.getStepAttributeString (id_step, "tableNameInField"))?true:false);
		fieldWithTableName =  rep.getStepAttributeString (id_step, "fieldWithTableName");
		storeTableName =  ("Y".equalsIgnoreCase(rep.getStepAttributeString (id_step, "storeTableName"))?true:false);

      int nrCols    = rep.countNrStepAttributes(id_step, "column_name"); //$NON-NLS-1$
      int nrStreams = rep.countNrStepAttributes(id_step, "stream_name"); //$NON-NLS-1$
      
      int nrRows = (nrCols < nrStreams ? nrStreams : nrCols);
      allocate(nrRows);
         
      for (int idx=0; idx < nrRows; idx++) {
         fieldDatabase[idx] = Const.NVL(rep.getStepAttributeString(id_step, idx, "column_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
         fieldStream[idx]   = Const.NVL(rep.getStepAttributeString(id_step, idx, "stream_name"), ""); //$NON-NLS-1$ //$NON-NLS-2$
      }
	}
	
	@Override
	public void saveRep(Repository rep, ObjectId id_transformation, ObjectId id_step)
	throws KettleException {
		try {
			super.saveRep(rep, id_transformation, id_step);
			rep.saveDatabaseMetaStepAttribute(id_transformation, id_step, "id_connection", databaseMeta);
			rep.saveStepAttribute(id_transformation, id_step, "truncateTable", (truncateTable==true?"Y":"N"));
			rep.saveStepAttribute(id_transformation, id_step, "targetTableName", targetTableName);
			rep.saveStepAttribute(id_transformation, id_step, "tableNameInField", (tableNameInField==true?"Y":"N"));
			rep.saveStepAttribute(id_transformation, id_step, "fieldWithTableName", fieldWithTableName);
			rep.saveStepAttribute(id_transformation, id_step, "storeTableName", (storeTableName==true?"Y":"N"));			

         int nrRows = (fieldDatabase.length < fieldStream.length ? fieldStream.length:fieldDatabase.length);
         for (int idx=0; idx < nrRows; idx++) {
            String columnName = (idx < fieldDatabase.length ? fieldDatabase[idx] : "");
            String streamName = (idx < fieldStream.length   ? fieldStream[idx] : "");
            rep.saveStepAttribute(id_transformation, id_step, idx, "column_name", columnName); //$NON-NLS-1$
            rep.saveStepAttribute(id_transformation, id_step, idx, "stream_name", streamName); //$NON-NLS-1$
         }
		}
		catch(Exception e)
		{
			throw new KettleException("Unable to save step information to the repository for id_step="+id_step, e);
		}
	}
	
	@Override
	public Object clone() {
	   
	   HiveTableOutputMeta retval = (HiveTableOutputMeta)super.clone();
		
      int nrStream = fieldStream.length;
	   int nrDatabase = fieldDatabase.length;

	   retval.fieldStream   = new String[nrStream];
	   retval.fieldDatabase = new String[nrDatabase];

	   for (int i = 0; i < nrStream; i++) {
         retval.fieldStream[i] = fieldStream[i];
	   }
	      
	   for (int i = 0; i < nrDatabase; i++) {
	         retval.fieldDatabase[i] = fieldDatabase[i];
      }  
	      
	   return retval;
	}
	
	/**
	 * Sets the database meta.
	 * @param DatbaseMeta
	 */
	public void setDatabaseMeta(DatabaseMeta databaseMeta) {
		this.databaseMeta = databaseMeta;
	}
	
	/**
	 * @return DatabaseMeta database metadata
	 */
	public DatabaseMeta getDatabaseMeta() {
		return this.databaseMeta;
	}
	
	public void setBlocking(boolean blocking) {
	   this.blocking = blocking;
	}

	public int getLoggingInterval() {
		return loggingInterval;
	}

	public void setLoggingInterval(int loggingInterval) {
		this.loggingInterval = loggingInterval;
	}
		  
	public void setHiveHostName(String hostName) {
		databaseMeta.setHostname(hostName);
		databaseMeta.setServername(hostName);
	}
		  
	public String getHiveHostName() {
		return databaseMeta.getHostname();
  	}
  
   public void setHivePort(String port) {
		databaseMeta.setDBPort(port);
  	}
  
  	public String getHivePort() {
		return databaseMeta.getDatabasePortNumberString();
  	}
  	
  	public void setHiveUsername(String username) {
		databaseMeta.setUsername(username);
  	}
  
  	public String getHiveUsername() {
		return databaseMeta.getUsername();
  	}
  
  	public void setHivePassword(String password) {

		databaseMeta.setPassword(password);
  	}
  
  	public String getHivePassword() {
		return databaseMeta.getPassword();
  	}
  
  	public void setHiveDatabase(String databaseName) {
		databaseMeta.setDBName(databaseName);
  	}
  
  	public String getHiveDatabase() {
		return databaseMeta.getDatabaseName();
  	}
  
  	public void setTargetTableName(String targetTableName) {
		this.targetTableName = targetTableName; 
  	}
  
  	public String getTargetTableName() {
		return targetTableName; 
  	}
  
  	public void setTruncateTable(boolean truncateTable) {
		this.truncateTable = truncateTable; 
 	}
  
  	public boolean getTruncateTable() {
		return truncateTable; 
  	}

  	public void setTableNameInField(boolean tableNameInField) {
		this.tableNameInField = tableNameInField; 
  	}
  
  	public boolean getTableNameInField() {
		return tableNameInField; 
  	}
  
  	public void setFieldWithTableName(String fieldWithTableName) {
		this.fieldWithTableName = fieldWithTableName; 
  	}
  
	public String getFieldWithTableName() {
		return fieldWithTableName; 
	}
	   
	public void setStoreTableName(boolean storeTableName) {
		this.storeTableName = storeTableName; 
	}
	  
	public boolean getStoreTableName() {
		return storeTableName; 
	}
	
   /**
    * @return Fields containing the values in the input stream to insert.
    */
   public String[] getFieldStream() {
       return fieldStream;
   }
   
   /**
    * @param fieldStream The fields containing the values in the input stream to insert in the table.
    */
   public void setFieldStream(String[] fieldStream) {
       this.fieldStream = fieldStream;
   }

   /**
    * @return Fields containing the fieldnames in the database insert.
    */
   public String[] getFieldDatabase() {
       return fieldDatabase;
   }
   
   /**
    * @param fieldDatabase The fields containing the names of the fields to insert.
    */
   public void setFieldDatabase(String[] fieldDatabase) {
       this.fieldDatabase = fieldDatabase;
   }
      
   public void setDatabaseMeta(String hostName, String databasePort, String databaseName, 
                               String username,  String password) {
   }	
   
   public SQLStatement getSQLStatements(TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev, String targetTableName)
   {
      SQLStatement retval = new SQLStatement(stepMeta.getName(), databaseMeta, null); // default: nothing to do!
   
      if (databaseMeta!=null)
      {
         if (prev!=null && prev.size()>0)
         {
            if (!Const.isEmpty(targetTableName))
            {
               Database db = new Database(loggingObject, databaseMeta);
               db.shareVariablesWith(transMeta);
               try
               {
                  db.connect();
                  
                        String schemaTable = databaseMeta.getQuotedSchemaTableCombination("", targetTableName);
                        String cr_table = db.getDDL(schemaTable, prev);
                  
                  // Empty string means: nothing to do: set it to null...
                  if (cr_table==null || cr_table.length()==0) cr_table=null;
                  
                  retval.setSQL(cr_table);
               }
               catch(KettleDatabaseException dbe)
               {
                  retval.setError(BaseMessages.getString(CLZ, "TableOutputMeta.Error.ErrorConnecting", dbe.getMessage()));
               }
               finally
               {
                  db.disconnect();
               }
            }
            else
            {
               retval.setError(BaseMessages.getString(CLZ, "TableOutputMeta.Error.NoTable"));
            }
         }
         else
         {
            retval.setError(BaseMessages.getString(CLZ, "TableOutputMeta.Error.NoInput"));
         }
      }
      else
      {
         retval.setError(BaseMessages.getString(CLZ, "TableOutputMeta.Error.NoConnection"));
      }

      return retval;
   }
   
}
	  
