/*!
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
 */

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
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;

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
 * These test will verify that correct YAML is being generated. YAML contains the specifics of a GPLoad job.
 * 
 * There are also tests for the gpload command line generation.
 * 
 * @author sflatley
 * 
 */
public class GPLoadTest {

  // Target database objects
  private static final String TARGET_TABLE = "customers_100";
  private static final String GPLOAD_ERROR_TABLE = "err_customers_100";

  // YAML location and files
  private static final String YAML_TEST_FILE_LOCATION = "testfiles";
  private static final String SCHEMA_NAME = "public";
  private static final String[] TABLE_FIELD = new String[] { "id", "name", "firstname", "zip", "city", "birthdate",
    "street", "housenr", "statecode", "state" };

  private File testDirectory = null;

  // Data files
  private static final String INSERT_DATA_FILE = "customers-100.txt";
  private static final String UPDATE_DATA_FILE = "customers-update.txt";
  private static final String MERGE_DATA_FILE = "customers-merge.txt";

  // Paths to files
  private String pathToGPLoadExecutable = null;
  private String pathToControlFile = null;
  private String pathToLogfile = null;

  public static final String GREENPLUM_DATABASE_CONNECTION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
    + "<connection>" + "<name>foodmartOnGreenplum</name>" + "<server>10.100.2.42</server>" + "<type>Greenplum</type>"
    + "<access>Native</access>" + "<database>foodmart</database>" + "<port>5432</port>"
    + "<username>gpadmin</username>" + "<password>doesntmatter</password>" + "</connection>";

  /**
   * Initialize the test environment.
   */
  @Before
  public void init() {

    // initialize testDirectory
    testDirectory = new File( GPLoadTest.YAML_TEST_FILE_LOCATION );
    if ( !testDirectory.isDirectory() ) {
      fail( GPLoadTest.YAML_TEST_FILE_LOCATION + " does not exist." );
    }

    // Get the path a valid configuratiomn file.
    // We will use this path as the path top the mock
    // GPLoad executable and the log file.
    File file = new File( testDirectory.getAbsolutePath() + "/GPLoad-update1.cfg" );
    if ( file.exists() ) {
      pathToControlFile = file.getAbsolutePath();
      pathToGPLoadExecutable = pathToControlFile;
      pathToLogfile = pathToGPLoadExecutable;
    } else {
      fail( "Could not set up path to mock GPLoad executable." );
    }

    // initialize the Kettle environment
    try {
      KettleEnvironment.init();
    } catch ( KettleException ke ) {
      fail( ke.getMessage() );
    }
  }

  /**
   * Returns the contents of the passed file name
   * 
   * @param filename
   * @return String the content of filename which is located in GPLoadTest.YAML_TEST_FILE_LOCATION.
   */
  private String getYamlFileContents( String filename ) {

    StringBuilder sbFileContents = new StringBuilder();
    BufferedReader bufferedReader = null;
    try {
      // create a buffered reader
      File file = new File( testDirectory.getAbsolutePath() + "/" + filename );
      bufferedReader = new BufferedReader( new FileReader( file ) );
      String lineFromFile = null;

      // read each line and append it with a
      // carriage return to our string builder
      while ( ( lineFromFile = bufferedReader.readLine() ) != null ) {
        sbFileContents.append( lineFromFile ).append( Const.CR );
      }
    } catch ( FileNotFoundException fnfe ) {
      fail( fnfe.getMessage() );
    } catch ( IOException ioe ) {
      fail( ioe.getMessage() );
    } finally {
      if ( bufferedReader != null ) {
        try {
          bufferedReader.close();
        } catch ( IOException e ) {
          fail( e.getMessage() );
        }
      }
    }

    // convert to string and return
    return sbFileContents.toString();
  }

  /**
   * Creates a transformation with a row generator step and hopped to a GPLoadStep with the passed name.
   * 
   * @param gpLoadStepname
   *          The name of the GPLoad step.
   * 
   * @throws KettleException
   */
  public TransMeta createTransformationMeta( String gpLoadStepname ) throws Exception {

    // Create a new transformation...
    TransMeta transMeta = new TransMeta();
    transMeta.setName( "row generatortest" );

    // Add a database connection to the trans meta
    transMeta.addDatabase( new DatabaseMeta( GREENPLUM_DATABASE_CONNECTION ) );

    // get a reference to the plugin registry
    PluginRegistry registry = PluginRegistry.getInstance();
    if ( registry == null ) {
      throw new Exception( "Plugin registry is null.  Make sure that the Kettle environment was initialized." );
    }

    // create the GPLoad step
    GPLoadMeta gpLoadMeta = new GPLoadMeta();
    String dummyPid = registry.getPluginId( StepPluginType.class, gpLoadMeta );
    StepMeta gpLoadStepMeta = new StepMeta( dummyPid, gpLoadStepname, gpLoadMeta );
    transMeta.addStep( gpLoadStepMeta );

    return transMeta;
  }

  private GPLoadMeta createGPLoadMeta( String gpLoadStepname, String actionInsert, String targetTable,
    String gploadErrorTable, String insertDataFile, String s, String pathToGPLoadExecutable,
    String pathToControlFile, String pathToLogfile, String[] strings, boolean[] booleans, boolean[] booleans1,
    String o, String[] o1, String o2, String o3 ) throws Exception {
    return createGPLoadMeta( gpLoadStepname, actionInsert, null, targetTable, gploadErrorTable, insertDataFile, s,
      pathToGPLoadExecutable, pathToControlFile, pathToLogfile, strings, booleans, booleans1, o, o1, o2, o3 );
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
  private GPLoadMeta createGPLoadMeta( String gpLoadStepname, String action, String targetTableName,
    String errorTableName, String dataFilename, String delimiter, String[] tableColumn, boolean[] matchColumn,
    boolean[] updateColumn, String localhostPort, String[] localHosts, String updateCondition, String encoding ) throws Exception {

    return createGPLoadMeta( gpLoadStepname, action, null, targetTableName, errorTableName, dataFilename, delimiter,
      null, null, null, tableColumn, matchColumn, updateColumn, localhostPort, localHosts, updateCondition, encoding );
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
  private GPLoadMeta createGPLoadMeta( String gpLoadStepname, String action, String schemaName, String targetTableName,
    String errorTableName, String dataFilename, String delimiter, String[] tableColumn, boolean[] matchColumn,
    boolean[] updateColumn, String localhostPort, String[] localHosts, String updateCondition, String encoding ) throws Exception {

    return createGPLoadMeta( gpLoadStepname, action, schemaName, targetTableName, errorTableName, dataFilename,
      delimiter, null, null, null, tableColumn, matchColumn, updateColumn, localhostPort, localHosts,
      updateCondition, encoding );
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
  private GPLoadMeta createGPLoadMeta( String gpLoadStepname, String action, String schemaTableName,
    String targetTableName, String errorTableName, String dataFilename, String delimiter, String pathToGPLoad,
    String pathToControlfile, String logFilename, String[] tableColumn, boolean[] matchColumn,
    boolean[] updateColumn, String localhostPort, String[] localHosts, String updateCondition, String encoding ) throws Exception {

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // get a reference to the GPLoad step meta and then it's
    StepMeta gpLoadStepMeta = transMeta.getStep( 0 );
    GPLoadMeta gpLoadMeta = (GPLoadMeta) gpLoadStepMeta.getStepMetaInterface();

    // setDefault is called from Spoon.newStep if we were creating a new step using the interface.
    gpLoadMeta.setDefault();

    // set properties
    gpLoadMeta.setSchemaName( schemaTableName );
    gpLoadMeta.setTableName( targetTableName );
    gpLoadMeta.setErrorTableName( GPLoadTest.GPLOAD_ERROR_TABLE );
    gpLoadMeta.setLoadAction( action );
    gpLoadMeta.setDataFile( dataFilename );
    gpLoadMeta.setDelimiter( delimiter );
    gpLoadMeta.setFieldTable( tableColumn );
    gpLoadMeta.setMatchColumns( matchColumn );
    gpLoadMeta.setUpdateColumn( updateColumn );
    gpLoadMeta.setControlFile( pathToControlFile );
    gpLoadMeta.setLogFile( logFilename );
    gpLoadMeta.setGploadPath( pathToGPLoad );
    gpLoadMeta.setDatabaseMeta( transMeta.getDatabase( 0 ) );
    gpLoadMeta.setLocalhostPort( localhostPort );
    gpLoadMeta.setLocalHosts( localHosts );
    gpLoadMeta.setUpdateCondition( updateCondition );
    gpLoadMeta.setEncoding( encoding );

    return gpLoadMeta;
  }

  /**
   * Tests the YAML contents generated by the GPLoad step in the transformation.
   * 
   * @param GPLoadMeta
   * @param TransMeta
   * @param expectedYAMLFilename
   *          The file name of the YAML file whose contents should match the YAML generated by the GPLoad step.
   */
  private void testYAMLContents( GPLoadMeta gpLoadMeta, TransMeta transMeta, String expectedYAMLFileName ) throws KettleException, Exception {

    // create the transformation and prepare the transformation
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // get the step meta from step 1- the only step in the trans
    StepMeta gpLoadStepMeta = transMeta.getStep( 0 );

    // create a GPLoad using the transformation
    GPLoad gpLoad = new GPLoad( gpLoadStepMeta, new GPLoadData(), 0, transMeta, trans );

    // get the YAML file contents that we expect
    String expectedContents = getYamlFileContents( expectedYAMLFileName );

    // we need to the row meta interface to create the control file
    RowMetaInterface rowMetaInterface = gpLoad.getPreviewRowMeta();

    // get the file contents from the GPLoad object
    String actualContents = null;
    actualContents = gpLoad.getControlFileContents( gpLoadMeta, rowMetaInterface );

    // test that the built YAML contest are expected
    assertEquals( expectedContents, actualContents );
  }

  /**
   * Tests the command line generated by the GPLoad step in the transformation.
   * 
   * The default control file is used and a log file is specified.
   * 
   * @param GPLoadMeta
   * @param TransMeta
   * @param expectedCommadnLine
   *          The expected command line to be generated.
   * 
   */
  private void testCommandLine( GPLoadMeta gpLoadMeta, TransMeta transMeta, String expectedCommandLine ) throws KettleException, Exception {

    // create the transformation and prepare the transformation
    Trans trans = new Trans( transMeta );
    trans.prepareExecution( null );

    // get the step meta from step 1- the only step in the trans
    StepMeta gpLoadStepMeta = transMeta.getStep( 0 );

    // create a GPLoad using the transformation
    GPLoad gpLoad = new GPLoad( gpLoadStepMeta, new GPLoadData(), 0, transMeta, trans );

    // get the file contents from the GPLoad object
    String actualCommandLine = null;
    actualCommandLine = gpLoad.createCommandLine( gpLoadMeta, false );

    if ( Const.getOS().startsWith( "Windows" ) ) {
      expectedCommandLine = "cmd /c " + expectedCommandLine;
    }
    // test that the built YAML contest are expected
    assertEquals( expectedCommandLine, actualCommandLine );
  }

  // //////////////////////////////
  //
  // Insert tests
  //
  // /////////////////////////////

  /**
   * Tests an insert using the default path to the GPLoad.
   */
  @Test
  public void testInsert1() throws Exception {

    String gpLoadStepname = "GPLoad: test insert 1";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ";", new String[0], new boolean[0],
        new boolean[0], null, null, // no local host port or localhosts
        null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-insert1.cfg" );
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
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", new String[0], new boolean[0],
        new boolean[0], "8000", new String[] { "localhost" }, null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-insert2.cfg" );
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
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", new String[0], new boolean[0],
        new boolean[0], "8000", new String[] { "etl-host1", "etl-host2" }, null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-insert3.cfg" );
  }

  /**
   * Tests inserting with encoding specified.
   * 
   * @throws Exception
   */
  @Test
  public void testInsert4() throws Exception {

    String gpLoadStepname = "GPLoad: test insert 4";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ";", new String[0], new boolean[0],
        new boolean[0], null, null, // no local host port or localhosts
        null, "UTF-8" );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-insert4.cfg" );
  }

  /**
   * Tests an insert using the default path to the GPLoad.
   */
  @Test
  public void testSchemaNameSpecified() throws Exception {

    String gpLoadStepname = "GPLoad: test schema name specified";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.SCHEMA_NAME, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ";", new String[0], new boolean[0],
        new boolean[0], null, null, // no local host port or localhosts
        null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-schemaSpecified.cfg" );
  }

  // //////////////////////////////
  //
  // Update tests
  //
  // //////////////////////////////

  @Test
  public void testUpdate1() throws Exception {

    String gpLoadStepname = "GPLoad: test update 1";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of column names

    // create an array of boolean that indicates the columns to match
    boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };

    // create array of boolean to
    boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.UPDATE_DATA_FILE, ";", TABLE_FIELD, matchColumn, updateColumn,
        null, null, // no local host port or localhosts);
        null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update1.cfg" );
  }

  /**
   * Test an update where the match columns are not specified.
   * 
   * @throws Exception
   */
  @Test
  public void testUpdate2() throws Exception {

    String gpLoadStepname = "GPLoad: test update 2";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create array of boolean to
    boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.UPDATE_DATA_FILE, ",", TABLE_FIELD, null, updateColumn, null,
        null, // no local host port or localhosts);
        null, null );

    try {
      testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update1.cfg" );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "An update or merge can not be performed without columns to match on." ) );
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

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of column names

    // create an array of boolean that indicates the columns to match
    boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.UPDATE_DATA_FILE, ",", TABLE_FIELD, matchColumn, null, null,
        null, // no local host port or localhosts);
        null, null );

    try {
      testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update1.cfg" );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "An update or merge action must have Update columns specified." ) );
    }
  }

  /**
   * Tests an update where no match or update columns are specified. The match column missing exception should be
   * thrown.
   * 
   * @throws Exception
   */
  @Test
  public void testUpdate4() throws Exception {

    String gpLoadStepname = "GPLoad: test update 3";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of column names

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.UPDATE_DATA_FILE,
        ",", TABLE_FIELD, null, null, null, null, // no local host port or localhosts);
        null, null );

    try {
      testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update1.cfg" );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "An update or merge can not be performed without columns to match on." ) );
    }
  }

  @Test
  public void testUpdate5() throws Exception {

    String gpLoadStepname = "GPLoad: test update 5";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of boolean that indicates the columns to match
    boolean[] matchColumn = new boolean[] { true, true, true, false, false, false, false, false, false, false };

    // create array of boolean to
    boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.UPDATE_DATA_FILE, ";", TABLE_FIELD, matchColumn, updateColumn,
        null, null, // no local host port or localhosts);
        null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update5.cfg" );
  }

  /**
   * Tests exception handling when the target table is not provided.
   */
  @Test
  public void testUpdate6() throws Exception {

    String gpLoadStepname = "GPLoad: test update 6";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of boolean that indicates the columns to match
    boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };

    // create array of boolean to
    boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, null, GPLoadTest.GPLOAD_ERROR_TABLE,
        GPLoadTest.UPDATE_DATA_FILE, ";", TABLE_FIELD, matchColumn, updateColumn, null, null, // no local host port
                                                                                              // or localhosts);
        null, null );

    try {
      testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update1.cfg" );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "table must be specified" ) );
    }
  }

  @Test
  public void testUpdate7() throws Exception {

    String gpLoadStepname = "GPLoad: test update 7";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of boolean that indicates the columns to match
    boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };

    // create array of boolean to
    boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };

    // create an update condition
    String updateCondition = "id > 0";

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_UPDATE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.UPDATE_DATA_FILE, ";", TABLE_FIELD, matchColumn, updateColumn,
        null, null, // no local host port or localhosts);
        updateCondition, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-update7.cfg" );
  }

  // //////////////////////////////
  //
  // Merge tests
  //
  // /////////////////////////////

  @Test
  public void testMerge1() throws Exception {

    String gpLoadStepname = "GPLoad: test merge 1";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create an array of boolean that indicates the columns to match
    boolean[] matchColumn = new boolean[] { true, false, false, false, false, false, false, false, false, false };

    // create array of boolean to
    boolean[] updateColumn = new boolean[] { false, true, true, false, false, false, false, false, false, false };

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_MERGE, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.MERGE_DATA_FILE, ",", TABLE_FIELD, matchColumn, updateColumn,
        null, null, // no local host port or localhosts);
        null, null );

    testYAMLContents( gpLoadMeta, transMeta, "GPLoad-merge1.cfg" );
  }

  // //////////////////////////////
  //
  // Command line generation tests
  //
  // /////////////////////////////

  /**
   * Tests the GPLoad command line generation with a log file specified.
   * 
   */
  @Test
  public void testCommandLine1() throws Exception {

    String gpLoadStepname = "GPLoad: test command line 1";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", pathToGPLoadExecutable, pathToControlFile,
        pathToLogfile, new String[0], new boolean[0], new boolean[0], null, null, // no local
                                                                                  // host port or
                                                                                  // localhosts);
        null, null );

    // get the path to the control file
    File controlFile = new File( gpLoadMeta.getControlFile() );
    String pathToControlFile = controlFile.getAbsolutePath();

    String expectedCommandLine = ( gpLoadMeta.getGploadPath() + " -f " + pathToControlFile + " -l " + pathToLogfile );
    testCommandLine( gpLoadMeta, transMeta, expectedCommandLine );
  }

  /**
   * Tests the GPLoad command line generation with a log file NOT specified.
   * 
   */
  @Test
  public void testCommandLine2() throws Exception {

    String gpLoadStepname = "GPLoad: test command line 2";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", pathToGPLoadExecutable, null, null,
        new String[0], new boolean[0], new boolean[0], null, null, // no local host port or localhosts);
        null, null );

    // get the path to the control file
    File controlFile = new File( gpLoadMeta.getControlFile() );
    String pathToControlFile = controlFile.getAbsolutePath();

    String expectedCommandLine = ( gpLoadMeta.getGploadPath() + " -f " + pathToControlFile );
    testCommandLine( gpLoadMeta, transMeta, expectedCommandLine );
  }

  /**
   * Tests the GPLoad command line generation with an invalid path to GPLoad.
   * 
   */
  @Test
  public void testCommandLine4() throws Exception {

    String gpLoadStepname = "GPLoad: test command line 4";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // An invalid path to GPLoad.
    String invalidPath = "/invalid path/gpload.exe";

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", invalidPath, null, null, new String[0],
        new boolean[0], new boolean[0], null, null, // no local host port or localhosts);
        null, null );

    String expectedCommandLine = ( "/invalid path" + " -f " + invalidPath );
    try {
      testCommandLine( gpLoadMeta, transMeta, expectedCommandLine );
      fail( "A KettleException was expected as we provided an invalid path to GPLoad." );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "The file " + invalidPath + " does not exist" ) );
    }
  }

  /**
   * Tests the GPLoad command line generation with a good path to gpload, a good path to the control file and a valid
   * log file path.
   */
  @Test
  public void testCommandLine5() throws Exception {

    String gpLoadStepname = "GPLoad: test command line 5";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // an invalid path to the control file
    String invalidPath = "/invalid path/gpload.log";

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", pathToGPLoadExecutable, pathToControlFile,
        invalidPath, new String[0], new boolean[0], new boolean[0], null, null, null, null );

    // get the path to the control file
    File controlFile = new File( gpLoadMeta.getControlFile() );
    String pathToControlFile = controlFile.getAbsolutePath();

    String expectedCommandLine = ( gpLoadMeta.getGploadPath() + " -f " + pathToControlFile + " -l " + invalidPath );
    try {
      testCommandLine( gpLoadMeta, transMeta, expectedCommandLine );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "The directory" ) && ke.getMessage().contains( "does not exist" ) );
    }
  }

  /**
   * Tests the GPLoad command line generation with a good path to gpload, a good path to the control file and a valid
   * log file path.
   */
  @Test
  public void testCommandLine6() throws Exception {

    String gpLoadStepname = "GPLoad: test command line 6";

    // create the trans meta
    TransMeta transMeta = createTransformationMeta( gpLoadStepname );

    // create GPLoadMeta to do an insert
    // and specifying to columns
    GPLoadMeta gpLoadMeta =
      createGPLoadMeta( gpLoadStepname, GPLoadMeta.ACTION_INSERT, GPLoadTest.TARGET_TABLE,
        GPLoadTest.GPLOAD_ERROR_TABLE, GPLoadTest.INSERT_DATA_FILE, ",", pathToGPLoadExecutable, pathToControlFile,
        "/invalid path", new String[0], new boolean[0], new boolean[0], null, null, // no local host port or
                                                                                    // localhosts);
        null, null );

    // get the path to the control file
    File controlFile = new File( gpLoadMeta.getControlFile() );
    String pathToControlFile = controlFile.getAbsolutePath();
    File root = new File( "/" );
    String expectedCommandLine =
      ( gpLoadMeta.getGploadPath() + " -f " + pathToControlFile + " -l " + root.getAbsolutePath() + "invalid path" );
    try {
      testCommandLine( gpLoadMeta, transMeta, expectedCommandLine );
    } catch ( KettleException ke ) {
      assertTrue( ke.getMessage().contains( "The file /invalid path does not exist" ) );
    }
  }
}
