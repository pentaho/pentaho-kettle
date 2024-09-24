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

package org.pentaho.di.ui.spoon;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.junit.Test;
import org.pentaho.di.ui.spoon.trans.TransGraph;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ExpandedContentManagerTest {

  @Test
  public void testIsBrowserVisibleTransGraph() {
    TransGraph transGraphMock = mock( TransGraph.class );
    Control control1 = mock( Control.class );
    Control control2 = mock( Control.class );
    Browser browser = mock( Browser.class );
    Control[] children = new Control[] { control1, control2, browser };
    when( transGraphMock.getChildren() ).thenReturn( children );
    Boolean result = ExpandedContentManager.isVisible( transGraphMock );
    assertFalse( result );
    children = new Control[] { browser, control1, control2 };
    when( transGraphMock.getChildren() ).thenReturn( children );
    result = ExpandedContentManager.isVisible( transGraphMock );
    assertTrue( result );
  }

  @Test
  public void testShowTransformationBrowserh() {
    TransGraph transGraphMock = mock( TransGraph.class );
    Control control1 = mock( Control.class );
    Control control2 = mock( Control.class );
    Browser browser = mock( Browser.class );
    SashForm sash = mock( SashForm.class );
    when( sash.getWeights() ).thenReturn( new int[] { 277, 722 } );
    Composite comp1 = mock( Composite.class );
    Composite comp2 = mock( Composite.class );
    Composite comp3 = mock( Composite.class );
    Composite comp4 = mock( Composite.class );
    when( browser.getParent() ).thenReturn( comp1 );
    when( comp1.getParent() ).thenReturn( comp2 );
    when( comp2.getParent() ).thenReturn( comp3 );
    when( comp3.getParent() ).thenReturn( sash );
    when( comp4.getParent() ).thenReturn( sash );
    Control[] children = new Control[] { control1, control2, browser };
    when( transGraphMock.getChildren() ).thenReturn( children );
    ExpandedContentManager.createExpandedContent( transGraphMock, "" );
    verify( browser ).setUrl( "" );
  }

  @Test
  public void testHideExpandedContentManager() throws Exception {
    TransGraph transGraph = mock( TransGraph.class );
    Browser browser = mock( Browser.class );
    SashForm sashForm = mock( SashForm.class );

    Composite parent = setupExpandedContentMocks( transGraph, browser, sashForm );
    ExpandedContentManager.hideExpandedContent( transGraph );
    verify( browser ).moveBelow( null );
    verify( parent ).layout( true, true );
    verify( parent ).redraw();
    verify( sashForm ).setWeights( new int[] { 3, 2, 1 } );
  }

  @Test
  public void testCloseExpandedContentManager() throws Exception {
    TransGraph transGraph = mock( TransGraph.class );
    Browser browser = mock( Browser.class );
    SashForm sashForm = mock( SashForm.class );

    setupExpandedContentMocks( transGraph, browser, sashForm );
    ExpandedContentManager.closeExpandedContent( transGraph );
    verify( browser ).close();
    verify( sashForm ).setWeights( new int[] { 3, 2, 1 } );
  }

  private Composite setupExpandedContentMocks( TransGraph transGraph, Browser browser, SashForm sashForm ) {
    Spoon spoon = mock( Spoon.class );
    Composite parent = mock( Composite.class );
    TabSet tabSet = mock( TabSet.class );
    TabItem tabItem = mock( TabItem.class );
    ExpandedContentManager.spoonSupplier = () -> spoon;
    when( spoon.getDesignParent() ).thenReturn( sashForm );
    when( spoon.getTabSet() ).thenReturn( tabSet );
    when( tabSet.getSelected() ).thenReturn( tabItem );
    when( tabItem.getSashWeights() ).thenReturn( new int[] { 3, 2, 1 } );
    when( transGraph.getChildren() ).thenReturn( new Control[]{ browser } );
    when( browser.getParent() ).thenReturn( parent );
    return parent;
  }

}
