/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2015 by Pentaho : http://www.pentaho.com
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
