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

package org.pentaho.di.trans.steps.mailinput;

import org.mockito.AdditionalMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang.StringUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mailinput.MailInput.MessageParser;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class ParseMailInputTest {

  // mock is existed per-class instance loaded by junit loader
  private static StepMockHelper<MailInputMeta, StepDataInterface> stepMockHelper;

  // test data
  public static final int MSG_NUMB = 3;
  public static final String MSG_BODY = "msg_body";
  public static final String FLD_NAME = "junit_folder";
  public static final int ATTCH_COUNT = 3;
  public static final String CNTNT_TYPE = "text/html";
  public static final String FROM1 = "localhost_1";
  public static final String FROM2 = "localhost_2";
  public static final String REP1 = "127.0.0.1";
  public static final String REP2 = "127.0.0.2";
  public static final String REC1 = "Vasily";
  public static final String REC2 = "Pupkin";
  public static final String SUBJ = "mocktest";
  public static final String DESC = "desc";
  public static final Date DATE1 = new Date( 0 );
  public static final Date DATE2 = new Date( 60000 );
  public static final String CNTNT_TYPE_EMAIL = "application/acad";
  public static int CNTNT_SIZE = 23;
  public static String HDR_EX1 = "header_ex1";
  public static String HDR_EX1V = "header_ex1_value";
  public static String HDR_EX2 = "header_ex2";
  public static String HDR_EX2V = "header_ex2_value";

  // this objects re-created for every test method
  private Message message;
  private MailInputData data;
  private MailInputMeta meta;
  private MailInput mailInput;

  @BeforeClass
  public static void setup() {
    stepMockHelper =
      new StepMockHelper<MailInputMeta, StepDataInterface>(
        "ABORT TEST", MailInputMeta.class, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) )
      .thenReturn( stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }

  @AfterClass
  public static void tearDown() {
    stepMockHelper.cleanUp();
  }

  @Before
  public void beforeTest() throws MessagingException, IOException, KettleException {
    message = Mockito.mock( Message.class );

    MailConnection conn = mock( MailConnection.class );
    when( conn.getMessageBody( any( Message.class ) ) ).thenReturn( MSG_BODY );
    when( conn.getFolderName() ).thenReturn( FLD_NAME );
    when( conn.getAttachedFilesCount( any(), any() ) ).thenReturn( ATTCH_COUNT );
    when( conn.getMessageBodyContentType( any( Message.class ) ) ).thenReturn( CNTNT_TYPE );
    data = mock( MailInputData.class );
    data.mailConn = conn;

    mailInput = new MailInput( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta, stepMockHelper.trans );

    Address addrFrom1 = mock( Address.class );
    when( addrFrom1.toString() ).thenReturn( FROM1 );
    Address addrFrom2 = mock( Address.class );
    when( addrFrom2.toString() ).thenReturn( FROM2 );
    Address addrRep1 = mock( Address.class );
    when( addrRep1.toString() ).thenReturn( REP1 );
    Address addrRep2 = mock( Address.class );
    when( addrRep2.toString() ).thenReturn( REP2 );
    Address allRec1 = mock( Address.class );
    when( allRec1.toString() ).thenReturn( REC1 );
    Address allRec2 = mock( Address.class );
    when( allRec2.toString() ).thenReturn( REC2 );

    Address[] adrFr = { addrFrom1, addrFrom2 };
    Address[] adrRep = { addrRep1, addrRep2 };
    Address[] adrRecip = { allRec1, allRec2 };

    message = Mockito.mock( Message.class );
    when( message.getMessageNumber() ).thenReturn( MSG_NUMB );
    when( message.getSubject() ).thenReturn( SUBJ );

    when( message.getFrom() ).thenReturn( adrFr );
    when( message.getReplyTo() ).thenReturn( adrRep );
    when( message.getAllRecipients() ).thenReturn( adrRecip );
    when( message.getDescription() ).thenReturn( DESC );
    when( message.getReceivedDate() ).thenReturn( DATE1 );
    when( message.getSentDate() ).thenReturn( DATE2 );
    when( message.getContentType() ).thenReturn( CNTNT_TYPE_EMAIL );
    when( message.getSize() ).thenReturn( CNTNT_SIZE );

    Header ex1 = new Header( HDR_EX1, HDR_EX1V );
    Header ex2 = new Header( HDR_EX2, HDR_EX2V );

    // for fixed [PDI-6532]
    when( message.getMatchingHeaders( AdditionalMatchers.aryEq( new String[] { HDR_EX1 } ) ) ).thenReturn(
      getEnum( new Header[] { ex1 } ) );
    when( message.getMatchingHeaders( AdditionalMatchers.aryEq( new String[] { HDR_EX2 } ) ) ).thenReturn(
      getEnum( new Header[] { ex2 } ) );
    when( message.getMatchingHeaders( AdditionalMatchers.aryEq( new String[] { HDR_EX1, HDR_EX2 } ) ) ).thenReturn(
      getEnum( new Header[] { ex1, ex2 } ) );

    // for previous implementation
    when( message.getHeader( eq( HDR_EX1 ) ) ).thenReturn( new String[] { ex1.getValue() } );
    when( message.getHeader( eq( HDR_EX2 ) ) ).thenReturn( new String[] { ex2.getValue() } );
  }

  /**
   * [PDI-6532] When mail header is found returns his actual value.
   *
   * @throws Exception
   * @throws KettleException
   */
  @Test
  public void testHeadersParsedPositive() throws Exception {
    // add expected fields:
    int[] fields = { MailInputField.COLUMN_HEADER };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    // points to existed header
    farr[0].setName( HDR_EX1 );

    this.mockMailInputMeta( farr );

    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Header is correct", HDR_EX1V, String.class.cast( r[0] ) );
  }

  /**
   * [PDI-6532] When mail header is not found returns empty String
   *
   * @throws Exception
   *
   */
  @Test
  public void testHeadersParsedNegative() throws Exception {
    int[] fields = { MailInputField.COLUMN_HEADER };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    farr[0].setName( HDR_EX1 + "salt" );

    this.mockMailInputMeta( farr );

    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Header is correct", "", String.class.cast( r[0] ) );
  }

  /**
   * Test, message number can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageNumberIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_MESSAGE_NR };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );
    Assert.assertEquals( "Message number is correct", new Long( MSG_NUMB ), Long.class.cast( r[0] ) );
  }

  /**
   * Test message subject can be parsed
   *
   * @throws Exception
   */
  @Test
  public void testMessageSubjectIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_SUBJECT };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );
    Assert.assertEquals( "Message subject is correct", SUBJ, String.class.cast( r[0] ) );
  }

  /**
   * Test message From can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageFromIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_SENDER };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    // expect, that from is concatenated with ';'
    String expected = StringUtils.join( new String[] { FROM1, FROM2 }, ";" );
    Assert.assertEquals( "Message From is correct", expected, String.class.cast( r[0] ) );
  }

  /**
   * Test message ReplayTo can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageReplayToIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_REPLY_TO };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    // is concatenated with ';'
    String expected = StringUtils.join( new String[] { REP1, REP2 }, ";" );
    Assert.assertEquals( "Message ReplayTo is correct", expected, String.class.cast( r[0] ) );
  }

  /**
   * Test message recipients can be parsed
   *
   * @throws Exception
   */
  @Test
  public void testMessageRecipientsIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_RECIPIENTS };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    // is concatenated with ';'
    String expected = StringUtils.join( new String[] { REC1, REC2 }, ";" );
    Assert.assertEquals( "Message Recipients is correct", expected, String.class.cast( r[0] ) );
  }

  /**
   * Test message description is correct
   *
   * @throws Exception
   */
  @Test
  public void testMessageDescriptionIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_DESCRIPTION };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Description is correct", DESC, String.class.cast( r[0] ) );
  }

  /**
   * Test message received date is correct
   *
   * @throws Exception
   */
  @Test
  public void testMessageRecivedDateIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_RECEIVED_DATE };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Recived date is correct", DATE1, Date.class.cast( r[0] ) );
  }

  /**
   * Test message sent date is correct
   *
   * @throws Exception
   */
  @Test
  public void testMessageSentDateIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_SENT_DATE };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Sent date is correct", DATE2, Date.class.cast( r[0] ) );
  }

  /**
   * Message content type is correct
   *
   * @throws Exception
   */
  @Test
  public void testMessageContentTypeIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_CONTENT_TYPE };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Content type is correct", CNTNT_TYPE_EMAIL, String.class.cast( r[0] ) );
  }

  /**
   * Test message size is correct
   *
   * @throws Exception
   */
  @Test
  public void testMessageSizeIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_SIZE };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Size is correct", new Long( CNTNT_SIZE ), Long.class.cast( r[0] ) );
  }

  /**
   * Test that message body can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageBodyIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_BODY };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Body is correct", MSG_BODY, String.class.cast( r[0] ) );
  }

  /**
   * Test that message folder name can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageFolderNameIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_FOLDER_NAME };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Folder Name is correct", FLD_NAME, String.class.cast( r[0] ) );
  }

  /**
   * Test that message folder name can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageAttachedFilesCountNameIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_ATTACHED_FILES_COUNT };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message Attached files count is correct", new Long( ATTCH_COUNT ), Long.class
      .cast( r[0] ) );
  }

  /**
   * Test that message body content type can be parsed correctly
   *
   * @throws Exception
   */
  @Test
  public void testMessageBodyContentTypeIsParsed() throws Exception {
    int[] fields = { MailInputField.COLUMN_BODY_CONTENT_TYPE };
    MailInputField[] farr = this.getDefaultInputFields( fields );
    this.mockMailInputMeta( farr );
    try {
      mailInput.processRow( meta, data );
    } catch ( KettleException e ) {
      // don't worry about it
    }
    MessageParser underTest = mailInput.new MessageParser();
    Object[] r = RowDataUtil.allocateRowData( data.nrFields );
    underTest.parseToArray( r, message );

    Assert.assertEquals( "Message body content type is correct", CNTNT_TYPE, String.class.cast( r[0] ) );
  }

  private void mockMailInputMeta( MailInputField[] arr ) {
    data.nrFields = arr.length;
    meta = mock( MailInputMeta.class );
    when( meta.getInputFields() ).thenReturn( arr );
  }

  private MailInputField[] getDefaultInputFields( int[] arr ) {
    MailInputField[] fields = new MailInputField[arr.length];
    for ( int i = 0; i < arr.length; i++ ) {
      fields[i] = new MailInputField();
      fields[i].setColumn( arr[i] );
      fields[i].setName( MailInputField.getColumnDesc( arr[i] ) );
    }
    return fields;
  }

  private Enumeration<Header> getEnum( Header[] headers ) {
    return Collections.enumeration( Arrays.asList( headers ) );
  }
}
