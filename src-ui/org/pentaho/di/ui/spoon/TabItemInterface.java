/*
 * Copyright (c) 2007 Pentaho Corporation.  All rights reserved. 
 * This software was developed by Pentaho Corporation and is provided under the terms 
 * of the GNU Lesser General Public License, Version 2.1. You may not use 
 * this file except in compliance with the license. If you need a copy of the license, 
 * please go to http://www.gnu.org/licenses/lgpl-2.1.txt. The Original Code is Pentaho 
 * Data Integration.  The Initial Developer is Pentaho Corporation.
 *
 * Software distributed under the GNU Lesser Public License is distributed on an "AS IS" 
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
 * the license for the specific language governing your rights and limitations.
*/
package org.pentaho.di.ui.spoon;

import org.pentaho.di.core.EngineMetaInterface;
import org.pentaho.di.core.exception.KettleException;

public interface TabItemInterface
{
    /** 
     * Closes the content behind the tab, de-allocates resources.
     * 
     * @return true if the tab was closed, false if it was prevented by the user. (are you sure dialog)
     */
    public boolean canBeClosed();
    public boolean canHandleSave();
    public Object getManagedObject();
    public boolean hasContentChanged();
    public ChangedWarningInterface getChangedWarning();
    public int showChangedWarning() throws KettleException;
    public boolean applyChanges() throws KettleException;
    public EngineMetaInterface getMeta();
    public void setControlStates();
    public boolean setFocus();
    
}
