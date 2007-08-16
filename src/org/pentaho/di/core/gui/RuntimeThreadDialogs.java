package org.pentaho.di.core.gui;

public class RuntimeThreadDialogs implements ThreadDialogs {

	public boolean threadMessageBox(String message, String text,
			boolean allowCancel, int type) {

		// assume its ok to return to ok
		return true;
	}

}
