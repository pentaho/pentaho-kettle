package org.pentaho.di.core.database;

import junit.framework.TestCase;

import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.trans.TransMeta;

public class TablespaceDDLFragmentTest extends TestCase {

   public static final String h2DatabaseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "<connection>" +
           "<name>H2</name>" +
           "<server>127.0.0.1</server>" +
           "<type>H2</type>" +
           "<access>Native</access>" + 
           "<database>mem:db</database>" +
           "<port></port>" +
           "<username>sa</username>" +
           "<password></password>" +
         "</connection>";
         
   public static final String MySQLDatabaseXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "<connection>" +
           "<name>MySQL</name>" +
           "<server>127.0.0.1</server>" +
           "<type>MySQL</type>" +
           "<access></access>" + 
           "<database>test</database>" +
           "<port>3306</port>" +
           "<username>sa</username>" +
           "<password></password>" +
         "</connection>";

   
   public static final String OracleDatabaseXMLWithoutTablespaces = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
         "<connection>" +
           "<name>Oracle</name>" +
           "<server>127.0.0.1</server>" +
           "<type>Oracle</type>" +
           "<access>Native</access>" + 
           "<database>test</database>" +
           "<port>1024</port>" +
           "<username>scott</username>" +
           "<password>tiger</password>" +
         "</connection>";
   
   public static final String OracleDatabaseXMLWithTablespacesAsValues = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
   "<connection>" +
     "<name>Oracle</name>" +
     "<server>127.0.0.1</server>" +
     "<type>Oracle</type>" +
     "<access>Native</access>" + 
     "<database>test</database>" +
     "<port>1024</port>" +
     "<username>scott</username>" +
     "<password>tiger</password>" +
     "<data_tablespace>TABLES</data_tablespace>" +
     "<index_tablespace>INDEXES</index_tablespace>" +
   "</connection>";
   
    public static final String OracleDatabaseXMLWithTablespacesAsVariables = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
          "<connection>" +
            "<name>Oracle</name>" +
            "<server>127.0.0.1</server>" +
            "<type>Oracle</type>" +
            "<access>Native</access>" + 
            "<database>test</database>" +
            "<port>1024</port>" +
            "<username>scott</username>" +
            "<password>tiger</password>" +
            "<data_tablespace>${TablespaceDDLFragmentTest_DATA_TABLESPACE_1}</data_tablespace>" +
            "<index_tablespace>${TablespaceDDLFragmentTest_INDEX_TABLESPACE_1}</index_tablespace>" +
          "</connection>";
    
   
   @Test
   public void testOracleDatabase() {
       try {
          
           //   keep in mind that this execution will read a kettle.properties file
           KettleEnvironment.init();          
           TransMeta transMeta = new TransMeta();
           
           // set up variables to be used for tablespace specification
           // the variables specified should not be specified in the kettle.proeprties file
           
           //  these should not have quotes generated arounf them          
           transMeta.setVariable("TablespaceDDLFragmentTest_DATA_TABLESPACE_1", "TABLES");
           transMeta.setVariable("TablespaceDDLFragmentTest_INDEX_TABLESPACE_1", "INDEXES");
           
           //  these do have quotes generated around them
           transMeta.setVariable("TablespaceDDLFragmentTest_DATA_TABLESPACE_2", "TABLE");
           transMeta.setVariable("TablespaceDDLFragmentTest_INDEX_TABLESPACE_2", "INDEX");           
           
           String ddlFragment = "";
           
           //  test without tablespaces not specified
           DatabaseMeta databaseMeta = new DatabaseMeta(OracleDatabaseXMLWithoutTablespaces);

           ddlFragment = databaseMeta.getDatabaseInterface().getDataTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "");

           ddlFragment = databaseMeta.getDatabaseInterface().getIndexTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "");
           
           //  test with tablespaces specified by value
           databaseMeta = new DatabaseMeta(OracleDatabaseXMLWithTablespacesAsValues);
           
           ddlFragment = databaseMeta.getDatabaseInterface().getDataTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "TABLESPACE TABLES");

           ddlFragment = databaseMeta.getDatabaseInterface().getIndexTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "TABLESPACE INDEXES");
           
           //  test with tablespaces specified as variables: TEST CASE 1
           databaseMeta = new DatabaseMeta(OracleDatabaseXMLWithTablespacesAsVariables);
           
           ddlFragment = databaseMeta.getDatabaseInterface().getDataTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "TABLESPACE TABLES");

           ddlFragment = databaseMeta.getDatabaseInterface().getIndexTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "TABLESPACE INDEXES");
       }
       catch (Exception e ) {
           e.printStackTrace();
       }
   }
   
   @Test
   public void testMySQLDatabase() {
       try {
           KettleEnvironment.init();          
           TransMeta transMeta = new TransMeta();
           DatabaseMeta databaseMeta = new DatabaseMeta(MySQLDatabaseXML);
           transMeta.setVariable("TablespaceDDLFragmentTest_DATA_TABLESPACE", "TABLES");
           transMeta.setVariable("TablespaceDDLFragmentTest_INDEX_TABLESPACE", "INDEXES");
           String ddlFragment = "";
           
           ddlFragment = databaseMeta.getDatabaseInterface().getDataTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "");

           ddlFragment = databaseMeta.getDatabaseInterface().getIndexTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "");
       }
       catch (Exception e ) {
          e.printStackTrace();
      }
   }
   
   @Test
   public void testH2Database() {
       try {
           KettleEnvironment.init();          
           TransMeta transMeta = new TransMeta();
           DatabaseMeta databaseMeta = new DatabaseMeta(h2DatabaseXML);
           transMeta.setVariable("TablespaceDDLFragmentTest_DATA_TABLESPACE", "TABLES");
           transMeta.setVariable("TablespaceDDLFragmentTest_INDEX_TABLESPACE", "INDEXES");
           String ddlFragment = "";
           
           ddlFragment = databaseMeta.getDatabaseInterface().getDataTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "");

           ddlFragment = databaseMeta.getDatabaseInterface().getIndexTablespaceDDL(transMeta, databaseMeta);
           assertEquals(ddlFragment, "");
       }
       catch (Exception e ) {
          e.printStackTrace();
      }
   }
}
