/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.job;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleDatabaseException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingObjectType;
import org.pentaho.di.core.logging.SimpleLoggingObject;
import org.pentaho.di.core.parameters.UnknownParamException;

public abstract class JobTrackerExecution {

  public static String CREATE = "logTableCreate.sql";
  public static final String NAME = "Junit_JobTest";
  public static final String DB = "mem:HSQLDB-JUNIT-LOGJOB";
  public static String PKG = "org/pentaho/di/job/";

  @Before
  public void setUpBeforeClass() throws Exception {
    KettleEnvironment.init();
    LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );

    File file = File.createTempFile( JobTrackerExecution.class.getSimpleName(), "" );
    file.deleteOnExit();
    DatabaseMeta databaseMeta =
        new DatabaseMeta( NAME, "Hypersonic", "JDBC", null, "mem:HSQLDB-JUNIT-LOGJOB", null, null, null );
    Database logDataBase = new Database( log, databaseMeta );
    logDataBase.connect();

    // run sql create for database
    InputStream input = JobTrackerExecution.class.getClassLoader().getResourceAsStream( PKG + CREATE );
    String sql = getStringFromInput( input );
    logDataBase.execStatements( sql );
    logDataBase.commit( true );

    logDataBase.disconnect();
  }

  @After
  public void after() throws KettleDatabaseException {
    // DatabaseMeta databaseMeta = new DatabaseMeta( NAME, "H2", "JDBC", null, TMP, null, USER, USER );
    DatabaseMeta databaseMeta =
        new DatabaseMeta( NAME, "Hypersonic", "JDBC", null, "mem:HSQLDB-JUNIT-LOGJOB", null, null, null );
    LoggingObjectInterface log = new SimpleLoggingObject( "junit", LoggingObjectType.GENERAL, null );
    Database db = new Database( log, databaseMeta );
    db.connect();

    db.execStatements( "DROP SCHEMA PUBLIC CASCADE" );
    db.commit( true );
    db.disconnect();
  }

  public static String getCanonicalPath( String resource ) throws URISyntaxException, IOException {
    URL url = JobTrackerExecution.class.getClassLoader().getResource( PKG + resource );
    File file = new File( url.toURI() );
    return file.getCanonicalPath();
  }

  private static String getStringFromInput( InputStream in ) throws IOException {
    StringBuilder sb = new StringBuilder();
    InputStreamReader is = null;
    BufferedReader br = null;
    try {
      is = new InputStreamReader( in );
      br = new BufferedReader( is );
      String read = br.readLine();
      while ( read != null ) {
        sb.append( read );
        read = br.readLine();
      }
    } finally {
      if ( is != null ) {
        try {
          is.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
      if ( br != null ) {
        try {
          br.close();
        } catch ( IOException e ) {
          // Suppress
        }
      }
    }
    return sb.toString();
  }

  public static List<String> getJobDefaultRunParameters() {
    List<String> list = new ArrayList<String>();
    /*
     * list.add( "-param:junit.name=" + TMP ); list.add( "-param:junit.user=" + USER ); list.add(
     * "-param:junit.password=" + USER );
     */
    return list;
  }

  protected JobMeta getJobMeta( String resource ) throws KettleXMLException, URISyntaxException, IOException,
    UnknownParamException {
    JobMeta jobMeta = new JobMeta( getCanonicalPath( resource ), null );
    /*
     * jobMeta.setParameterValue( "junit.name", TMP ); jobMeta.setParameterValue( "junit.user", USER );
     * jobMeta.setParameterValue( "junit.password", USER );
     */
    return jobMeta;
  }
}
