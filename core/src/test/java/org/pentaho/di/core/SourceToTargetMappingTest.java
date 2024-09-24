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

public class SourceToTargetMappingTest {

  @Test
  public void testClass() throws Exception {
    SourceToTargetMapping mapping = new SourceToTargetMapping( 2, 3 );
    assertEquals( 2, mapping.getSourcePosition() );
    assertEquals( 3, mapping.getTargetPosition() );
    mapping.setSourcePosition( 0 );
    mapping.setTargetPosition( 1 );
    assertEquals( 0, mapping.getSourcePosition() );
    assertEquals( 1, mapping.getTargetPosition() );
    assertEquals( "foo", mapping.getSourceString( new String[]{ "foo", "bar" } ) );
    assertEquals( "bar", mapping.getTargetString( new String[]{ "foo", "bar" } ) );
  }
}
