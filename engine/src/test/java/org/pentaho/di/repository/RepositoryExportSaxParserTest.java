/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2024 by Hitachi Vantara : http://www.pentaho.com
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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.pentaho.test.util.XXEUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

@RunWith ( MockitoJUnitRunner.class )
public class RepositoryExportSaxParserTest {
  private static final String PKG = "org/pentaho/di/repository/";
  private static final String REPOSITORY_FILE = "test_repo";
  private static final String DIR_WITH_SPECIFIC_CHARS = "\u30A2\u30FF\u30CF";
  private static final String BASE_TEMP_DIR = System.getProperty( "java.io.tmpdir" );
  private static final File TEMP_DIR_WITH_REP_FILE = new File( BASE_TEMP_DIR, DIR_WITH_SPECIFIC_CHARS );

  private RepositoryExportSaxParser repExpSAXParser;
  private final RepositoryImportFeedbackInterface repImpPgDlg = mock( RepositoryImportFeedbackInterface.class );
  private final RepositoryImporter repImpMock = mock( RepositoryImporter.class );

  @Mock private Attributes attributes;


  @Before
  public void setUp() throws IOException, URISyntaxException {
    System.out.println( "Temp Dir: " + BASE_TEMP_DIR );
    createTempDirWithSpecialCharactersInName();
    copyTestResourceIntoTempDir();
  }

  @After
  public void tearDown() throws IOException {
    cleanTempDir();

  }

  @Test
  public void startElementIncludesAttributes() {
    repExpSAXParser = new RepositoryExportSaxParser( "nofile", null );

    when( attributes.getLength() ).thenReturn( 2 );
    when( attributes.getQName( 0 ) ).thenReturn( "name1" );
    when( attributes.getQName( 1 ) ).thenReturn( "name2" );
    when( attributes.getValue( 0 ) ).thenReturn( "val1" );
    when( attributes.getValue( 1 ) ).thenReturn( "val2" );

    repExpSAXParser.startElement( "uri", "", "qualifiedTagName", attributes );

    assertThat( repExpSAXParser.xml.toString(), equalTo( "<qualifiedTagName name1=\"val1\" name2=\"val2\">" ) );
  }

  @Test
  public void startElementWithoutAttributes() {
    repExpSAXParser = new RepositoryExportSaxParser( "nofile", null );

    repExpSAXParser.startElement( "uri", "", "tagName", null );
    assertThat( repExpSAXParser.xml.toString(), equalTo( "<tagName>" ) );
  }

  @Test
  public void testNoExceptionOccurs_WhenNameContainsJapaneseCharacters() throws Exception {
    repExpSAXParser = new RepositoryExportSaxParser( getRepositoryFile().getCanonicalPath(), repImpPgDlg );
    try {
      repExpSAXParser.parse( repImpMock );
    } catch ( Exception e ) {
      Assert.fail( "No exception is expected But occured: " + e );
    }
  }

  @Test ( expected = SAXParseException.class )
  public void exceptionIsThrownWhenParsingXmlWithBigAmountOfExternalEntities() throws Exception {
    File file = createTmpFile( XXEUtils.MALICIOUS_XML );

    repExpSAXParser = new RepositoryExportSaxParser( file.getAbsolutePath(), null );
    repExpSAXParser.parse( repImpMock );
  }

  private File createTmpFile( String content ) throws Exception {
    File tmpFile = File.createTempFile( "RepositoryExportSaxParserTest", ".xml" );
    tmpFile.deleteOnExit();

    try ( PrintWriter writer = new PrintWriter( tmpFile ) ) {
      writer.write( content );
    }

    return tmpFile;
  }

  private static void createTempDirWithSpecialCharactersInName() throws IOException {
    if ( !TEMP_DIR_WITH_REP_FILE.exists() ) {
      if ( TEMP_DIR_WITH_REP_FILE.mkdir() ) {
        System.out.println( "CREATED: " + TEMP_DIR_WITH_REP_FILE.getCanonicalPath() );
      } else {
        System.out.println( "NOT CREATED: " + TEMP_DIR_WITH_REP_FILE );
      }
    }

  }

  private void copyTestResourceIntoTempDir() throws IOException, URISyntaxException {
    File destFile = getRepositoryFile();
    File sourceFile =
      new File( RepositoryExportSaxParserTest.class.getClassLoader().getResource( PKG + REPOSITORY_FILE ).toURI() );

    copyFile( sourceFile, destFile );
    System.out.println( "Copied: " + sourceFile + "-->" + destFile );
  }

  private static void copyFile( File sourceFile, File destFile ) throws IOException {
    if ( !destFile.exists() ) {
      destFile.createNewFile();
    }

    try ( FileChannel source = new FileInputStream( sourceFile ).getChannel();
          FileChannel destination = new FileOutputStream( destFile ).getChannel() ) {
      destination.transferFrom( source, 0, source.size() );
    } catch ( Exception e ) {
      e.printStackTrace();
    }
  }

  private static void cleanTempDir() {

    delete( TEMP_DIR_WITH_REP_FILE );
  }

  private static void delete( File file ) {

    if ( file.isDirectory() ) {
      if ( file.list().length == 0 ) {
        file.delete();
      } else {
        String[] files = file.list();
        for ( String tempFile : files ) {
          File fileDelete = new File( file, tempFile );
          delete( fileDelete );
        }
        if ( file.list().length == 0 ) {
          file.delete();
          System.out.println( "Deleted: " + file );
        }
      }
    } else {
      file.delete();
      System.out.println( "Deleted: " + file );
    }

  }

  private File getRepositoryFile() {
    return new File( TEMP_DIR_WITH_REP_FILE, REPOSITORY_FILE );
  }
}
