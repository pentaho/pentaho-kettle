package be.ibridge.kettle.core.changes;

import be.ibridge.kettle.core.list.ObjectAlreadyExistsException;

public interface IDChangedListener
{
    public void IDChanged(IDChangedEvent event) throws ObjectAlreadyExistsException;
}
