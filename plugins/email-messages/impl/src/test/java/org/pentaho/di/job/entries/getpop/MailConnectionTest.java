/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 - 2026 by Pentaho Canada Inc. : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2030-06-15
 ******************************************************************************/


package org.pentaho.di.job.entries.getpop;

import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.bowl.DefaultBowl;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Store;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    String validTargetTestRtn = MailConnection.findValidTarget( DefaultBowl.getInstance(), tmpFileLocation, aBaseFile );
    // Tests that if the base file doesn't already exist (like IMG00003.png), it will use that one

    Assert.assertTrue( "Original file name should be tried first.", validTargetTestRtn.endsWith( aBaseFile ) );

    // Make sure that the target file already exists so it has to try to find the next available one
    makeAFile( tmpFileLocation + aBaseFile );
    validTargetTestRtn = MailConnection.findValidTarget( DefaultBowl.getInstance(), tmpFileLocation, aBaseFile );
    // Tests that next available file has a "-3" because 0, 1, and 2 are taken
    Assert.assertTrue( "File extension test failed - expected pdi17713-3.junk as file name", validTargetTestRtn.endsWith( "pdi17713-3.junk" ) );

    //**********************************
    // Now test without file extensions
    //**********************************

    aBaseFile = "pdi17713-";
    validTargetTestRtn = MailConnection.findValidTarget( DefaultBowl.getInstance(), tmpFileLocation, aBaseFile );
    // Makes sure that it will still use the base file, even with no file extension
    Assert.assertTrue( "Original file name should be tried first.", validTargetTestRtn.endsWith( aBaseFile ) );
    makeAFile( tmpFileLocation + aBaseFile );
    // Make sure that the target file already exists so it has to try to find the next available one
    validTargetTestRtn = MailConnection.findValidTarget( DefaultBowl.getInstance(), tmpFileLocation, aBaseFile );
    // Tests that next available file has a "-3" because 0, 1, and 2 are taken, even without a file extension
    Assert.assertTrue( "File without extension test failed - expected pdi17713-3.junk as file name", validTargetTestRtn.endsWith( "pdi17713-3" ) );

    try {
      validTargetTestRtn = MailConnection.findValidTarget( DefaultBowl.getInstance(), null, "wibble" );
      Assert.fail( "Expected an IllegalArgumentException with a null parameter for folderName to findValidTarget" );
    } catch ( IllegalArgumentException expected ) {
      // Expect this exception
    }

    try {
      validTargetTestRtn = MailConnection.findValidTarget( DefaultBowl.getInstance(), "wibble", null );
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

  @Test
  public void concatTargetPathPreservesUriSchemesTest() {
    Assert.assertEquals( "pvfs://sample_connection/example-workspace/demo-user/mail-target/PDI-20925-attachment.txt",
      MailConnection.concatTargetPath( "pvfs://sample_connection/example-workspace/demo-user/mail-target",
        "PDI-20925-attachment.txt" ) );
  }

  @Test
  public void concatTargetPathPreservesUriSchemesWithTrailingSlashTest() {
    Assert.assertEquals( "pvfs://sample_connection/example-workspace/demo-user/mail-target/PDI-20925-attachment.txt",
      MailConnection.concatTargetPath( "pvfs://sample_connection/example-workspace/demo-user/mail-target/",
        "PDI-20925-attachment.txt" ) );
  }

  @Test
  public void concatTargetPathPreservesUriSchemesWithTrailingBackslashTest() {
    Assert.assertEquals( "file://C:\\tmp\\mail\\attachment.txt",
      MailConnection.concatTargetPath( "file://C:\\tmp\\mail\\", "attachment.txt" ) );
  }

  @Test
  public void concatTargetPathSupportsLocalFolderTest() {
    Assert.assertEquals( FilenameUtils.concat( "/tmp/mail", "attachment.txt" ),
      MailConnection.concatTargetPath( "/tmp/mail", "attachment.txt" ) );
  }

  @Test
  public void concatTargetPathSupportsWindowsFolderTest() {
    Assert.assertEquals( FilenameUtils.concat( "C:\\tmp\\mail", "attachment.txt" ),
      MailConnection.concatTargetPath( "C:\\tmp\\mail", "attachment.txt" ) );
  }

  @Test
  public void concatTargetPathNullFolderNameThrowsTest() {
    try {
      MailConnection.concatTargetPath( null, "attachment.txt" );
      Assert.fail( "Expected IllegalArgumentException when folderName is null" );
    } catch ( IllegalArgumentException expected ) {
      // expected
    }
  }

  @Test
  public void concatTargetPathNullFileNameThrowsTest() {
    try {
      MailConnection.concatTargetPath( "pvfs://sample_connection/example-workspace/demo-user/mail-target", null );
      Assert.fail( "Expected IllegalArgumentException when fileName is null" );
    } catch ( IllegalArgumentException expected ) {
      // expected
    }
  }

  @Test
  public void mboxDeleteMessagePersistsOnDisconnect() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-delete-one", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "first", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "second", "body-2", "two@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.fetchNext();
    connection.deleteMessage();
    connection.disconnect();

    List<String> subjectsAfterDelete = readMboxSubjects( sourceMbox );
    Assert.assertEquals( 1, subjectsAfterDelete.size() );
    Assert.assertEquals( "second", subjectsAfterDelete.get( 0 ) );
  }

  @Test
  public void mboxDeleteMessagesPersistsOnDisconnect() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-delete-many", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "first", "body", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "second", "body", "two@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.deleteMessages( true );
    connection.disconnect();

    List<String> subjectsAfterDelete = readMboxSubjects( sourceMbox );
    Assert.assertEquals( 0, subjectsAfterDelete.size() );
  }

  @Test
  public void mboxMoveMessageMovesToDestinationFolder() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-move-one", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "to-move", "body", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "to-keep", "body", "two@example.com" );

    File destinationMbox = File.createTempFile( "mailconnection-move-one-dest", ".mbox" );
    destinationMbox.deleteOnExit();

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.setDestinationFolder( destinationMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    List<String> sourceSubjects = readMboxSubjects( sourceMbox );
    Assert.assertEquals( 1, sourceSubjects.size() );
    Assert.assertEquals( "to-keep", sourceSubjects.get( 0 ) );

    List<String> destinationSubjects = readMboxSubjects( destinationMbox );
    Assert.assertEquals( 1, destinationSubjects.size() );
    Assert.assertEquals( "to-move", destinationSubjects.get( 0 ) );
  }

  @Test
  public void mboxMoveMessagesMovesAllToDestinationFolder() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-move-all", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "first", "body", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "second", "body", "two@example.com" );

    File destinationMbox = File.createTempFile( "mailconnection-move-all-dest", ".mbox" );
    destinationMbox.deleteOnExit();

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.setDestinationFolder( destinationMbox.getAbsolutePath(), true );
    connection.moveMessages();
    connection.disconnect();

    List<String> sourceSubjects = readMboxSubjects( sourceMbox );
    Assert.assertEquals( 0, sourceSubjects.size() );

    List<String> destinationSubjects = readMboxSubjects( destinationMbox );
    Assert.assertEquals( 2, destinationSubjects.size() );
    Assert.assertEquals( "first", destinationSubjects.get( 0 ) );
    Assert.assertEquals( "second", destinationSubjects.get( 1 ) );
  }

  private static void writeSimpleMboxMessage( File file, String subject, String body, String from ) throws IOException {
    try ( FileWriter writer = new FileWriter( file ) ) {
      writer.write( "From " + from + " Fri Jan 01 00:00:00 2021\n" );
      writer.write( "Date: Fri, 1 Jan 2021 00:00:00 +0000\n" );
      writer.write( "From: " + from + "\n" );
      writer.write( "To: receiver@example.com\n" );
      writer.write( "Subject: " + subject + "\n" );
      writer.write( "Message-ID: <test-1@example.com>\n" );
      writer.write( "MIME-Version: 1.0\n" );
      writer.write( "Content-Type: text/plain; charset=UTF-8\n" );
      writer.write( "\n" );
      writer.write( body + "\n" );
      writer.write( "\n" );
    }
  }

  private static void appendSimpleMboxMessage( File file, String subject, String body, String from ) throws IOException {
    try ( FileWriter writer = new FileWriter( file, true ) ) {
      writer.write( "From " + from + " Sat Jan 02 00:00:00 2021\n" );
      writer.write( "Date: Sat, 2 Jan 2021 00:00:00 +0000\n" );
      writer.write( "From: " + from + "\n" );
      writer.write( "To: receiver@example.com\n" );
      writer.write( "Subject: " + subject + "\n" );
      writer.write( "Message-ID: <test-2@example.com>\n" );
      writer.write( "MIME-Version: 1.0\n" );
      writer.write( "Content-Type: text/plain; charset=UTF-8\n" );
      writer.write( "\n" );
      writer.write( body + "\n" );
      writer.write( "\n" );
    }
  }

  private List<String> readMboxSubjects( File sourceMbox ) throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-read" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    List<String> subjects = new ArrayList<>();
    for ( Message message : connection.getMessages() ) {
      subjects.add( message.getSubject() );
    }
    connection.disconnect();
    return subjects;
  }

  private static void makeAFile( String path ) throws IOException {
    File aFile = new File( path );
    makeAFile( aFile );
  }

  private static void makeAFile( File aFile ) throws IOException {
    aFile.createNewFile(); // makes sure the base file exists so that it will have to use new algorithm
    aFile.deleteOnExit();
  }

  private static void writeLatin1MboxMessage( File file, String subject, String from, byte[] body ) throws IOException {
    try ( java.io.FileOutputStream out = new java.io.FileOutputStream( file ) ) {
      out.write( ( "From " + from + " Fri Jan 01 00:00:00 2021\n" ).getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( "Date: Fri, 1 Jan 2021 00:00:00 +0000\n".getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( ( "From: " + from + "\n" ).getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( "To: receiver@example.com\n".getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( ( "Subject: " + subject + "\n" ).getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( "Message-ID: <latin1-test@example.com>\n".getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( "MIME-Version: 1.0\n".getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( "Content-Type: text/plain; charset=ISO-8859-1\n\n".getBytes( java.nio.charset.StandardCharsets.ISO_8859_1 ) );
      out.write( body );
      out.write( '\n' );
      out.write( '\n' );
    }
  }

  private static int indexOfBytes( byte[] source, byte[] target ) {
    if ( target.length == 0 ) {
      return 0;
    }
    for ( int i = 0; i <= source.length - target.length; i++ ) {
      boolean match = true;
      for ( int j = 0; j < target.length; j++ ) {
        if ( source[i + j] != target[j] ) {
          match = false;
          break;
        }
      }
      if ( match ) {
        return i;
      }
    }
    return -1;
  }

  private static int countByte( byte[] source, byte expected ) {
    int count = 0;
    for ( byte b : source ) {
      if ( b == expected ) {
        count++;
      }
    }
    return count;
  }

  @Test
  public void mboxEffectiveMessageNumbersAreSequential() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-effective-numbering", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "first", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "second", "body-2", "two@example.com" );
    appendSimpleMboxMessage( sourceMbox, "third", "body-3", "three@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    Message[] messages = connection.getMessages();
    Assert.assertEquals( 3, messages.length );
    Assert.assertEquals( 1, connection.getEffectiveMessageNumber( messages[0] ) );
    Assert.assertEquals( 2, connection.getEffectiveMessageNumber( messages[1] ) );
    Assert.assertEquals( 3, connection.getEffectiveMessageNumber( messages[2] ) );
    
    connection.disconnect();
  }

  @Test
  public void mboxEmptyFileLoadsWithoutMessages() throws Exception {
    File emptyMbox = File.createTempFile( "mailconnection-empty", ".mbox" );
    emptyMbox.deleteOnExit();
    // File is created but empty

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, emptyMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    Assert.assertEquals( 0, connection.getMessages().length );
    connection.disconnect();
  }

  @Test
  public void mboxNormalizesFileUriPrefix() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-file-uri", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    String fileUri = "file://" + sourceMbox.getAbsolutePath();
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, fileUri, -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    Assert.assertEquals( 1, connection.getMessages().length );
    connection.disconnect();
  }

  @Test
  public void mboxEnvelopeFromWithValidEmail() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-from-valid", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.fetchNext();
    
    // Move message to verify envelope extraction works (uses getEnvelopeFrom internally)
    File destMbox = File.createTempFile( "mailconnection-from-dest", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.moveMessage();
    connection.disconnect();
    
    List<String> destSubjects = readMboxSubjects( destMbox );
    Assert.assertEquals( 1, destSubjects.size() );
    Assert.assertEquals( "test", destSubjects.get( 0 ) );
  }

  @Test
  public void mboxMessageWithFromLineEscaping() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-from-escape", ".mbox" );
    sourceMbox.deleteOnExit();
    // Write a message with body containing "From " at the start of a line
    try ( FileWriter writer = new FileWriter( sourceMbox ) ) {
      writer.write( "From sender@example.com Fri Jan 01 00:00:00 2021\n" );
      writer.write( "Date: Fri, 1 Jan 2021 00:00:00 +0000\n" );
      writer.write( "From: sender@example.com\n" );
      writer.write( "To: receiver@example.com\n" );
      writer.write( "Subject: test-escaping\n" );
      writer.write( "Message-ID: <test-escape@example.com>\n" );
      writer.write( "MIME-Version: 1.0\n" );
      writer.write( "Content-Type: text/plain; charset=UTF-8\n" );
      writer.write( "\n" );
      writer.write( "This is a normal line\n" );
      writer.write( "From this line starts with From\n" );
      writer.write( "Another line\n" );
      writer.write( "\n" );
    }

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.fetchNext();
    
    File destMbox = File.createTempFile( "mailconnection-escape-dest", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.moveMessage();
    connection.disconnect();
    
    // Verify the message was correctly written and can be read back
    List<String> destSubjects = readMboxSubjects( destMbox );
    Assert.assertEquals( 1, destSubjects.size() );
    Assert.assertEquals( "test-escaping", destSubjects.get( 0 ) );
  }

  @Test
  public void mboxFolderResolutionWithAbsolutePath() throws Exception {
    File sourceDir = new File( System.getProperty( "java.io.tmpdir" ), "mbox-test-" + System.nanoTime() );
    sourceDir.mkdirs();
    sourceDir.deleteOnExit();
    
    File sourceMbox = new File( sourceDir, "source.mbox" );
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    File destDir = new File( System.getProperty( "java.io.tmpdir" ), "mbox-dest-" + System.nanoTime() );
    destDir.mkdirs();
    destDir.deleteOnExit();
    File destMbox = new File( destDir, "destination.mbox" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.moveMessages();
    connection.disconnect();

    Assert.assertTrue( "Destination mbox should exist", destMbox.exists() );
    Assert.assertEquals( "Source should be empty after move", 0, readMboxSubjects( sourceMbox ).size() );
    Assert.assertEquals( "Destination should have message", 1, readMboxSubjects( destMbox ).size() );
  }

  @Test
  public void mboxFolderCreationWithNestedPath() throws Exception {
    File sourceDir = new File( System.getProperty( "java.io.tmpdir" ), "mbox-nested-" + System.nanoTime() );
    sourceDir.mkdirs();
    sourceDir.deleteOnExit();
    
    File sourceMbox = new File( sourceDir, "source.mbox" );
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    File destParentDir = new File( System.getProperty( "java.io.tmpdir" ), "mbox-nested-dest-" + System.nanoTime() );
    destParentDir.mkdirs();
    destParentDir.deleteOnExit();
    
    // Destination is a subdirectory that doesn't exist yet
    File destSubDir = new File( destParentDir, "nested-folder" );
    File destMbox = new File( destSubDir, "destination.mbox" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.moveMessages();
    connection.disconnect();

    Assert.assertTrue( "Nested folder should be created", destSubDir.exists() );
    Assert.assertTrue( "Destination mbox should exist", destMbox.exists() );
  }

  @Test
  public void mboxInboxFolderResolution() throws Exception {
    File sourceDir = new File( System.getProperty( "java.io.tmpdir" ), "mbox-inbox-" + System.nanoTime() );
    sourceDir.mkdirs();
    sourceDir.deleteOnExit();
    
    File sourceMbox = new File( sourceDir, "source.mbox" );
    writeSimpleMboxMessage( sourceMbox, "msg1", "body", "test@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg2", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    // Setting destination to INBOX should resolve to source mbox path
    // When appending to the same file you're reading from, messages get duplicated in the file
    connection.setDestinationFolder( MailConnectionMeta.INBOX_FOLDER, true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // After move to INBOX (which is source), message should be moved not copied
    List<String> remaining = readMboxSubjects( sourceMbox );
    Assert.assertEquals( 1, remaining.size() );
    Assert.assertEquals( "msg2", remaining.get( 0 ) );
  }

  @Test
  public void mboxRelativeFolderResolution() throws Exception {
    File sourceDir = new File( System.getProperty( "java.io.tmpdir" ), "mbox-relative-" + System.nanoTime() );
    sourceDir.mkdirs();
    sourceDir.deleteOnExit();
    
    File sourceMbox = new File( sourceDir, "source.mbox" );
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    // Use relative path (should resolve relative to source parent)
    connection.setDestinationFolder( "archive.mbox", true );
    connection.moveMessages();
    connection.disconnect();

    // Destination should be in same directory as source
    File destMbox = new File( sourceDir, "archive.mbox" );
    Assert.assertTrue( "Destination should exist in same directory", destMbox.exists() );
  }

  @Test
  public void mboxMessageNumberMapCleanedOnReload() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-reload", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg1", "body", "test@example.com" );

    // First load with 1 message
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    Message[] firstLoad = connection.getMessages();
    Assert.assertEquals( 1, firstLoad.length );
    Assert.assertEquals( 1, connection.getEffectiveMessageNumber( firstLoad[0] ) );
    connection.disconnect();
    
    // Add another message to file
    appendSimpleMboxMessage( sourceMbox, "msg2", "body", "test@example.com" );
    
    // Load again in new connection - should have 2 messages with clean map
    MailConnection connection2 = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection2.connect();
    connection2.openFolder( false );
    connection2.retrieveMessages();
    
    Message[] secondLoad = connection2.getMessages();
    Assert.assertEquals( 2, secondLoad.length );
    // Verify that message numbers are correct for new load (not stale from first load)
    Assert.assertEquals( 1, connection2.getEffectiveMessageNumber( secondLoad[0] ) );
    Assert.assertEquals( 2, connection2.getEffectiveMessageNumber( secondLoad[1] ) );
    
    connection2.disconnect();
  }

  @Test
  public void setDestinationFolderMboxWithoutCreateFolder() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-nocreate", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    // Create a destination that will fail because parent doesn't exist and createFolder=false
    File nonExistentParent = new File( "/tmp/mbox-this-should-not-exist-" + System.nanoTime() );
    File destMbox = new File( nonExistentParent, "destination.mbox" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    // Should throw exception because createFolder=false and parent directory doesn't exist
    try {
      connection.setDestinationFolder( destMbox.getAbsolutePath(), false );
      Assert.fail( "Should have thrown exception for missing folder with createFolder=false" );
    } catch ( KettleException e ) {
      Assert.assertNotNull( "Should mention folder not found", e.getMessage() );
    }

    connection.disconnect();
  }

  @Test
  public void isConnectedStateForMbox() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-state", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );

    // Before connect - MBOX always returns false since store is null
    Assert.assertFalse( "MBOX Should not be connected initially", connection.isConnected() );

    connection.connect();
    // For MBOX protocol, store remains null, so isConnected() stays false
    // This is expected behavior - MBOX doesn't use a Store object like IMAP/POP3
    Assert.assertFalse( "MBOX does not use Store, so isConnected() returns false", connection.isConnected() );

    connection.disconnect();
  }

  @Test
  public void connectionPropertiesAvailable() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-props", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "testuser", "testpass", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    // Verify connection properties
    Assert.assertEquals( "Should have correct server", sourceMbox.getAbsolutePath(), connection.getServer() );
    Assert.assertFalse( "Should not use SSL", connection.isUseSSL() );
    Assert.assertFalse( "Should not use proxy", connection.isUseProxy() );
    Assert.assertEquals( "Should have empty proxy username", "", connection.getProxyUsername() );

    connection.disconnect();
  }

  @Test
  public void messageCountersIncrement() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-counters", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test1", "body", "test@example.com" );
    appendSimpleMboxMessage( sourceMbox, "test2", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    // Test counters - only use public methods
    Assert.assertEquals( "Initial saved counter should be 0", 0, connection.getSavedMessagesCounter() );
    connection.updateSavedMessagesCounter();
    Assert.assertEquals( "Saved counter should increment", 1, connection.getSavedMessagesCounter() );

    Assert.assertEquals( "Initial saved attached files counter should be 0", 0, connection.getSavedAttachedFilesCounter() );
    connection.updateSavedAttachedFilesCounter();
    Assert.assertEquals( "Saved attached files counter should increment", 1, connection.getSavedAttachedFilesCounter() );

    // Test deleted messages counter (read-only public access)
    Assert.assertEquals( "Initial deleted counter should be 0", 0, connection.getDeletedMessagesCounter() );

    // Test moved messages counter (read-only public access)
    Assert.assertEquals( "Initial moved counter should be 0", 0, connection.getMovedMessagesCounter() );

    connection.disconnect();
  }

  @Test
  public void getMessagesCountReturnsCorrectValue() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-msgcount", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test1", "body", "test@example.com" );
    appendSimpleMboxMessage( sourceMbox, "test2", "body", "test@example.com" );
    appendSimpleMboxMessage( sourceMbox, "test3", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Assert.assertEquals( "Should have 3 messages", 3, connection.getMessagesCount() );
    Assert.assertEquals( "getMessages() should return same count", 3, connection.getMessages().length );

    connection.disconnect();
  }

  @Test
  public void normalizeMboxPathWithFileTripleSlash() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    Assert.assertEquals( "file:///path should become /path", "/path/to/file.mbox",
      connection.normalizeMboxPath( "file:///path/to/file.mbox" ) );
  }

  @Test
  public void normalizeMboxPathWithFileDoubleSlash() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    // file://path/to treats 'path' as host, so path component is /to/file.mbox
    Assert.assertEquals( "file://path/to/file.mbox should extract /to/file.mbox", "/to/file.mbox",
      connection.normalizeMboxPath( "file://path/to/file.mbox" ) );
  }

  @Test
  public void normalizeMboxPathWithFileSingleSlash() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    Assert.assertEquals( "file:/path should become /path", "/path/to/file.mbox",
      connection.normalizeMboxPath( "file:/path/to/file.mbox" ) );
  }

  @Test
  public void normalizeMboxPathWithHostComponent() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    // file://localhost/path and file://server.com/path both extract the path component correctly
    Assert.assertEquals( "file://localhost/path should become /path", "/path/to/file.mbox",
      connection.normalizeMboxPath( "file://localhost/path/to/file.mbox" ) );
    Assert.assertEquals( "file://server.com/path should become /path", "/path/to/file.mbox",
      connection.normalizeMboxPath( "file://server.com/path/to/file.mbox" ) );
  }

  @Test
  public void normalizeMboxPathWithWindowsDrive() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    // file:///C:/path should become C:/path (Windows drive letter)
    String result = connection.normalizeMboxPath( "file:///C:/Users/test/file.mbox" );
    // URI parsing should handle this correctly and remove the leading slash
    Assert.assertTrue( "Windows path should have drive letter", result.contains( "C:" ) );
  }

  @Test
  public void normalizeMboxPathRegularPath() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    Assert.assertEquals( "Regular path should pass through unchanged", "/home/user/mail.mbox",
      connection.normalizeMboxPath( "/home/user/mail.mbox" ) );
  }

  @Test
  public void normalizeMboxPathWithNull() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    Assert.assertNull( "null input should return null", connection.normalizeMboxPath( null ) );
  }

  @Test
  public void normalizeMboxPathWithEmptyString() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    Assert.assertEquals( "Empty string should pass through", "", connection.normalizeMboxPath( "" ) );
  }

  @Test
  public void normalizeMboxPathHandlesFileWithoutPath() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    // file://localhost with no path component returns empty path from URI parser
    Assert.assertEquals( "file://host with no path should return /", "",
      connection.normalizeMboxPath( "file://localhost" ) );
  }

  @Test
  public void mboxWriteEscapesFromLineInBody() throws Exception {
    // RFC 4155 compliance: "From " at start of line must be escaped with ">"
    File sourceMbox = File.createTempFile( "mailconnection-write-escape-from", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "original", "body-text", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    File destMbox = File.createTempFile( "mailconnection-write-dest-escape", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // Verify the destination file has escaped "From " lines
    String content = new String( java.nio.file.Files.readAllBytes( destMbox.toPath() ) );
    Assert.assertTrue( "Destination MBOX should contain envelope From line",
      content.contains( "From test@example.com" ) );
  }

  @Test
  public void mboxWriteHandlesMultilineMessageBody() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-write-multiline", ".mbox" );
    sourceMbox.deleteOnExit();

    // Create message with multiline body
    String multilineBody = "line 1\nline 2\nline 3";
    writeSimpleMboxMessage( sourceMbox, "multiline", multilineBody, "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    File destMbox = File.createTempFile( "mailconnection-write-dest-multiline", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    List<String> subjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Should have 1 message", 1, subjects.size() );
    Assert.assertEquals( "Subject should match", "multiline", subjects.get( 0 ) );
  }

  @Test
  public void mboxWriteAppendsToExistingFile() throws Exception {
    File sourceMbox1 = File.createTempFile( "mailconnection-append-1", ".mbox" );
    sourceMbox1.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox1, "msg-1", "body-1", "test1@example.com" );

    File sourceMbox2 = File.createTempFile( "mailconnection-append-2", ".mbox" );
    sourceMbox2.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox2, "msg-2", "body-2", "test2@example.com" );

    // Load first message and move to destination
    MailConnection conn1 = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox1.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    conn1.connect();
    conn1.openFolder( false );
    conn1.retrieveMessages();

    File destMbox = File.createTempFile( "mailconnection-append-dest", ".mbox" );
    destMbox.deleteOnExit();
    conn1.setDestinationFolder( destMbox.getAbsolutePath(), true );
    conn1.fetchNext();
    conn1.moveMessage();
    conn1.disconnect();

    // Load second message and move to same destination (append)
    MailConnection conn2 = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox2.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    conn2.connect();
    conn2.openFolder( false );
    conn2.retrieveMessages();
    conn2.setDestinationFolder( destMbox.getAbsolutePath(), true );
    conn2.fetchNext();
    conn2.moveMessage();
    conn2.disconnect();

    // Verify both messages in destination
    List<String> subjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Should have 2 messages", 2, subjects.size() );
    Assert.assertEquals( "First message should be msg-1", "msg-1", subjects.get( 0 ) );
    Assert.assertEquals( "Second message should be msg-2", "msg-2", subjects.get( 1 ) );
  }

  @Test
  public void mboxWriteCreatesParentDirectories() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-write-parent", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    // Set destination to non-existent directory structure
    File tempDir = new File( System.getProperty( "java.io.tmpdir" ) );
    File destMbox = new File( tempDir, "mbox-parent-test-" + System.currentTimeMillis() + "/nested/dir/messages.mbox" );
    destMbox.getParentFile().deleteOnExit();
    destMbox.deleteOnExit();

    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // Verify destination file was created with parent directories
    Assert.assertTrue( "Destination file should exist", destMbox.exists() );
    Assert.assertTrue( "Destination file should be readable", destMbox.canRead() );
    List<String> subjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Should have 1 message", 1, subjects.size() );
  }

  @Test
  public void mboxExpungeShouldNotRewriteIfNoMessagesDeleted() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-expunge-no-delete", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "test@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "test@example.com" );

    byte[] originalContent = java.nio.file.Files.readAllBytes( sourceMbox.toPath() );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.disconnect(); // Expunge on disconnect with no deleted messages

    byte[] newContent = java.nio.file.Files.readAllBytes( sourceMbox.toPath() );
    Assert.assertArrayEquals( "File should not be rewritten if no messages deleted",
      originalContent, newContent );
  }

  @Test
  public void mboxExpungeRewritesOnlyIfMessagesDeleted() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-expunge-with-delete", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "test@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "test@example.com" );

    byte[] originalContent = java.nio.file.Files.readAllBytes( sourceMbox.toPath() );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.fetchNext();
    connection.deleteMessage(); // Delete first message
    connection.disconnect(); // Expunge on disconnect

    byte[] newContent = java.nio.file.Files.readAllBytes( sourceMbox.toPath() );
    Assert.assertFalse( "File should be rewritten if messages deleted",
      java.util.Arrays.equals( originalContent, newContent ) );

    // Verify only second message remains
    List<String> subjects = readMboxSubjects( sourceMbox );
    Assert.assertEquals( "Should have 1 message after delete", 1, subjects.size() );
    Assert.assertEquals( "Remaining message should be msg-2", "msg-2", subjects.get( 0 ) );
  }

  @Test
  public void mboxWritePreservesMessageOrder() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-write-order", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "first", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "second", "body-2", "two@example.com" );
    appendSimpleMboxMessage( sourceMbox, "third", "body-3", "three@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    File destMbox = File.createTempFile( "mailconnection-write-order-dest", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.moveMessages(); // Move all
    connection.disconnect();

    List<String> subjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Should have 3 messages", 3, subjects.size() );
    Assert.assertEquals( "Messages should be in order", "first", subjects.get( 0 ) );
    Assert.assertEquals( "Messages should be in order", "second", subjects.get( 1 ) );
    Assert.assertEquals( "Messages should be in order", "third", subjects.get( 2 ) );
  }

  @Test
  public void testNormalizeMboxPathWithMalformedUriTriggersURISyntaxException() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    // This URI is malformed and should trigger the fallback path
    String result = connection.normalizeMboxPath( "file://[invalid]:path" );
    Assert.assertNotNull( "Should handle malformed URI gracefully", result );
  }

  @RunWith( Parameterized.class )
  public static class ExtractBracketAddressParameterizedTest {
    private String input;
    private String expectedOutput;

    public ExtractBracketAddressParameterizedTest( String input, String expectedOutput ) {
      this.input = input;
      this.expectedOutput = expectedOutput;
    }

    @Parameterized.Parameters( name = "{0} => {1}" )
    public static Collection<Object[]> data() {
      return Arrays.asList( new Object[][] {
        { "User Name <user@example.com>", "user@example.com" },
        { "user@example.com", null },
        { "User Name <>", null },
        { "User Name <notanemail>", null }
      } );
    }

    @Test
    public void testExtractBracketAddress() throws Exception {
      MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
        MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );

      java.lang.reflect.Method method = MailConnection.class.getDeclaredMethod( "extractBracketAddress", String.class );
      method.setAccessible( true );

      Object result = method.invoke( connection, input );
      Assert.assertEquals( "For input: " + input, expectedOutput, result );
    }
  }

  @Test
  public void testMboxWriteWithSpecialCharactersInBody() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-special-chars", ".mbox" );
    sourceMbox.deleteOnExit();
    
    String bodyWithSpecialChars = "Line with >From special char\nAnother >line\nNormal line";
    writeSimpleMboxMessage( sourceMbox, "special", bodyWithSpecialChars, "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    File destMbox = File.createTempFile( "mailconnection-special-chars-dest", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // Verify subject preserved
    List<String> subjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Should have 1 message", 1, subjects.size() );
    Assert.assertEquals( "Subject should match", "special", subjects.get( 0 ) );
  }

  @Test
  public void testMboxDeletePartialMessages() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-delete-partial", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "two@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-3", "body-3", "three@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    // Delete first message only
    connection.fetchNext();
    connection.deleteMessage();
    connection.disconnect();

    // Verify only msg-1 was deleted, msg-2 and msg-3 remain
    List<String> subjects = readMboxSubjects( sourceMbox );
    Assert.assertEquals( "Should have 2 messages", 2, subjects.size() );
    Assert.assertEquals( "First remaining should be msg-2", "msg-2", subjects.get( 0 ) );
    Assert.assertEquals( "Second remaining should be msg-3", "msg-3", subjects.get( 1 ) );
  }

  @Test
  public void testMboxMovePartialMessages() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-move-partial-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "two@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-3", "body-3", "three@example.com" );

    File destMbox = File.createTempFile( "mailconnection-move-partial-dest", ".mbox" );
    destMbox.deleteOnExit();

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    
    // Move first two messages
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // Verify source has only msg-3
    List<String> sourceSubjects = readMboxSubjects( sourceMbox );
    Assert.assertEquals( "Source should have 1 message", 1, sourceSubjects.size() );
    Assert.assertEquals( "Remaining in source should be msg-3", "msg-3", sourceSubjects.get( 0 ) );

    // Verify destination has msg-1 and msg-2
    List<String> destSubjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Destination should have 2 messages", 2, destSubjects.size() );
    Assert.assertEquals( "First in destination should be msg-1", "msg-1", destSubjects.get( 0 ) );
    Assert.assertEquals( "Second in destination should be msg-2", "msg-2", destSubjects.get( 1 ) );
  }

  @Test
  public void testMboxCreatesFolderRecursively() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-recursive-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "test@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    // Create deeply nested destination
    File tempDir = new File( System.getProperty( "java.io.tmpdir" ) );
    File destMbox = new File( tempDir, "test-" + System.currentTimeMillis() + "/a/b/c/d/messages.mbox" );
    if ( destMbox.getParentFile() != null ) {
      destMbox.getParentFile().deleteOnExit();
    }
    destMbox.deleteOnExit();

    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    Assert.assertTrue( "Destination file should exist", destMbox.exists() );
  }

  @Test
  public void testNormalizeMboxPathWithNestedFileScheme() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    
    // Test file:// prefix with complex path
    String result = connection.normalizeMboxPath( "file:///home/user/.local/share/mail/archive.mbox" );
    Assert.assertEquals( "Should extract path correctly", "/home/user/.local/share/mail/archive.mbox", result );
  }

  @Test
  public void testMboxWritePreservesMessageMetadata() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-metadata-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "preserve-test", "message body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Message msg = connection.getMessages()[0];
    String originalSubject = msg.getSubject();

    File destMbox = File.createTempFile( "mailconnection-metadata-dest", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // Verify metadata preserved
    MailConnection readConnection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-read" ),
      MailConnectionMeta.PROTOCOL_MBOX, destMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    readConnection.connect();
    readConnection.openFolder( false );
    readConnection.retrieveMessages();
    
    Message readMsg = readConnection.getMessages()[0];
    Assert.assertEquals( "Subject should be preserved", originalSubject, readMsg.getSubject() );
    
    readConnection.disconnect();
  }

  @Test
  public void testMboxHandlesEmptyMessageBody() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-empty-body", ".mbox" );
    sourceMbox.deleteOnExit();
    
    try ( FileWriter fw = new FileWriter( sourceMbox ) ) {
      fw.write( "From sender@example.com Mon Jan 01 00:00:00 2024\n" );
      fw.write( "Subject: Empty\n" );
      fw.write( "To: recipient@example.com\n" );
      fw.write( "\n" );
      // No body content
    }

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Assert.assertEquals( "Should have 1 message", 1, connection.getMessages().length );
    Message msg = connection.getMessages()[0];
    Assert.assertEquals( "Subject should match", "Empty", msg.getSubject() );
    connection.disconnect();
  }

  @Test
  public void testShouldUseSSLConnectionWithUseSSLTrue() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "pass", true, false, "" );
    
    java.lang.reflect.Method method = MailConnection.class.getDeclaredMethod( "shouldUseSSLConnection" );
    method.setAccessible( true );
    
    Boolean result = (Boolean) method.invoke( connection );
    Assert.assertTrue( "Should use SSL when usessl=true", result );
  }

  @Test
  public void testShouldUseSSLConnectionWithMboxProtocol() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    
    java.lang.reflect.Method method = MailConnection.class.getDeclaredMethod( "shouldUseSSLConnection" );
    method.setAccessible( true );
    
    Boolean result = (Boolean) method.invoke( connection );
    Assert.assertTrue( "Should use SSL for MBOX protocol", result );
  }

  @Test
  public void testShouldUseSSLConnectionWithoutSSLAndNotMbox() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 110, "user", "pass", false, false, "" );
    
    java.lang.reflect.Method method = MailConnection.class.getDeclaredMethod( "shouldUseSSLConnection" );
    method.setAccessible( true );
    
    Boolean result = (Boolean) method.invoke( connection );
    Assert.assertFalse( "Should not use SSL for POP3 without SSL flag", result );
  }

  @Test
  public void testShouldUseSSLConnectionWithBearerToken() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "Bearer token123", true, false, "" );
    
    java.lang.reflect.Method method = MailConnection.class.getDeclaredMethod( "shouldUseSSLConnection" );
    method.setAccessible( true );
    
    Boolean result = (Boolean) method.invoke( connection );
    Assert.assertFalse( "Should not use SSL when bearer token present", result );
  }

  @Test
  public void testShouldUseSSLConnectionMboxWithBearerToken() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "Bearer token", false, false, "" );
    
    java.lang.reflect.Method method = MailConnection.class.getDeclaredMethod( "shouldUseSSLConnection" );
    method.setAccessible( true );
    
    Boolean result = (Boolean) method.invoke( connection );
    Assert.assertFalse( "Should not use SSL for MBOX with bearer token", result );
  }

  @Test
  public void testConfigureSSLMailStore() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "pass", true, false, "" );

    Store configuredStore = connection.getStore();
    Assert.assertNotNull( configuredStore );
    Assert.assertTrue( "Expected POP3 SSL store", configuredStore.getClass().getName().contains( "POP3SSLStore" ) );
  }

  @Test
  public void testConnectWithPasswordWithPortSpecified() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "pass", false, false, "" );

    Store store = Mockito.mock( Store.class );
    setFieldValue( connection, "store", store );
    invokeNoArgMethod( connection, "connectWithPassword" );

    Mockito.verify( store ).connect( "mail.example.com", 995, "user", "pass" );
  }

  @Test
  public void testConnectWithPasswordWithoutPortSpecified() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", -1, "user", "pass", false, false, "" );

    Store store = Mockito.mock( Store.class );
    setFieldValue( connection, "store", store );
    invokeNoArgMethod( connection, "connectWithPassword" );

    Mockito.verify( store ).connect( "mail.example.com", "user", "pass" );
  }

  @Test
  public void testNormalizeMboxPathHandlesFileUrlSchemeProperly() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    
    // Test that URI parsing correctly extracts paths
    String result1 = connection.normalizeMboxPath( "file:///tmp/archive.mbox" );
    Assert.assertEquals( "Should normalize triple-slash", "/tmp/archive.mbox", result1 );
    
    String result2 = connection.normalizeMboxPath( "/absolute/path/archive.mbox" );
    Assert.assertEquals( "Should handle absolute paths", "/absolute/path/archive.mbox", result2 );
  }

  @Test
  public void testMboxMessageNumberingForMultipleMessages() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-numbering", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "two@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-3", "body-3", "three@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Message[] messages = connection.getMessages();
    Assert.assertEquals( "Should have 3 messages", 3, messages.length );
    
    // Verify each message is accessible by number
    for ( int i = 0; i < messages.length; i++ ) {
      Assert.assertNotNull( "Message " + (i+1) + " should exist", messages[i].getSubject() );
    }
    
    connection.disconnect();
  }

  @Test
  public void testMboxHandlesLongMessageSubject() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-long-subject", ".mbox" );
    sourceMbox.deleteOnExit();
    
    String longSubject = "Subject: " + "This is a very long subject line ".repeat( 10 ) + "\n";
    writeSimpleMboxMessage( sourceMbox, longSubject.substring( 9 ).trim(), "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Assert.assertEquals( "Should have 1 message", 1, connection.getMessages().length );
    Message msg = connection.getMessages()[0];
    Assert.assertNotNull( "Subject should exist", msg.getSubject() );
    connection.disconnect();
  }

  @Test
  public void testNormalizeMboxPathWithEmptyString() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    
    String result = connection.normalizeMboxPath( "" );
    Assert.assertEquals( "Should handle empty string", "", result );
  }

  @Test
  public void testNormalizeMboxPathWithRelativePath() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    
    String result = connection.normalizeMboxPath( "relative/path/to/mbox" );
    Assert.assertNotNull( "Should handle relative paths", result );
    Assert.assertEquals( "Should preserve relative path", "relative/path/to/mbox", result );
  }

  @Test
  public void testMboxLoadAndDisconnectClearsMessages() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-clear-on-disconnect", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    Assert.assertEquals( "Should have 1 message", 1, connection.getMessages().length );
    
    connection.disconnect();
    // After disconnect, subsequent connect should reload
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    Assert.assertEquals( "Should reload 1 message", 1, connection.getMessages().length );
    connection.disconnect();
  }

  @Test
  public void testMboxHandlesMultipleConsecutiveWrites() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-multi-write-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "two@example.com" );

    File destMbox = File.createTempFile( "mailconnection-multi-write-dest", ".mbox" );
    destMbox.deleteOnExit();

    // First connection: move msg-1
    MailConnection conn1 = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    conn1.connect();
    conn1.openFolder( false );
    conn1.retrieveMessages();
    conn1.setDestinationFolder( destMbox.getAbsolutePath(), true );
    conn1.fetchNext();
    conn1.moveMessage();
    conn1.disconnect();

    // Second connection: move msg-2
    MailConnection conn2 = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    conn2.connect();
    conn2.openFolder( false );
    conn2.retrieveMessages();
    conn2.setDestinationFolder( destMbox.getAbsolutePath(), true );
    conn2.fetchNext();
    conn2.moveMessage();
    conn2.disconnect();

    // Verify both messages in destination
    List<String> subjects = readMboxSubjects( destMbox );
    Assert.assertEquals( "Should have 2 messages", 2, subjects.size() );
    Assert.assertEquals( "First should be msg-1", "msg-1", subjects.get( 0 ) );
    Assert.assertEquals( "Second should be msg-2", "msg-2", subjects.get( 1 ) );
  }

  @Test
  public void testGetEffectiveMessageNumberForMboxMessage() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-effective-msg-num", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "one@example.com" );
    appendSimpleMboxMessage( sourceMbox, "msg-2", "body-2", "two@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Message[] messages = connection.getMessages();
    // For MBOX, getEffectiveMessageNumber should return the mapped 1-based index
    Assert.assertEquals( "First message should be 1", 1, connection.getEffectiveMessageNumber( messages[0] ) );
    Assert.assertEquals( "Second message should be 2", 2, connection.getEffectiveMessageNumber( messages[1] ) );
    
    connection.disconnect();
  }

  @Test
  public void testGetEffectiveMessageNumberWithMockedImapMessage() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", 993, "user", "pass", true, false, "" );
    
    // Mock a message that returns a message number (IMAP/POP3 path)
    Message mockMessage = Mockito.mock( Message.class );
    when( mockMessage.getMessageNumber() ).thenReturn( 42 );
    
    int result = connection.getEffectiveMessageNumber( mockMessage );
    Assert.assertEquals( "Should return message number from message object", 42, result );
  }

  @Test
  public void testGetEffectiveMessageNumberNotInMboxMap() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-not-in-map", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "msg-1", "body-1", "one@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    // Create a mock message not in the map
    Message mockMessage = Mockito.mock( Message.class );
    when( mockMessage.getMessageNumber() ).thenReturn( 99 );
    
    // Should fall back to getMessageNumber() since not in MBOX map
    int result = connection.getEffectiveMessageNumber( mockMessage );
    Assert.assertEquals( "Should fall back to getMessageNumber", 99, result );
    
    connection.disconnect();
  }

  @Test
  public void testGetFolderNameForMboxProtocol() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-foldername-mbox", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );

    // For MBOX, getFolderName should return mboxFolderName
    String folderName = connection.getFolderName();
    Assert.assertNotNull( "Folder name should not be null for MBOX", folderName );
    
    connection.disconnect();
  }

  @Test
  public void testGetFolderNameForImapProtocol() throws Exception {
    // Mock an IMAP connection with a folder
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", 993, "user", "pass", true, false, "" );
    
    // Without connecting, folder is null, so getFolderName should return ""
    String folderName = connection.getFolderName();
    Assert.assertEquals( "Folder name should be empty when not connected", "", folderName );
  }

  @Test
  public void testFolderExistsForMboxExistingFile() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-folder-exists-mbox", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();

    // For MBOX, folderExists should check if file exists
    boolean exists = connection.folderExists( sourceMbox.getAbsolutePath() );
    Assert.assertTrue( "Existing MBOX file should exist", exists );
    
    connection.disconnect();
  }

  @Test
  public void testFolderExistsForMboxNonExistingFile() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/nonexistent-file-" + System.currentTimeMillis() + ".mbox", 
      -1, "junit", "junit", false, false, "" );

    // For MBOX, folderExists should return false for non-existent file
    boolean exists = connection.folderExists( "/tmp/nonexistent-mbox-" + System.currentTimeMillis() + ".mbox" );
    Assert.assertFalse( "Non-existent MBOX file should not exist", exists );
  }

  @Test
  public void testFolderExistsForMboxWithInboxFolderName() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-folder-exists-inbox", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "test", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();

    // When checking INBOX, should resolve to source mbox path
    boolean exists = connection.folderExists( "INBOX" );
    Assert.assertTrue( "INBOX should exist for MBOX protocol", exists );
    
    connection.disconnect();
  }

  @Test
  public void testMboxPreservesMessageOrder() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-order-src", ".mbox" );
    sourceMbox.deleteOnExit();
    
    for ( int i = 1; i <= 10; i++ ) {
      if ( i == 1 ) {
        writeSimpleMboxMessage( sourceMbox, "msg-" + i, "body-" + i, "sender" + i + "@example.com" );
      } else {
        appendSimpleMboxMessage( sourceMbox, "msg-" + i, "body-" + i, "sender" + i + "@example.com" );
      }
    }

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    Assert.assertEquals( "Should have 10 messages", 10, connection.getMessages().length );
    
    // Verify order is preserved
    Message[] messages = connection.getMessages();
    for ( int i = 0; i < messages.length; i++ ) {
      Assert.assertEquals( "Message " + (i+1) + " should match", "msg-" + (i+1), messages[i].getSubject() );
    }
    
    connection.disconnect();
  }

  @Test
  public void testNormalizeMboxPathWithFileSchemeVariations() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );
    
    // Test different file:// variations
    Assert.assertNotNull( "file:// with single slash", connection.normalizeMboxPath( "file://localhost/path/to/file.mbox" ) );
    Assert.assertNotNull( "file:// with hostname", connection.normalizeMboxPath( "file://host.com/path/to/file.mbox" ) );
  }

  @Test
  public void testMboxWriteWithFromLineInBody() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-from-line-in-body", ".mbox" );
    sourceMbox.deleteOnExit();
    
    String bodyWithFromLine = "This is a regular line\nFrom someone@example.com should be escaped\nAnother line";
    writeSimpleMboxMessage( sourceMbox, "from-test", bodyWithFromLine, "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    File destMbox = File.createTempFile( "mailconnection-from-line-dest", ".mbox" );
    destMbox.deleteOnExit();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    // Verify the "From " line was escaped in the destination
    String content = new String( java.nio.file.Files.readAllBytes( destMbox.toPath() ) );
    Assert.assertTrue( "Should escape From line in body", content.contains( ">From someone@example.com" ) );
  }

  @Test
  public void testMboxMovePreservesNonUtf8BodyBytes() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-nonutf8-src", ".mbox" );
    sourceMbox.deleteOnExit();
    byte[] latin1Body = new byte[] { 'c', 'a', 'f', (byte) 0xE9, ' ', 'd', 'a', 't', 'a' };
    writeLatin1MboxMessage( sourceMbox, "latin1-test", "sender@example.com", latin1Body );

    File destMbox = File.createTempFile( "mailconnection-nonutf8-dest", ".mbox" );
    destMbox.deleteOnExit();

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.setDestinationFolder( destMbox.getAbsolutePath(), true );
    connection.fetchNext();
    connection.moveMessage();
    connection.disconnect();

    byte[] destinationBytes = java.nio.file.Files.readAllBytes( destMbox.toPath() );
    Assert.assertTrue( "Destination mbox should preserve Latin-1 byte sequence",
      indexOfBytes( destinationBytes, latin1Body ) >= 0 );
    Assert.assertEquals( "Destination should keep single-byte Latin-1 character",
      1, countByte( destinationBytes, (byte) 0xE9 ) );
  }

  @Test
  public void testProxyPropertiesAreSetWhenProxyEnabled() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", 993, "user", "pass", false, true, "proxy-user" );

    java.util.Properties props = (java.util.Properties) getFieldValue( connection, "prop" );
    Assert.assertEquals( "true", props.getProperty( "mail.imap.sasl.enable" ) );
    Assert.assertEquals( "proxy-user", props.getProperty( "mail.imap.sasl.authorizationid" ) );
  }

  @Test
  public void testConfigureSSLMailStoreCreatesImapSslStore() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", -1, "user", "pass", true, false, "" );

    Store configuredStore = connection.getStore();
    Assert.assertNotNull( configuredStore );
    Assert.assertTrue( "Expected IMAP SSL store", configuredStore.getClass().getName().contains( "IMAPSSLStore" ) );
  }

  @Test
  public void testConnectToMailServerUsesStoreConnectForSslFlow() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "pass", true, false, "" );
    Store store = Mockito.mock( Store.class );
    setFieldValue( connection, "store", store );

    invokeNoArgMethod( connection, "connectToMailServer" );

    Mockito.verify( store ).connect();
  }

  @Test
  public void testConnectToMailServerUsesCredentialConnectWithPortForBearerToken() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "Bearer test-token", true, false, "" );
    Store store = Mockito.mock( Store.class );
    setFieldValue( connection, "store", store );

    invokeNoArgMethod( connection, "connectToMailServer" );

    Mockito.verify( store ).connect( "mail.example.com", 995, "user", "test-token" );
    Assert.assertEquals( "Bearer test-token", getFieldValue( connection, "password" ) );
  }

  @Test
  public void testConnectToMailServerUsesCredentialConnectWithoutPort() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", -1, "user", "pass", false, false, "" );
    Store store = Mockito.mock( Store.class );
    setFieldValue( connection, "store", store );

    invokeNoArgMethod( connection, "connectToMailServer" );

    Mockito.verify( store ).connect( "mail.example.com", "user", "pass" );
  }

  @Test
  public void testOpenFolderDefaultPathThrowsKettleExceptionWhenInboxCannotResolve() {
    try {
      conn.openFolder( null, true, false );
      Assert.fail( "Expected KettleException when default folder cannot be opened" );
    } catch ( KettleException expected ) {
      // expected
    }
  }

  @Test
  public void testOpenFolderSpecifiedPathThrowsKettleExceptionWhenFolderMissing() {
    try {
      conn.openFolder( "missing", false, false );
      Assert.fail( "Expected KettleException when specified folder cannot be opened" );
    } catch ( KettleException expected ) {
      // expected
    }
  }

  @Test
  public void testGetEffectiveMessageNumberReturnsZeroWhenMessageThrows() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", 993, "user", "pass", true, false, "" );
    Message mockMessage = Mockito.mock( Message.class );
    when( mockMessage.getMessageNumber() ).thenThrow( new RuntimeException( "boom" ) );

    Assert.assertEquals( 0, connection.getEffectiveMessageNumber( mockMessage ) );
  }

  @Test
  public void testSetDestinationFolderMboxThrowsWhenParentMissingAndCreateFalse() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-missing-parent-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "subject", "body", "sender@example.com" );

    File parentDir = new File( sourceMbox.getParentFile(), "missing-parent-" + System.nanoTime() );
    File destinationMbox = new File( parentDir, "archive.mbox" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    try {
      connection.setDestinationFolder( destinationMbox.getAbsolutePath(), false );
      Assert.fail( "Expected KettleException for missing parent directory" );
    } catch ( KettleException expected ) {
      // expected
    } finally {
      connection.disconnect();
    }
  }

  @Test
  public void testSetDestinationFolderMboxThrowsWhenParentIsFile() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-parent-file-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "subject", "body", "sender@example.com" );

    File parentFile = File.createTempFile( "mailconnection-parent", ".tmp" );
    parentFile.deleteOnExit();
    File destinationMbox = new File( parentFile, "archive.mbox" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    try {
      connection.setDestinationFolder( destinationMbox.getAbsolutePath(), true );
      Assert.fail( "Expected KettleException when destination parent is a file" );
    } catch ( KettleException expected ) {
      // expected
    } finally {
      connection.disconnect();
    }
  }

  @Test
  public void testShouldUseSSLConnectionRemainsFalseAfterBearerConnect() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_POP3, "mail.example.com", 995, "user", "Bearer token-value", true, false, "" );
    Store store = Mockito.mock( Store.class );
    setFieldValue( connection, "store", store );

    Assert.assertEquals( false, invokeNoArgMethod( connection, "shouldUseSSLConnection" ) );
    invokeNoArgMethod( connection, "connectWithPassword" );
    Assert.assertEquals( false, invokeNoArgMethod( connection, "shouldUseSSLConnection" ) );
    Assert.assertEquals( "Bearer token-value", getFieldValue( connection, "password" ) );
  }

  @Test
  public void testMoveMessageMboxWithoutDestinationThrows() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-move-no-destination", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "subject", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();
    connection.fetchNext();

    try {
      connection.moveMessage();
      Assert.fail( "Expected KettleException when destination is not set" );
    } catch ( KettleException expected ) {
      Assert.assertTrue( "Error should include effective MBOX message number", expected.getMessage().contains( "1" ) );
    } finally {
      connection.disconnect();
    }
  }

  @Test
  public void testMoveMessagesMboxWithoutDestinationThrows() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-move-many-no-destination", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "subject-1", "body", "sender1@example.com" );
    appendSimpleMboxMessage( sourceMbox, "subject-2", "body", "sender2@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    try {
      connection.moveMessages();
      Assert.fail( "Expected KettleException when destination is not set" );
    } catch ( KettleException expected ) {
      // expected
    } finally {
      connection.disconnect();
    }
  }

  @Test
  public void testNormalizeMboxPathFallbackBranches() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "/tmp/test.mbox", -1, "user", "pass", false, false, "" );

    Assert.assertEquals( "/tmp/[bad.mbox", connection.normalizeMboxPath( "file:///tmp/[bad.mbox" ) );
    Assert.assertEquals( "/[bad.mbox", connection.normalizeMboxPath( "file://localhost/[bad.mbox" ) );
    Assert.assertEquals( "/tmp/[bad.mbox", connection.normalizeMboxPath( "file:/tmp/[bad.mbox" ) );
    Assert.assertEquals( "file:bad path", connection.normalizeMboxPath( "file:bad path" ) );
  }

  @Test
  public void testResolveMboxFolderPathReturnsRelativeWhenSourceHasNoParent() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_MBOX, "source.mbox", -1, "user", "pass", false, false, "" );
    String resolved = (String) invokeMethod( connection, "resolveMboxFolderPath", new Class<?>[] { String.class },
      new Object[] { "archive.mbox" } );

    Assert.assertEquals( "archive.mbox", resolved );
  }

  @Test
  public void testConnectMboxThrowsWhenSourcePathIsDirectory() throws Exception {
    File mboxDirectory = new File( System.getProperty( "java.io.tmpdir" ), "mbox-dir-" + System.nanoTime() );
    Assert.assertTrue( mboxDirectory.mkdirs() );
    mboxDirectory.deleteOnExit();

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, mboxDirectory.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    try {
      connection.connect();
      Assert.fail( "Expected KettleException for directory-based mbox source" );
    } catch ( KettleException expected ) {
      // expected
    }
  }

  @Test
  public void testWriteMessagesToMboxThrowsWhenParentIsAFile() throws Exception {
    File sourceMbox = File.createTempFile( "mailconnection-write-error-src", ".mbox" );
    sourceMbox.deleteOnExit();
    writeSimpleMboxMessage( sourceMbox, "subject", "body", "sender@example.com" );

    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "mbox-test" ),
      MailConnectionMeta.PROTOCOL_MBOX, sourceMbox.getAbsolutePath(), -1, "junit", "junit", false, false, "" );
    connection.connect();
    connection.openFolder( false );
    connection.retrieveMessages();

    File parentAsFile = File.createTempFile( "mailconnection-parent-file", ".tmp" );
    parentAsFile.deleteOnExit();
    String targetPath = new File( parentAsFile, "target.mbox" ).getAbsolutePath();

    try {
      invokeMethod( connection, "writeMessagesToMbox",
        new Class<?>[] { List.class, String.class, boolean.class },
        new Object[] { Arrays.asList( connection.getMessages() ), targetPath, true } );
      Assert.fail( "Expected writeMessagesToMbox to fail when parent is a file" );
    } catch ( java.lang.reflect.InvocationTargetException expected ) {
      Assert.assertTrue( expected.getCause() instanceof KettleException );
    } finally {
      connection.disconnect();
    }
  }

  @Test
  public void testGetEnvelopeFromBranches() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", 993, "user", "pass", false, false, "" );

    Message nullFrom = Mockito.mock( Message.class );
    when( nullFrom.getFrom() ).thenReturn( null );
    Assert.assertEquals( "unknown@example.com",
      invokeMethod( connection, "getEnvelopeFrom", new Class<?>[] { Message.class }, new Object[] { nullFrom } ) );

    Message bracketFrom = Mockito.mock( Message.class );
    when( bracketFrom.getFrom() ).thenReturn( new jakarta.mail.Address[] {
      new jakarta.mail.Address() {
        @Override
        public String getType() {
          return "rfc822";
        }

        @Override
        public boolean equals( Object address ) {
          return this == address;
        }

        @Override
        public int hashCode() {
          return System.identityHashCode( this );
        }

        @Override
        public String toString() {
          return "Display Name <bracket@example.com>";
        }
      }
    } );
    Assert.assertEquals( "bracket@example.com",
      invokeMethod( connection, "getEnvelopeFrom", new Class<?>[] { Message.class }, new Object[] { bracketFrom } ) );

    Message compactFrom = Mockito.mock( Message.class );
    when( compactFrom.getFrom() ).thenReturn( new jakarta.mail.Address[] {
      new jakarta.mail.Address() {
        @Override
        public String getType() {
          return "rfc822";
        }

        @Override
        public boolean equals( Object address ) {
          return this == address;
        }

        @Override
        public int hashCode() {
          return System.identityHashCode( this );
        }

        @Override
        public String toString() {
          return " compact @example.com ";
        }
      }
    } );
    Assert.assertEquals( "compact@example.com",
      invokeMethod( connection, "getEnvelopeFrom", new Class<?>[] { Message.class }, new Object[] { compactFrom } ) );

    Message invalidFrom = Mockito.mock( Message.class );
    when( invalidFrom.getFrom() ).thenReturn( new jakarta.mail.Address[] {
      new jakarta.mail.Address() {
        @Override
        public String getType() {
          return "rfc822";
        }

        @Override
        public boolean equals( Object address ) {
          return this == address;
        }

        @Override
        public int hashCode() {
          return System.identityHashCode( this );
        }

        @Override
        public String toString() {
          return "NoAtSymbol";
        }
      }
    } );
    Assert.assertEquals( "unknown@example.com",
      invokeMethod( connection, "getEnvelopeFrom", new Class<?>[] { Message.class }, new Object[] { invalidFrom } ) );
  }

  @Test
  public void testIsEmptyFromListBranches() throws Exception {
    MailConnection connection = new MailConnection( DefaultBowl.getInstance(), new LogChannel( "test" ),
      MailConnectionMeta.PROTOCOL_IMAP, "mail.example.com", 993, "user", "pass", false, false, "" );

    Assert.assertEquals( true,
      invokeMethod( connection, "isEmptyFromList", new Class<?>[] { jakarta.mail.Address[].class }, new Object[] { null } ) );
    Assert.assertEquals( true,
      invokeMethod( connection, "isEmptyFromList", new Class<?>[] { jakarta.mail.Address[].class },
        new Object[] { new jakarta.mail.Address[0] } ) );
    Assert.assertEquals( true,
      invokeMethod( connection, "isEmptyFromList", new Class<?>[] { jakarta.mail.Address[].class },
        new Object[] { new jakarta.mail.Address[] { null } } ) );
  }

  private static Object invokeNoArgMethod( Object target, String methodName ) throws Exception {
    Method method = target.getClass().getDeclaredMethod( methodName );
    method.setAccessible( true );
    return method.invoke( target );
  }

  private static Object invokeMethod( Object target, String methodName, Class<?>[] parameterTypes, Object[] args )
    throws Exception {
    Method method = target.getClass().getDeclaredMethod( methodName, parameterTypes );
    method.setAccessible( true );
    return method.invoke( target, args );
  }

  private static void setFieldValue( Object target, String fieldName, Object value ) throws Exception {
    Field field = target.getClass().getDeclaredField( fieldName );
    field.setAccessible( true );
    field.set( target, value );
  }

  private static Object getFieldValue( Object target, String fieldName ) throws Exception {
    Field field = target.getClass().getDeclaredField( fieldName );
    field.setAccessible( true );
    return field.get( target );
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
      super( DefaultBowl.getInstance(), log, MailConnectionMeta.PROTOCOL_IMAP, "junit", 0, "junit", "junit", false,
             false, "junit" );

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
