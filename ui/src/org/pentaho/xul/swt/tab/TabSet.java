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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class TabSet implements SelectionListener, CTabFolder2Listener {

  protected CTabFolder tabfolder;
  protected List<TabItem> tabList = new ArrayList<TabItem>();
  protected int selectedIndex = -1;
  protected Font changedFont;
  protected Font unchangedFont;
  private List<TabListener> listeners = new ArrayList<TabListener>();

  public List<TabItem> lastUsedTabs = new ArrayList<TabItem>();

  public TabSet( Composite parent ) {
    super();
    tabfolder = createTabFolder( parent );

    tabfolder.setSimple( false );
    tabfolder.setUnselectedImageVisible( true );
    tabfolder.setUnselectedCloseVisible( true );
    tabfolder.addSelectionListener( this );
    tabfolder.addCTabFolder2Listener( this );

    workaroundTabChevronOverflow( tabfolder );
  }

  private void workaroundTabChevronOverflow( CTabFolder tabfolder ) {
    // hack to prevent two tab rows on swt 4.6+
    // eclipse bug #499215
    tabfolder.addListener( SWT.Resize, new Listener() {
      @Override
      public void handleEvent( Event event ) {
        int idx = tabfolder.getSelectionIndex();
        if ( idx > 0 && isTabOverflowing( tabfolder ) ) {
          // trigger CTabFolder.updateTabHeight
          tabfolder.setSelection( idx - 1 );
          tabfolder.setSelection( idx );
        }
      }

      private boolean isTabOverflowing( CTabFolder tabfolder ) {
        // overflow happens when size of inner toolbar exceeds assigned tabHeight
        return ( tabfolder.getChildren().length > 0
            && tabfolder.getChildren()[0].getSize().y > tabfolder.getTabHeight() );
      }
    } );
  }


  public void widgetSelected( SelectionEvent event ) {
    if ( selectedIndex >= 0 && selectedIndex < tabList.size() ) {
      TabItem deSelectedTabItem = tabList.get( selectedIndex );
      if ( deSelectedTabItem != null ) {
        notifyDeselectListeners( deSelectedTabItem );
      }
    }
    for ( int i = 0; i < tabList.size(); i++ ) {
      TabItem item = tabList.get( i );
      if ( event.item.equals( item.getSwtTabItem() ) ) {
        selectedIndex = i;
        notifySelectListeners( item );
      }
    }
  }

  public void widgetDefaultSelected( SelectionEvent event ) {
    widgetSelected( event );
  }

  public void close( CTabFolderEvent event ) {
    for ( int i = 0; i < tabList.size(); i++ ) {
      TabItem item = tabList.get( i );
      if ( event.item.equals( item.getSwtTabItem() ) ) {
        event.doit = notifyCloseListeners( item );
      }
    }
  }

  public void maximize( CTabFolderEvent event ) {

  }

  public void minimize( CTabFolderEvent event ) {

  }

  public void showList( CTabFolderEvent event ) {

  }

  public void restore( CTabFolderEvent event ) {

  }

  public void notifySelectListeners( TabItem item ) {
    for ( int i = 0; i < listeners.size(); i++ ) {
      ( listeners.get( i ) ).tabSelected( item );
    }
    // add to the lat used tab
    addItemToHistory( item );
  }

  protected CTabFolder createTabFolder( Composite parent ) {
    return new CTabFolder( parent, SWT.MULTI );
  }


  /**
   * Add a tab item to the tab usage history
   *
   * @param item
   *          the tab item to add
   */
  private void addItemToHistory( TabItem item ) {
    // Just don't add the same item twice in a row
    //
    if ( lastUsedTabs.size() == 0 || lastUsedTabs.lastIndexOf( item ) != lastUsedTabs.size() - 1 ) {
      lastUsedTabs.add( item );
    }
  }

  /**
   * Remove all occurrences of the specified item from the last used list.
   *
   * @param item
   *          the tab item to remove
   */
  private void removeItemFromHistory( TabItem item ) {
    // Remove
    boolean removed;
    do {
      removed = lastUsedTabs.remove( item );
    } while ( removed );
  }

  public void notifyDeselectListeners( TabItem item ) {
    for ( int i = 0; i < listeners.size(); i++ ) {
      ( listeners.get( i ) ).tabDeselected( item );
    }
  }

  public boolean notifyCloseListeners( TabItem item ) {
    boolean doit = item.notifyCloseListeners();
    for ( int i = 0; i < listeners.size(); i++ ) {
      doit &= ( listeners.get( i ) ).tabClose( item );
    }
    if ( doit ) {
      removeItemFromHistory( item );
      selectLastUsedTab();
    }
    return doit;
  }

  /**
   * Select the last tab in the tab usage history list
   */
  private void selectLastUsedTab() {
    int historySize = lastUsedTabs.size();
    if ( historySize > 0 ) {
      TabItem lastItem = lastUsedTabs.get( historySize - 1 );
      setSelected( lastItem );
    }
  }

  public CTabFolder getSwtTabset() {
    return tabfolder;
  }

  public void addTab( TabItem item ) {
    tabList.add( item );
  }

  public void addKeyListener( KeyAdapter keys ) {
    tabfolder.addKeyListener( keys );
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public TabItem getSelected() {
    if ( selectedIndex >= 0 && selectedIndex < tabList.size() ) {
      return tabList.get( selectedIndex );
    }
    return null;
  }

  public int indexOf( TabItem item ) {

    return tabList.indexOf( item );

  }

  public void setSelected( int index ) {
    if ( index >= 0 && index < tabList.size() ) {
      TabItem item = ( tabList.get( index ) );
      tabfolder.setSelection( item.getSwtTabItem() );
      selectedIndex = index;
      notifySelectListeners( tabList.get( index ) );
    }
  }

  public void setSelected( TabItem item ) {
    selectedIndex = indexOf( item );
    if ( selectedIndex != -1 ) {
      setSelected( selectedIndex );
    }
  }

  public void remove( TabItem item ) {
    int itemIndex = tabList.indexOf( item );
    if ( itemIndex >= 0 && itemIndex < selectedIndex ) {
      // removal would change selected
      selectedIndex--;
    }
    tabList.remove( item );
    item.dispose();
  }

  public Font getChangedFont() {
    return changedFont;
  }

  public void setChangedFont( Font changedFont ) {
    this.changedFont = changedFont;
  }

  public Font getUnchangedFont() {
    return tabfolder.getDisplay().getSystemFont();
  }

  public void addListener( TabListener listener ) {
    listeners.add( listener );
  }

  public void removeListener( TabListener listener ) {
    listeners.remove( listener );
  }

}
