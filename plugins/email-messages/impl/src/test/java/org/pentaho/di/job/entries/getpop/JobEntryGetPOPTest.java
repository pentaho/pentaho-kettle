/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/

package org.pentaho.di.job.entries.getpop;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Flags.Flag;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.util.HttpClientManager;
import org.pentaho.di.core.util.Utils;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.utils.TestUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JobEntryGetPOPTest {

  @Mock
  MailConnection mailConn;
  @Mock
  Job parentJob;
  @Mock
  Message message;
  @Mock
  private HttpClientManager httpClientManager;
  @Mock
  private CloseableHttpClient httpClient;
  @Mock
  private CloseableHttpResponse httpResponse;

  JobEntryGetPOP entry = new JobEntryGetPOP();

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks( this );

    httpClientManager = mock( HttpClientManager.class );
    httpClient = mock( CloseableHttpClient.class );
    httpResponse = mock( CloseableHttpResponse.class );

    when( parentJob.getLogLevel() ).thenReturn( LogLevel.BASIC );
    entry.setParentJob( parentJob );
    entry.setParentJobMeta( new JobMeta() );
    entry.setSaveMessage( true );

    when( message.getMessageNumber() ).thenReturn( 1 );
    when( message.getContent() ).thenReturn( createMessageContent() );
    when( mailConn.getMessage() ).thenReturn( message );

    doNothing().when( mailConn ).openFolder( Mockito.anyBoolean() );
    doNothing().when( mailConn ).openFolder( Mockito.anyString(), Mockito.anyBoolean() );

    when( mailConn.getMessagesCount() ).thenReturn( 1 );
  }

  private Object createMessageContent() throws Exception {
    MimeMultipart content = new MimeMultipart();
    MimeBodyPart contentText = new MimeBodyPart();
    contentText.setText( "Hello World!" );
    content.addBodyPart( contentText );

    MimeBodyPart contentFile = new MimeBodyPart();
    File testFile = TestUtils.getInputFile( "GetPOP", "txt" );
    FileDataSource fds = new FileDataSource( testFile.getAbsolutePath() );
    contentFile.setDataHandler( new DataHandler( fds ) );
    contentFile.setFileName( testFile.getName() );
    content.addBodyPart( contentFile );

    return content;
  }

  /**
   * PDI-10942 - Job get emails JobEntry does not mark emails as 'read' when load emails content.
   * <p>
   * Test that we always open remote folder in rw mode, and after email attachment is loaded email is marked as read.
   * Set for openFolder rw mode if this is pop3.
   */
  @Test
  public void testFetchOneFolderModePop3() throws Exception {
    entry.fetchOneFolder( mailConn, true, "junitImapFolder", "junitRealOutputFolder", "junitTargetAttachmentFolder",
      "junitRealMoveToIMAPFolder", "junitRealFilenamePattern", 0, mock( SimpleDateFormat.class ) );
    Mockito.verify( mailConn ).openFolder( true );
    Mockito.verify( message ).setFlag( Flag.SEEN, true );
  }

  /**
   * PDI-10942 - Job get emails JobEntry does not mark emails as 'read' when load emails content.
   * <p>
   * Test that we always open remote folder in rw mode, and after email attachment is loaded email is marked as read.
   * protocol IMAP and default remote folder is overridden
   */
  @Test
  public void testFetchOneFolderModeIMAPWithNonDefFolder() throws Exception {
    entry.fetchOneFolder( mailConn, false, "junitImapFolder", "junitRealOutputFolder", "junitTargetAttachmentFolder",
      "junitRealMoveToIMAPFolder", "junitRealFilenamePattern", 0, mock( SimpleDateFormat.class ) );
    Mockito.verify( mailConn ).openFolder( "junitImapFolder", true );
    Mockito.verify( message ).setFlag( Flag.SEEN, true );
  }

  /**
   * PDI-10942 - Job get emails JobEntry does not mark emails as 'read' when load emails content.
   * <p>
   * Test that we always open remote folder in rw mode, and after email attachment is loaded email is marked as read.
   * protocol IMAP and default remote folder is NOT overridden
   */
  @Test
  public void testFetchOneFolderModeIMAPWithIsDefFolder() throws Exception {
    entry.fetchOneFolder( mailConn, false, null, "junitRealOutputFolder", "junitTargetAttachmentFolder",
      "junitRealMoveToIMAPFolder", "junitRealFilenamePattern", 0, mock( SimpleDateFormat.class ) );
    Mockito.verify( mailConn ).openFolder( true );
    Mockito.verify( message ).setFlag( Flag.SEEN, true );
  }

  /**
   * PDI-11943 - Get Mail Job Entry: Attachments folder not created
   * <p>
   * Test that the Attachments folder is created when the entry is
   * configured to save attachments and messages in the same folder
   */
  @Test
  public void testCreateSameAttachmentsFolder() {
    File attachmentsDir = new File( TestUtils.createTempDir() );
    attachmentsDir.deleteOnExit();

    entry.setCreateLocalFolder( true );
    entry.setSaveAttachment( true );
    entry.setOutputDirectory( attachmentsDir.getAbsolutePath() );
    entry.setDifferentFolderForAttachment( false );

    String outputFolderName = "";
    String attachmentsFolderName = "";
    try {
      outputFolderName = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_OUTPUT );
      attachmentsFolderName = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );
    } catch ( Exception e ) {
      fail( "Could not create folder " + e.getLocalizedMessage() );
    }

    assertFalse( "Output Folder should be a local path", Utils.isEmpty( outputFolderName ) );
    assertFalse( "Attachment Folder should be a local path", Utils.isEmpty( attachmentsFolderName ) );
    assertEquals( "Output and Attachment Folder should match", outputFolderName, attachmentsFolderName );
  }

  /**
   * PDI-11943 - Get Mail Job Entry: Attachments folder not created
   * <p>
   * Test that the Attachments folder is created when the entry is
   * configured to save attachments and messages in different folders
   */
  @Test
  public void testCreateDifferentAttachmentsFolder() {
    File outputDir = new File( TestUtils.createTempDir() );
    File attachmentsDir = new File( TestUtils.createTempDir() );

    entry.setCreateLocalFolder( true );
    entry.setSaveAttachment( true );
    entry.setOutputDirectory( outputDir.getAbsolutePath() );
    entry.setDifferentFolderForAttachment( true );
    entry.setAttachmentFolder( attachmentsDir.getAbsolutePath() );

    String outputFolderName = "";
    String attachmentsFolderName = "";
    try {
      outputFolderName = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_OUTPUT );
      attachmentsFolderName = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );
    } catch ( Exception e ) {
      fail( "Could not create folder: " + e.getLocalizedMessage() );
    }

    assertFalse( "Output Folder should be a local path", Utils.isEmpty( outputFolderName ) );
    assertFalse( "Attachment Folder should be a local path", Utils.isEmpty( attachmentsFolderName ) );
    assertNotEquals( "Output and Attachment Folder should not match", outputFolderName, attachmentsFolderName );
  }

  /**
   * PDI-11943 - Get Mail Job Entry: Attachments folder not created
   * <p>
   * Test that the Attachments folder is not created when the entry is
   * configured to not create folders
   */
  @Test
  public void testFolderIsNotCreatedWhenCreateFolderSettingIsDisabled() {
    File outputDir = new File( TestUtils.createTempDir() );
    File attachmentsDir = new File( TestUtils.createTempDir() );
    // The folders already exist from TestUtils.  Delete them so they don't exist during the test
    outputDir.delete();
    attachmentsDir.delete();

    entry.setCreateLocalFolder( false );
    entry.setSaveAttachment( true );
    entry.setOutputDirectory( outputDir.getAbsolutePath() );
    entry.setDifferentFolderForAttachment( true );
    entry.setAttachmentFolder( attachmentsDir.getAbsolutePath() );

    try {
      entry.createOutputDirectory( JobEntryGetPOP.FOLDER_OUTPUT );
      fail( "A KettleException should have been thrown" );
    } catch ( Exception e ) {
      if ( e instanceof KettleException ) {
        assertEquals( "Output Folder should not be created",
          BaseMessages.getString( JobEntryGetPOP.class,
            "JobGetMailsFromPOP.Error.OutputFolderNotExist", outputDir.getAbsolutePath() ),
          Const.trim( e.getMessage() ) ); ;
      } else {
        fail( "Output Folder should not have been created: " + e.getLocalizedMessage() );
      }
    }
    try {
      entry.createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );
      fail( "A KettleException should have been thrown" );
    } catch ( Exception e ) {
      if ( e instanceof KettleException ) {
        assertEquals( "Output Folder should not be created",
          BaseMessages.getString( JobEntryGetPOP.class,
            "JobGetMailsFromPOP.Error.AttachmentFolderNotExist", attachmentsDir.getAbsolutePath() ),
          Const.trim( e.getMessage() ) );
      } else {
        fail( "Attachments Folder should not have been created: " + e.getLocalizedMessage() );
      }
    }
  }

  /**
   * PDI-14305 - Get Mails (POP3/IMAP) Not substituting environment variables for target directories
   * <p>
   * Test that environment variables are appropriately substituted when creating output and attachment folders
   */
  @Test
  public void testEnvVariablesAreSubstitutedForFolders() {
    // create variables and add them to the variable space
    String outputVariableName = "myOutputVar";
    String outputVariableValue = "myOutputFolder";
    String attachmentVariableName = "myAttachmentVar";
    String attachmentVariableValue = "myOutputFolder";
    entry.setVariable( outputVariableName, outputVariableValue );
    entry.setVariable( attachmentVariableName, attachmentVariableValue );

    // create temp directories for testing using variable value
    String tempDirBase = TestUtils.createTempDir();
    File outputDir = new File( tempDirBase, outputVariableValue );
    outputDir.mkdir();
    File attachmentDir = new File( tempDirBase, attachmentVariableValue );
    attachmentDir.mkdir();

    // set output and attachment folders to path with variable
    String outputDirWithVariable = tempDirBase + File.separator + "${" + outputVariableName + "}";
    String attachmentDirWithVariable = tempDirBase + File.separator + "${" + attachmentVariableName + "}";
    entry.setOutputDirectory( outputDirWithVariable );
    entry.setAttachmentFolder( attachmentDirWithVariable );

    // directly test environment substitute functions
    assertEquals( "Error in Direct substitute test for output directory", outputDir.toString(),
      entry.getRealOutputDirectory() );
    assertEquals( "Error in Direct substitute test for  attachment directory", attachmentDir.toString(),
      entry.getRealAttachmentFolder() );

    // test environment substitute for output dir via createOutputDirectory method
    try {
      String outputRes = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_OUTPUT );
      assertEquals( "Variables not working in createOutputDirectory: output directory", outputDir.toString(),
        outputRes );
    } catch ( Exception e ) {
      fail( "Unexpected exception when calling createOutputDirectory for output directory" );
    }

    // test environment substitute for attachment dir via createOutputDirectory method
    try {
      String attachOutputRes = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );
      assertEquals( "Variables not working in createOutputDirectory: attachment with options false",
        outputDir.toString(),
        attachOutputRes );
      // set options that trigger alternate path for FOLDER_ATTACHMENTS option
      entry.setSaveAttachment( true );
      entry.setDifferentFolderForAttachment( true );
      String attachRes = entry.createOutputDirectory( JobEntryGetPOP.FOLDER_ATTACHMENTS );
      assertEquals( "Variables not working in createOutputDirectory: attachment with options true",
        outputDir.toString(),
        attachRes );
    } catch ( Exception e ) {
      fail( "Unexpected exception when calling createOutputDirectory for attachment directory" );
    }
  }

  @Test
  public void testGrantTypeIsClientCredentials() {
    entry.setGrant_type( JobEntryGetPOP.GRANTTYPE_CLIENTCREDENTIALS );
    assertEquals( JobEntryGetPOP.GRANTTYPE_CLIENTCREDENTIALS, entry.getGrant_type() );
  }

  @Test
  public void testGrantTypeIsAuthorizationCode() {
    entry.setGrant_type( JobEntryGetPOP.GRANTTYPE_AUTHORIZATION_CODE );
    assertEquals( JobEntryGetPOP.GRANTTYPE_AUTHORIZATION_CODE, entry.getGrant_type() );
  }

  @Test( expected = NullPointerException.class )
  public void testGetOauthTokenThrowsExceptionOnUnsuccessfulResponse() throws IOException {
    String tokenUrl = "http://example.com/token";

    StatusLine statusLine = new BasicStatusLine( new ProtocolVersion( "HTTP", 1, 1 ), 400, "Bad Request" );

    when( httpClientManager.createDefaultClient() ).thenReturn( httpClient );
    when( httpClient.execute( any( HttpPost.class ) ) ).thenReturn( httpResponse );
    when( httpResponse.getStatusLine() ).thenReturn( statusLine );

    entry.getOauthToken( tokenUrl );
  }

  @Test( expected = NullPointerException.class )
  public void testGetOauthTokenThrowsExceptionOnHttpClientExecuteFailure() throws IOException {
    String tokenUrl = "http://example.com/token";

    when( httpClientManager.createDefaultClient() ).thenReturn( httpClient );
    when( httpClient.execute( any( HttpPost.class ) ) ).thenThrow( new IOException() );
    entry.getOauthToken( tokenUrl );
  }

  @Test( expected = RuntimeException.class )
  public void testGetOauthTokenThrowsExceptionOnHttpError() throws Exception {
    entry.setGrant_type( JobEntryGetPOP.GRANTTYPE_REFRESH_TOKEN );
    entry.setRefresh_token( "refresh_token_value" );
    entry.setTokenUrl( "http://example.com/token" );

    when( httpClient.execute( any( HttpPost.class ) ) ).thenReturn( httpResponse );
    when( httpResponse.getStatusLine().getStatusCode() ).thenReturn( 500 );

    entry.getOauthToken( "http://example.com/token" );
  }

  @Test
  public void testAuthorizationCodeAndRedirectUri() {
    JobEntryGetPOP jobEntry = new JobEntryGetPOP();
    String authorizationCode = "testAuthCode";
    String redirectUri = "http://test.redirect.uri";

    jobEntry.setAuthorization_code( authorizationCode );
    jobEntry.setRedirectUri( redirectUri );

    assertEquals( authorizationCode, jobEntry.getAuthorization_code() );
    assertEquals( redirectUri, jobEntry.getRedirectUri() );
  }

  @Test( expected = RuntimeException.class )
  public void testGetOauthTokenThrowsExceptionOnIOException() throws Exception {
    entry.setGrant_type( JobEntryGetPOP.GRANTTYPE_REFRESH_TOKEN );
    entry.setRefresh_token( "refresh_token_value" );
    entry.setTokenUrl( "http://example.com/token" );

    when( httpClient.execute( any( HttpPost.class ) ) ).thenThrow( new IOException() );

    entry.getOauthToken( "http://example.com/token" );
  }

  @Test
  public void testAuthenticationTypeNull() {
    entry.setUsingAuthentication( null );
    assertEquals( JobEntryGetPOP.AUTENTICATION_BASIC, entry.isUsingAuthentication() );
  }

  @Test
  public void testAuthenticationTypeEmpty() {
    entry.setUsingAuthentication( "" );
    assertEquals( JobEntryGetPOP.AUTENTICATION_BASIC, entry.isUsingAuthentication() );
  }

  @Test
  public void testAuthenticationTypeUnrecognized() {
    entry.setUsingAuthentication( "Unrecognized" );
    assertEquals( JobEntryGetPOP.AUTENTICATION_BASIC, entry.isUsingAuthentication() );
  }

  @Test
  public void testAuthenticationTypeBasic() {
    entry.setUsingAuthentication( JobEntryGetPOP.AUTENTICATION_BASIC );
    assertEquals( JobEntryGetPOP.AUTENTICATION_BASIC, entry.isUsingAuthentication() );
  }

  @Test
  public void testAuthenticationTypeOAuth() {
    entry.setUsingAuthentication( JobEntryGetPOP.AUTENTICATION_OAUTH );
    assertEquals( JobEntryGetPOP.AUTENTICATION_OAUTH, entry.isUsingAuthentication() );
  }
}
