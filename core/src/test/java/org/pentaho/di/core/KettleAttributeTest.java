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
