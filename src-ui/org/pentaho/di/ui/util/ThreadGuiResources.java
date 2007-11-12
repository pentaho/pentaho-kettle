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
package org.pentaho.di.ui.util;

import org.eclipse.swt.widgets.Display;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.gui.SpoonFactory;
import org.pentaho.di.core.gui.ThreadDialogs;

public class ThreadGuiResources implements ThreadDialogs {

	public boolean threadMessageBox( final String message, final String text, boolean allowCancel, int type ) {
		
		final boolean result[] = new boolean[1];
    	Display.getDefault().syncExec(
    			new Runnable ()	{
    				public void run()	{
    					result[0] = SpoonFactory.getInstance().messageBox( 
    	        				message, 
    	        				text, true, Const.INFO );
    				}
    			}
    		);
		return result[0];
	}

}
