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

/* Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.*/

/**
 * JUnit test for GPLoad step.
 * 
 * These test will verify that correct YAML is being generated. 
 * YAML contains the specifics of a GPLoad job.
 * 
 * There are also tests for the gpload command line generation.
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
   private final static String INSERT_DATA_FILE = "customers-100.txt";
   private final static String UPDATE_DATA_FILE = "customers-update.txt";
   private final static String MERGE_DATA_FILE = "customers-merge.txt";
   
   //  Paths to files
   private String pathToGPLoadExecutable = null;
   private String pathToControlFile = null;
   private String pathToLogfile = null;

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
      
      //  Get the path a valid configuratiomn file.
      //  We will use this path as the path top the mock
      //  GPLoad executable and the log file. 
      File file = new File(testDirectory.getAbsolutePath() + "/GPLoad-update1.cfg");
      if (file.exists()) {
         pathToControlFile = file.getAbsolutePath();
         pathToGPLoadExecutable = pathToControlFile;
         pathToLogfile = pathToGPLoadExecutable;
      }
      else {
         fail("Could not set up path to mock GPLoad executable.");
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

      // create the GPLoad step
      GPLoadMeta gpLoadMeta = new GPLoadMeta();
      String dummyPid = registry.getPluginId(StepPluginType.class, gpLoadMeta);
      StepMeta gpLoadStepMeta = new StepMeta(dummyPid, gpLoadStepname, (StepMetaInterface) gpLoadMeta);
      transMeta.addStep(gpLoadStepMeta);

      return transMeta;

   }
   
   /**
    * Creates and returns a GPLoad meta.
    * 
    * @param gpLoadStepname
    * @param action
    * @param targetTableName
    * @param errorTableName
    * @param dataFilename
    * @param delimiter
    * @param tableColumn
    * @param matchColumn
    * @param updateColumn
    * @param localhostPort
    * @param localHosts
    * @return
    * @throws Exception
    */
   private GPLoadMeta createGPLoadMeta(String gpLoadStepname,
                                       String action,
                                       String targetTableName,
                                       String errorTableName,
                                       String dataFilename,
                                       String delimiter,
                                       String[] tableColumn,
                                       boolean[] matchColumn, 
                                       boolean[] updateColumn,
                                       String localhostPort,
                                       String[] localHosts) throws Exception {
   
      
      return createGPLoadMeta(gpLoadStepname, action, targetTableName, errorTableName, 
                              dataFilename, delimiter, null, null, null, tableColumn, 
                              matchColumn, updateColumn, localhostPort, localHosts);
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
                                       String pathToGPLoad,
                                       String pathToControlfile,
                                       String logFilename,
                                       String[] tableColumn,
                                       boolean[] matchColumn, 
                                       boolean[] updateColumn,
                                       String localhostPort,
                                       String[] localHosts)
               throws Exception {
      
      // create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);

      // get a reference to the GPLoad step meta and then it's
      StepMeta gpLoadStepMeta = transMeta.getStep(0);
      GPLoadMeta gpLoadMeta = (GPLoadMeta) gpLoadStepMeta.getStepMetaInterface();

      // setDefault is called from Spoon.newStep if we were creating a new step using the interface.
      gpLoadMeta.setDefault();
      
      // set properties
      gpLoadMeta.setTableName(GPLoadTests.TARGET_TABLE);
      gpLoadMeta.setErrorTableName(GPLoadTests.GPLOAD_ERROR_TABLE);
      gpLoadMeta.setLoadAction(action);
      gpLoadMeta.setDataFile(dataFilename);
      gpLoadMeta.setDelimiter(delimiter);
      gpLoadMeta.setFieldTable(tableColumn);
      gpLoadMeta.setMatchColumns(matchColumn);
      gpLoadMeta.setUpdateColumn(updateColumn);
      gpLoadMeta.setControlFile(pathToControlFile);
      gpLoadMeta.setLogFile(logFilename);
      gpLoadMeta.setGploadPath(pathToGPLoad);
      gpLoadMeta.setDatabaseMeta(transMeta.getDatabase(0));
      gpLoadMeta.setLocalhostPort(localhostPort);
      gpLoadMeta.setLocalHosts(localHosts);
      
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
      StepMeta gpLoadStepMeta = transMeta.getStep(0);
      
      // create a GPLoad using the transformation
      GPLoad gpLoad = new GPLoad(gpLoadStepMeta, new GPLoadData(), 0, transMeta, trans);

      // get the YAML file contents that we expect
      String expectedContents = getYamlFileContents(expectedYAMLFileName);

      // we need to the row meta interface to create the control file
      RowMetaInterface rowMetaInterface = gpLoad.getPreviewRowMeta();

      // get the file contents from the GPLoad object
      String actualContents = null;
      actualContents = gpLoad.getControlFileContents(gpLoadMeta, rowMetaInterface);

      // test that the built YAML contest are expected
      assertEquals(expectedContents, actualContents);      
   }
   
   /**
    * Tests the command line generated by the GPLoad step in the transformation.
    * 
    * The default control file is used and a log file is specified.
    * 
    * @param GPLoadMeta 
    * @param TransMeta
    * @param expectedCommadnLine The expected command line to be generated.
    *
    */
   private void testCommandLine(GPLoadMeta gpLoadMeta,
                                TransMeta transMeta,
                                String expectedCommandLine)
         throws KettleException, Exception {

      // create the transformation and prepare the transformation
      Trans trans = new Trans(transMeta);
      trans.prepareExecution(null);
      
      //  get the step meta from step 1- the only step in the trans
      StepMeta gpLoadStepMeta = transMeta.getStep(0);
      
      // create a GPLoad using the transformation
      GPLoad gpLoad = new GPLoad(gpLoadStepMeta, new GPLoadData(), 0, transMeta, trans);
      
      // get the file contents from the GPLoad object
      String actualCommandLine = null;
      actualCommandLine = gpLoad.createCommandLine(gpLoadMeta, false);
      
      // test that the built YAML contest are expected
      assertEquals(expectedCommandLine, actualCommandLine);      
   }  
   
   ////////////////////////////////
   //  
   //  Insert tests
   //
   ///////////////////////////////
   
   /**
    * Tests an insert using the default path to the GPLoad.
    */
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
            ";",
            new String[0],
            new boolean[0], 
            new boolean[0],
            null, null); //  no local host port or localhosts
            
      
      testYAMLContents(gpLoadMeta, transMeta, "GPLoad-insert1.cfg");
   }

   /**
    * Tests inserting with local host port and one local host specified.
    * 
    * @throws Exception
    */
   @Test
   public void testInsert2() throws Exception {

      String gpLoadStepname = "GPLoad: test insert 2";

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
            new String[0],
            new boolean[0], 
            new boolean[0],
            "8000", new String[] { "localhost" } ); 
            
      testYAMLContents(gpLoadMeta, transMeta, "GPLoad-insert2.cfg");
   }
   
   /**
    * Tests inserting with local host port and two local hosts specified.
    * 
    * @throws Exception
    */
   @Test
   public void testInsert3() throws Exception {

      String gpLoadStepname = "GPLoad: test insert 3";

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
            new String[0],
            new boolean[0], 
            new boolean[0],
            "8000", new String[] { "etl-host1", "etl-host2" } ); 
            
      testYAMLContents(gpLoadMeta, transMeta, "GPLoad-insert3.cfg");

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
            ";",
            tableField,
            matchColumn, 
            updateColumn,
            null, null); //  no local host port or localhosts);
      
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
            tableField,
            null, 
            updateColumn,
            null, null); //  no local host port or localhosts);
      
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
            tableField,
            matchColumn, 
            null,
            null, null); //  no local host port or localhosts);
      
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
            tableField,
            null, 
            null,
            null, null); //  no local host port or localhosts);
      
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
            tableField,
            matchColumn, 
            updateColumn,
            null, null); //  no local host port or localhosts);
      
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
   
   /**
    * Tests the GPLoad command line generation with a log file specified.
    * 
    */
   @Test
   public void testCommandLine1() throws Exception {

      String gpLoadStepname = "GPLoad: test command line 1";

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
            pathToGPLoadExecutable,
            pathToControlFile,
            pathToLogfile,
            new String[0],
            new boolean[0], 
            new boolean[0],
            null, null); //  no local host port or localhosts);
      
      //  get the path to the control file
      File controlFile = new File(gpLoadMeta.getControlFile());
      String pathToControlFile = controlFile.getAbsolutePath();
      
      String expectedCommandLine=(gpLoadMeta.getGploadPath()+" -f "+pathToControlFile+" -l "+pathToLogfile);
      testCommandLine(gpLoadMeta, transMeta, expectedCommandLine);
   }
   
   /**
    * Tests the GPLoad command line generation with a log file NOT specified.
    * 
    */
   @Test
   public void testCommandLine2() throws Exception {

      String gpLoadStepname = "GPLoad: test command line 2";

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
            pathToGPLoadExecutable,
            null,
            null,
            new String[0],
            new boolean[0], 
            new boolean[0],
            null, null); //  no local host port or localhosts);
      
      //  get the path to the control file
      File controlFile = new File(gpLoadMeta.getControlFile());
      String pathToControlFile = controlFile.getAbsolutePath();
            
      String expectedCommandLine=(gpLoadMeta.getGploadPath()+" -f "+pathToControlFile);
      testCommandLine(gpLoadMeta, transMeta, expectedCommandLine);
   }
   
   /**
    * Tests the GPLoad command line generation with an invalid path to GPLoad.
    * 
    */
   @Test
   public void testCommandLine4() throws Exception {

      String gpLoadStepname = "GPLoad: test command line 4";

      // create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);

      //  An invalid path to GPLoad.
      String invalidPath = "/invalid path/gpload.exe";
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_INSERT,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.INSERT_DATA_FILE,
            ",",
            invalidPath,
            null,
            null,
            new String[0],
            new boolean[0], 
            new boolean[0],
            null, null); //  no local host port or localhosts);
      
      String expectedCommandLine=("/invalid path"+" -f "+invalidPath);
      try {
         testCommandLine(gpLoadMeta, transMeta, expectedCommandLine);
         fail("A KettleException was expected as we provided an invalid path to GPLoad.");
      }
      catch (KettleException ke) {
         assertTrue(ke.getMessage().contains("The file "+invalidPath+" does not exist"));
      }
   }
   
   /**
    * Tests the GPLoad command line generation with a good path to gpload,
    * a good path to the control file and a valid log file path.
    */
   @Test
   public void testCommandLine5() throws Exception {

      String gpLoadStepname = "GPLoad: test command line 5";

      // create the trans meta
      TransMeta transMeta = createTransformationMeta(gpLoadStepname);
      
      // an invalid path to the control file
      String invalidPath = "/invalid path/gpload.log";
      
      //  create GPLoadMeta to do an insert
      //  and specifying to columns
      GPLoadMeta gpLoadMeta = createGPLoadMeta(
            gpLoadStepname,
            GPLoadMeta.ACTION_INSERT,
            GPLoadTests.TARGET_TABLE,
            GPLoadTests.GPLOAD_ERROR_TABLE,
            GPLoadTests.INSERT_DATA_FILE,
            ",",
            pathToGPLoadExecutable,
            pathToControlFile,
            invalidPath,
            new String[0],
            new boolean[0], 
            new boolean[0],
            null, null); //  no local host port or localhosts);
      
      //  get the path to the control file
      File controlFile = new File(gpLoadMeta.getControlFile());
      String pathToControlFile = controlFile.getAbsolutePath();
      
      String expectedCommandLine=(gpLoadMeta.getGploadPath()+" -f "+pathToControlFile+" -l "+invalidPath);
      try {
         testCommandLine(gpLoadMeta, transMeta, expectedCommandLine);
      }
      catch (KettleException ke) {
         assertTrue(    ke.getMessage().contains("The directory") 
                     && ke.getMessage().contains("does not exist"));
      }
   }
   
   
   /**
    * Tests the GPLoad command line generation with a good path to gpload,
    * a good path to the control file and a valid log file path.
    */
   @Test
   public void testCommandLine6() throws Exception {

      String gpLoadStepname = "GPLoad: test command line 6";

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
            pathToGPLoadExecutable,
            pathToControlFile,
            "/invalid path",
            new String[0],
            new boolean[0], 
            new boolean[0],
            null, null); //  no local host port or localhosts);
      
      //  get the path to the control file
      File controlFile = new File(gpLoadMeta.getControlFile());
      String pathToControlFile = controlFile.getAbsolutePath();
      
      String expectedCommandLine=(gpLoadMeta.getGploadPath()+" -f "+pathToControlFile+" -l "+"/invalid path");
      try {
         testCommandLine(gpLoadMeta, transMeta, expectedCommandLine);
      }
      catch (KettleException ke) {
         assertTrue(ke.getMessage().contains("The file /invalid path does not exist"));
      }
   }
}
