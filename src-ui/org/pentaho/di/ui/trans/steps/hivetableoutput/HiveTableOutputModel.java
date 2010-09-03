package org.pentaho.di.ui.trans.steps.hivetableoutput;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.database.HiveDatabaseMeta;
import org.pentaho.di.trans.steps.hivetableoutput.HiveTableOutput;
import org.pentaho.di.trans.steps.hivetableoutput.HiveTableOutputMeta;
import org.pentaho.di.ui.core.PropsUI;
import org.pentaho.di.ui.core.gui.WindowProperty;
import org.pentaho.ui.xul.containers.XulDialog;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;

public class HiveTableOutputModel extends AbstractXulEventHandler {

  private static final Class<?> PKG = HiveTableOutput.class;

  //  public static final String STEP_NAME = "stepName"; //$NON-NLS-1$
  public static final String BLOCKING = "blocking"; //$NON-NLS-1$
  public static final String LOGGING_INTERVAL = "loggingInterval"; //$NON-NLS-1$
  public static final String CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
  public static final String TARGET_TABLE_NAME = "targetTableName"; //$NON-NLS-1$
  public static final String TRUNCATE_TABLE = "truncateTable"; //$NON-NLS-1$
  public static final String TABLE_NAME_IN_FIELD = "tableNameInField"; //$NON-NLS-1$
  public static final String FIELD_WITH_TABLE_NAME = "fieldWithTableName"; //$NON-NLS-1$
  public static final String STORE_TABLE_NAME= "storeTableName"; //$NON-NLS-1$
  public static final String FIELDS_TO_COLUMNS = "fieldsToColumns";
  
  //  private String stepName;
  private String connectionName;
  private boolean blocking;
  private int loggingInterval = 60;
  private String targetTableName; 
  private boolean truncateTable;
  private ArrayList<FieldToColumn> fieldsToColumns;
  private boolean tableNameInField;
  private String fieldWithTableName; 
  private boolean storeTableName; 
  private Shell shell;
  private HiveTableOutputMeta hiveTableOutputMeta;
  private HiveDatabaseMeta hiveDatabaseMeta;
  private DatabaseMeta databaseMeta;

  public void initFields() {
	  
    if (hiveTableOutputMeta != null) {
      // common/simple
      setName(hiveTableOutputMeta.getName());
      setConnectionName(hiveTableOutputMeta.getDatabaseMeta().getDatabaseInterface().getName());
      setTruncateTable(hiveTableOutputMeta.getTruncateTable());
      setTargetTableName(hiveTableOutputMeta.getTargetTableName());
      setTableNameInField(hiveTableOutputMeta.getTableNameInField());
      setFieldWithTableName(hiveTableOutputMeta.getFieldWithTableName());
      setStoreTableName(hiveTableOutputMeta.getStoreTableName());   
      setFieldToColumns(hiveTableOutputMeta.getFieldStream(), hiveTableOutputMeta.getFieldDatabase());
      setDatabaseMeta(hiveTableOutputMeta.getDatabaseMeta());
    }
  }
  
/*   public void setShell(Shell shell) {
      this.shell = shell;
   }
*/  
   public void cancel() {
      XulDialog xulDialog = (XulDialog) getXulDomContainer().getDocumentRoot().getRootElement();
      Shell shell = (Shell) xulDialog.getRootObject();
      if (!shell.isDisposed()) {
         WindowProperty winprop = new WindowProperty(shell);
         PropsUI.getInstance().setScreen(winprop);
         ((Composite) xulDialog.getManagedObject()).dispose();
          shell.dispose();
      }
   }
  
   public void saveMeta() {
	   hiveTableOutputMeta.setChanged(true);
	   hiveTableOutputMeta.setLoggingInterval(getLoggingInterval());
	   hiveTableOutputMeta.setTargetTableName(getTargetTableName());
	   hiveTableOutputMeta.setTruncateTable(getTruncateTable());
	   hiveTableOutputMeta.setTableNameInField(getTableNameInField());
	   hiveTableOutputMeta.setFieldWithTableName(getFieldWithTableName());
	   hiveTableOutputMeta.setStoreTableName(getStoreTableName());
	   hiveTableOutputMeta.setFieldStream(getFieldsFromFieldToColumns());
	   hiveTableOutputMeta.setFieldDatabase(getColumnsFromFieldToColumns());
	   hiveTableOutputMeta.setDatabaseMeta(getDatabaseMeta());
   }
    
   protected void addNewFieldRow() {}
  
   @Override
   public String getName() {
      return "controller"; //$NON-NLS-1$
   }

   public HiveTableOutputMeta getStepMeta() {
     return hiveTableOutputMeta;
   }

   public void setStepMeta(HiveTableOutputMeta hiveTableOutputMeta) {
     this.hiveTableOutputMeta = hiveTableOutputMeta;
     this.setDatabaseMeta(hiveTableOutputMeta.getDatabaseMeta());
     this.setHiveDatabseMeta((HiveDatabaseMeta)hiveTableOutputMeta.getDatabaseMeta().getDatabaseInterface());
     initFields();
   } 

   public void setBlocking(boolean blocking) {
      boolean previousValue = this.blocking; 
      this.blocking = blocking;
      firePropertyChange(HiveTableOutputModel.BLOCKING, previousValue, blocking);
   }
  
   public boolean getBlocking() {
	   return blocking;
   }

   public int getLoggingInterval() {
      return loggingInterval;
   }

   public void setLoggingInterval(int loggingInterval) {
      int previousValue = this.loggingInterval;
      this.loggingInterval = loggingInterval;
      firePropertyChange(HiveTableOutputModel.LOGGING_INTERVAL, previousValue, loggingInterval);
   }
  
   public void setTargetTableName(String targetTableName) {
      String previousValue = this.targetTableName;
      this.targetTableName = targetTableName ;
	   firePropertyChange(HiveTableOutputModel.TARGET_TABLE_NAME, previousValue, targetTableName);
   }
  
   public String getTargetTableName() {
	   return targetTableName; 
   }
  
   public void setTruncateTable(boolean truncateTable) {
      boolean previousValue = this.truncateTable;
	   this.truncateTable = truncateTable;
	   firePropertyChange(HiveTableOutputModel.TRUNCATE_TABLE, previousValue, truncateTable);
   }
  
   public boolean getTruncateTable() {
	   return truncateTable; 
   }
  
   public void setFieldToColumns(String[] fieldNames, String[] columnNames) {     
      ArrayList<FieldToColumn> fieldsToColumns = new ArrayList<FieldToColumn>();
      for(int i = 0; i< fieldNames.length; i++) {
         fieldsToColumns.add(new FieldToColumn(fieldNames[i], columnNames[i]));
      }
     
      setFieldsToColumns(fieldsToColumns);
   }
  
   public void setFieldsToColumns(ArrayList<FieldToColumn> fieldsToColumns) {
	   this.fieldsToColumns = fieldsToColumns;
	   firePropertyChange(HiveTableOutputModel.FIELDS_TO_COLUMNS, null, fieldsToColumns);
   }
  
   public String[] getFieldsFromFieldToColumns() {
      if (fieldsToColumns != null) {
         int numberOfFieldToColumns = fieldsToColumns.size();
         String[] fieldNames = new String[numberOfFieldToColumns];
         for(int i = 0; i < numberOfFieldToColumns; i++) {
            fieldNames[i] = fieldsToColumns.get(i).getFieldName();
         }
         return fieldNames;
      }
      else {
         return new String[0];
      }
   }
  
   public String[] getColumnsFromFieldToColumns() {
      if (fieldsToColumns != null ) {
      int numberOfFieldToColumns = fieldsToColumns.size();
         String[] columnNames = new String[numberOfFieldToColumns];
         for(int i = 0; i < numberOfFieldToColumns; i++) {
            columnNames[i] = fieldsToColumns.get(i).getColumnName();
         }
        
         return columnNames;
      }
      else {
         return new String[0];
      }
   }
  
   public void removeAllFieldsToColumns() {
      fieldsToColumns = new ArrayList<FieldToColumn>();
      firePropertyChange(HiveTableOutputModel.FIELDS_TO_COLUMNS, null, fieldsToColumns);
   }
  
   public ArrayList<FieldToColumn> getFieldsToColumns() {
	   return fieldsToColumns;
   }
  
   public void setTableNameInField(boolean tableNameInField) {
      boolean previousValue = this.tableNameInField;
	   this.tableNameInField = tableNameInField; 
	   firePropertyChange(HiveTableOutputModel.TABLE_NAME_IN_FIELD, previousValue, tableNameInField);
   }
  
   public boolean getTableNameInField() {
	  return tableNameInField; 
   }
  
   public void setFieldWithTableName(String fieldWithTableName) {
      String previousValue = this.fieldWithTableName;
      this.fieldWithTableName = fieldWithTableName;
      firePropertyChange(HiveTableOutputModel.FIELD_WITH_TABLE_NAME, previousValue, fieldWithTableName);
   }
  
   public String getFieldWithTableName() {
	   return fieldWithTableName; 
   }
   
   public void setStoreTableName(boolean storeTableName) {
      boolean previousValue = this.storeTableName;
	   this.storeTableName = storeTableName; 
	   firePropertyChange(HiveTableOutputModel.STORE_TABLE_NAME, previousValue, storeTableName);
   }
  
   public boolean getStoreTableName() {
	   return storeTableName; 
   }
   
   public void setConnectionName(String connectionName) {
      String previousValue = this.connectionName;
      this.connectionName = connectionName;
      firePropertyChange(HiveTableOutputModel.CONNECTION_NAME, previousValue, storeTableName);
   }
   
   public String getConnectionName() {
      return connectionName;
   }
   
   public void setHiveDatabseMeta(HiveDatabaseMeta hiveDatabaseMeta) {
      this.hiveDatabaseMeta = hiveDatabaseMeta;
   }
   
   public HiveDatabaseMeta getHiveDatabaseMeta() {
      return hiveDatabaseMeta;
   }
   
   public void setDatabaseMeta(DatabaseMeta databaseMeta) {
      DatabaseMeta previousValue = this.databaseMeta;
      this.databaseMeta = databaseMeta;
      firePropertyChange(HiveTableOutputModel.STORE_TABLE_NAME, previousValue, databaseMeta);
   }
   
   public DatabaseMeta getDatabaseMeta() {
      return databaseMeta;
   }
   
}