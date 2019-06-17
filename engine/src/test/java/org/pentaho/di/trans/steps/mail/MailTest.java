/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.junit.rules.RestorePDIEngineEnvironment;

import javax.activation.DataHandler;
import javax.activation.URLDataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
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

  @BeforeClass
  public static void setupBeforeClass() throws KettleException {
    wasMailMimeCharset = System.getProperty( MAIL_CHARSET_KEY );
    wasFileEncoding = System.getProperty( FILE_ENCODING_KEY );
    KettleClientEnvironment.init();
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
}
