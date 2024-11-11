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


package org.pentaho.di.engine.model;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class ActingPrincipalTest {
  ActingPrincipal principal1, principal2;

  @Test
  public void equals() throws Exception {
    principal1 = new ActingPrincipal( "suzy" );
    principal2 = new ActingPrincipal( "joe" );

    assertFalse( principal1.equals( principal2 ) );
    assertFalse( principal1.equals( ActingPrincipal.ANONYMOUS ) );

    principal2 = new ActingPrincipal( "suzy" );

    assertTrue( principal1.equals( principal2 ) );

    principal2 = ActingPrincipal.ANONYMOUS;
    assertTrue( principal2.equals( ActingPrincipal.ANONYMOUS ) );

  }

  @Test
  public void isAnonymous() throws Exception {
    assertTrue( ActingPrincipal.ANONYMOUS.isAnonymous() );
    assertFalse( new ActingPrincipal( "harold" ).isAnonymous() );
    assertFalse( new ActingPrincipal( "" ).isAnonymous() );
  }

}
