/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
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

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.widgets.Text;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Andrey Khayrutdinov
 */
public class SpoonFilterMatchingTest {

  private Spoon spoon;

  @Before
  public void setUp() throws Exception {
    spoon = mock( Spoon.class );
    try {
      spoon.selectionFilter = mock( Text.class );
    } catch ( Throwable err ) {
      // skip if unable to mock widgets
      Assume.assumeNoException( err );
    }

    doCallRealMethod().when( spoon ).filterMatch( anyString() );
  }

  @Test
  public void filterIsEmpty() {
    when( spoon.selectionFilter.getText() ).thenReturn( "" );
    assertTrue( spoon.filterMatch( "qwerty" ) );
  }

  @Test
  public void givenIsEmpty() {
    when( spoon.selectionFilter.getText() ).thenReturn( "qwerty" );
    assertTrue( spoon.filterMatch( "" ) );
  }

  @Test
  public void plainTextMatching() {
    when( spoon.selectionFilter.getText() ).thenReturn( "qwerty" );
    assertTrue( spoon.filterMatch( "qwerty" ) );
    assertTrue( spoon.filterMatch( "qwertyasdfg" ) );
    assertTrue( spoon.filterMatch( "asdfgqwerty" ) );
  }

  @Test
  public void specialCharsMatching() {
    when( spoon.selectionFilter.getText() ).thenReturn( "qw*y" );
    assertFalse( spoon.filterMatch( "qwerty" ) );
    assertTrue( spoon.filterMatch( "qw*y" ) );
    assertFalse( spoon.filterMatch( "qwwwwy" ) );
  }
}
