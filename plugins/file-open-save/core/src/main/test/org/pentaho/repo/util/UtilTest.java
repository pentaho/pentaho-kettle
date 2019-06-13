/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2019 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.repo.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UtilTest {

  private static List<String> files =
    Arrays.asList( "File1.ktr", "MyFile.kjb", "File3.ktr", "File2", "TheFile", "Albatross", "lowerfile" );

  @Test
  public void testIsFiltered() {
    List<String> output = new ArrayList<>();
    for ( String filename : files ) {
      if ( !Util.isFiltered( filename, "F*" ) ) {
        output.add( filename );
      }
    }
    Assert.assertTrue( output.contains( "File1.ktr" ) );
    Assert.assertTrue( output.contains( "File2" ) );
    Assert.assertTrue( output.contains( "File3.ktr" ) );
    Assert.assertFalse( output.contains( "lowerfile" ) );
    Assert.assertFalse( output.contains( "MyFile.kjb" ) );
    Assert.assertFalse( output.contains( "TheFile" ) );
    Assert.assertFalse( output.contains( "Albatross" ) );
  }

  @Test
  public void testIsFiltered2() {
    List<String> output = new ArrayList<>();
    for ( String filename : files ) {
      if ( !Util.isFiltered( filename, "*F*" ) ) {
        output.add( filename );
      }
    }
    Assert.assertTrue( output.contains( "File1.ktr" ) );
    Assert.assertTrue( output.contains( "File2" ) );
    Assert.assertTrue( output.contains( "File3.ktr" ) );
    Assert.assertTrue( output.contains( "MyFile.kjb" ) );
    Assert.assertTrue( output.contains( "TheFile" ) );
    Assert.assertTrue( output.contains( "lowerfile" ) );
    Assert.assertFalse( output.contains( "Albatross" ) );
  }

  @Test
  public void testIsFiltered3() {
    List<String> output = new ArrayList<>();
    for ( String filename : files ) {
      if ( !Util.isFiltered( filename, "F*ktr" ) ) {
        output.add( filename );
      }
    }
    Assert.assertTrue( output.contains( "File1.ktr" ) );
    Assert.assertFalse( output.contains( "File2" ) );
    Assert.assertTrue( output.contains( "File3.ktr" ) );
    Assert.assertFalse( output.contains( "MyFile.kjb" ) );
    Assert.assertFalse( output.contains( "TheFile" ) );
    Assert.assertFalse( output.contains( "lowerfile" ) );
    Assert.assertFalse( output.contains( "Albatross" ) );
  }

}
