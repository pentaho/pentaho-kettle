/*!
 * Copyright 2010 - 2024 Hitachi Vantara.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.pentaho.di.repository.pur;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pentaho.di.core.util.ExecutorUtil;
import org.pentaho.di.repository.pur.ActiveCache.ExecutorServiceGetter;

public class ActiveCacheTest {
  private class FutureHolder {
    @SuppressWarnings( "rawtypes" )
    public Future future = null;
  }

  @Test
  public void testActiveCacheLoadsWhenNull() throws Exception {
    long timeout = 100;
    @SuppressWarnings( "unchecked" )
    ActiveCacheLoader<String, String> mockLoader = mock( ActiveCacheLoader.class );
    ActiveCache<String, String> cache = new ActiveCache<String, String>( mockLoader, timeout );
    String testKey = "TEST-KEY";
    String testResult = "TEST-RESULT";
    when( mockLoader.load( testKey ) ).thenReturn( testResult );
    assertEquals( testResult, cache.get( testKey ) );
    verify( mockLoader, times( 1 ) ).load( testKey );
  }

  @Test
  public void testActiveCacheLoadsWhenTimedOut() throws Exception {
    long timeout = 100;
    @SuppressWarnings( "unchecked" )
    ActiveCacheLoader<String, String> mockLoader = mock( ActiveCacheLoader.class );
    ActiveCache<String, String> cache = new ActiveCache<String, String>( mockLoader, timeout );
    String testKey = "TEST-KEY";
    String testResult = "TEST-RESULT";
    String testResult2 = "TEST-RESULT-2";
    when( mockLoader.load( testKey ) ).thenReturn( testResult ).thenReturn( testResult2 );
    assertEquals( testResult, cache.get( testKey ) );
    Thread.sleep( timeout + 10 );
    assertEquals( testResult2, cache.get( testKey ) );
    verify( mockLoader, times( 2 ) ).load( testKey );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Test
  public void testActiveCachePreemtivelyReloadsWhenHalfwayToTimeout() throws Exception {
    long timeout = 500;
    ActiveCacheLoader<String, String> mockLoader = mock( ActiveCacheLoader.class );
    final ExecutorService mockService = mock( ExecutorService.class );
    final FutureHolder lastSubmittedFuture = new FutureHolder();
    when( mockService.submit( any( Callable.class ) ) ).thenAnswer( new Answer<Future>() {

      @Override
      public Future answer( InvocationOnMock invocation ) throws Throwable {
        lastSubmittedFuture.future = ExecutorUtil.getExecutor().submit( (Callable) invocation.getArguments()[0] );
        return lastSubmittedFuture.future;
      }
    } );
    ActiveCache<String, String> cache =
        new ActiveCache<String, String>( mockLoader, timeout, new ExecutorServiceGetter() {

          @Override
          public ExecutorService getExecutor() {
            return mockService;
          }
        } );
    String testKey = "TEST-KEY";
    String testResult = "TEST-RESULT";
    String testResult2 = "TEST-RESULT-2";
    when( mockLoader.load( testKey ) ).thenReturn( testResult ).thenReturn( testResult2 );
    assertEquals( testResult, cache.get( testKey ) );
    Thread.sleep( 255 );
    // Trigger reload, we should get original result back here as it hasn't timed out
    assertEquals( testResult, cache.get( testKey ) );
    // Wait on new value to load
    lastSubmittedFuture.future.get();
    // Should get new value when it's ready
    assertEquals( testResult2, cache.get( testKey ) );
    verify( mockLoader, times( 2 ) ).load( testKey );
  }

  @Test
  public void testActiveCacheDoesntCacheExceptions() throws Exception {
    long timeout = 100;
    @SuppressWarnings( "unchecked" )
    ActiveCacheLoader<String, String> mockLoader = mock( ActiveCacheLoader.class );
    ActiveCache<String, String> cache = new ActiveCache<String, String>( mockLoader, timeout );
    String testKey = "TEST-KEY";
    Exception testResult = new Exception( "TEST-RESULT" );
    String testResult2 = "TEST-RESULT-2";
    when( mockLoader.load( testKey ) ).thenThrow( testResult ).thenReturn( testResult2 );
    try {
      cache.get( testKey );
      fail();
    } catch ( Exception e ) {
      assertEquals( testResult, e );
    }
    assertEquals( testResult2, cache.get( testKey ) );
    verify( mockLoader, times( 2 ) ).load( testKey );
  }
}
