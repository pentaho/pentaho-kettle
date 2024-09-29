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


package org.pentaho.di.trans.steps.transexecutor;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TransExecutorParametersTest {

  @Test
  public void testClone() throws Exception {
    TransExecutorParameters meta = new TransExecutorParameters();
    meta.setField( new String[] { "field1", "field2" } );
    meta.setVariable( new String[] { "var1", "var2" } );
    meta.setInput( new String[] { "input1", "input2" } );

    TransExecutorParameters cloned = (TransExecutorParameters) meta.clone();
    assertFalse( cloned.getField() == meta.getField() );
    assertTrue( Arrays.equals( cloned.getField(), meta.getField() ) );
    assertFalse( cloned.getVariable() == meta.getVariable() );
    assertTrue( Arrays.equals( cloned.getVariable(), meta.getVariable() ) );
    assertFalse( cloned.getInput() == meta.getInput() );
    assertTrue( Arrays.equals( cloned.getInput(), meta.getInput() ) );
  }

}
