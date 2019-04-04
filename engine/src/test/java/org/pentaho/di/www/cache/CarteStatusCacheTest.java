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

package org.pentaho.di.www.cache;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;

public class CarteStatusCacheTest {

  CarteStatusCache cache = null;
  CarteStatusCache cacheSpy = null;

  @Before
  public void setup() {
    cache = CarteStatusCache.getInstance();
    cacheSpy = spy( cache );
  }

  @Test
  public void testGetInstance() {
    Assert.assertTrue( CarteStatusCache.getInstance() == CarteStatusCache.getInstance() );
  }

  @Test
  public void testPut() throws Exception {
    initializeTestData( cache.getMap() );
    String id = "40";
    cacheSpy.put( "logId" + id, "test string data", 0 );
    Assert.assertEquals( 41, cache.getMap().size() );
    id = "20";
    File mockFile = cache.getMap().get( "logId" + id ).getFile();
    when( mockFile.exists() ).thenReturn( true );
    cacheSpy.put(   "logId" + id, "test string data", 0  );
    Assert.assertEquals( 41, cache.getMap().size() );
  }

  @Test
  public void testGet() throws Exception {
    initializeTestData( cache.getMap() );

    Assert.assertNull( cacheSpy.get( "logId40", 0 ) );

    File mockFile = cache.getMap().get( "logId1" ).getFile();
    Path path = mock( Path.class );
    when( mockFile.toPath() ).thenReturn( path );

    cacheSpy.get( "logId1", 0 );

    verify( mockFile ).toPath();
  }

  @Test
  public void testRemove() throws Exception {
    initializeTestData( cache.getMap() );
    Assert.assertEquals( 40, cache.getMap().size() );
    cacheSpy.remove( "logId1" );
    Assert.assertEquals( 39, cache.getMap().size() );
    Assert.assertNull( cache.getMap().get( "logId1" ) );
  }


  void initializeTestData( Map<String, CachedItem> map ) {
    map.clear();
    for ( int i = 0; i < 40; i++ ) {
      map.put( "logId" + i, new CachedItem( mock( File.class ), 0 ) );
    }
  }
}
