/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.di.trans.steps.mailinput;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.job.entries.getpop.MailConnectionMeta;
import org.pentaho.di.trans.steps.mock.StepMockHelper;

public class MailInputTest {

  private StepMockHelper<MailInputMeta, MailInputData> mockHelper;

  @Before
  public void setUp() throws Exception {
    mockHelper =
        new StepMockHelper<MailInputMeta, MailInputData>( "MailInput", MailInputMeta.class, MailInputData.class );
    when( mockHelper.logChannelInterfaceFactory.create( any(), any( LoggingObjectInterface.class ) ) ).thenReturn(
        mockHelper.logChannelInterface );
    when( mockHelper.trans.isRunning() ).thenReturn( true );
  }

  @After
  public void cleanUp() {
    mockHelper.cleanUp();
  }

  /**
   * PDI-10909 Check that imap retrieve ... first will be applied.
   */
  @Test
  public void testInitSetGetFirstForIMAP() {
    MailInput step =
        new MailInput( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    MailInputData data = new MailInputData();
    MailInputMeta meta = mock( MailInputMeta.class );
    when( meta.isDynamicFolder() ).thenReturn( false );
    when( meta.getProtocol() ).thenReturn( MailConnectionMeta.PROTOCOL_STRING_IMAP );
    when( meta.getFirstIMAPMails() ).thenReturn( "2" );
    when( meta.getFirstMails() ).thenReturn( "3" );

    step.init( meta, data );

    Assert.assertEquals( "Row Limit is set up to 2 rows.", 2, data.rowlimit );
  }

  /**
   * PDI-10909 Check that pop3 retrieve ... first will be applied.
   */
  @Test
  public void testInitSetGetFirstForPOP3() {
    MailInput step =
        new MailInput( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    MailInputData data = new MailInputData();
    MailInputMeta meta = mock( MailInputMeta.class );
    when( meta.isDynamicFolder() ).thenReturn( false );
    when( meta.getProtocol() ).thenReturn( MailConnectionMeta.PROTOCOL_STRING_POP3 );
    when( meta.getFirstIMAPMails() ).thenReturn( "2" );
    when( meta.getFirstMails() ).thenReturn( "3" );

    step.init( meta, data );

    Assert.assertEquals( "Row Limit is set up to 3 rows.", 3, data.rowlimit );
  }

  /**
   * PDI-10909 Check that Limit value overrides retrieve ... first if any.
   */
  @Test
  public void testInitSetGetFirstLimitOverride() {
    MailInput step =
        new MailInput( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    MailInputData data = new MailInputData();
    MailInputMeta meta = mock( MailInputMeta.class );
    when( meta.isDynamicFolder() ).thenReturn( false );
    when( meta.getProtocol() ).thenReturn( MailConnectionMeta.PROTOCOL_STRING_POP3 );
    when( meta.getFirstIMAPMails() ).thenReturn( "2" );
    when( meta.getFirstMails() ).thenReturn( "3" );

    when( meta.getRowLimit() ).thenReturn( "5" );

    step.init( meta, data );

    Assert.assertEquals( "Row Limit is set up to 5 rows as the Limit has priority.", 5, data.rowlimit );
  }

  /**
   * We do not use any of retrieve ... first if protocol is MBOX
   */
  @Test
  public void testInitSetGetFirstForMBOXIgnored() {
    MailInput step =
        new MailInput( mockHelper.stepMeta, mockHelper.stepDataInterface, 0, mockHelper.transMeta, mockHelper.trans );
    MailInputData data = new MailInputData();
    MailInputMeta meta = mock( MailInputMeta.class );
    when( meta.isDynamicFolder() ).thenReturn( false );
    when( meta.getProtocol() ).thenReturn( MailConnectionMeta.PROTOCOL_STRING_MBOX );
    when( meta.getFirstIMAPMails() ).thenReturn( "2" );
    when( meta.getFirstMails() ).thenReturn( "3" );

    step.init( meta, data );

    Assert.assertEquals( "Row Limit is set up to 0 rows as the Limit has priority.", 0, data.rowlimit );
  }

}
