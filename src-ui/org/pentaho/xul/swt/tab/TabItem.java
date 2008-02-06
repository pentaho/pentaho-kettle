/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.xul.swt.tab;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class TabItem {

	private TabSet tabset;
	private CTabItem item;
	private String id;
	private String text;
	private Object control;
	private boolean changed;
	private List<TabListener> listeners = new ArrayList<TabListener>();
	
	public TabItem( TabSet tabset, String text, String id) {
		super();
		this.tabset = tabset;
		this.id = id;
		item = new CTabItem( tabset.getSwtTabset(), SWT.CLOSE );
		setText( text );
		tabset.addTab( this );
	}

	public CTabItem getSwtTabItem() {
		return item;
	}
	
	public String getText() {
		return text;
	}

	public Object getControl() {
		return control;
	}

	public void setControl(Control control) {
		this.control = control;
		item.setControl( control );
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public TabSet getTabset() {
		return tabset;
	}

	public void setTabset(TabSet tabset) {
		this.tabset = tabset;
	}

	public void setText(String text) {
		this.text = text;
		item.setText( text );
	}
	
	public boolean isDisposed() {
		return item.isDisposed();
	}
	
	public void dispose() {
		if( !isDisposed() ) {
			if (control instanceof Composite) {
				((Composite)control).dispose();
			}
			item.dispose();
			tabset.remove( this );
		}
	}
	
	public void setToolTipText( String tip ) {
		item.setToolTipText( tip );
	}
	
	public void setImage( Image image ) {
		item.setImage( image );
	}
	
	public void setChanged( boolean changed ) {
		this.changed = changed;
		if( changed && tabset.getChangedFont() != null ) {
			item.setFont( tabset.getChangedFont() );
		} 
		else if( !changed && tabset.getUnchangedFont() != null ) {
			item.setFont( tabset.getUnchangedFont() );
		}
	}
	
	public void addListener( TabListener listener ) {
		listeners.add( listener );
	}
	
	public void removeListener( TabListener listener ) {
		listeners.remove( listener );
	}
	
	public void notifySelectListeners(  ) {
		for( int i=0; i<listeners.size(); i++ ) {
			(listeners.get(i)).tabSelected( this );
		}
	}

	public void notifyDeselectListeners(  ) {
		for( int i=0; i<listeners.size(); i++ ) {
			(listeners.get(i)).tabDeselected( this );
		}
	}

	public boolean notifyCloseListeners(  ) {
		boolean doit = true;
		for( int i=0; i<listeners.size(); i++ ) {
			doit  &= (listeners.get(i)).tabClose( this );
		}
		return doit;
	}

	public boolean isChanged() {
		return changed;
	}

}
