/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.repository;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoriesMetaTest {
  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
  }

  @Test
  public void testToString() throws Exception {
    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    assertEquals( "RepositoriesMeta", repositoriesMeta.toString() );
  }

  @Test
  public void testReadData() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.getKettleUserRepositoriesFile() ).thenReturn( getClass().getResource( "repositories.xml" ).getPath() );
    when( spy.newLogChannel() ).thenReturn( log );
    spy.readData();

    String repositoriesXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
      + "<repositories>" + Const.CR
      + "  <connection>" + Const.CR
      + "    <name>local postgres</name>" + Const.CR
      + "    <server>localhost</server>" + Const.CR
      + "    <type>POSTGRESQL</type>" + Const.CR
      + "    <access>Native</access>" + Const.CR
      + "    <database>hibernate</database>" + Const.CR
      + "    <port>5432</port>" + Const.CR
      + "    <username>auser</username>" + Const.CR
      + "    <password>Encrypted 2be98afc86aa7f285bb18bd63c99dbdde</password>" + Const.CR
      + "    <servername/>" + Const.CR
      + "    <data_tablespace/>" + Const.CR
      + "    <index_tablespace/>" + Const.CR
      + "    <attributes>" + Const.CR
      + "      <attribute><code>FORCE_IDENTIFIERS_TO_LOWERCASE</code><attribute>N</attribute></attribute>" + Const.CR
      + "      <attribute><code>FORCE_IDENTIFIERS_TO_UPPERCASE</code><attribute>N</attribute></attribute>" + Const.CR
      + "      <attribute><code>IS_CLUSTERED</code><attribute>N</attribute></attribute>" + Const.CR
      + "      <attribute><code>PORT_NUMBER</code><attribute>5432</attribute></attribute>" + Const.CR
      + "      <attribute><code>PRESERVE_RESERVED_WORD_CASE</code><attribute>N</attribute></attribute>" + Const.CR
      + "      <attribute><code>QUOTE_ALL_FIELDS</code><attribute>N</attribute></attribute>" + Const.CR
      + "      <attribute><code>SUPPORTS_BOOLEAN_DATA_TYPE</code><attribute>Y</attribute></attribute>" + Const.CR
      + "      <attribute><code>SUPPORTS_TIMESTAMP_DATA_TYPE</code><attribute>Y</attribute></attribute>" + Const.CR
      + "      <attribute><code>USE_POOLING</code><attribute>N</attribute></attribute>" + Const.CR
      + "    </attributes>" + Const.CR
      + "  </connection>" + Const.CR
      + "  <repository>    <id>KettleFileRepository</id>" + Const.CR
      + "    <name>Test Repository</name>" + Const.CR
      + "    <description>Test Repository Description</description>" + Const.CR
      + "    <base_directory>test-repository</base_directory>" + Const.CR
      + "    <read_only>N</read_only>" + Const.CR
      + "    <hides_hidden_files>N</hides_hidden_files>" + Const.CR
      + "  </repository>  </repositories>" + Const.CR;
    assertEquals( repositoriesXml, spy.getXML() );
    RepositoriesMeta clone = spy.clone();
    assertEquals( repositoriesXml, spy.getXML() );
    assertNotSame( clone, spy );

    assertEquals( 1, spy.nrRepositories() );
    RepositoryMeta repository = spy.getRepository( 0 );
    assertEquals( "Test Repository", repository.getName() );
    assertEquals( "Test Repository Description", repository.getDescription() );
    assertEquals( "  <repository>    <id>KettleFileRepository</id>" + Const.CR
      + "    <name>Test Repository</name>" + Const.CR
      + "    <description>Test Repository Description</description>" + Const.CR
      + "    <base_directory>test-repository</base_directory>" + Const.CR
      + "    <read_only>N</read_only>" + Const.CR
      + "    <hides_hidden_files>N</hides_hidden_files>" + Const.CR
      + "  </repository>", repository.getXML() );
    assertSame( repository, spy.searchRepository( "Test Repository" ) );
    assertSame( repository, spy.findRepositoryById( "KettleFileRepository" ) );
    assertSame( repository, spy.findRepository( "Test Repository" ) );
    assertNull( spy.findRepository( "not found" ) );
    assertNull( spy.findRepositoryById( "not found" ) );
    assertEquals( 0, spy.indexOfRepository( repository ) );
    spy.removeRepository( 0 );
    assertEquals( 0, spy.nrRepositories() );
    assertNull( spy.searchRepository( "Test Repository" ) );
    spy.addRepository( 0, repository );
    assertEquals( 1, spy.nrRepositories() );
    spy.removeRepository( 1 );
    assertEquals( 1, spy.nrRepositories() );


    assertEquals( 1, spy.nrDatabases() );
    assertEquals( "local postgres", spy.getDatabase( 0 ).getName() );
    DatabaseMeta searchDatabase = spy.searchDatabase( "local postgres" );
    assertSame( searchDatabase, spy.getDatabase( 0 ) );
    assertEquals( 0, spy.indexOfDatabase( searchDatabase ) );
    spy.removeDatabase( 0 );
    assertEquals( 0, spy.nrDatabases() );
    assertNull( spy.searchDatabase( "local postgres" ) );
    spy.addDatabase( 0, searchDatabase );
    assertEquals( 1, spy.nrDatabases() );
    spy.removeDatabase( 1 );
    assertEquals( 1, spy.nrDatabases() );

    assertEquals( "Unable to read repository with id [junk]. RepositoryMeta is not available.", spy.getErrorMessage() );
  }

  @Test
  public void testNothingToRead() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.getKettleUserRepositoriesFile() ).thenReturn( "filedoesnotexist.xml" );
    when( spy.newLogChannel() ).thenReturn( log );
    assertTrue( spy.readData() );
    assertEquals( 0, spy.nrDatabases() );
    assertEquals( 0, spy.nrRepositories() );
  }

  @Test
  public void testReadDataFromInputStream() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.newLogChannel() ).thenReturn( log );
    InputStream inputStream = getClass().getResourceAsStream( "repositories.xml" );
    spy.readDataFromInputStream( inputStream );
    assertEquals( 1, spy.nrDatabases() );
    assertEquals( 1, spy.nrRepositories() );
  }

  @Test
  public void testErrorReadingInputStream() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.newLogChannel() ).thenReturn( log );
    try {
      spy.readDataFromInputStream(  getClass().getResourceAsStream( "filedoesnotexist.xml" ) );
    } catch ( KettleException e ) {
      assertEquals( Const.CR
        + "Error reading information from file:" + Const.CR
        + "InputStream cannot be null" + Const.CR, e.getMessage() );
    }
  }

  @Test
  public void testErrorReadingFile() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.newLogChannel() ).thenReturn( log );
    when( spy.getKettleUserRepositoriesFile() ).thenReturn( getClass().getResource( "bad-repositories.xml" ).getPath() );
    try {
      spy.readData();
    } catch ( KettleException e ) {
      assertEquals( Const.CR
        + "Error reading information from file:" + Const.CR
        + "The element type \"repositories\" must be terminated by the matching end-tag \"</repositories>\"."
        + Const.CR, e.getMessage() );
    }
  }

  @Test
  public void testWriteFile() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.newLogChannel() ).thenReturn( log );
    String path = getClass().getResource( "repositories.xml" ).getPath().replace( "repositories.xml", "new-repositories.xml" );
    when( spy.getKettleUserRepositoriesFile() ).thenReturn( path );
    spy.writeData();
    InputStream resourceAsStream = getClass().getResourceAsStream( "new-repositories.xml" );
    assertEquals(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
        + "<repositories>" + Const.CR
        + "  </repositories>" + Const.CR, IOUtils.toString( resourceAsStream ) );
    new File( path ).delete();
  }

  @Test
  public void testErrorWritingFile() throws Exception {
    RepositoriesMeta meta = new RepositoriesMeta();
    RepositoriesMeta spy = Mockito.spy( meta );
    LogChannel log = mock( LogChannel.class );
    when( spy.newLogChannel() ).thenReturn( log );
    when( spy.getKettleUserRepositoriesFile() ).thenReturn( null );
    try {
      spy.writeData();
    } catch ( KettleException e ) {
      assertTrue( e.getMessage().startsWith( Const.CR + "Error writing repositories metadata" ) );
    }
  }
}
