/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.di.ui.spoon.delegates;

import org.junit.Test;
import org.pentaho.di.shared.SharedObjectInterface;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.pentaho.di.ui.spoon.delegates.SpoonSharedObjectDelegate.isDuplicate;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonSharedObjectDelegateTest {

  @Test
  public void isDuplicate_Same() {
    assertTrue( isDuplicate( singletonList( mockObject( "qwerty" ) ), mockObject( "qwerty" ) ) );
  }

  @Test
  public void isDuplicate_DifferentCase() {
    assertTrue( isDuplicate( singletonList( mockObject( "qwerty" ) ), mockObject( "Qwerty" ) ) );
  }

  @Test
  public void isDuplicate_NotSame() {
    assertFalse( isDuplicate( singletonList( mockObject( "qwerty" ) ), mockObject( "asdfg" ) ) );
  }


  private SharedObjectInterface mockObject( String name ) {
    SharedObjectInterface obj = mock( SharedObjectInterface.class );
    when( obj.getName() ).thenReturn( name );
    return obj;
  }
}
