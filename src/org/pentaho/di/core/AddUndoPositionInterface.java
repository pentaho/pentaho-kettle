package org.pentaho.di.core;

import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.UndoInterface;

public interface AddUndoPositionInterface
{
    public void addUndoPosition(UndoInterface undoInterface, Object obj[], int pos[], Point prev[], Point curr[]);
}
