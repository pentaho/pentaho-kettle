/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2016 - 2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.trans.steps.fileinput.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;

public class BOMDetectorTest {

  BufferedInputStream getFile( String filename ) throws Exception {
    String inPrefix = '/' + this.getClass().getPackage().getName().replace( '.', '/' ) + "/files/";
    InputStream file = this.getClass().getResourceAsStream( inPrefix + filename );
    assertNotNull( "There is no file " + filename, file );
    return new BufferedInputStream( file );
  }

  public void testBOM( String file, String charset ) throws Exception {
    BufferedInputStream in = getFile( file );
    BOMDetector detector = new BOMDetector( in );
    assertEquals( "Wrong BOM detected", charset, detector.getCharset() );
    BufferedReader rd = new BufferedReader( new InputStreamReader( in, detector.getCharset() ) );
    String data = rd.readLine();
    assertEquals( "Wrong data in file", "data;1", data );
    in.close();
  }

  @Test
  public void testBOMs() throws Exception {
    testBOM( "test-BOM-UTF-8.txt", "UTF-8" );
    testBOM( "test-BOM-UTF-16BE.txt", "UTF-16BE" );
    testBOM( "test-BOM-UTF-16LE.txt", "UTF-16LE" );
    testBOM( "test-BOM-UTF-32BE.txt", "UTF-32BE" );
    testBOM( "test-BOM-UTF-32LE.txt", "UTF-32LE" );
    testBOM( "test-BOM-GB-18030.txt", "GB18030" );
  }
}
