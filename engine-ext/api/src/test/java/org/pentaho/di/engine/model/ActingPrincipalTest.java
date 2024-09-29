/*
 * ! ******************************************************************************
 *
 *  Pentaho Data Integration
 *
 *  Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * *****************************************************************************
 */

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
