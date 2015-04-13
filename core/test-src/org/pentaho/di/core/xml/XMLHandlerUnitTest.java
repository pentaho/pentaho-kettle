/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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

package org.pentaho.di.core.xml;

import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.pentaho.di.core.xml.XMLHandler.buildCDATA;
import static org.pentaho.di.core.xml.XMLHandler.openTag;

/**
 */
public class XMLHandlerUnitTest {

  @Test
  public void openTagWithNotNull() {
    assertEquals( "<qwerty>", openTag( "qwerty" ) );
  }

  @Test
  public void openTagWithNull() {
    assertEquals( "<null>", openTag( null ) );
  }

  @Test
  public void openTagWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    openTag( builder, "rty" );
    assertEquals( "qwe<rty>", builder.toString() );
  }


  @Test
  public void buildCdataWithNotNull() {
    assertEquals( "<![CDATA[qwerty]]>", buildCDATA( "qwerty" ) );
  }

  @Test
  public void buildCdataWithNull() {
    assertEquals( "<![CDATA[]]>", buildCDATA( null ) );
  }

  @Test
  public void buildCdataWithExternalBuilder() {
    StringBuilder builder = new StringBuilder( "qwe" );
    buildCDATA( builder, "rty" );
    assertEquals( "qwe<![CDATA[rty]]>", builder.toString() );
  }

  @Test
  public void timestamp2stringTest() {
    String actual = XMLHandler.timestamp2string( null );
    assertNull( actual );
  }

  @Test
  public void date2stringTest() {
    String actual = XMLHandler.date2string( null );
    assertNull( actual );
  }
}
