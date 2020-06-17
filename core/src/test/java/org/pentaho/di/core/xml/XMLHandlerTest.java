/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2020 by Hitachi Vantara : http://www.pentaho.com
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
import org.pentaho.di.core.Const;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class XMLHandlerTest {

  @Test
  public void getTagValueWithNullNode() {
    assertNull( XMLHandler.getTagValue( null, "text"  ) );
  }

  /**
   * Default behavior, an empty XML tag in the "Filter rows" step meta will be considered {@code null}.
   * This will prevent filtering rows with empty values.
   */
  @Test
  public void getTagValueEmptyTagYieldsNullValue() {
    System.setProperty( Const.KETTLE_XML_EMPTY_TAG_YIELDS_EMPTY_VALUE, "N" );
    assertNull( XMLHandler.getTagValue( getNode(), "text"  ) );
  }

  /**
   * An empty XML tag in the "Filter rows" step meta will be considered an empty string.
   * This will allow filtering rows with empty values.
   */
  @Test
  public void getTagValueEmptyTagYieldsEmptyValue() {
    System.setProperty( Const.KETTLE_XML_EMPTY_TAG_YIELDS_EMPTY_VALUE, "Y" );
    assertEquals( "", XMLHandler.getTagValue( getNode(), "text"  ) );
  }

  private Node getNode() {
    Element first = mock( Element.class );
    doReturn( null ).when( first ).getNodeValue();

    Node child = mock( Node.class );
    doReturn( "text" ).when( child ).getNodeName();
    doReturn( first ).when( child ).getFirstChild();
    doReturn( "" ).when( child ).getTextContent();

    NodeList children = mock( NodeList.class );
    doReturn( 1 ).when( children ).getLength();
    doReturn( child ).when( children ).item( 0 );

    Node node = mock( Node.class );
    doReturn( children ).when( node ).getChildNodes();

    return node;
  }
}
