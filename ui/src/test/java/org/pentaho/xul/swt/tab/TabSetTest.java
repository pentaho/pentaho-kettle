/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
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

package org.pentaho.xul.swt.tab;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TabSetTest {

  /**
   * PDI-14411 NPE on Ctrl-W
   */
  @Test
  public void testCloseFirstTabOfTwo() {
    final CTabFolder cTabFolder = mock( CTabFolder.class );
    final TabSet tabSet = createTabSet( cTabFolder );

    final CTabItem cTabItem1 = mock( CTabItem.class );
    TabItem firstItem = createItem( tabSet, "first", "1st", cTabItem1 );
    final CTabItem cTabItem2 = mock( CTabItem.class );
    TabItem secondItem = createItem( tabSet, "second", "2nd", cTabItem2 );

    assertEquals( 0, tabSet.indexOf( firstItem ) );
    assertEquals( 1, tabSet.indexOf( secondItem ) );
    tabSet.setSelected( firstItem );
    assertEquals( 0, tabSet.getSelectedIndex() );

    wireDisposalSelection( cTabFolder, tabSet, cTabItem1, cTabItem2 );

    firstItem.dispose();
    assertEquals( -1, tabSet.indexOf( firstItem ) );
    assertNotNull( "selected is null", tabSet.getSelected() );
  }

  /**
   * Ctrl-W on first and second in succession would close first and third
   */
  @Test
  public void testCloseFirstTabOfThree() {
    final CTabFolder cTabFolder = mock( CTabFolder.class );
    final TabSet tabSet = createTabSet( cTabFolder );

    final CTabItem cTabItem1 = mock( CTabItem.class );
    TabItem firstItem = createItem( tabSet, "first", "1st", cTabItem1 );
    final CTabItem cTabItem2 = mock( CTabItem.class );
    TabItem secondItem = createItem( tabSet, "second", "2nd", cTabItem2 );
    TabItem thirdItem = createItem( tabSet, "third", "3rd", mock( CTabItem.class ) );

    assertEquals( 0, tabSet.indexOf( firstItem ) );
    assertEquals( 1, tabSet.indexOf( secondItem ) );
    assertEquals( 2, tabSet.indexOf( thirdItem ) );

    wireDisposalSelection( cTabFolder, tabSet, cTabItem1, cTabItem2 );
    tabSet.setSelected( firstItem );
    assertEquals( 0, tabSet.getSelectedIndex() );

    firstItem.dispose();
    assertEquals( "should select second", secondItem, tabSet.getSelected() );
  }

  /**
   * PDI-16196 index out of bounds after closing tabs with the same name
   */
  @Test
  public void testDuplicateNameCloseTab() {
    final CTabFolder cTabFolder = mock( CTabFolder.class );
    final TabSet tabSet = createTabSet( cTabFolder );

    final CTabItem cTabItem1 = mock( CTabItem.class );
    TabItem firstItem = createItem( tabSet, "equalName", "equals", cTabItem1 );
    final CTabItem cTabItem2 = mock( CTabItem.class );
    TabItem secondItem = createItem( tabSet, "equalName", "equals", cTabItem2 );
    final CTabItem cTabItem3 = mock( CTabItem.class );
    TabItem thirdItem = createItem( tabSet, "different", "different", cTabItem3 );

    assertEquals( 0, tabSet.indexOf( firstItem ) );
    assertEquals( 1, tabSet.indexOf( secondItem ) );

    wireDisposalSelection( cTabFolder, tabSet, cTabItem1, cTabItem3 );
    wireDisposalSelection( cTabFolder, tabSet, cTabItem2, cTabItem3 );
    firstItem.dispose();
    secondItem.dispose();

    tabSet.setSelected( firstItem );
    assertEquals( -1, tabSet.getSelectedIndex() );

    Event evt = new Event();
    evt.item = cTabItem1;
    evt.widget = cTabFolder;
    tabSet.widgetSelected( new SelectionEvent( evt ) );
  }

  @Test
  public void testRegularCloseTab() {
    final CTabFolder cTabFolder = mock( CTabFolder.class );
    final TabSet tabSet = createTabSet( cTabFolder );

    final CTabItem cTabItem1 = mock( CTabItem.class );
    TabItem firstItem = createItem( tabSet, "first", "1st", cTabItem1 );
    final CTabItem cTabItem2 = mock( CTabItem.class );
    TabItem secondItem = createItem( tabSet, "second", "2nd", cTabItem2 );
    TabItem thirdItem = createItem( tabSet, "third", "3rd", mock( CTabItem.class ) );

    assertEquals( 0, tabSet.indexOf( firstItem ) );
    assertEquals( 1, tabSet.indexOf( secondItem ) );
    assertEquals( 2, tabSet.indexOf( thirdItem ) );

    // after close the previous tab is selected if available
    wireDisposalSelection( cTabFolder, tabSet, cTabItem2, cTabItem1 );
    tabSet.setSelected( secondItem );
    assertEquals( 1, tabSet.getSelectedIndex() );
    secondItem.dispose();
    assertEquals( "should select first", firstItem, tabSet.getSelected() );
  }

  private TabSet createTabSet( final CTabFolder cTabFolder ) {
    return new TabSet( null ) {
      @Override
      protected CTabFolder createTabFolder( Composite parent ) {
        return cTabFolder;
      }
    };
  }

  private TabItem createItem( TabSet tabSet, String name, String id, final CTabItem cTabItem ) {
    int[] weights = new int[] { 30, 70 };
    return new TabItem( tabSet, name, id, weights ) {
      @Override
      protected CTabItem createTabItem( TabSet tabset ) {
        return cTabItem;
      }
    };
  }

  protected void wireDisposalSelection(
      final CTabFolder cTabFolder,
      final TabSet tabSet,
      final CTabItem closedItem,
      final CTabItem nextSelectItem ) {
    // emulate swt side
    // on CTabItem disposal CTabFolder selects another item and notifies TabSet
    final Boolean[] disposed = { false };
    when( closedItem.isDisposed() ).then( new Answer<Boolean>() {
      public Boolean answer( InvocationOnMock invocation ) throws Throwable {
        return disposed[0];
      }
    } );
    doAnswer( new Answer<Void>() {
      public Void answer( InvocationOnMock invocation ) throws Throwable {
        Event evt = new Event();
        evt.item = nextSelectItem;
        evt.widget = cTabFolder;
        tabSet.widgetSelected( new SelectionEvent( evt ) );
        disposed[0] = true;
        return null;
      }
    } ).when( closedItem ).dispose();
  }

}
