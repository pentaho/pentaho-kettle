package org.pentaho.di.core;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class ObjectLocationSpecificationMethodTest {
  @Test
  public void testClass() {
    ObjectLocationSpecificationMethod[] values = ObjectLocationSpecificationMethod.values();
    List<String> descriptions = Arrays.asList( ObjectLocationSpecificationMethod.FILENAME.getDescriptions() );

    for ( ObjectLocationSpecificationMethod method : values ) {
      assertNotNull( method.getCode() );
      assertFalse( method.getCode().isEmpty() );
      assertNotNull( method.getDescription() );
      assertFalse( method.getDescription().isEmpty() );
      assertTrue( descriptions.contains( method.getDescription() ) );
      assertEquals( ObjectLocationSpecificationMethod.getSpecificationMethodByCode( method.getCode() ), method );
      assertEquals( ObjectLocationSpecificationMethod.getSpecificationMethodByDescription( method.getDescription() ),
          method );
    }

    assertEquals( values.length, descriptions.size() );
    assertNull( ObjectLocationSpecificationMethod.getSpecificationMethodByCode( "Invalid code" ) );
    assertNull( ObjectLocationSpecificationMethod.getSpecificationMethodByDescription( "Invalid description" ) );
  }
}
