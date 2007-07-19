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
