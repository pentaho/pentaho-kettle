package org.pentaho.di.trans.steps.hivetableoutput;

import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.textfileoutput.TextFileOutputData;

public class HiveTableOutputData 
       extends TextFileOutputData
       implements StepDataInterface {

   public Database db;
   public DatabaseMeta databaseMeta;
   public String targetTableName;
   public String hostName;
   public String port;
   public boolean blocking;
   public int loggingInterval;
   public String username; 
   public String password;
   public String database;  
   public boolean truncateTable; 
   public boolean tableNameInField;
   public String fieldWithTableName; 
   public boolean storeTableNameInField;

   public HiveTableOutputData() {
      super();
      
   }
}
