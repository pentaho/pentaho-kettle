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

package org.pentaho.di.concurrency;

import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.AbstractFileProvider;
import org.apache.commons.vfs2.provider.FileProvider;
import org.junit.After;
import org.junit.Test;
import org.pentaho.di.core.osgi.api.VfsEmbeddedFileSystemCloser;
import org.pentaho.di.core.vfs.ConcurrentFileSystemManager;
import org.pentaho.di.core.vfs.KettleVFS;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConcurrentFileSystemManagerTest {

  private DefaultFileSystemManager fileSystemManager =
          (DefaultFileSystemManager)  KettleVFS.getInstance().getFileSystemManager();

  @After
  public void tearUp() {
    this.fileSystemManager.freeUnusedResources();
    this.fileSystemManager.close();
  }

  @Test
  public void getAndPutConcurrently() throws Exception {
    int numberOfGetters = 5;
    int numberOfPutters = 1;

    AtomicBoolean condition = new AtomicBoolean( true );

    List<Getter> getters = new ArrayList<>();
    for ( int i = 0; i < numberOfGetters; i++ ) {
      getters.add( new Getter( condition, this.fileSystemManager ) );
    }

    List<Putter> putters = new ArrayList<>();
    for ( int i = 0; i < numberOfPutters; i++ ) {
      putters.add( new Putter( condition, this.fileSystemManager ) );
    }

    ConcurrencyTestRunner.runAndCheckNoExceptionRaised( putters, getters, condition );
  }

  @Test
  public void testNcCloseEmbeddedFileSystem() throws Exception {
    ConcurrentFileSystemManager concurrentFileSystemManager = new ConcurrentFileSystemManager();
    MockNamedClusterProvider mockFileProvider = mock( MockNamedClusterProvider.class );
    concurrentFileSystemManager.addProvider( new String[] { "hc" }, mockFileProvider );
    concurrentFileSystemManager.closeEmbeddedFileSystem( "key" );
    verify( mockFileProvider ).closeFileSystem( "key" );
  }

  @Test
  public void testNonNcCloseEmbeddedFileSystem() throws Exception {
    ConcurrentFileSystemManager concurrentFileSystemManager = new ConcurrentFileSystemManager();
    MockNamedClusterProvider mockFileProvider = mock( MockNamedClusterProvider.class );
    concurrentFileSystemManager.addProvider( new String[] { "notnc" }, mockFileProvider );
    concurrentFileSystemManager.closeEmbeddedFileSystem( "key" );
    verify( mockFileProvider, times( 0 ) ).closeFileSystem( "key" );
  }

  private interface MockNamedClusterProvider extends FileProvider, VfsEmbeddedFileSystemCloser {
  }

  private class Getter extends StopOnErrorCallable<Object> {
    private DefaultFileSystemManager fsm;

    Getter( AtomicBoolean condition, DefaultFileSystemManager fsm ) {
      super( condition );
      this.fsm = fsm;
    }

    @Override
    Object doCall() throws Exception {
      while ( condition.get() ) {
        this.fsm.getSchemes();
      }
      return null;
    }
  }

  private class Putter extends StopOnErrorCallable<Object> {
    private DefaultFileSystemManager fsm;
    AbstractFileProvider provider;

    Putter( AtomicBoolean condition, DefaultFileSystemManager fsm ) {
      super( condition );
      this.fsm = fsm;
      provider = mock( AbstractFileProvider.class );
      doNothing().when( provider ).freeUnusedResources();
    }

    @Override
    Object doCall() throws Exception {
      while ( condition.get() ) {
        this.fsm.addProvider( "scheme", provider );
        // to register only one provider with a given scheme
        condition.set( false );
      }
      return null;
    }
  }
}
