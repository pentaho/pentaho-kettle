/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.job.entries.getpop;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;
import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.when;

public class MailConnectionTest {

  private Mconn conn;

  @Before
  public void beforeExec() throws KettleException, MessagingException {
    Object subj = new Object();
    LogChannelInterface log = new LogChannel( subj );
    conn = new Mconn( log );
  }

  /**
   * PDI-7426 Test {@link MailConnection#openFolder(String, boolean, boolean)} method. tests that folders are opened
   * recursively
   * 
   * @throws KettleException
   * @throws MessagingException
   */
  @Test
  public void openFolderTest() throws KettleException, MessagingException {
    conn.openFolder( "a/b", false, false );
    Folder folder = conn.getFolder();
    Assert.assertEquals( "Folder B is opened", "B", folder.getFullName() );
  }

  /**
   * PDI-7426 Test {@link MailConnection#setDestinationFolder(String, boolean)} method.
   * 
   * @throws KettleException
   * @throws MessagingException
   */
  @Test
  public void setDestinationFolderTest() throws KettleException, MessagingException {
    conn.setDestinationFolder( "a/b/c", true );
    Assert.assertTrue( "Folder C created", conn.cCreated );
    Assert.assertEquals( "Folder created with holds messages mode", Folder.HOLDS_MESSAGES, conn.mode.intValue() );
  }

  /**
   * PDI-17713 Test {@link MailConnection#findValidTarget(String, String) }
   *
   * Note - this test case relies on the ability to create temporary files
   * of zero-byte size in the java.io.tmpdir folder.
   */
  @Test
  public void findValidTargetTest() throws IOException, KettleException {
    File aFile = null;
    String tmpFileLocation = System.getProperty( "java.io.tmpdir" );
    String aBaseFile = "pdi17713-.junk";
    tmpFileLocation = tmpFileLocation.replace( "\\", "/" );
    if ( !tmpFileLocation.endsWith( "/" ) ) {
      tmpFileLocation = tmpFileLocation + "/";
    }

    // Create temporary files to set up algorithm to have to find an available temp file
    for ( int i = 0; i < 3; i++ ) {
      aFile = new File( tmpFileLocation + "pdi17713-" + i + ".junk" );
      if ( !aFile.exists() ) {
        makeAFile( aFile );
      }
      aFile = new File( tmpFileLocation + "pdi17713-" + i ); // no extension version
      if ( !aFile.exists() ) {
        makeAFile( aFile );
      }
    }

    //**********************************
    // Test with file extensions...
    //**********************************

    // Should now have six files in the tmp folder...
    // with extensions: {tempdir}/pdi17713-0.junk, {tempdir}/pdi17713-1.junk, and {tempdir}/pdi17713-2.junk
    // without extensions: {tempdir}/pdi17713-0, {tempdir}/pdi17713-1, and {tempdir}/pdi17713-2
    String validTargetTestRtn = MailConnection.findValidTarget( tmpFileLocation, aBaseFile );
    // Tests that if the base file doesn't already exist (like IMG00003.png), it will use that one

    Assert.assertTrue( "Original file name should be tried first.", validTargetTestRtn.endsWith( aBaseFile ) );

    // Make sure that the target file already exists so it has to try to find the next available one
    makeAFile( tmpFileLocation + aBaseFile );
    validTargetTestRtn = MailConnection.findValidTarget( tmpFileLocation, aBaseFile );
    // Tests that next available file has a "-3" because 0, 1, and 2 are taken
    Assert.assertTrue( "File extension test failed - expected pdi17713-3.junk as file name", validTargetTestRtn.endsWith( "pdi17713-3.junk" ) );

    //**********************************
    // Now test without file extensions
    //**********************************

    aBaseFile = "pdi17713-";
    validTargetTestRtn = MailConnection.findValidTarget( tmpFileLocation, aBaseFile );
    // Makes sure that it will still use the base file, even with no file extension
    Assert.assertTrue( "Original file name should be tried first.", validTargetTestRtn.endsWith( aBaseFile ) );
    makeAFile( tmpFileLocation + aBaseFile );
    // Make sure that the target file already exists so it has to try to find the next available one
    validTargetTestRtn = MailConnection.findValidTarget( tmpFileLocation, aBaseFile );
    // Tests that next available file has a "-3" because 0, 1, and 2 are taken, even without a file extension
    Assert.assertTrue( "File without extension test failed - expected pdi17713-3.junk as file name", validTargetTestRtn.endsWith( "pdi17713-3" ) );

    try {
      validTargetTestRtn = MailConnection.findValidTarget( null, "wibble" );
      Assert.fail( "Expected an IllegalArgumentException with a null parameter for folderName to findValidTarget" );
    } catch ( IllegalArgumentException expected ) {
      // Expect this exception
    }

    try {
      validTargetTestRtn = MailConnection.findValidTarget( "wibble", null );
      Assert.fail( "Expected an IllegalArgumentException with a null parameter for fileName to findValidTarget" );
    } catch ( IllegalArgumentException expected ) {
      // Expect this exception
    }
  }

  /**
   * PDI-7426 Test {@link MailConnection#folderExists(String)} method.
   */
  @Test
  public void folderExistsTest() {
    boolean actual = conn.folderExists( "a/b" );
    Assert.assertTrue( "Folder B exists", actual );
  }

  private static void makeAFile( String path ) throws IOException {
    File aFile = new File( path );
    makeAFile( aFile );
  }

  private static void makeAFile( File aFile ) throws IOException {
    aFile.createNewFile(); // makes sure the base file exists so that it will have to use new algorithm
    aFile.deleteOnExit();
  }

  private class Mconn extends MailConnection {

    Store store;
    Folder a;
    Folder b;
    Folder c;
    Folder inbox;

    Integer mode = -1;

    boolean cCreated = false;

    public Mconn( LogChannelInterface log ) throws KettleException, MessagingException {
      super( log, MailConnectionMeta.PROTOCOL_IMAP, "junit", 0, "junit", "junit", false, false, "junit" );

      store = Mockito.mock( Store.class );

      inbox = Mockito.mock( Folder.class );
      a = Mockito.mock( Folder.class );
      b = Mockito.mock( Folder.class );
      c = Mockito.mock( Folder.class );

      when( a.getFullName() ).thenReturn( "A" );
      when( b.getFullName() ).thenReturn( "B" );
      when( c.getFullName() ).thenReturn( "C" );

      when( a.exists() ).thenReturn( true );
      when( b.exists() ).thenReturn( true );
      when( c.exists() ).thenReturn( cCreated );
      when( c.create( Mockito.anyInt() ) ).thenAnswer( new Answer<Boolean>() {
        @Override
        public Boolean answer( InvocationOnMock invocation ) throws Throwable {
          Object arg0 = invocation.getArguments()[0];
          mode = Integer.class.cast( arg0 );
          cCreated = true;
          return true;
        }
      } );

      when( inbox.getFolder( "a" ) ).thenReturn( a );
      when( a.getFolder( "b" ) ).thenReturn( b );
      when( b.getFolder( "c" ) ).thenReturn( c );

      when( store.getDefaultFolder() ).thenReturn( inbox );

    }

    @Override
    public Store getStore() {
      return this.store;
    }
  }
}
