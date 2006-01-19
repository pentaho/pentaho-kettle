package be.ibridge.kettle.trans.step;

import be.ibridge.kettle.core.Row;

public interface RowListener
{
    public void rowReadEvent(Row row);
    public void rowWrittenEvent(Row row);
}
