package org.pentaho.di.spoon.job;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class JobHistoryRefresher implements SelectionListener {

	private final CTabItem tiTabsHist;
	private final JobHistory chefHist;

	public JobHistoryRefresher(CTabItem tiTabsHist, JobHistory chefHist) {
		this.tiTabsHist = tiTabsHist;
		this.chefHist = chefHist;
	}

	public void widgetSelected(SelectionEvent selectionEvent) {
		if(selectionEvent.item == tiTabsHist)
			chefHist.refreshHistoryIfNeeded();
	}

	public void widgetDefaultSelected(SelectionEvent selectionEvent) {
		widgetSelected(selectionEvent);
	}
	
	public void markRefreshNeeded()
	{
		chefHist.markRefreshNeeded();
	}

}
