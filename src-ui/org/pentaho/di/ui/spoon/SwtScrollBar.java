package org.pentaho.di.ui.spoon;

import org.eclipse.swt.widgets.ScrollBar;
import org.pentaho.di.core.gui.ScrollBarInterface;

public class SwtScrollBar implements ScrollBarInterface {

	private ScrollBar scrollBar;

	/**
	 * @param scrollBar
	 */
	public SwtScrollBar(ScrollBar scrollBar) {
		this.scrollBar = scrollBar;
	}

	public int getSelection() {
		return scrollBar.getSelection();
	}
	
	public void setThumb(int thumb) {
		scrollBar.setThumb(thumb);
	}
}
