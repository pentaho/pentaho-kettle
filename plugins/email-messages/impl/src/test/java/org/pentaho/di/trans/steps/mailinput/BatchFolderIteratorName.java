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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class BatchFolderIteratorName {

  static Folder folder = null;

  @BeforeClass
  public static void setUp() throws MessagingException {
    folder = mock( Folder.class );
    when( folder.getName() ).thenReturn( "INBOX" );
    when( folder.getMessages( anyInt(), anyInt() ) ).thenAnswer( new Answer<Message[]>() {
      @Override
      public Message[] answer( InvocationOnMock invocation ) throws Throwable {
        Object[] args = invocation.getArguments();
        int start = ( (Integer) args[0] ).intValue();
        int end = ( (Integer) args[1] ).intValue();
        return new Message[end - start + 1];
      }
    } );
  }

  @Test
  public void testBatchSize2() {
    BatchFolderIterator bfi = new BatchFolderIterator( folder, 2, 1, 2 );
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertFalse( bfi.hasNext() );
  }

  @Test
  public void testBatchSize1x2() {
    BatchFolderIterator bfi = new BatchFolderIterator( folder, 1, 1, 2 );
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertFalse( bfi.hasNext() );
  }

  @Test
  public void testBatchSize1() {
    BatchFolderIterator bfi = new BatchFolderIterator( folder, 1, 1, 1 );
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertFalse( bfi.hasNext() );
  }

  @Test
  public void testBatchSize2x2() {
    BatchFolderIterator bfi = new BatchFolderIterator( folder, 2, 1, 4 );
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertFalse( bfi.hasNext() );
  }

  @Test
  public void testBatchSize2x3() {
    BatchFolderIterator bfi = new BatchFolderIterator( folder, 2, 1, 5 );
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertTrue( bfi.hasNext() );
    bfi.next();
    Assert.assertFalse( bfi.hasNext() );
  }

}
