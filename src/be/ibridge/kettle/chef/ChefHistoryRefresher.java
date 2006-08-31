package be.ibridge.kettle.chef;

import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class ChefHistoryRefresher implements SelectionListener {

	private final CTabItem tiTabsHist;
	private final ChefHistory chefHist;

	public ChefHistoryRefresher(CTabItem tiTabsHist, ChefHistory chefHist) {
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
