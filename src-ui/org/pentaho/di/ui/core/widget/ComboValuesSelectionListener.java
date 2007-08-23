package org.pentaho.di.ui.core.widget;

import org.eclipse.swt.widgets.TableItem;

public interface ComboValuesSelectionListener {
	public String[] getComboValues(TableItem tableItem, int rowNr, int colNr);
}
