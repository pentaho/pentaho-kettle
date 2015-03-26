package org.pentaho.di.ui.spoon;

import org.eclipse.swt.widgets.Text;
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
    spoon.selectionFilter = mock( Text.class );

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
    assertTrue( spoon.filterMatch( "qwy" ) );
    assertTrue( spoon.filterMatch( "qwwwwy" ) );
  }
}
