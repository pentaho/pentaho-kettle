/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
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
package org.pentaho.di.core;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

public class ConcurrentMapPropertiesTest {

  @Test
  public void runMapTests() throws IOException {
    Properties p = new Properties();
    ConcurrentMapProperties c = new ConcurrentMapProperties();

    for ( int i = 0; i < 10000; i++ ) {
      String unique = UUID.randomUUID().toString();
      p.put( unique, unique );
      c.put( unique, unique );
    }

    Assert.assertTrue( p.equals( c ) );
    Assert.assertEquals( p.size(), c.size() );

    List<String> removeKeys = new ArrayList<>( c.size() );

    for ( Object key : c.keySet() ) {
      if ( Math.random() > 0.2 ) {
        removeKeys.add( (String) key );
      }
    }

    for ( String rmKey : removeKeys ) {
      c.remove( rmKey );
      p.remove( rmKey );
    }

    Assert.assertEquals( p.size(), c.size() );
    Assert.assertTrue( p.equals( c ) );

    p.clear();
    c.clear();

    Assert.assertTrue( p.equals( c ) );
    Assert.assertEquals( 0, p.size() );
    Assert.assertEquals( 0, c.size() );

    Map<String, String> addKeys = removeKeys.stream().collect( Collectors.toMap( x -> x, x -> x ) );
    p.putAll( addKeys );
    c.putAll( addKeys );

    Assert.assertTrue( p.equals( c ) );

    for ( String property : removeKeys ) {
      Assert.assertEquals( p.getProperty( property ), c.getProperty( property ) );
    }

    Path tempFile = Files.createTempFile( "propstest", "props" );

    c.store( new FileOutputStream( tempFile.toFile() ), "No Comments" );
    c.clear();

    Assert.assertEquals( 0, c.size() );

    c.load( new FileInputStream( tempFile.toFile() ) );

    Assert.assertEquals( c, p );
  }
}