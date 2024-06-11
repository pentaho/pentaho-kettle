/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2023 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.trans.steps.mail;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.RowSet;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;
import org.pentaho.di.trans.steps.mock.StepMockHelper;
import org.springframework.util.ReflectionUtils;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Collections;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.pentaho.di.core.util.Assert.assertTrue;

public class MailTest {

  private static final String MAIL_CHARSET_KEY = "mail.mime.charset";
  private static final String FILE_ENCODING_KEY = "file.encoding";
  private static final String MAIL_CHARSET = "UTF-8";
  private static final String FILE_ENCODING = "Cp1252";
  private static final String SPECIAL_CHARS = "サブジェクト";
  private static final String SPECIAL_CHARS_ENCODED = "=?UTF-8?B?44K144OW44K444Kn44Kv44OI?=";
  // MimeUtility actually sets the Line Separator that is more Microsoft friendly
  private static final String SPECIAL_CHARS_FILE_ENCODED = "attachment; \r\n"
    + "\tfilename*=UTF-8''%E3%82%B5%E3%83%96%E3%82%B8%E3%82%A7%E3%82%AF%E3%83%88";
  private static String wasMailMimeCharset;
  private static String wasFileEncoding;
  @ClassRule public static RestorePDIEngineEnvironment env = new RestorePDIEngineEnvironment();

  private static final String SMTP_HOST = "smtp_host";
  private static final String SMTP_AUTH_USER = "smtp_auth_user";
  private static final String SMTP_AUTH_PASSWORD = "smtp_auth_password";
  private static final String EMAIL_RECIPIENT = "email_recipient";
  private static final String EMAIL_SENDER_NAME = "email_sender_name";
  private static final String EMAIL_SENDER_ADDRESS = "email_sender_address";
  private static final String EMAIL_SUBJECT = "email_subject";
  private static final String EMAIL_BODY = "email_body";
  private static final String SMTP_PORT = "smtp_port";

  private static final String SMTP_HOST_VALUE = "smtp.gmail.com";
  private static final String SMTP_PORT_VALUE = "587";
  private static final String EMAIL_SENDER_ADDRESS_VALUE = "sender@mail.com";
  private static final String EMAIL_RECIPIENT_VALUE = "destination@mail.com";
  private static final String EMAIL_SENDER_NAME_VALUE = "Leonardo";
  private static final String SMTP_AUTH_USER_VALUE = "sender@mail.com";
  private static final String SMTP_AUTH_PASSWORD_ENCRYPTED_VALUE = "Encrypted 2be98afc86aa7f2e4cb79bf67db80bbc3";
  private static final String SMTP_AUTH_PASSWORD_DECRYPTED_VALUE = "qwerty";
  private static final String EMAIL_SUBJECT_VALUE = "Test Transform Auth Email";
  private static final String EMAIL_BODY_VALUE = "This is a test email";

  private StepMockHelper<MailMeta, MailData> stepMockHelper;

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    wasMailMimeCharset = System.getProperty( MAIL_CHARSET_KEY );
    wasFileEncoding = System.getProperty( FILE_ENCODING_KEY );
    KettleClientEnvironment.init();
  }

  @Before
  public void setup() {
    stepMockHelper =
      new StepMockHelper<MailMeta, MailData>( "Test Mail", MailMeta.class, MailData.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void teardownAfterTest() {
    if ( System.getProperty( FILE_ENCODING_KEY ) != null ) {
      if ( wasFileEncoding != null ) {
        System.setProperty( FILE_ENCODING_KEY, wasFileEncoding );
      } else {
        System.clearProperty( FILE_ENCODING_KEY );
      }
    }
    if ( System.getProperty( MAIL_CHARSET_KEY ) != null ) {
      if ( wasMailMimeCharset != null ) {
        System.setProperty( MAIL_CHARSET_KEY, wasMailMimeCharset );
      } else {
        System.clearProperty( MAIL_CHARSET_KEY );
      }
    }
  }

  /*
   * [PDI-13333] Testing the fix for special characters being sent in mail. Need to replicate the specific instance
   * that caused the problem. Replicating the basic functionality that occurs with the Mail step without dealing with
   * the Transport protocol to setup a SMTP server and to test sending an email.
   */
  @Test
  public void verifyMimeCharsetTranslationTest() throws Exception {
    /*
     *  Replicating the specific issue, we need to set the file.encoding to something either than UTF-8, which is what
     * is occurring in this case, the fix being that despite the file-encoding, we need to ensure the Mail Mime Charset
     * is appropriately set to UTF-8, so it can translate special characters appropriately to email clients.
     */
    System.setProperty( FILE_ENCODING_KEY, FILE_ENCODING );
    // If MAIL_CHARSET is not set in the System Properties, the filename will return badly, so in our fix we apply the
    // system property when the transformation Mail step is initialized
    System.setProperty( MAIL_CHARSET_KEY, MAIL_CHARSET );

    File tempFile = File.createTempFile( SPECIAL_CHARS, "test.txt" );
    Message message = createMimeMessage( SPECIAL_CHARS, tempFile );
    /*
     * Before, if the mail.mime.charset was not set to UTF-8, the filename, subject, and content (when no encoding specified)
     * in the bodypart would replace the special characters with %3F or ?, so when you retrieved the filename,
     * it would come back with ???? instead of the special characters. With the mail.mime.charset set, it will
     * decode the values back correctly to the original special characters.
     */
    validateMimeCharacters( message, tempFile );
  }

  private void validateMimeCharacters( Message message, File tempFile ) throws Exception {
    assertEquals( SPECIAL_CHARS, message.getSubject() );
    assertEquals( SPECIAL_CHARS_ENCODED, message.getHeader( "Subject" )[0] );
    assertEquals( tempFile.getName(), ( (MimeMultipart) message.getContent() ).getBodyPart( 0 ).getFileName() );
    assertTrue( ( (MimeMultipart) message.getContent() ).getBodyPart( 0 )
      .getHeader( "Content-Disposition" )[0].contains( SPECIAL_CHARS_FILE_ENCODED ) );
    assertEquals( SPECIAL_CHARS, ( (MimeMultipart) message.getContent() ).getBodyPart( 0 ).getContent() );
  }

  private Message createMimeMessage( String specialCharacters, File attachedFile ) throws Exception {
    Session session = Session.getInstance( new Properties() );
    Message message = new MimeMessage( session );

    MimeMultipart multipart = new MimeMultipart();
    MimeBodyPart attachedFileAndContent = new MimeBodyPart();
    attachedFile.deleteOnExit();
    // create a data source
    URLDataSource fds = new URLDataSource( attachedFile.toURI().toURL() );
    // get a data Handler to manipulate this file type;
    attachedFileAndContent.setDataHandler( new DataHandler( fds ) );
    // include the file in the data source
    String tempFileName = attachedFile.getName();
    message.setSubject( specialCharacters );
    attachedFileAndContent.setFileName( tempFileName );
    attachedFileAndContent.setText( specialCharacters );

    multipart.addBodyPart( attachedFileAndContent );
    message.setContent( multipart );

    return message;
  }

  @Test
  public void testSendMailWithPlainPassword( ) throws Exception {
    testSendMailWithPassword( SMTP_AUTH_PASSWORD_DECRYPTED_VALUE );
  }

  @Test
  public void testSendMailWithEncryptedPassword( ) throws Exception {
    testSendMailWithPassword( SMTP_AUTH_PASSWORD_ENCRYPTED_VALUE );
  }

  public void testSendMailWithPassword( String authPassword ) throws Exception {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMeta rowMeta = new RowMeta();
    ValueMetaInterface valueMeta = new ValueMetaString( SMTP_HOST );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( SMTP_AUTH_USER );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( SMTP_AUTH_PASSWORD );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( EMAIL_RECIPIENT );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( EMAIL_SENDER_NAME );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( EMAIL_SENDER_ADDRESS );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( EMAIL_SUBJECT );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( EMAIL_BODY );
    rowMeta.addValueMeta( valueMeta );
    valueMeta = new ValueMetaString( SMTP_PORT );
    rowMeta.addValueMeta( valueMeta );
    step.setInputRowMeta( rowMeta );

    String[] strings = new String[] {
      SMTP_HOST_VALUE, SMTP_AUTH_USER_VALUE,
      authPassword, EMAIL_RECIPIENT_VALUE,
      EMAIL_SENDER_NAME_VALUE, EMAIL_SENDER_ADDRESS_VALUE,
      EMAIL_SUBJECT_VALUE, EMAIL_BODY_VALUE,
      SMTP_PORT_VALUE
    };
    RowSet inputRowSet = stepMockHelper.getMockInputRowSet( strings );
    when( inputRowSet.getRowMeta() ).thenReturn( rowMeta );
    step.setInputRowSets( Collections.singletonList( inputRowSet ) );

    when( stepMockHelper.processRowsStepMetaInterface.getDestination() ).thenReturn( EMAIL_RECIPIENT );
    when( stepMockHelper.processRowsStepMetaInterface.getReplyAddress() ).thenReturn( EMAIL_SENDER_NAME );
    when( stepMockHelper.processRowsStepMetaInterface.getServer() ).thenReturn( SMTP_HOST );
    when( stepMockHelper.processRowsStepMetaInterface.isUsingAuthentication() ).thenReturn( true );
    when( stepMockHelper.processRowsStepMetaInterface.getAuthenticationUser() ).thenReturn( SMTP_AUTH_USER );
    when( stepMockHelper.processRowsStepMetaInterface.getAuthenticationPassword() ).thenReturn( SMTP_AUTH_PASSWORD );
    when( stepMockHelper.processRowsStepMetaInterface.getDestination() ).thenReturn( EMAIL_RECIPIENT );

    MailData mailData = new MailData();
    // Make sure we don't send an email, while testing Mail step.
    doNothing().when( step ).sendMail( any( Object[].class ), nullable( String.class ), anyInt(), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ), nullable( String.class ) );
    step.processRow( stepMockHelper.processRowsStepMetaInterface, mailData );
    // Check if we call sendMail using the decrypted password.
    verify( step ).sendMail( strings, SMTP_HOST_VALUE, -1, EMAIL_SENDER_NAME_VALUE, null, EMAIL_RECIPIENT_VALUE, null, null, null, null, EMAIL_SENDER_ADDRESS_VALUE, SMTP_AUTH_PASSWORD_DECRYPTED_VALUE, null, null, null );
  }

  @Test
  public void processAttachedFilesNotDynamicTest() throws KettleException, NoSuchFieldException {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isDynamicFilename() ).thenReturn( true );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 0 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isDynamicFilename() ).thenReturn( false );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.processAttachedFiles();

    assertEquals( 0, stepMockHelper.initStepDataInterface.indexOfSourceWildcard );
    assertEquals( "sourceFileFolderName", stepMockHelper.initStepDataInterface.realSourceFileFoldername );
    assertEquals( "sourceWildcard", stepMockHelper.initStepDataInterface.realSourceWildcard );
  }

  @Test
  public void processAttachedFilesDynamicTest() throws KettleException, NoSuchFieldException {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isDynamicFilename() ).thenReturn( true );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 0 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.processAttachedFiles();

    assertEquals( 0, stepMockHelper.initStepDataInterface.indexOfSourceWildcard );
    assertEquals( null, stepMockHelper.initStepDataInterface.realSourceFileFoldername );
    assertEquals( null, stepMockHelper.initStepDataInterface.realSourceWildcard );
  }

  @Test
  public void processAttachedFilesDynamicIndexOfSourceWildcardNotSetTest() throws KettleException, NoSuchFieldException {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );
    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isDynamicFilename() ).thenReturn( true );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    //Index of Source Wildcard is not yet set (value is -1)
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceWildcard" ), stepMockHelper.initStepDataInterface, -1 );
    //Index that will be set in the dynamicWildcard
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 3 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.processAttachedFiles();

    assertEquals( 3, stepMockHelper.initStepDataInterface.indexOfSourceWildcard );
    assertEquals( null, stepMockHelper.initStepDataInterface.realSourceFileFoldername );
    assertEquals( null, stepMockHelper.initStepDataInterface.realSourceWildcard );
  }

  @Test
  public void setAttachedFilesTestCallWithDynamicFilename() throws Exception {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );

    Object[] p = new Object[1];


    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isDynamicFilename() ).thenReturn( true );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    //Index of Source Wildcard is not yet set (value is -1)
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceWildcard" ), stepMockHelper.initStepDataInterface, -1 );
    //Index that will be set in the dynamicWildcard
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 3 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.setAttachedFiles( stepMockHelper.initStepMetaInterface, p, null );

    verify( step , times( 1 ) ).setAttachedFilesList( p, null );

  }

  @Test
  public void setAttachedFilesTestCallWithZipDynamicFilename() throws Exception {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );

    Object[] p = new Object[1];


    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isZipFilenameDynamic() ).thenReturn( true );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    //Index of Source Wildcard is not yet set (value is -1)
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceWildcard" ), stepMockHelper.initStepDataInterface, -1 );
    //Index that will be set in the dynamicWildcard
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 3 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.setAttachedFiles( stepMockHelper.initStepMetaInterface, p, null );

    verify( step , times( 1 ) ).setAttachedFilesList( p, null );

  }

  @Test
  public void setAttachedFilesTestWithNonDynamicFields() throws Exception {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );

    Object[] p = new Object[1];


    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isDynamicFilename() ).thenReturn( false );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    //Index of Source Wildcard is not yet set (value is -1)
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceWildcard" ), stepMockHelper.initStepDataInterface, -1 );
    //Index that will be set in the dynamicWildcard
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 3 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.setAttachedFiles( stepMockHelper.initStepMetaInterface, p, null );

    verify( step , times( 1 ) ).setAttachedFilesList( null, null );

  }

  @Test( expected = KettleException.class )
  public void validateZipFilesTestWithoutDynamicFields() throws Exception {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );

    Object[] p = new Object[1];


    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isZipFiles() ).thenReturn( true );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getZipFilename() ).thenReturn( "" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isZipFilenameDynamic() ).thenReturn( false );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    //Index of Source Wildcard is not yet set (value is -1)
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceWildcard" ), stepMockHelper.initStepDataInterface, -1 );
    //Index that will be set in the dynamicWildcard
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 3 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.validateZipFiles( stepMockHelper.initStepMetaInterface );

  }

  @Test
  public void validateZipFilesTestWithDynamicFields() throws Exception {
    Mail step =
      spy( new Mail( stepMockHelper.stepMeta, stepMockHelper.stepDataInterface, 0, stepMockHelper.transMeta,
        stepMockHelper.trans ) );

    Object[] p = new Object[1];


    step.init( stepMockHelper.initStepMetaInterface, stepMockHelper.initStepDataInterface );
    step.setParentVariableSpace( new Variables() );

    RowMetaInterface rowMetaMock = mock( RowMetaInterface.class );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isZipFiles() ).thenReturn( true );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getZipFilename() ).thenReturn( "" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).isZipFilenameDynamic() ).thenReturn( true );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceFilename" ), stepMockHelper.initStepDataInterface, 0 );
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "previousRowMeta" ), stepMockHelper.initStepDataInterface, rowMetaMock );
    //Index of Source Wildcard is not yet set (value is -1)
    ReflectionUtils.setField( stepMockHelper.initStepDataInterface.getClass().getField( "indexOfSourceWildcard" ), stepMockHelper.initStepDataInterface, -1 );
    //Index that will be set in the dynamicWildcard
    when( rowMetaMock.indexOfValue( "dynamicWildcard" ) ).thenReturn( 3 );

    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getDynamicWildcard() ).thenReturn( "dynamicWildcard" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceFileFoldername() ).thenReturn( "sourceFileFolderName" );
    when( ( (MailMeta) stepMockHelper.initStepMetaInterface ).getSourceWildcard() ).thenReturn( "sourceWildcard" );
    when( step.environmentSubstitute( "sourceFileFolderName" ) ).thenReturn( "sourceFileFolderName" );
    when( step.environmentSubstitute( "sourceWildcard" ) ).thenReturn( "sourceWildcard" );

    step.validateZipFiles( stepMockHelper.initStepMetaInterface );

  }

}
