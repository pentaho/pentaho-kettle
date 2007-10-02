package org.pentaho.di.ui.spoon.job;

import org.pentaho.di.ui.spoon.job.JobHistory;
import org.pentaho.xul.swt.tab.TabItem;
import org.pentaho.xul.swt.tab.TabListener;

public class JobHistoryRefresher implements TabListener {

	private final TabItem tiTabsHist;
	private final JobHistory jobHist;

	public JobHistoryRefresher(TabItem tiTabsHist, JobHistory jobHist) {
		this.tiTabsHist = tiTabsHist;
		this.jobHist = jobHist;
	}

	public void tabSelected(TabItem item) {
		if(item == tiTabsHist)
			jobHist.refreshHistoryIfNeeded();
	}

	public void tabDeselected(TabItem item) {
	}

	public boolean tabClose(TabItem item) {
		return true;
	}

	public void markRefreshNeeded()
	{
		jobHist.markRefreshNeeded();
	}

}
