/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.hitachivantara.com
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

package org.pentaho.di.ui.i18n;

import org.apache.commons.io.FileUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.internal.verification.Times;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.core.vfs.KettleVFS;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author sribeiro
 */
public class MessagesSourceCrawlerTest {

  private static final String DUMMY_CONTENT = "Dummy content!";

  private TemporaryFolder temporaryFolder = null;

  @Before
  public void setup() throws Exception {
    temporaryFolder = new TemporaryFolder();
    temporaryFolder.create();
  }

  @After
  public void cleanup() {
    if ( null != temporaryFolder ) {
      temporaryFolder.delete();
      temporaryFolder = null;
    }
  }

  @Test
  public void testGetSetSourceDirectories() {
    MessagesSourceCrawler messagesSourceCrawler = new MessagesSourceCrawler( null, null, null, null );

    // Check that what is set is returned
    List<String> sourceDirectories = new ArrayList<>();
    messagesSourceCrawler.setSourceDirectories( sourceDirectories );
    List<String> res = messagesSourceCrawler.getSourceDirectories();
    assertEquals( sourceDirectories, res );

    // Check that setting null, a new object is created and returned
    messagesSourceCrawler.setSourceDirectories( null );
    res = messagesSourceCrawler.getSourceDirectories();
    assertNotNull( res );
    assertEquals( 0, res.size() );
  }

  @Test
  public void testGetSetFilesToAvoid() {
    MessagesSourceCrawler messagesSourceCrawler = new MessagesSourceCrawler( null, null, null, null );

    // Check that what is set is returned
    List<String> filesToAvoid = new ArrayList<>();
    messagesSourceCrawler.setFilesToAvoid( filesToAvoid );
    List<String> res = messagesSourceCrawler.getFilesToAvoid();
    assertEquals( filesToAvoid, res );

    // Check that setting null, a new object is created and returned
    messagesSourceCrawler.setFilesToAvoid( null );
    res = messagesSourceCrawler.getFilesToAvoid();
    assertNotNull( res );
    assertEquals( 0, res.size() );
  }

  @Test
  public void testGetSetSourcePackageOccurrences() {
    MessagesSourceCrawler messagesSourceCrawler = new MessagesSourceCrawler( null, null, null, null );

    // Check that what is set is returned
    Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences = new HashMap<>();
    messagesSourceCrawler.setSourcePackageOccurrences( sourcePackageOccurrences );
    Map<String, Map<String, List<KeyOccurrence>>> res = messagesSourceCrawler.getSourcePackageOccurrences();
    assertEquals( sourcePackageOccurrences, res );

    // Check that setting null, a new object is created and returned
    messagesSourceCrawler.setSourcePackageOccurrences( null );
    res = messagesSourceCrawler.getSourcePackageOccurrences();
    assertNotNull( res );
    assertEquals( 0, res.size() );
  }

  @Test
  public void testGetSetSingleMessagesFile() {
    MessagesSourceCrawler messagesSourceCrawler = new MessagesSourceCrawler( null, null, null, null );

    // Check that what is set is returned
    messagesSourceCrawler.setSingleMessagesFile( DUMMY_CONTENT );
    String res = messagesSourceCrawler.getSingleMessagesFile();
    assertEquals( DUMMY_CONTENT, res );

    // Setting null, null is kept and returned
    messagesSourceCrawler.setSingleMessagesFile( null );
    assertNull( messagesSourceCrawler.getSingleMessagesFile() );
  }

  @Test
  public void testGetSetScanPhrases() {
    MessagesSourceCrawler messagesSourceCrawler = new MessagesSourceCrawler( null, null, null, null );

    // Check that what is set is returned
    String[] scanPhrases = { DUMMY_CONTENT };
    messagesSourceCrawler.setScanPhrases( scanPhrases );
    String[] res = messagesSourceCrawler.getScanPhrases();
    assertArrayEquals( scanPhrases, res );

    // Check that setting null, a new object is created and returned
    messagesSourceCrawler.setScanPhrases( null );
    res = messagesSourceCrawler.getScanPhrases();
    assertNotNull( res );
    assertEquals( 0, res.length );
  }

  @Test
  public void testCrawl() throws Exception {
    // Prepare the mock for this test
    MessagesSourceCrawler messagesSourceCrawler = mock( MessagesSourceCrawler.class );

    doNothing().when( messagesSourceCrawler ).crawlSourceDirectories();
    doNothing().when( messagesSourceCrawler ).crawlXmlFolders();
    doCallRealMethod().when( messagesSourceCrawler ).crawl();

    // Test
    messagesSourceCrawler.crawl();

    // Check results
    verify( messagesSourceCrawler ).crawlSourceDirectories();
    verify( messagesSourceCrawler ).crawlXmlFolders();
  }

  @Test
  public void testCrawlSourceDirectories() throws Exception {
    // Prepare the mock for this test
    MessagesSourceCrawler messagesSourceCrawler = mock( MessagesSourceCrawler.class );

    doCallRealMethod().when( messagesSourceCrawler ).setFilesToAvoid( any() );
    doCallRealMethod().when( messagesSourceCrawler ).setSourceDirectories( any() );
    doCallRealMethod().when( messagesSourceCrawler ).crawlSourceDirectories();
    doNothing().when( messagesSourceCrawler ).lookForOccurrencesInFile( any(), any() );
    messagesSourceCrawler
      .setScanPhrases( new String[] { "Not relevant for this scenario!" } );

    // Create and populate the filder to use in this test
    List<String> sourceDirectories = new ArrayList<>();
    sourceDirectories.add( temporaryFolder.getRoot().getPath() );
    createDummyFiles( temporaryFolder,
      new String[] { "a.txt", "b.txt", "c.txt", "d.txt", "a.java", "b.java", "c.java", "d.java" }, DUMMY_CONTENT );
    messagesSourceCrawler.setSourceDirectories( sourceDirectories );

    // The files set to be ignored
    messagesSourceCrawler.setFilesToAvoid( Arrays.asList( "a.txt", "b.java", "c.java" ) );

    // Test
    messagesSourceCrawler.crawlSourceDirectories();

    // Check results
    verify( messagesSourceCrawler, new Times( 2 ) ).lookForOccurrencesInFile( any(), any() );
  }

  @Test
  public void testLookForOccurrencesInFile_SplitLines() throws Exception {
    // Prepare the mock for this test
    LogChannelInterface log = mock( LogChannelInterface.class );
    doNothing().when( log ).logError( anyString() );
    List<String> sourceDirectories = new ArrayList<>();
    sourceDirectories.add( temporaryFolder.getRoot().getPath() );

    MessagesSourceCrawler messagesSourceCrawler =
      new MessagesSourceCrawler( log, sourceDirectories, null, null );

    messagesSourceCrawler
      .setScanPhrases( new String[] { "SomeOtherMessages.getString(PKG,", "BaseMessages.getString( PKG," } );

    // Test
    messagesSourceCrawler
      .lookForOccurrencesInFile( "keyOccurrences", createFileObjectFromResource( "split_lines_scenarios.txt" ) );

    // Check results
    List<KeyOccurrence> keyOccurrences = messagesSourceCrawler.getKeyOccurrences( "keyOccurrences" );
    assertNotNull( keyOccurrences );
    assertEquals( 50, keyOccurrences.size() );
  }

  @Test
  public void testAddKeyOccurrence_null() throws Exception {

    MessagesSourceCrawler messagesSourceCrawler =
      new MessagesSourceCrawler( null, null, null, null );

    messagesSourceCrawler.setSourcePackageOccurrences( null );

    messagesSourceCrawler.addKeyOccurrence( null );

    Map<String, Map<String, List<KeyOccurrence>>> sourcePackageOccurrences =
      messagesSourceCrawler.getSourcePackageOccurrences();
    assertNotNull( sourcePackageOccurrences );
    assertEquals( 0, sourcePackageOccurrences.size() );
  }

  @Test
  public void testAddKeyOccurrence() throws Exception {

    MessagesSourceCrawler messagesSourceCrawler =
      new MessagesSourceCrawler( null, null, null, null );

    // After adding an occurrence stating "Source Folder" and "Message Package", it should be retrieved
    final String thePackage = "a.b.c.Package";
    KeyOccurrence keyOccurrence =
      new KeyOccurrence( null, "Some Source Folder", thePackage, 1, 2, null, null, null );

    messagesSourceCrawler.addKeyOccurrence( keyOccurrence );

    // Check results
    List<KeyOccurrence> messagesPackage = messagesSourceCrawler.getOccurrencesForPackage( thePackage );
    assertNotNull( messagesPackage );
    assertEquals( 1, messagesPackage.size() );
    assertEquals( keyOccurrence, messagesPackage.get( 0 ) );
  }

  @Test( expected = RuntimeException.class )
  public void testAddKeyOccurrence_invalidParameter() throws Exception {

    MessagesSourceCrawler messagesSourceCrawler =
      new MessagesSourceCrawler( null, null, null, null );

    // After adding an occurrence stating "Source Folder" and "Message Package", it should be retrieved
    final String thePackage = "a.b.c.Package";
    KeyOccurrence keyOccurrence =
      new KeyOccurrence( null, null, thePackage, 1, 2, null, null, null );

    // Test
    messagesSourceCrawler.addKeyOccurrence( keyOccurrence );

    // An exception should have been raised.
    fail( "It should not have reached here!" );
  }

  private FileObject createFileObjectFromResource( String resourceName ) throws Exception {

    File tempFile = temporaryFolder.newFile();
    tempFile.deleteOnExit();

    InputStream fileScenarios = getClass().getResourceAsStream( resourceName );
    FileUtils.copyInputStreamToFile( fileScenarios, tempFile );
    FileObject folder = KettleVFS.getFileObject( temporaryFolder.getRoot().getAbsolutePath() );
    FileObject[] javaFiles = folder.findFiles( new FileSelector() {
      @Override
      public boolean traverseDescendents( FileSelectInfo info ) throws Exception {
        return true;
      }

      @Override
      public boolean includeFile( FileSelectInfo info ) throws Exception {
        return info.getFile().isFile();
      }
    } );

    return javaFiles[ 0 ];
  }

  /**
   * <p>Given an array with file names, a corresponding file is created in the stated folder. Each file will contain
   * some dummy content.</p>
   *
   * @param temporaryFolder the folder where the files are to be created
   * @param filesToCreate   array containing the names for the files to be created
   * @param content         the content to write to the created file(s)
   * @throws IOException
   */
  private void createDummyFiles( TemporaryFolder temporaryFolder, String[] filesToCreate, String content )
    throws IOException {
    for ( String fileToCreate : filesToCreate ) {
      File tempFile = temporaryFolder.newFile( fileToCreate );
      try ( PrintWriter pw = new PrintWriter( tempFile ) ) {
        pw.println( content );
      }
    }
  }
}
