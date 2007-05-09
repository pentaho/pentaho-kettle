package org.pentaho.di.core.trans.step;

import org.eclipse.swt.widgets.TableItem;
import org.pentaho.di.core.row.ValueMetaInterface;

public interface TableItemInsertListener
{
    public boolean tableItemInserted(TableItem tableItem, ValueMetaInterface v);
}
