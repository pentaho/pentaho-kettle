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