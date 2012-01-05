/*******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2012 by Pentaho : http://www.pentaho.com
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
        tabfolder= new CTabFolder(parent, SWT.MULTI);
        
        tabfolder.setSimple(false);
        tabfolder.setUnselectedImageVisible(true);
        tabfolder.setUnselectedCloseVisible(true);
        tabfolder.addSelectionListener( this );
        tabfolder.addCTabFolder2Listener( this );
	}
	
	public void widgetSelected( SelectionEvent event ) {
		for( int i=0; i<tabList.size(); i++ ) {
			TabItem item = tabList.get(i);
			if( event.item.equals( item.getSwtTabItem() ) ) {
				selectedIndex = i;
				notifySelectListeners( item );
			}
		}
	}

	public void widgetDefaultSelected( SelectionEvent event ) {
		widgetSelected( event );
	}

	 public void close(CTabFolderEvent event) {
			for( int i=0; i<tabList.size(); i++ ) {
				TabItem item = tabList.get(i);
				if( event.item.equals( item.getSwtTabItem() ) ) {
					event.doit = notifyCloseListeners( item );
				}
			}
	 }
	
	 public void maximize(CTabFolderEvent event) {
		 
	 }

	 public void minimize(CTabFolderEvent event) {
		 
	 }
	
	 public void showList(CTabFolderEvent event) {
		 
	 }
	
	 public void restore(CTabFolderEvent event) {
		 
	 }
	

	public void notifySelectListeners( TabItem item ) {
		for( int i=0; i<listeners.size(); i++ ) {
			(listeners.get(i)).tabSelected( item );
		}
		// add to the lat used tab
		addItemToHistory(item);
	}

	/**
	 * Add a tab item to the tab usage history
	 * @param item the tab item to add
	 */
	private void addItemToHistory(TabItem item) {
		// Just don't add the same item twice in a row
		//
		if (lastUsedTabs.size()==0 || lastUsedTabs.lastIndexOf(item)!=lastUsedTabs.size()-1) {
			lastUsedTabs.add(item);
		}
	}

	/**
	 *  Remove all occurrences of the specified item from the last used list.
	 * @param item the tab item to remove
	 */
	private void removeItemFromHistory(TabItem item) {
		while (lastUsedTabs.remove(item));	
	}


	public void notifyDeselectListeners( TabItem item ) {
		for( int i=0; i<listeners.size(); i++ ) {
			(listeners.get(i)).tabDeselected( item );
		}
	}

	public boolean notifyCloseListeners( TabItem item ) {
		boolean doit = item.notifyCloseListeners( );
		for( int i=0; i<listeners.size(); i++ ) {
			doit  &= (listeners.get(i)).tabClose( item );
		}
		removeItemFromHistory(item);
		selectLastUsedTab();
		return doit;
	}

	/**
	 * Select the last tab in the tab usage history list
	 */
	private void selectLastUsedTab() {
		int historySize = lastUsedTabs.size();
		if (historySize>0) { 
			TabItem lastItem = lastUsedTabs.get(historySize-1);
			setSelected(lastItem);
		}	
	}

	public CTabFolder getSwtTabset() {
		return tabfolder;
	}
	
	public void addTab( TabItem item ) {
		tabList.add( item );
	}
	
	public void addKeyListener( KeyAdapter keys ) {
        tabfolder.addKeyListener(keys);
	}
	
	public int getSelectedIndex() {
		return selectedIndex;
	}
	
	public TabItem getSelected() {
		if( selectedIndex >= 0 && selectedIndex < tabList.size() ) {
			return tabList.get( selectedIndex );
		}
		return null;
	}
	
	public int indexOf( TabItem item ) {

	    return tabList.indexOf(item); 

	}

	public void setSelected( int index ) {
		if( index >= 0 && index < tabList.size() ) {
			TabItem item = (tabList.get( index ));
		    tabfolder.setSelection( item.getSwtTabItem() );
			selectedIndex = index;
			notifySelectListeners( tabList.get( index ) );
		}
	}

	public void setSelected( TabItem item ) {
		selectedIndex = indexOf( item );
		if( selectedIndex != -1 ) {
			setSelected( selectedIndex );
		}
	}
    
	public void remove( TabItem item ) {
		tabList.remove( item );
		item.dispose();
	}

	public Font getChangedFont() {
		return changedFont;
	}

	public void setChangedFont(Font changedFont) {
		this.changedFont = changedFont;
	}

	public Font getUnchangedFont() {
		return unchangedFont;
	}

	public void setUnchangedFont(Font unchangedFont) {
		this.unchangedFont = unchangedFont;
	}
	
	public void addListener( TabListener listener ) {
		listeners.add( listener );
	}
	
	public void removeListener( TabListener listener ) {
		listeners.remove( listener );
	}

}
