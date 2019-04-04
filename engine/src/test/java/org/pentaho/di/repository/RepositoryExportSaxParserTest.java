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
package org.pentaho.di.repository;

import static org.mockito.Mockito.mock;

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
import org.pentaho.test.util.XXEUtils;
import org.xml.sax.SAXParseException;

public class RepositoryExportSaxParserTest {
  private static final String PKG = "org/pentaho/di/repository/";
  private static final String REPOSITORY_FILE = "test_repo";
  private static final String DIR_WITH_SPECIFIC_CHARS = "\u30A2\u30FF\u30CF";
  private static final String BASE_TEMP_DIR = System.getProperty( "java.io.tmpdir" );
  private static final File TEMP_DIR_WITH_REP_FILE = new File( BASE_TEMP_DIR, DIR_WITH_SPECIFIC_CHARS );

  private RepositoryExportSaxParser repExpSAXParser;
  private RepositoryImportFeedbackInterface repImpPgDlg = mock( RepositoryImportFeedbackInterface.class );
  private RepositoryImporter repImpMock = mock( RepositoryImporter.class );

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
  public void testNoExceptionOccurs_WhenNameContainsJapaneseCharacters() throws Exception {
    repExpSAXParser = new RepositoryExportSaxParser( getRepositoryFile().getCanonicalPath(), repImpPgDlg );
    try {
      repExpSAXParser.parse( repImpMock );
    } catch ( Exception e ) {
      Assert.fail( "No exception is expected But occured: " + e );
    }
  }

  @Test( expected = SAXParseException.class )
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
        System.out.println( "NOT CREATED: " + TEMP_DIR_WITH_REP_FILE.toString() );
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

    FileChannel source = null;
    FileChannel destination = null;

    try {
      source = new FileInputStream( sourceFile ).getChannel();
      destination = new FileOutputStream( destFile ).getChannel();
      destination.transferFrom( source, 0, source.size() );
    } catch ( Exception e ) {
      e.printStackTrace();
    } finally {
      if ( source != null ) {
        source.close();
      }
      if ( destination != null ) {
        destination.close();
      }
    }
  }

  private static void cleanTempDir() throws IOException {

    delete( TEMP_DIR_WITH_REP_FILE );
  }

  private static void delete( File file ) throws IOException {

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
