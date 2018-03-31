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

package org.pentaho.di.trans.steps.csvinput;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CsvInputDataTest {
  @Test
  public void testRemoveEscapedEnclosuresWithOneEscapedInMiddle() {
    CsvInputData csvInputData = new CsvInputData();
    csvInputData.enclosure = "\"".getBytes();
    String result = new String( csvInputData.removeEscapedEnclosures( "abcd \"\" defg".getBytes(), 1 ) );
    assertEquals( "abcd \" defg", result );
  }

  @Test
  public void testRemoveEscapedEnclosuresWithTwoEscapedInMiddle() {
    CsvInputData csvInputData = new CsvInputData();
    csvInputData.enclosure = "\"".getBytes();
    String result = new String( csvInputData.removeEscapedEnclosures( "abcd \"\"\"\" defg".getBytes(), 2 ) );
    assertEquals( "abcd \"\" defg", result );
  }

  @Test
  public void testRemoveEscapedEnclosuresWithOneByItself() {
    CsvInputData csvInputData = new CsvInputData();
    csvInputData.enclosure = "\"".getBytes();
    String result = new String( csvInputData.removeEscapedEnclosures( "\"\"".getBytes(), 1 ) );
    assertEquals( "\"", result );
  }

  @Test
  public void testRemoveEscapedEnclosuresWithTwoByThemselves() {
    CsvInputData csvInputData = new CsvInputData();
    csvInputData.enclosure = "\"".getBytes();
    String result = new String( csvInputData.removeEscapedEnclosures( "\"\"\"\"".getBytes(), 2 ) );
    assertEquals( "\"\"", result );
  }

  @Test
  public void testRemoveEscapedEnclosuresWithCharacterInTheMiddleOfThem() {
    CsvInputData csvInputData = new CsvInputData();
    csvInputData.enclosure = "\"".getBytes();
    String result = new String( csvInputData.removeEscapedEnclosures( "345\"\"1\"\"abc".getBytes(), 2 ) );
    assertEquals( "345\"1\"abc", result );
  }
}
