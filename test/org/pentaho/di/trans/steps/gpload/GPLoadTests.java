package org.pentaho.di.trans.steps.gpload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.rowgenerator.RowGeneratorMeta;

/**
 * JUnit test for GPLoad step.
 * 
 * These test will verify that correct YAML is being generated. 
 * YAML contains the specifics of a GPLoad job.
 * 
 * @author sflatley
 * 
 */
public class GPLoadTests {

   //  Target database objects
   private final static String TARGET_TABLE = "customers_100";
   private final static String GPLOAD_ERROR_TABLE = "err_customers_100";
   
   //  YAML location and files
   private final static String YAML_TEST_FILE_LOCATION = "testfiles";
   
   private File testDirectory = null;
   
   //  Data files
   private final static String INSERT_DATA_FILE = "customers-100.dat";
   private final static String UPDATE_DATA_FILE = "customers-update.dat";
   private final static String MERGE_DATA_FILE = "customers-merge.dat";
   
   
   //  For internationalization
   private static Class<?> PKG = GPLoadMeta.class; // for i18n purposes, needed
                                                   // by Translator2!!
                                                   // $NON-NLS-1$

   public static final String GREENPLUM_DATABASE_CONNECTION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
         + "<connection>"
         + "<name>foodmartOnGreenplum</name>"
         + "<server>10.100.2.42</server>"
         + "<type>Greenplum</type>"
         + "<access>Native</access>"
         + "<database>foodmart</database>"
         + "<port>5432</port>"
         + "<username>gpadmin</username>"
         + "<password>doesntmatter</password>" + "</connection>";

   /**
    * Initialize the test environment.
    */
   @Before
   public void init() {

      // initialize testDirectory
      testDirectory = new File(GPLoadTests.YAML_TEST_FILE_LOCATION);
      if (!testDirectory.isDirectory()) {
         fail(GPLoadTests.YAML_TEST_FILE_LOCATION + " does not exist.");
      }

      // initialize the Kettle environment
      try {
         KettleEnvironment.init();
      } catch (KettleException ke) {
         fail(ke.getMessage());
      }
   }

   /**
    * Returns the contents of the passed file name
    * 
    * @param filename
    * @return String the content of filename which is located in
    *         GPLoadTests.YAML_TEST_FILE_LOCATION.
    */
   private String getYamlFileContents(String filename) {

      StringBuilder sbFileContents = new StringBuilder();
      try {
         // create a buffered reader
         BufferedReader bufferedReader = new BufferedReader(new FileReader(
               new File(testDirectory.getAbsolutePath() + "/" + filename)));
         String lineFromFile = null;

         // read each line and append it with a 
         // carriage return to our string builder
         while ((lineFromFile = bufferedReader.readLine()) != null) {
            sbFileContents.append(lineFromFile).append(Const.CR);
         }
      } catch (FileNotFoundException fnfe) {
         fail(fnfe.getMessage());
      } catch (IOException ioe) {
         fail(ioe.getMessage());
      }

      // convert to string and return
      return sbFileContents.toString();
   }

   /**
    * Creates a transformation with a row generator step and 
    * hopped to a GPLoadStep with the passed name.
    * 
    * @param gpLoadStepname The name of the GPLoad step.
    * 
    * @throws KettleException
    */
   public TransMeta createTransformationMeta(String gpLoadStepname)
         throws Exception {

      // Create a new transformation...
      TransMeta transMeta = new TransMeta();
      transMeta.setName("row generatortest");

      // Add a database connection to the trans meta
      transMeta.addDatabase(new DatabaseMeta(GREENPLUM_DATABASE_CONNECTION));

      // get a reference to the plugin registry
      PluginRegistry registry = PluginRegistry.getInstance();
      if (registry == null) {
         throw new Exception("Plugin registry is null.  Make sure that the Kettle environment was initialized.");
      }

      // create a row generator step...
      //
      // TODO: Either remove the row generator step if it is not used 
      //       or, if it is to be used,  change it's field data to that 
      //       of the customers-100.txt file.
      //       The rowGenerator is not used since the transformaton is not run.
      
      String rowGeneratorStepname = "row generator step";
      RowGeneratorMeta rowGeneratorMeta = new RowGeneratorMeta();

      // set the information of the row generator.
      String rowGeneratorPid = registry.getPluginId(StepPluginType.class,
            rowGeneratorMeta);
      StepMeta rowGeneratorStep = new StepMeta(rowGeneratorPid,
            rowGeneratorStepname, (StepMetaInterface) rowGeneratorMeta);
      transMeta.addStep(rowGeneratorStep);

      // add three fields and related information to arrays
      String fieldName[] = { "string", "boolean", "integer" };
      String type[] = { "String", "Boolean", "Integer" };
      String value[] = { "string_value", "true", "20" };
      String fieldFormat[] = { "", "", "" };
      String group[] = { "", "", "" };
      String decimal[] = { "", "", "" };
      int intDummies[] = { -1, -1, -1 };

      // set row generator meta properties
      rowGeneratorMeta.setDefault();
      rowGeneratorMeta.setFieldName(fieldName);
      rowGeneratorMeta.setFieldType(type);
      rowGeneratorMeta.setValue(value);
      rowGeneratorMeta.setFieldLength(intDummies);
      rowGeneratorMeta.setFieldPrecision(intDummies);
      rowGeneratorMeta.setRowLimit("3");
      rowGeneratorMeta.setFieldFormat(fieldFormat);
      rowGeneratorMeta.setGroup(group);
      rowGeneratorMeta.setDecimal(decimal);

      // create the GPLoad step
      GPLoadMeta gpLoadMeta = new GPLoadMeta();
      String dummyPid = registry.getPluginId(StepPluginType.class, gpLoadMeta);
      StepMeta gpLoadStepMeta = new StepMeta(dummyPid, gpLoadStepname, (StepMetaInterface) gpLoadMeta);
      transMeta.addStep(gpLoadStepMeta);

      // hop the row generator to the GPLoad step
      TransHopMeta rowGeneratorGPLoadHop = new TransHopMeta(rowGeneratorStep, gpLoadStepMeta);
      transMeta.addTransHop(rowGeneratorGPLoadHop);

      // We now have a transformation meta that had a
      // RowGenerator generating three rows and three fields
      // hopped to a GPLoad step.
      return transMeta;

   }
   
  
   /**
    * Creates and returns a GPLoadMeta.
    * 
    * @param gpLoadStepname
    * @param action
    * @param targetTableName
    * @param errorTableName
    * @param dataFilename
    * @param delimiter
    * @param logFilename
    * @param[] tableColumns
    * @param matchColumns
    * @param updateColumns
    */
   private GPLoadMeta createGPLoadMeta(String gpLoadStepname,
                                       String action,
                                       String targetTableName,
                                       String errorTableName,
                                       String dataFilename,
                                       String delimiter,
                                       String logFilename,
                                       String[] tableColumn,
                                       boolean[] matchColumn, 
                                       boolean[] updateColumn)
               throws Exception {
      
      // create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);

      // get a reference to the GPLoad step meta and then it's
      StepMeta gpLoadStepMeta = transMeta.getStep(1);
      GPLoadMeta gpLoadMeta = (GPLoadMeta) gpLoadStepMeta.getStepMetaInterface();

      // setDefault is called from Spoon.newStep if we were creating a new step using the interface.
      gpLoadMeta.setDefault();
      
      // set properties
      gpLoadMeta.setTableName(GPLoadTests.TARGET_TABLE);
      gpLoadMeta.setErrorTableName(GPLoadTests.GPLOAD_ERROR_TABLE);
      gpLoadMeta.setLoadAction(action);
      gpLoadMeta.setDataFile(dataFilename);
      
      //  TODO:  Make sure that we use the delimiter parameter
      gpLoadMeta.setDelimiter(";");
      gpLoadMeta.setFieldTable(tableColumn);
      gpLoadMeta.setMatchColumns(matchColumn);
      gpLoadMeta.setUpdateColumn(updateColumn);
      gpLoadMeta.setLogFile(logFilename);

      // set the database meta
      gpLoadMeta.setDatabaseMeta(transMeta.getDatabase(0));
      
      return gpLoadMeta;
   }
   
   
   /**
    * Tests the YAML contents generated by the GPLoad step in the transformation.
    * 
    * @param GPLoadMeta 
    * @param TransMeta
    * @param expectedYAMLFilename The file name of the YAML file whose contents should match the YAML generated 
    *        by the GPLoad step. 
    */
   private void testYAMLContents(GPLoadMeta gpLoadMeta,
                         TransMeta transMeta,
                         String expectedYAMLFileName)
         throws KettleException, Exception {

      // create the transformation and prepare the transformation
      Trans trans = new Trans(transMeta);
      trans.prepareExecution(null);
      
      //  get the step meta from step 1- the only step in the trans
      StepMeta gpLoadStepMeta = transMeta.getStep(1);
      
      // create a GPLoad using the transformation
      GPLoad gpLoad = new GPLoad(gpLoadStepMeta, new GPLoadData(), 0, transMeta, trans);

      // create an empty row- we do not need data as the creation of a
      // control
      // file happens before rows are proceed by the GPLoad step
      Object[] row = { new Object() };
      RowMetaInterface rowMetaInterface = gpLoad.getPreviewRowMeta();

      // get the YAML file contents that we expect
      String expectedContents = getYamlFileContents(expectedYAMLFileName);

      // get the file contents from the GPLoad object
      String actualContents = null;
      actualContents = gpLoad.getControlFileContents(gpLoadMeta, rowMetaInterface, row);

      // test that the built YAML contest are expected
      assertEquals(expectedContents, actualContents);      
   }
   
   ////////////////////////////////
   //  
   //  Insert tests
   //
   ///////////////////////////////
   
   @Test
   public void testInsert1() throws Exception {

      String gpLoadStepname = "GPLoad: test insert 1";

      // create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);

      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_INSERT,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.INSERT_DATA_FILE,
            ",",
            gpLoadStepname+".log",
            new String[0],
            new boolean[0], 
            new boolean[0]);
      
      testYAMLContents(gpLoadMeta, transMeta, "GPLoad-insert1.cfg");

   }

   ////////////////////////////////
   //  
   //  Update tests
   //
   ////////////////////////////////
   
   @Test
   public void testUpdate1() throws Exception {
   
      String gpLoadStepname = "GPLoad: test update 1";

      //  create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);
      
      //  create an array of column names
      String tableField[] = new String[] {"id", "name", "firstname", "zip", "city", "birthdate", "street", "housenr", "statecode", "state"};
      
      //  create an array of boolean that indicates the columns to match
      boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };
      
      //  create array of boolean to 
      boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_UPDATE,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.UPDATE_DATA_FILE,
            ",",
            gpLoadStepname+".log",
            tableField,
            matchColumn, 
            updateColumn);
      
      testYAMLContents(gpLoadMeta, transMeta, "GPLoad-update1.cfg");
   }
   
   /**
    * Test an update where the match columns are not specified.
    * 
    * @throws Exception
    */
   @Test
   public void testUpdate2() throws Exception {
   
      String gpLoadStepname = "GPLoad: test update 2";

      //  create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);
      
      //  create an array of column names
      String tableField[] = new String[] {"id", "name", "firstname", "zip", "city", "birthdate", "street", "housenr", "statecode", "state"};
            
      //  create array of boolean to 
      boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_UPDATE,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.UPDATE_DATA_FILE,
            ",",
            gpLoadStepname+".log",
            tableField,
            null, 
            updateColumn);
      
      try {
         testYAMLContents(gpLoadMeta, transMeta, "GPLoad-update1.cfg");
      }
      catch (KettleException ke) {
         assertTrue(ke.getMessage().contains("An update or merge can not be performed without columns to match on."));
      }
   }
     
   /**
    * Tests an update where no update columns are specified.
    * 
    * @throws Exception
    */
   @Test 
   public void testUpdate3() throws Exception {
      
      String gpLoadStepname = "GPLoad: test update 3";

      //  create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);
      
      //  create an array of column names
      String tableField[] = new String[] {"id", "name", "firstname", "zip", "city", "birthdate", "street", "housenr", "statecode", "state"};
            
      //  create an array of boolean that indicates the columns to match
      boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_UPDATE,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.UPDATE_DATA_FILE,
            ",",
            gpLoadStepname+".log",
            tableField,
            matchColumn, 
            null);
      
      try {
         testYAMLContents(gpLoadMeta, transMeta, "GPLoad-update1.cfg");
      }
      catch (KettleException ke) {
         assertTrue(ke.getMessage().contains("An update or merge action must have Update columns specified."));
      }
   }

   /**
    * Tests an update where no match or update columns are specified.
    * The match column missing exception should be thrown.
    * 
    * @throws Exception
    */
   @Test 
   public void testUpdate4() throws Exception {
      
      String gpLoadStepname = "GPLoad: test update 3";

      //  create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);
      
      //  create an array of column names
      String tableField[] = new String[] {"id", "name", "firstname", "zip", "city", "birthdate", "street", "housenr", "statecode", "state"};
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_UPDATE,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.UPDATE_DATA_FILE,
            ",",
            gpLoadStepname+".log",
            tableField,
            null, 
            null);
      
      try {
         testYAMLContents(gpLoadMeta, transMeta, "GPLoad-update1.cfg");
      }
      catch (KettleException ke) {
         assertTrue(ke.getMessage().contains("An update or merge can not be performed without columns to match on."));
      }
   }
   
   
   @Test
   public void testUpdateMultiKeys() {

   }
   
   @Test
   public void testUpdateNoTargetTable() {

   }
   
   ////////////////////////////////
   //  
   //  Merge tests
   //
   ///////////////////////////////
   
   @Test
   public void testMerge1() throws Exception {
      
      
      String gpLoadStepname = "GPLoad: test merge 1";

      //  create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);
      
      //  create an array of column names
      String tableField[] = new String[] {"id", "name", "firstname", "zip", "city", "birthdate", "street", "housenr", "statecode", "state"};
      
      //  create an array of boolean that indicates the columns to match
      boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };
      
      //  create array of boolean to 
      boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_MERGE,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.MERGE_DATA_FILE,
            ",",
            gpLoadStepname+".log",
            tableField,
            matchColumn, 
            updateColumn);
      
      testYAMLContents(gpLoadMeta, transMeta, "GPLoad-merge1.cfg");

   }

   @Test
   public void testMergeMultiKeys() {

   }

   @Test
   public void testMergeNoTargetTable() {

   }
   
   ////////////////////////////////
   //  
   //  Command line generation tests
   //
   ///////////////////////////////
   
   @Test
   public void testCommandLine1() {
      
   }
}
