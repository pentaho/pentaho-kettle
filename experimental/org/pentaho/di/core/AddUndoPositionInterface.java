package org.pentaho.di.core;

import org.pentaho.di.spoon.UndoInterface;

import be.ibridge.kettle.core.Point;

public interface AddUndoPositionInterface
{
    public void addUndoPosition(UndoInterface undoInterface, Object obj[], int pos[], Point prev[], Point curr[]);
}
