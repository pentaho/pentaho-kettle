/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.test.util.XXEUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoriesMetaTest {
  private RepositoriesMeta repoMeta;

  @BeforeClass
  public static void setUpClass() throws Exception {
    if ( !KettleEnvironment.isInitialized() ) {
      KettleEnvironment.init();
    }
  }

  @Before
  public void setUp() {
    repoMeta = new RepositoriesMeta();
    repoMeta = Mockito.spy( repoMeta );
    LogChannel log = mock( LogChannel.class );
    when( repoMeta.newLogChannel() ).thenReturn( log );
  }


  @Test
  public void testToString() throws Exception {
    RepositoriesMeta repositoriesMeta = new RepositoriesMeta();
    assertEquals( "RepositoriesMeta", repositoriesMeta.toString() );
  }

  @Test
  public void testReadData_closeInput() throws Exception {
    String repositoriesFile = getClass().getResource( "repositories.xml" ).getPath();

    LogChannel log = mock( LogChannel.class );
    when( repoMeta.getKettleUserRepositoriesFile() ).thenReturn( repositoriesFile );
    when( repoMeta.newLogChannel() ).thenReturn( log );
    repoMeta.readData();

    RandomAccessFile fos = null;
    try {
      File file = new File( repositoriesFile );
      if ( file.exists() ) {
        fos = new RandomAccessFile( file, "rw" );
      }
    } catch ( FileNotFoundException | SecurityException e ) {
      fail( "the file with properties should be unallocated" );
    } finally {
      if ( fos != null ) {
        fos.close();
      }
    }
  }

  @Test
  public void testReadData() throws Exception {

    LogChannel log = mock( LogChannel.class );
    when( repoMeta.getKettleUserRepositoriesFile() ).thenReturn( getClass().getResource( "repositories.xml" ).getPath() );
    when( repoMeta.newLogChannel() ).thenReturn( log );
    repoMeta.readData();

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
      + "    <is_default>false</is_default>" + Const.CR
      + "    <base_directory>test-repository</base_directory>" + Const.CR
      + "    <read_only>N</read_only>" + Const.CR
      + "    <hides_hidden_files>N</hides_hidden_files>" + Const.CR
      + "  </repository>  </repositories>" + Const.CR;
    assertEquals( repositoriesXml, repoMeta.getXML() );
    RepositoriesMeta clone = repoMeta.clone();
    assertEquals( repositoriesXml, repoMeta.getXML() );
    assertNotSame( clone, repoMeta );

    assertEquals( 1, repoMeta.nrRepositories() );
    RepositoryMeta repository = repoMeta.getRepository( 0 );
    assertEquals( "Test Repository", repository.getName() );
    assertEquals( "Test Repository Description", repository.getDescription() );
    assertEquals( "  <repository>    <id>KettleFileRepository</id>" + Const.CR
      + "    <name>Test Repository</name>" + Const.CR
      + "    <description>Test Repository Description</description>" + Const.CR
      + "    <is_default>false</is_default>" + Const.CR
      + "    <base_directory>test-repository</base_directory>" + Const.CR
      + "    <read_only>N</read_only>" + Const.CR
      + "    <hides_hidden_files>N</hides_hidden_files>" + Const.CR
      + "  </repository>", repository.getXML() );
    assertSame( repository, repoMeta.searchRepository( "Test Repository" ) );
    assertSame( repository, repoMeta.findRepositoryById( "KettleFileRepository" ) );
    assertSame( repository, repoMeta.findRepository( "Test Repository" ) );
    assertNull( repoMeta.findRepository( "not found" ) );
    assertNull( repoMeta.findRepositoryById( "not found" ) );
    assertEquals( 0, repoMeta.indexOfRepository( repository ) );
    repoMeta.removeRepository( 0 );
    assertEquals( 0, repoMeta.nrRepositories() );
    assertNull( repoMeta.searchRepository( "Test Repository" ) );
    repoMeta.addRepository( 0, repository );
    assertEquals( 1, repoMeta.nrRepositories() );
    repoMeta.removeRepository( 1 );
    assertEquals( 1, repoMeta.nrRepositories() );


    assertEquals( 1, repoMeta.nrDatabases() );
    assertEquals( "local postgres", repoMeta.getDatabase( 0 ).getName() );
    DatabaseMeta searchDatabase = repoMeta.searchDatabase( "local postgres" );
    assertSame( searchDatabase, repoMeta.getDatabase( 0 ) );
    assertEquals( 0, repoMeta.indexOfDatabase( searchDatabase ) );
    repoMeta.removeDatabase( 0 );
    assertEquals( 0, repoMeta.nrDatabases() );
    assertNull( repoMeta.searchDatabase( "local postgres" ) );
    repoMeta.addDatabase( 0, searchDatabase );
    assertEquals( 1, repoMeta.nrDatabases() );
    repoMeta.removeDatabase( 1 );
    assertEquals( 1, repoMeta.nrDatabases() );

    assertEquals( "Unable to read repository with id [junk]. RepositoryMeta is not available.", repoMeta.getErrorMessage() );
  }

  @Test
  public void testNothingToRead() throws Exception {
    when( repoMeta.getKettleUserRepositoriesFile() ).thenReturn( "filedoesnotexist.xml" );

    assertTrue( repoMeta.readData() );
    assertEquals( 0, repoMeta.nrDatabases() );
    assertEquals( 0, repoMeta.nrRepositories() );
  }

  @Test
  public void testReadDataFromInputStream() throws Exception {
    InputStream inputStream = getClass().getResourceAsStream( "repositories.xml" );
    repoMeta.readDataFromInputStream( inputStream );

    assertEquals( 1, repoMeta.nrDatabases() );
    assertEquals( 1, repoMeta.nrRepositories() );
  }

  @Test
  public void testErrorReadingInputStream() throws Exception {
    try {
      repoMeta.readDataFromInputStream(  getClass().getResourceAsStream( "filedoesnotexist.xml" ) );
    } catch ( KettleException e ) {
      assertEquals( Const.CR
        + "Error reading information from file:" + Const.CR
        + "InputStream cannot be null" + Const.CR, e.getMessage() );
    }
  }

  @Test
  public void testErrorReadingFile() throws Exception {
    when( repoMeta.getKettleUserRepositoriesFile() ).thenReturn( getClass().getResource( "bad-repositories.xml" ).getPath() );
    try {
      repoMeta.readData();
    } catch ( KettleException e ) {
      assertEquals( Const.CR
        + "Error reading information from file:" + Const.CR
        + "The element type \"repositories\" must be terminated by the matching end-tag \"</repositories>\"."
        + Const.CR, e.getMessage() );
    }
  }

  @Test
  public void testWriteFile() throws Exception {
    String path = getClass().getResource( "repositories.xml" ).getPath().replace( "repositories.xml", "new-repositories.xml" );
    when( repoMeta.getKettleUserRepositoriesFile() ).thenReturn( path );
    repoMeta.writeData();
    InputStream resourceAsStream = getClass().getResourceAsStream( "new-repositories.xml" );
    assertEquals(
      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + Const.CR
        + "<repositories>" + Const.CR
        + "  </repositories>" + Const.CR, IOUtils.toString( resourceAsStream ) );
    new File( path ).delete();
  }

  @Test
  public void testErrorWritingFile() throws Exception {
    when( repoMeta.getKettleUserRepositoriesFile() ).thenReturn( null );
    try {
      repoMeta.writeData();
    } catch ( KettleException e ) {
      assertTrue( e.getMessage().startsWith( Const.CR + "Error writing repositories metadata" ) );
    }
  }



  @Test( expected = KettleException.class )
  public void exceptionThrownWhenParsingXmlWithBigAmountOfExternalEntitiesFromInputStream() throws Exception {

    repoMeta.readDataFromInputStream( new ByteArrayInputStream( XXEUtils.MALICIOUS_XML.getBytes() ) );
  }
}
