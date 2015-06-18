/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2014 by Pentaho : http://www.pentaho.com
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

import java.text.SimpleDateFormat;

import javax.mail.Message;
import javax.mail.Flags.Flag;
import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.job.Job;

public class JobEntryGetPOPTest {

  @Mock
  MailConnection mailConn;
  @Mock
  Job parentJob;
  @Mock
  Message message;

  JobEntryGetPOP entry = new JobEntryGetPOP();

  @Before
  public void before() throws KettleException {
    MockitoAnnotations.initMocks( this );

    Mockito.when( parentJob.getLogLevel() ).thenReturn( LogLevel.BASIC );
    entry.setParentJob( parentJob );
    entry.setSaveMessage( true );

    Mockito.when( message.getMessageNumber() ).thenReturn( 1 );
    Mockito.when( mailConn.getMessage() ).thenReturn( message );

    Mockito.doNothing().when( mailConn ).openFolder( Mockito.anyBoolean() );
    Mockito.doNothing().when( mailConn ).openFolder( Mockito.anyString(), Mockito.anyBoolean() );

    Mockito.when( mailConn.getMessagesCount() ).thenReturn( 1 );
  }

  /**
   * PDI-10942 - Job get emails JobEntry does not mark emails as 'read' when load emails content.
   * 
   * Test that we always open remote folder in rw mode, and after email attachment is loaded email is marked as read.
   * Set for openFolder rw mode if this is pop3.
   * 
   * @throws KettleException
   * @throws MessagingException
   */
  @Test
  public void testFetchOneFolderModePop3() throws KettleException, MessagingException {
    entry.fetchOneFolder( mailConn, true, "junitImapFolder", "junitRealOutputFolder", "junitTargetAttachmentFolder",
        "junitRealMoveToIMAPFolder", "junitRealFilenamePattern", 0, Mockito.mock( SimpleDateFormat.class ) );
    Mockito.verify( mailConn ).openFolder( true );
    Mockito.verify( message ).setFlag( Flag.SEEN, true );
  }

  /**
   * PDI-10942 - Job get emails JobEntry does not mark emails as 'read' when load emails content.
   * 
   * Test that we always open remote folder in rw mode, and after email attachment is loaded email is marked as read.
   * protocol IMAP and default remote folder is overridden
   * 
   * @throws KettleException
   * @throws MessagingException
   */
  @Test
  public void testFetchOneFolderModeIMAPWithNonDefFolder() throws KettleException, MessagingException {
    entry.fetchOneFolder( mailConn, false, "junitImapFolder", "junitRealOutputFolder", "junitTargetAttachmentFolder",
        "junitRealMoveToIMAPFolder", "junitRealFilenamePattern", 0, Mockito.mock( SimpleDateFormat.class ) );
    Mockito.verify( mailConn ).openFolder( "junitImapFolder", true );
    Mockito.verify( message ).setFlag( Flag.SEEN, true );
  }

  /**
   * PDI-10942 - Job get emails JobEntry does not mark emails as 'read' when load emails content.
   * 
   * Test that we always open remote folder in rw mode, and after email attachment is loaded email is marked as read.
   * protocol IMAP and default remote folder is NOT overridden
   * 
   * @throws KettleException
   * @throws MessagingException
   */
  @Test
  public void testFetchOneFolderModeIMAPWithIsDefFolder() throws KettleException, MessagingException {
    entry.fetchOneFolder( mailConn, false, null, "junitRealOutputFolder", "junitTargetAttachmentFolder",
        "junitRealMoveToIMAPFolder", "junitRealFilenamePattern", 0, Mockito.mock( SimpleDateFormat.class ) );
    Mockito.verify( mailConn ).openFolder( true );
    Mockito.verify( message ).setFlag( Flag.SEEN, true );
  }
}
