/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2013 by Pentaho : http://www.pentaho.com
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

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.job.entries.getpop.MailConnection;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.steps.mailinput.MailInput.TestClass;
import org.pentaho.di.trans.steps.mock.StepMockHelper;


/**
 * @author Dzmitry_Prakapenka
 *
 */
public class MailInputTest {

  private StepMockHelper<MailInputMeta, StepDataInterface> stepMockHelper;
  private static MailInputField[] fields = getDefaultInputFields();
  
  @Before
  public void setup() {
    stepMockHelper =
        new StepMockHelper<MailInputMeta, StepDataInterface>( "ABORT TEST", MailInputMeta.class, StepDataInterface.class );
    when( stepMockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        stepMockHelper.logChannelInterface );
    when( stepMockHelper.trans.isRunning() ).thenReturn( true );
  }
  
  @After
  public void tearDown() {
    stepMockHelper.cleanUp();
  }
  
  @Test
  public final void test() throws Exception {
    Message message = Mockito.mock(Message.class);
    
    MailConnection conn = mock (MailConnection.class);
    when (conn.getMessageBody( any( Message.class ) )).thenReturn( "body" );
    when (conn.getFolderName()).thenReturn( "junit_inbox" );
    when (conn.getAttachedFilesCount( any( Message.class ) , any( Pattern.class ) )).thenReturn( 1 );
    when (conn.getMessageBodyContentType( any( Message.class ) )).thenReturn( "cont_type" );
    MailInputData data = mock ( MailInputData.class );
    //not null
    data.mailConn = conn;
    data.nrFields = fields.length;
    
    MailInputMeta meta = mock ( MailInputMeta.class );
    when ( meta.getInputFields() ).thenReturn( fields );
    
    
    MailInput min = new MailInput( stepMockHelper.stepMeta, data, 0, stepMockHelper.transMeta,
        stepMockHelper.trans );
    min.processRow( meta, data );
    
    
    TestClass underTest = min.new TestClass();
    
    Object[] r = RowDataUtil.allocateRowData( 100 );
    
    Address addr = mock( Address.class );
    when(addr.toString()).thenReturn( "localhost" );        
    when(message.getMessageNumber()).thenReturn( 13 );
    when(message.getSubject()).thenReturn( "mocktest" );
    Address[] adresa = { addr };
    when(message.getFrom()).thenReturn( adresa );
    when ( message.getReplyTo() ).thenReturn( adresa );
    when ( message.getAllRecipients() ).thenReturn( adresa );
    when ( message.getDescription() ).thenReturn( "desc" );
    when ( message.getReceivedDate() ).thenReturn( new Date() );
    when ( message.getSentDate() ).thenReturn( new Date() );
    when ( message.getContentType() ).thenReturn( "content_type" );
    when ( message.getSize()).thenReturn( 13 );
    String[] strArr = { "" };
    when ( message.getMatchingHeaders( strArr ) ).thenReturn( null );
    when ( message.getHeader( "" )).thenReturn( null );

    underTest.incaps( r, message );
    
    fail( "Not yet implemented" );
  }
  
  private static MailInputField[] getDefaultInputFields(){
    MailInputField[] fields = new MailInputField[MailInputField.ColumnCode.length];
    for (int i=0; i<MailInputField.ColumnCode.length; i++){
      fields[i] = new MailInputField();
      fields[i].setColumn( MailInputField.getColumnByCode( MailInputField.ColumnCode[i] ) );
      fields[i].setName( MailInputField.getColumnDesc( fields[i].getColumn() ) );      
    }
    return fields;
  }
  

}
