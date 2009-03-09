package org.pentaho.di.ui.core.widget.warning;

import org.eclipse.swt.widgets.Control;

public interface WarningInterface {
	/**
	 * Verifies if a warning situation has occurred.
	 * 
	 * @param text The content of the text box to verify
	 * @param widget the text widget itself
	 * @param subject the parent subject that is being edited.
	 * @return the warning message 
	 */
	public WarningMessageInterface getWarningSituation(String text, Control widget, Object subject);
}
