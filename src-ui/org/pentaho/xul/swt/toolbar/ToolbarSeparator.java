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
package org.pentaho.xul.swt.toolbar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.pentaho.xul.XulObject;
import org.pentaho.xul.toolbar.XulToolbar;

public class ToolbarSeparator extends XulObject {

	private ToolItem toolItem;
    
	public ToolbarSeparator( Shell shell, XulToolbar parent ) {
		super( "", parent );

		toolItem = new ToolItem((ToolBar) parent.getNativeObject(), SWT.SEPARATOR);

	}

	public void dispose() {
		toolItem.dispose();
	}

	public boolean isDisposed() {
		return toolItem.isDisposed();
	}

}
