/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.www.cache;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.mockito.Mockito.*;

public class TransJobStatusCacheTest {

  TransJobStatusCache cache = null;
  TransJobStatusCache cacheSpy = null;

  @Before
  public void setup() {
    cache = new TransJobStatusCache();
    cacheSpy = spy( cache );
  }

  @Test
  public void testGetInstance() {
    Assert.assertTrue( TransJobStatusCache.getInstance() == TransJobStatusCache.getInstance() );
  }

  @Test
  public void testPut() throws Exception {
    initializeTestData( cache.getMap() );
    String id = "40";
    cacheSpy.put( id, "test string data", "logId" + id );
    Assert.assertEquals( 41, cache.getMap().size() );
    id = "20";
    File mockFile = cache.getMap().get( id ).getFile();
    when( mockFile.exists() ).thenReturn( true );
    cacheSpy.put(  id, "test string data", "logId" + id  );
    Assert.assertEquals( 41, cache.getMap().size() );
  }

  @Test
  public void testGet() throws Exception {
    initializeTestData( cache.getMap() );

    Assert.assertNull( cacheSpy.get( "40 ", "logId40" ) );

    String id = "1";
    File mockFile = cache.getMap().get( id ).getFile();
    Path path = mock( Path.class );
    when( mockFile.toPath() ).thenReturn( path );

    cacheSpy.get( id, "logId" + id );

    verify( mockFile ).toPath();
  }

  @Test
  public void testRemove() throws Exception {
    initializeTestData( cache.getMap() );
    Assert.assertEquals( 40, cache.getMap().size() );
    cacheSpy.remove( "1" );
    Assert.assertEquals( 39, cache.getMap().size() );
    Assert.assertNull( cache.getMap().get( "1" ) );
  }


  void initializeTestData( Map<String, CachedItem> map ) {
    for ( int i = 0; i < 40; i++ ) {
      map.put( "" + i, new CachedItem( "logId" + i, mock( File.class ) ) );
    }
  }
}
