package org.pentaho.di.core;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * //TODO Add some javadoc or remove this comment
 *
 * @author Pavel Sakun
 */
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
