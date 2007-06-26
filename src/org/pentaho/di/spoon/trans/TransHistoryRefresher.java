package org.pentaho.di.spoon.trans;

import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;

public class TransHistoryRefresher implements TabListener {

	private final TabItem tiTabsHist;
	private final TransHistory spoonhist;

	public TransHistoryRefresher(TabItem tiTabsHist, TransHistory spoonhist) {
		this.tiTabsHist = tiTabsHist;
		this.spoonhist = spoonhist;
	}

	public void tabSelected(TabItem item) {
		if(item == tiTabsHist)
			spoonhist.refreshHistoryIfNeeded();
	}

	public void tabDeselected(TabItem item) {
	}

	public boolean tabClose(TabItem item) {
		return true;
	}
	
	public void markRefreshNeeded()
	{
		spoonhist.markRefreshNeeded();
	}

}
