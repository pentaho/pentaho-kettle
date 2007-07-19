package org.pentaho.di.ui.spoon.job;

import org.pentaho.di.ui.spoon.job.JobHistory;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;

public class JobHistoryRefresher implements TabListener {

	private final TabItem tiTabsHist;
	private final JobHistory chefHist;

	public JobHistoryRefresher(TabItem tiTabsHist, JobHistory chefHist) {
		this.tiTabsHist = tiTabsHist;
		this.chefHist = chefHist;
	}

	public void tabSelected(TabItem item) {
		if(item == tiTabsHist)
			chefHist.refreshHistoryIfNeeded();
	}

	public void tabDeselected(TabItem item) {
	}

	public boolean tabClose(TabItem item) {
		return true;
	}

	public void markRefreshNeeded()
	{
		chefHist.markRefreshNeeded();
	}

}
