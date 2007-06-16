package be.ibridge.kettle.core;

import be.ibridge.kettle.spoon.UndoInterface;

public interface AddUndoPositionInterface
{
    public void addUndoPosition(UndoInterface undoInterface, Object obj[], int pos[], Point prev[], Point curr[]);
}
