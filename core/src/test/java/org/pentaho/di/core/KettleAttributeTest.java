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
package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class KettleAttributeTest {
  @Test
  public void testClass() {
    final String key = "key";
    final String xmlCode = "xmlCode";
    final String repCode = "repCode";
    final String description = "description";
    final String tooltip = "tooltip";
    final int type = 6;
    final KettleAttributeInterface parent = mock( KettleAttributeInterface.class );
    KettleAttribute attribute = new KettleAttribute( key, xmlCode, repCode, description, tooltip, type, parent );
    assertSame( key, attribute.getKey() );
    assertSame( xmlCode, attribute.getXmlCode() );
    assertSame( repCode, attribute.getRepCode() );
    assertSame( description, attribute.getDescription() );
    assertSame( tooltip, attribute.getTooltip() );
    assertEquals( type, attribute.getType() );
    assertSame( parent, attribute.getParent() );

    attribute.setKey( null );
    assertNull( attribute.getKey() );
    attribute.setXmlCode( null );
    assertNull( attribute.getXmlCode() );
    attribute.setRepCode( null );
    assertNull( attribute.getRepCode() );
    attribute.setDescription( null );
    assertNull( attribute.getDescription() );
    attribute.setTooltip( null );
    assertNull( attribute.getTooltip() );
    attribute.setType( -6 );
    assertEquals( -6, attribute.getType() );
    attribute.setParent( null );
    assertNull( attribute.getParent() );
  }
}
