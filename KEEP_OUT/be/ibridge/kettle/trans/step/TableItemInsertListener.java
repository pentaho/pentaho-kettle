package be.ibridge.kettle.trans.step;

import org.eclipse.swt.widgets.TableItem;

import be.ibridge.kettle.core.value.Value;

public interface TableItemInsertListener
{
    public boolean tableItemInserted(TableItem tableItem, Value v);
}
