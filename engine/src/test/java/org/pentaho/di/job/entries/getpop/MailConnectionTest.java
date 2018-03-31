/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

import static org.mockito.Mockito.when;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Store;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.logging.LogChannel;
import org.pentaho.di.core.logging.LogChannelInterface;

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
   * PDI-7426 Test {@link MailConnection#folderExists(String)} method.
   */
  @Test
  public void folderExistsTest() {
    boolean actual = conn.folderExists( "a/b" );
    Assert.assertTrue( "Folder B exists", actual );
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
