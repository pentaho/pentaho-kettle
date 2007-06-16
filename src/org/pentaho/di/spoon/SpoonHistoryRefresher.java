package org.pentaho.di.spoon;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class SpoonHistoryRefresher implements SelectionListener {

	private final CTabItem tiTabsHist;
	private final SpoonHistory spoonhist;

	public SpoonHistoryRefresher(CTabItem tiTabsHist, SpoonHistory spoonhist) {
		this.tiTabsHist = tiTabsHist;
		this.spoonhist = spoonhist;
	}

	public void widgetSelected(SelectionEvent selectionEvent) {
		if(selectionEvent.item == tiTabsHist)
			spoonhist.refreshHistoryIfNeeded();
	}

	public void widgetDefaultSelected(SelectionEvent selectionEvent) {
		widgetSelected(selectionEvent);
	}
	
	public void markRefreshNeeded()
	{
		spoonhist.markRefreshNeeded();
	}

}
