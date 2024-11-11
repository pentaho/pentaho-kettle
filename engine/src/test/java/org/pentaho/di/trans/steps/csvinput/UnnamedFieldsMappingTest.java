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


package org.pentaho.di.trans.steps.csvinput;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class UnnamedFieldsMappingTest {

  private UnnamedFieldsMapping fieldsMapping;

  @Before
  public void before() {
    fieldsMapping = new UnnamedFieldsMapping( 2 );
  }

  @Test
  public void fieldMetaIndex() {
    assertEquals( 1, fieldsMapping.fieldMetaIndex( 1 ) );
  }

  @Test
  public void fieldMetaIndexWithUnexistingField() {
    assertEquals( FieldsMapping.FIELD_DOES_NOT_EXIST, fieldsMapping.fieldMetaIndex( 2 ) );
  }

  @Test
  public void size() {
    assertEquals( 2, fieldsMapping.size() );
  }

  @Test
  public void mapping() {
    UnnamedFieldsMapping mapping = UnnamedFieldsMapping.mapping( 2 );
    assertEquals( 1, mapping.fieldMetaIndex( 1 ) );
  }

}
